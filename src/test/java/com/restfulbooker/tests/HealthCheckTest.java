package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import org.testng.annotations.Test;

public class HealthCheckTest extends BaseTest {

    @Test(groups = {"smoke"}, description = "GET /ping confirms the API is up")
    public void pingReturns201() {
        pingClient.healthCheck().then().statusCode(201);
    }
}
