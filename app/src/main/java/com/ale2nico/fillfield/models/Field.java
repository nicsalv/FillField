package com.ale2nico.fillfield.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

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
    private String position;

    // Working hours of the field
    private LocalTime openingHour;
    private LocalTime closingHour;

    // Calendar which contains the reservations of the field
    private Map<LocalDate, TimeTable> calendar;

    public Field() {
        // Default constructor required for calls to DataSnapshot.getValue(Field.class)
    }

    public Field(String userId, String position, String name,
                 CharSequence openingHour, CharSequence closingHour) {
        this.userId = userId;
        this.position = position;
        this.name = name;
        calendar = new HashMap<>();
        this.openingHour = LocalTime.parse(openingHour);
        this.closingHour = LocalTime.parse(closingHour);
    }

    public String getUserId() {
        return userId;
    }

    public String getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    /**
     * A date is legal if it's not in the past.
     * @param date reservation date
     * @return true if the date is in the future.
     */
    public boolean isDateLegal(CharSequence date) {
        LocalDate reservationDate = LocalDate.parse(date);
        return !reservationDate.isBefore(LocalDate.now());
    }

    /**
     * Get the timetable of a specified day or null if
     * there is no timetable for that day.
     * @param date any day
     * @return the timetable for the specified day
     */
    public TimeTable getTimeTable(CharSequence date) {
        return calendar.get(LocalDate.parse(date));
    }

    /**
     * Get reservation on a particular date.
     * @param date reservation date
     * @param time reservation time
     * @return the uid of the reservation owner, else null.
     */
    public String getReservation(CharSequence date, CharSequence time) {
        return calendar.get(LocalDate.parse(date)).getReservation(time);
    }

    public void insertReservation(CharSequence date, CharSequence time, String userId)
            throws IllegalArgumentException {
        if (!isDateLegal(date)) {
            // 'date' is in the past
            throw new IllegalArgumentException("date is illegal.");
        }
        if (getTimeTable(date) != null) {
            // There is at least one reservation on this day
            TimeTable timeTable = getTimeTable(date);
            timeTable.insertReservation(time, userId);
        } else {
            // On this day the field is completely free (so far)
            TimeTable timeTable = new TimeTable(openingHour, closingHour);
            timeTable.insertReservation(time, userId);
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("name", name);
        result.put("position", position);
        result.put("openingHour", openingHour);
        result.put("closingHour", closingHour);
        result.put("calendar", calendar);

        return result;
    }
}
