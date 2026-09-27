package com.nttho.orchestratorsaga.service;

import com.nttho.orchestratorsaga.client.OrderClient;
import com.nttho.orchestratorsaga.client.ProductClient;
import com.nttho.orchestratorsaga.model.command.ProductStockCommand;
import com.nttho.orchestratorsaga.model.constants.OrderStatus;
import com.nttho.orchestratorsaga.model.dto.request.OrderRequest;
import com.nttho.orchestratorsaga.model.dto.response.OrderResponse;
import com.nttho.orchestratorsaga.model.entity.OrderDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestratorService {
    private final OrderClient orderClient;
    private final ProductClient productClient;

    public OrderResponse orderProduct(OrderRequest orderRequest){
        List<ProductStockCommand> executedProducts = new ArrayList<>();
        OrderResponse orderResponse = orderClient.createOrder(orderRequest);

        try {
            for (OrderDetail detail: orderResponse.getOrderDetails()){
                ProductStockCommand command = new ProductStockCommand(detail.getProductId(), detail.getQuantity());
                productClient.reduceStock(command);
                executedProducts.add(command);
            }

            // khong xay ra batky loi nao -> cập nhập success thành công order
            return orderClient.updateOrder(orderResponse.getId(), OrderStatus.COMPLETED);
        }
        catch (Exception e){
            log.error(e.getMessage());

            // xữ lý = cách gọi compensating
            // khôi phục dữ liệu product và cập nhập cancelled order
            for (ProductStockCommand command: executedProducts){
                productClient.compensatingProduct(command);
            }
            return orderClient.updateOrder(orderResponse.getId(), OrderStatus.CANCELLED);
        }
    }
}
