package com.vibeus.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class LoggingFilter {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public GlobalFilter requestLoggingFilter() {
        return (exchange, chain) -> {
            log.info(">>> {} {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI());

            return chain.filter(exchange).then(Mono.fromRunnable(() ->
                    log.info("<<< {} {} - {}",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI(),
                            exchange.getResponse().getStatusCode())));
        };
    }
}
