package com.session06.order_service.client;

import com.session06.order_service.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.product-service.url:http://product-service}")
    private String productServiceUrl;

    public List<ProductDto> getAllProducts() {
        String url = productServiceUrl + "/api/products";
        log.info("Sending GET request to Product Service via RestTemplate: {}", url);
        try {
            ResponseEntity<List<ProductDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch products from Product Service: {}", e.getMessage());
            throw new RuntimeException("Error communicating with Product Service: " + e.getMessage(), e);
        }
    }

    public Optional<ProductDto> getProductById(Long productId) {
        String url = productServiceUrl + "/api/products/" + productId;
        log.info("Sending GET request to Product Service for product ID {}: {}", productId, url);
        try {
            ResponseEntity<ProductDto> response = restTemplate.getForEntity(url, ProductDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Product with ID {} not found in Product Service", productId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to fetch product {} from Product Service: {}", productId, e.getMessage());
            throw new RuntimeException("Error communicating with Product Service: " + e.getMessage(), e);
        }
    }
}
