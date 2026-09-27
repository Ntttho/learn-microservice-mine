package com.nttho.orchestratorsaga.controller;

import com.nttho.orchestratorsaga.model.dto.request.OrderRequest;
import com.nttho.orchestratorsaga.model.dto.response.OrderResponse;
import com.nttho.orchestratorsaga.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class OrchestratorController {
    private final OrchestratorService orchestratorService;
    @PostMapping("/order-product")
    public OrderResponse orderProduct(@RequestBody OrderRequest orderRequest){
        return orchestratorService.orderProduct(orderRequest);
    }
}
