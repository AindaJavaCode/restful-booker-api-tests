package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.config.Config;
import com.restfulbooker.models.AuthRequest;
import com.restfulbooker.models.AuthResponse;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthTest extends BaseTest {

    @Test(groups = {"regression"}, description = "Valid credentials return a token")
    public void validCredentialsReturnToken() {
        AuthResponse auth = authClient.createToken(new AuthRequest(Config.username(), Config.password()))
                .then().statusCode(200)
                .extract().as(AuthResponse.class);

        assertThat(auth.token()).isNotBlank();
    }
}
