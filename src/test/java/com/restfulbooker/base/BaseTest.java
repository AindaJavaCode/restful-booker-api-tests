package com.restfulbooker.base;

import com.restfulbooker.client.AuthClient;
import com.restfulbooker.client.BookingClient;
import com.restfulbooker.client.PingClient;
import com.restfulbooker.data.BookingFactory;
import com.restfulbooker.models.Booking;
import com.restfulbooker.models.CreatedBooking;
import org.testng.annotations.AfterClass;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTest {

    protected final PingClient pingClient = new PingClient();
    protected final AuthClient authClient = new AuthClient();
    protected final BookingClient bookingClient = new BookingClient();

    private final List<Integer> createdBookingIds = new ArrayList<>();
    private String token;

    /** Lazily fetched admin token, reused across the test class. */
    protected String token() {
        if (token == null) {
            token = authClient.getToken();
        }
        return token;
    }

    /** Creates a random booking that is deleted automatically after the test class. */
    protected CreatedBooking createBooking() {
        return createBooking(BookingFactory.random());
    }

    protected CreatedBooking createBooking(Booking booking) {
        CreatedBooking created = bookingClient.createBooking(booking)
                .then().statusCode(200)
                .extract().as(CreatedBooking.class);
        createdBookingIds.add(created.bookingid());
        return created;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpBookings() {
        if (createdBookingIds.isEmpty()) {
            return;
        }
        String adminToken = token();
        // Ignore the result: a test may already have deleted its booking.
        createdBookingIds.forEach(id -> bookingClient.deleteBooking(id, adminToken));
        createdBookingIds.clear();
    }
}
