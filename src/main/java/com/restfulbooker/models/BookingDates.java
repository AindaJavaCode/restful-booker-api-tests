package com.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Dates are kept as yyyy-MM-dd strings, exactly as the API sends and accepts them. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingDates(String checkin, String checkout) {
}
