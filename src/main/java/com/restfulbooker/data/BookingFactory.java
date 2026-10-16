package com.restfulbooker.data;

import com.restfulbooker.models.Booking;
import com.restfulbooker.models.BookingDates;
import net.datafaker.Faker;

import java.time.LocalDate;

public final class BookingFactory {

    private static final Faker FAKER = new Faker();

    private BookingFactory() {
    }

    /** A valid booking with random guest details and a stay starting within the next 30 days. */
    public static Booking random() {
        LocalDate checkin = LocalDate.now().plusDays(FAKER.number().numberBetween(1, 30));
        LocalDate checkout = checkin.plusDays(FAKER.number().numberBetween(1, 14));
        return new Booking(
                FAKER.name().firstName(),
                FAKER.name().lastName(),
                FAKER.number().numberBetween(50, 2000),
                FAKER.bool().bool(),
                new BookingDates(checkin.toString(), checkout.toString()),
                FAKER.options().option("Breakfast", "Late checkout", "Extra pillows", "Airport transfer"));
    }
}
