package com.restfulbooker.client;

import com.restfulbooker.config.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Builds request specifications. A fresh spec is created per call so tests are safe to
 * run in parallel (nothing is set on RestAssured's static state).
 */
public final class Specs {

    private Specs() {
    }

    public static RequestSpecification base() {
        int timeout = Config.timeoutMs();
        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeout)
                        .setParam("http.socket.timeout", timeout))
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails()
                        .enablePrettyPrinting(true));

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(Config.baseUrl())
                .setConfig(config)
                // restful-booker answers 418 to POST /booking unless Accept is JSON
                .setAccept("application/json")
                .setContentType(ContentType.JSON);

        if (Config.logAll()) {
            builder.addFilter(new RequestLoggingFilter()).addFilter(new ResponseLoggingFilter());
        }
        return builder.build();
    }
}
