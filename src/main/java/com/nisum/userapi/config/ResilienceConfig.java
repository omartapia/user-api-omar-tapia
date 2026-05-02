package com.nisum.userapi.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreaker userCircuitBreaker() {
        return CircuitBreaker.ofDefaults("userServiceCircuit");
    }

    @Bean
    public Retry userRetry() {
        return Retry.ofDefaults("userServiceRetry");
    }

    @Bean
    public CircuitBreaker phoneCircuitBreaker() {
        return CircuitBreaker.ofDefaults("phoneServiceCircuit");
    }

    @Bean
    public Retry phoneRetry() {
        return Retry.ofDefaults("phoneServiceRetry");
    }
}
