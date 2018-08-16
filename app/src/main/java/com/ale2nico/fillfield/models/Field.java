package com.ale2nico.fillfield.models;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a soccer field.
 */
@IgnoreExtraProperties
public class Field {

    private String userId;
    private String name;

    // Geographical position of the field
    private double latitude;
    private double longitude;

    // Working hours of the field. See TimeTable about why we use String.
    private String openingHour;
    private String closingHour;

    // Hearts put by users
    private int heartsCount = 0;
    private Map<String, Boolean> hearts = new HashMap<>();

    public Field() {
        // Default constructor required for calls to DataSnapshot.getValue(Field.class)
    }

    public Field(String userId, String name, double latitude, double longitude,
                 String openingHour, String closingHour) {

        // Set field's owner and name
        this.userId = userId;
        this.name = name;

        // Set position
        this.latitude = latitude;
        this.longitude = longitude;

        // Set working hours
        this.openingHour = openingHour;
        this.closingHour = closingHour;

    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getOpeningHour() { return openingHour; }

    public String getClosingHour() { return closingHour; }

    public void setHeartsCount(int heartsCount) {
        this.heartsCount = heartsCount;
    }

    public int getHeartsCount() {
        return heartsCount;
    }

    public Map<String, Boolean> getHearts() {
        return hearts;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("name", name);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("openingHour", openingHour);
        result.put("closingHour", closingHour);
        result.put("heartsCount", heartsCount);
        result.put("hearts", hearts);

        return result;
    }
}
