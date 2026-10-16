package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.data.BookingFactory;
import com.restfulbooker.models.Booking;
import com.restfulbooker.models.BookingDates;
import com.restfulbooker.models.CreatedBooking;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingUpdateTest extends BaseTest {

    @Test(groups = {"smoke"}, description = "PUT with a valid token replaces every field of the booking")
    public void updateBookingWithValidToken() {
        // Arrange: a booking to update, and the new values
        CreatedBooking original = createBooking();
        Booking changes = BookingFactory.random();

        // Act: send the update
        Booking returned = bookingClient.updateBooking(original.bookingid(), changes, token())
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking.json"))
                .extract().as(Booking.class);

        // Assert: the response and the stored booking both reflect the update
        assertThat(returned).isEqualTo(changes);
        assertThat(fetch(original.bookingid())).isEqualTo(changes);
    }

    @Test(description = "PUT without a token is rejected and the booking is left unchanged")
    public void updateBookingWithoutToken() {
        CreatedBooking original = createBooking();

        bookingClient.updateBooking(original.bookingid(), BookingFactory.random(), null)
                .then().statusCode(403);

        assertThat(fetch(original.bookingid())).isEqualTo(original.booking());
    }

    @Test(description = "PUT with an invalid token is rejected and the booking is left unchanged")
    public void updateBookingWithInvalidToken() {
        CreatedBooking original = createBooking();

        bookingClient.updateBooking(original.bookingid(), BookingFactory.random(), "not-a-real-token")
                .then().statusCode(403);

        assertThat(fetch(original.bookingid())).isEqualTo(original.booking());
    }

    @DataProvider
    public Object[][] bookingsMissingARequiredField() {
        Booking b = BookingFactory.random();
        return new Object[][]{
                {"firstname", new Booking(null, b.lastname(), b.totalprice(),
                        b.depositpaid(), b.bookingdates(), b.additionalneeds())},
                {"totalprice", new Booking(b.firstname(), b.lastname(), null,
                        b.depositpaid(), b.bookingdates(), b.additionalneeds())},
                {"checkin", new Booking(b.firstname(), b.lastname(), b.totalprice(),
                        b.depositpaid(), new BookingDates(null, b.bookingdates().checkout()),
                        b.additionalneeds())},
        };
    }

    @Test(dataProvider = "bookingsMissingARequiredField",
            description = "PUT with a required field missing returns 400 and changes nothing")
    public void updateBookingWithMissingField(String missingField, Booking invalid) {
        CreatedBooking original = createBooking();

        bookingClient.updateBooking(original.bookingid(), invalid, token())
                .then().statusCode(400);

        assertThat(fetch(original.bookingid()))
                .as("booking after rejected update without %s", missingField)
                .isEqualTo(original.booking());
    }

    @Test(description = "PUT on a booking that doesn't exist returns 405")
    public void updateNonExistentBooking() {
        bookingClient.updateBooking(Integer.MAX_VALUE, BookingFactory.random(), token())
                .then().statusCode(405);
    }

    /** Reads a booking back from the API so tests can check what was actually stored. */
    private Booking fetch(int bookingId) {
        return bookingClient.getBooking(bookingId)
                .then().statusCode(200)
                .extract().as(Booking.class);
    }
}