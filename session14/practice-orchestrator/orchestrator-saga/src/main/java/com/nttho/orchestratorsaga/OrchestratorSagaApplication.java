package com.nttho.orchestratorsaga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrchestratorSagaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorSagaApplication.class, args);
    }

}
