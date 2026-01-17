package org.example.todo_web_service.client;

import org.example.todo_web_service.dto.response.CheckTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${app.userServiceBaseUrl}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public CheckTokenResponse checkToken(String bearerToken) {

        try {
            return restClient.post()
                    .uri("/api/auth/checkToken")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .body(CheckTokenResponse.class);
        } catch (HttpClientErrorException.Unauthorized ex) {
            return new CheckTokenResponse(false, null, null, null, "Invalid or expired token");
        }
    }
}