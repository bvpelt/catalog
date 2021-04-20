package com.bsoft.catalogus.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Getter
@AllArgsConstructor
public class AbstractBaseEndpoint {

    private final String baseUrl;

    private final String apiKey;

    private final RestTemplate restTemplate;

    private RestTemplate restTemplateWithConnectReadTimeout;

    public RestTemplate getRestTemplate() {

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        return restTemplate;
    }

    public RestTemplate getRestTemplateWithConnectReadTimeout() {
        int TIMEOUT = 60*1000; // 60 * 1000 ms
        restTemplateWithConnectReadTimeout = new RestTemplateBuilder()
                .messageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .setConnectTimeout(Duration.ofMillis(TIMEOUT))
                .setReadTimeout(Duration.ofMillis(TIMEOUT))
                .build();
        return restTemplateWithConnectReadTimeout;
    }


    public HttpHeaders buildGetRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.add("Content-Type", "application/json");
        headers.add("Content-Type", "application/problem+json");
        headers.add("x-api-key", getApiKey());
        return headers;
    }

    public HttpHeaders buildPostRequestHeaders(boolean withRdNewCrsHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept-Type", "application/json");
        headers.add("x-api-key", getApiKey());

        if (withRdNewCrsHeaders) {
            headers.add("content-crs", "epsg:28992");
            headers.add("accept-crs", "epsg:28992");
        }
        return headers;
    }

}
