package com.nttho.apigateway.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

public class LoadBalancingConfiguration {
    @Bean
    public ReactorServiceInstanceLoadBalancer randomLoadBalancer(
            Environment environment, LoadBalancerClientFactory loadBalancerClientFactory
    ){
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        assert name != null;
        return
                new RandomLoadBalancer(
                        loadBalancerClientFactory.getProvider(name, ServiceInstanceListSupplier.class), name
                );
    }
}
