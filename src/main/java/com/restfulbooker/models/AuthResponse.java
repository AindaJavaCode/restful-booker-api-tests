package com.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** On success only token is set; on bad credentials the API returns 200 with reason set. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(String token, String reason) {
}
