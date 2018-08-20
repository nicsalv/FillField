package com.ale2nico.fillfield.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user reservation with some important details.
 * It will be stored inside the user node.
 */
public class Reservation {

    // Field that's been reserved
    private String fieldKey;

    // Reservation time and date
    private String date;
    private String time;

    public Reservation() {
        // Default constructor required for calls to DataSnapshot.getValue(Reservation.class)
    }

    public Reservation(String fieldKey, String date, String time) {
        this.fieldKey = fieldKey;
        this.date = date;
        this.time = time;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fieldKey", fieldKey);
        result.put("date", date);
        result.put("time", time);

        return result;
    }
}
