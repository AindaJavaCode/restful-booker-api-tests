package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.data.BookingFactory;
import com.restfulbooker.models.Booking;
import com.restfulbooker.models.CreatedBooking;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingCrudTest extends BaseTest {

    @Test(groups = {"regression"}, description = "A created booking can be read back unchanged")
    public void createAndGetBooking() {
        Booking booking = BookingFactory.random();

        CreatedBooking created = createBooking(booking);
        assertThat(created.booking()).isEqualTo(booking);

        Booking fetched = bookingClient.getBooking(created.bookingid())
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking.json"))
                .extract().as(Booking.class);
        assertThat(fetched).isEqualTo(booking);
    }

}
