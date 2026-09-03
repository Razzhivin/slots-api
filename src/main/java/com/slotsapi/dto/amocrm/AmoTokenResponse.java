package com.slotsapi.dto.amocrm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AmoTokenResponse {
    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;
}