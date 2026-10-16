package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.models.CreatedBooking;
import org.testng.annotations.Test;

public class DeleteBookingTest extends BaseTest {

    @Test(groups = {"smoke"}, description = "DELETE with a valid token removes the booking")
    public void deleteBookingWithValidToken() {
        // Arrange
        CreatedBooking booking = createBooking();

        // Act
        bookingClient.deleteBooking(booking.bookingid(), token())
                .then().statusCode(201);

        // Assert: it's really gone, not just "201 returned"
        bookingClient.getBooking(booking.bookingid())
                .then().statusCode(404);
    }

    @Test(description = "DELETE without a token is rejected and the booking still exists")
    public void deleteBookingWithoutToken() {
        CreatedBooking booking = createBooking();

        bookingClient.deleteBooking(booking.bookingid(), null)
                .then().statusCode(403);

        bookingClient.getBooking(booking.bookingid())
                .then().statusCode(200);
    }

    @Test(description = "DELETE with an invalid token is rejected and the booking still exists")
    public void deleteBookingWithInvalidToken() {
        CreatedBooking booking = createBooking();

        bookingClient.deleteBooking(booking.bookingid(), "not-a-real-token")
                .then().statusCode(403);

        bookingClient.getBooking(booking.bookingid())
                .then().statusCode(200);
    }

    @Test(description = "DELETE on a booking that doesn't exist returns 405")
    public void deleteNonExistentBooking() {
        bookingClient.deleteBooking(Integer.MAX_VALUE, token())
                .then().statusCode(405);
    }

    @Test(description = "Deleting the same booking twice fails the second time")
    public void deleteBookingTwice() {
        CreatedBooking booking = createBooking();

        bookingClient.deleteBooking(booking.bookingid(), token())
                .then().statusCode(201);
        bookingClient.deleteBooking(booking.bookingid(), token())
                .then().statusCode(405);
    }
}

