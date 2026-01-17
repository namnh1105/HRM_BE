package com.hainam.worksphere.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hainam.worksphere.user.dto.response.UserResponse;
import com.hainam.worksphere.user.dto.response.UserWithAuthorizationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("user")
    private UserWithAuthorizationResponse user;
}
