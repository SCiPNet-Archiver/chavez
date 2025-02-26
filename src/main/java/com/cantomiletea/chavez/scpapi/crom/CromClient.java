package com.cantomiletea.chavez.scpapi.crom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CromClient {
    private final WebClient webClient;

    public <T> Optional<T> executeQuery(String query, Class<T> responseType) {
        Map<String, String> body = Map.of("query", query);
        return webClient.post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .blockOptional();
    }
}
