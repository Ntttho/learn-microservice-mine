package com.nttho.orchestratorsaga.client;

import com.nttho.orchestratorsaga.model.command.ProductStockCommand;
import com.nttho.orchestratorsaga.model.dto.response.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {
    @PutMapping("/reduce-stock")
    void reduceStock(@RequestBody ProductStockCommand request);

    @PutMapping("/restore-stock")
    void compensatingProduct(@RequestBody ProductStockCommand request);
}
