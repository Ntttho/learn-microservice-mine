package com.nttho.orchestratorservice.controller;

import com.nttho.orchestratorservice.common.dto.request.OrderRequest;
import com.nttho.orchestratorservice.common.dto.response.OrderResponse;
import com.nttho.orchestratorservice.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orchestrator/order-product")
public class OrchestratorController {
    private final OrchestratorService orchestratorService;
    @PostMapping
    public OrderResponse orderProducts(@RequestBody OrderRequest orderRequest){
        return orchestratorService.orderTransactions(orderRequest);
    }
}
