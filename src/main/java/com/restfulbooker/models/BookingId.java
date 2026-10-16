package com.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One element of the GET /booking response array. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingId(int bookingid) {
}
