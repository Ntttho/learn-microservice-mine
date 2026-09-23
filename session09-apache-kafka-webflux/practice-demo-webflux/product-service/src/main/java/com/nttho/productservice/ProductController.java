package com.nttho.productservice;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {
    @GetMapping("/mono")
    public Mono<String> getProductFirst(){
        return Mono.just("product by mono");
    }

    @GetMapping("/flux")
    public Flux<String> getProductFlux(){
        String[] strings = {"Product flux1", "Product flux2"};
        return Flux.fromArray(strings);
    }
}
