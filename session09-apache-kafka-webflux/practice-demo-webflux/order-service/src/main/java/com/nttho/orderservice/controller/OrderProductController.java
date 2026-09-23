package com.nttho.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderProductController {
    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8081/api/product")
            .build();

    @GetMapping("/product")
    public Mono<String> getProduct(){
        return webClient.get()
                .uri("/mono")
                .retrieve()
                .bodyToMono(String.class)
                ;
    }

    @GetMapping("/products")
    public Flux<String> getProducts(){
        return webClient.get()
                .uri("/flux")
                .retrieve()
                .bodyToFlux(String.class)
                ;
    }


}
