package com.restfulbooker.client;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class PingClient {

    public Response healthCheck() {
        return given().spec(Specs.base())
                .when().get(Endpoints.PING);
    }
}
