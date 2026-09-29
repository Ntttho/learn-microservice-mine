package com.nttho.orchestratorservice.service;

import com.nttho.orchestratorservice.client.OrderClient;
import com.nttho.orchestratorservice.client.ProductClient;
import com.nttho.orchestratorservice.common.OrderStatus;
import com.nttho.orchestratorservice.common.command.ProductCommand;
import com.nttho.orchestratorservice.common.dto.request.OrderDetailRequest;
import com.nttho.orchestratorservice.common.dto.request.OrderRequest;
import com.nttho.orchestratorservice.common.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrchestratorService {
    private final ProductClient productClient;
    private final OrderClient orderClient;

    public OrderResponse orderTransactions(OrderRequest orderRequest){
        List<OrderDetailRequest> orderDetailRequests = new ArrayList<>();
        OrderResponse orderResponse = null;
        try{
            orderResponse = orderClient.createOrder(orderRequest);
            if (orderResponse == null || orderResponse.getOrder() == null) {
                throw new RuntimeException("Create order failed: order is null");
            }
            for (OrderDetailRequest detailRequest : orderRequest.getOrderDetailRequest()){
                productClient.reduceStock(new ProductCommand(detailRequest.getProductId(), detailRequest.getQuantity()));
                orderDetailRequests.add(detailRequest);
            }
            // tat ca product duoc giam quantity trong kho thanhf cong
            orderClient.updateStatus(orderResponse.getOrder().getId(), OrderStatus.COMPLETED);
            orderResponse.getOrder().setOrderStatus(OrderStatus.COMPLETED);
            return orderResponse;
        }catch (Exception e){
            // fail thif hoan tra quantity
            for(OrderDetailRequest orderDetailRequest: orderDetailRequests){
                productClient.productCompensation(new ProductCommand(orderDetailRequest.getProductId(), orderDetailRequest.getQuantity()));
            }
            if (orderResponse != null && orderResponse.getOrder() != null && orderResponse.getOrder().getId() != null) {
                try {
                    orderClient.updateStatus(orderResponse.getOrder().getId(), OrderStatus.CANCELLED);
                    orderResponse.getOrder().setOrderStatus(OrderStatus.CANCELLED);
                } catch (Exception ex) {
                    System.out.printf("Lỗi khi update trạng thái CANCELLED cho order {%s}: {%s}", orderResponse.getOrder().getId(), ex.getMessage());
                }
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Giao dịch đơn hàng thất bại: " + e.getMessage());

//            return orderResponse;
        }
    }


}
