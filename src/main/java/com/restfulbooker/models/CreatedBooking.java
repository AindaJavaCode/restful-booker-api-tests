package com.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response body of POST /booking. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreatedBooking(int bookingid, Booking booking) {
}
