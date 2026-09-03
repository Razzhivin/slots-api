package com.slotsapi.client;

import com.slotsapi.dto.amocrm.AmoTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AmoApiClient {
    public static final String HTTPS_S_AMOCRM_RU_OAUTH_2_ACCESS_TOKEN = "https://%s.amocrm.ru/oauth2/access_token";
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${amocrm.client-id}")
    private String clientId;

    @Value("${amocrm.client-secret}")
    private String clientSecret;

    @Value("${amocrm.redirect-uri}")
    private String redirectUri;

    public AmoTokenResponse exchangeCodeForTokens(String code, String subdomain) {
        String url = String.format(HTTPS_S_AMOCRM_RU_OAUTH_2_ACCESS_TOKEN, subdomain);

        Map<String, Object> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", redirectUri
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<AmoTokenResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, AmoTokenResponse.class
        );

        return response.getBody();
    }

    public AmoTokenResponse refreshTokens(String refreshToken, String subdomain) {
        String url = String.format(HTTPS_S_AMOCRM_RU_OAUTH_2_ACCESS_TOKEN, subdomain);

        Map<String, Object> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "grant_type", "refresh_token",
                "refresh_token", refreshToken,
                "redirect_uri", redirectUri
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<AmoTokenResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, AmoTokenResponse.class
        );

        return response.getBody();
    }
}