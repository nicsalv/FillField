package com.ale2nico.fillfield.models;

import android.util.Log;

import com.google.firebase.database.Exclude;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeParseException;

import java.util.HashMap;
import java.util.Map;

public class FieldAgenda {

    // Key of the field stored in the database
    private String fieldKey;

    // Calendar which contains the reservations of the field
    private Map<String, TimeTable> calendar = new HashMap<>();

    // Working hours of the field. See TimeTable about why we use String.
    private String openingHour;
    private String closingHour;

    public FieldAgenda() {
        // Default constructor required for calls to DataSnapshot.getValue(FieldAgenda.class)
    }

    public FieldAgenda(String fieldKey, String openingHour, String closingHour) {
        this.fieldKey = fieldKey;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getOpeningHour() {
        return openingHour;
    }

    public String getClosingHour() {
        return closingHour;
    }

    /**
     * A date is legal if it's not in the past.
     *
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
     *
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
     *
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

        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fieldKey", fieldKey);
        result.put("calendar", calendar);
        result.put("openingHour", openingHour);
        result.put("closingHour", closingHour);

        return result;

    }
}
