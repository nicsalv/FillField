package com.ale2nico.fillfield.models;

import android.location.Address;
import android.support.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a soccer field.
 */
@IgnoreExtraProperties
public class Field implements SortedListAdapter.ViewModel {

    private String userId;
    private String name;

    // Geographical position of the field
    private double latitude;
    private double longitude;

    // Geographical address of the field
    private String address;

    // Working hours of the field. See TimeTable about why we use String.
    private String openingHour;
    private String closingHour;

    // Field features
    private String surface;
    private String size;
    private String price;

    // Hearts put by users
    private int heartsCount = 0;
    private Map<String, Boolean> hearts = new HashMap<>();



    public Field() {
        // Default constructor required for calls to DataSnapshot.getValue(Field.class)
    }


    public Field(String userId, String name, double latitude, double longitude,
                 String openingHour, String closingHour, String address,
                 String surface, String size, String price) {

        // Set field's owner and name
        this.userId = userId;
        this.name = name;

        // Set position
        this.latitude = latitude;

        this.longitude = longitude;
        this.address = address;

        // Set working hours
        this.openingHour = openingHour;
        this.closingHour = closingHour;

    }

    public String getAddress() {
        return address;
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

    public String getOpeningHour() {
        return openingHour;
    }

    public String getClosingHour() {
        return closingHour;
    }

    public String getSurface() {
        return surface;
    }

    public String getSize() {
        return size;
    }

    public String getPrice() {
        return price;
    }

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
        result.put("address", address);
        result.put("openingHour", openingHour);
        result.put("closingHour", closingHour);
        result.put("heartsCount", heartsCount);
        result.put("hearts", hearts);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return Double.compare(field.latitude, latitude) == 0 &&
                Double.compare(field.longitude, longitude) == 0 &&
                heartsCount == field.heartsCount &&
                Objects.equals(userId, field.userId) &&
                Objects.equals(name, field.name) &&
                Objects.equals(openingHour, field.openingHour) &&
                Objects.equals(closingHour, field.closingHour) &&
                Objects.equals(hearts, field.hearts);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userId, name, latitude, longitude, openingHour, closingHour, heartsCount, hearts);
    }

    @Override
    public <T> boolean isSameModelAs(@NonNull T model) {
        if (model instanceof Field) {
            final Field field = (Field) model;
            return field.getName() == this.name;
        }

        return false;
    }

    @Override
    public <T> boolean isContentTheSameAs(@NonNull T model) {
        if (model instanceof Field) {
            final Field other = (Field) model;
            return name != null ? name.equals(other.name) : other.name == null;
        }

        return false;
    }
}
