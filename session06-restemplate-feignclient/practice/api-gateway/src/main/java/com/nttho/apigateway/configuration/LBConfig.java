package com.nttho.apigateway.configuration;

import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LBConfig {
    @Bean
    public ReactorServiceInstanceLoadBalancer reactorServiceInstanceLoadBalancer (
            Environment environment, LoadBalancerClientFactory loadBalancerClientFactory
    ){
        String name = environment.getProperty(loadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(
                loadBalancerClientFactory.getProvider(name, ServiceInstanceListSupplier.class)
                ,name);
    }
}
