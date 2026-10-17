package com.inwza.e2e.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class HttpJsonClient {

    private final HttpClient http =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode get(String url, Optional<String> bearer) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).GET();
        bearer.ifPresent(token -> builder.header("Authorization", "Bearer " + token));
        HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return parseResponse(response);
    }

    public JsonNode post(String url, Object body, Optional<String> bearer)
            throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        bearer.ifPresent(token -> builder.header("Authorization", "Bearer " + token));
        HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return parseResponse(response);
    }

    public int getStatus(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }

    private JsonNode parseResponse(HttpResponse<String> response) throws IOException {
        JsonNode node = mapper.readTree(response.body().isBlank() ? "{}" : response.body());
        if (response.statusCode() >= 400) {
            String message = node.path("message").asText("HTTP " + response.statusCode());
            throw new HttpStatusException(response.statusCode(), message);
        }
        return node;
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public static class HttpStatusException extends RuntimeException {
        private final int status;

        public HttpStatusException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }
}
