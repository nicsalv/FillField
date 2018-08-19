package com.ale2nico.fillfield.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeParseException;

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

    // Working hours of the field. See TimeTable about why we use String.
    private String openingHour;
    private String closingHour;

    // Calendar which contains the reservations of the field
    private Map<String, TimeTable> calendar = new HashMap<>();

    // Hearts put by users
    private int heartsCount = 0;
    private Map<String, Boolean> hearts = new HashMap<>();



    public Field() {
        // Default constructor required for calls to DataSnapshot.getValue(Field.class)
    }

    public Field(String userId, String name, double latitude, double longitude,
                 String openingHour, String closingHour, Context context) {

        // Set field's owner and name
        this.userId = userId;
        this.name = name;

        // Set position
        this.latitude = latitude;
        this.longitude = longitude;

        // Set timetables
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

    public String getOpeningHour() {
        return openingHour;
    }

    public String getClosingHour() {
        return closingHour;
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

    /**
     * A date is legal if it's not in the past.
     * @param date reservation date
     * @return true if the date is in the future.
     */
    public boolean isDateLegal(String date) {
        LocalDate reservationDate = LocalDate.parse(date);
        return !reservationDate.isBefore(LocalDate.now());
    }

    /**
     * Get the timetable of a specified day or null if
     * there is no timetable for that day.
     * @param date any day
     * @return the timetable for the specified day
     */
    public TimeTable getTimeTable(String date) {
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            Log.e("LocalDate.parse()", "Cannot parse date.");
            // In order not to crash the app, date becomes now.
            date = LocalDate.now().toString();
        }
        return calendar.get(date);
    }

    /**
     * Get reservation on a particular date.
     * @param date reservation date
     * @param time reservation time
     * @return the uid of the reservation owner, else null.
     */
    public String getReservation(String date, String time) {
        try {
            LocalDate.parse(date);
            LocalDate.parse(time);
        } catch (DateTimeParseException e) {
            Log.e("LocalDate.parse()", "Cannot parse date or time.");
            // In order not to crash the app, date and time become now.
            date = LocalDate.now().toString();
            time = LocalTime.now().toString();
        }

        return calendar.get(date).getReservation(time);
    }

    public void insertReservation(String date, String time, String userId)
            throws IllegalArgumentException {
        if (!isDateLegal(date)) {
            // 'date' is in the past
            throw new IllegalArgumentException("date is illegal.");
        }
        if (getTimeTable(date) != null) {
            // There is at least one reservation on this day
            TimeTable timeTable = getTimeTable(date);
            // Parameter 'time' is checked inside the method
            timeTable.insertReservation(time, userId);
        } else {
            // On this day the field is completely free (so far)
            TimeTable timeTable = new TimeTable(openingHour, closingHour);
            timeTable.insertReservation(time, userId);

            // Insert new TimeTable into the calendar
            calendar.put(date, timeTable);
        }
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
        result.put("calendar", calendar);
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
                Objects.equals(calendar, field.calendar) &&
                Objects.equals(hearts, field.hearts);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userId, name, latitude, longitude, openingHour, closingHour, calendar, heartsCount, hearts);
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
