package com.restfulbooker.client;

import com.restfulbooker.models.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper over the /booking endpoints. Methods return the raw Response so tests can
 * assert on status codes for both positive and negative scenarios.
 *
 * Write operations take a token; pass null to send the request unauthenticated.
 */
public class BookingClient {

    public Response getBookingIds() {
        return getBookingIds(Map.of());
    }

    /** Supported filters: firstname, lastname, checkin, checkout (yyyy-MM-dd). */
    public Response getBookingIds(Map<String, ?> filters) {
        return given().spec(Specs.base())
                .queryParams(filters)
                .when().get(Endpoints.BOOKING);
    }

    public Response getBooking(int id) {
        return given().spec(Specs.base())
                .pathParam("id", id)
                .when().get(Endpoints.BOOKING_BY_ID);
    }

    public Response createBooking(Object body) {
        return given().spec(Specs.base())
                .body(body)
                .when().post(Endpoints.BOOKING);
    }

    public Response updateBooking(int id, Booking booking, String token) {
        return authorized(token)
                .pathParam("id", id)
                .body(booking)
                .when().put(Endpoints.BOOKING_BY_ID);
    }

    public Response partialUpdateBooking(int id, Map<String, ?> fields, String token) {
        return authorized(token)
                .pathParam("id", id)
                .body(fields)
                .when().patch(Endpoints.BOOKING_BY_ID);
    }

    public Response deleteBooking(int id, String token) {
        return authorized(token)
                .pathParam("id", id)
                .when().delete(Endpoints.BOOKING_BY_ID);
    }

    private RequestSpecification authorized(String token) {
        RequestSpecification spec = given().spec(Specs.base());
        return token == null ? spec : spec.cookie("token", token);
    }
}
