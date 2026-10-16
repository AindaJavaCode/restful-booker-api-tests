package com.restfulbooker.client;

import com.restfulbooker.config.Config;
import com.restfulbooker.models.AuthRequest;
import com.restfulbooker.models.AuthResponse;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthClient {

    public Response createToken(AuthRequest credentials) {
        return given().spec(Specs.base())
                .body(credentials)
                .when().post(Endpoints.AUTH);
    }

    /** Returns a valid token for the configured admin user, failing fast if none is issued. */
    public String getToken() {
        AuthResponse auth = createToken(new AuthRequest(Config.username(), Config.password()))
                .then().statusCode(200)
                .extract().as(AuthResponse.class);
        if (auth.token() == null) {
            throw new IllegalStateException("No token issued, reason: " + auth.reason());
        }
        return auth.token();
    }
}
