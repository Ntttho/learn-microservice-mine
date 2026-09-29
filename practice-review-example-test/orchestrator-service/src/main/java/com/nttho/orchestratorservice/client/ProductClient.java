package com.nttho.orchestratorservice.client;

import com.nttho.orchestratorservice.common.client.Product;
import com.nttho.orchestratorservice.common.command.ProductCommand;
import com.nttho.orchestratorservice.common.dto.request.ProductRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {
    @GetMapping
    public List<Product> findAll();

    @GetMapping("/{id}")
    public Product findById(@PathVariable Long id);

    @PutMapping("/reduce-stock")
    public void reduceStock(@RequestBody ProductCommand command);

    @PutMapping("/compensation")
    public void productCompensation(@RequestBody ProductCommand command);

    @PostMapping("/create")
    public Product createProduct(@RequestBody ProductRequest request);
}
