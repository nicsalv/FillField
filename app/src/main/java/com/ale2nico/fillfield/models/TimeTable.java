package com.ale2nico.fillfield.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the timetable for a specific day
 * in which a Field can store its reservations.
 * So far, reservations are available only on an hour-basis,
 * which means that a 9.00-10.00 reservation is ok but
 * a 9.30-10.30 is not.
 */
@IgnoreExtraProperties
public class TimeTable {

    // Working hours of a specific day.
    // The type is String because LocalTime has no no-arguments constructor,
    // so it can't be stored on Firebase. Therefore, all the computations are
    // made with casting to LocalTime but the times are stored as String.
    private String openingHour;
    private String closingHour;

    // Hours left to complete the day
    private int hoursAvailable;

    // Reservations of the day in the form <time, userId>
    private Map<String, String> reservations = new HashMap<>();

    public TimeTable() {
        // Default constructor required for calls to DataSnapshot.getValue(TimeTable.class)
    }

    public TimeTable(String openingHour, String closingHour) {
        this.openingHour = openingHour;
        this.closingHour = closingHour;

        // Initial calculation of the amount of hours available in this day
        hoursAvailable = LocalTime.parse(closingHour)
                .minus(LocalTime.parse(openingHour).getHour(), ChronoUnit.HOURS).getHour();
    }

    public int getHoursAvailable() {
        return hoursAvailable;
    }

    public String getOpeningHour() {
        return openingHour;
    }

    public String getClosingHour() {
        return closingHour;
    }

    public Map<String, String> getReservations() {
        return reservations;
    }

    /**
     * Check whether the time is between the working hours.
     * @param time time of the reservation
     * @return true if time is between the working hours.
     */
    public boolean isReservationTimeLegal(String time) {
        LocalTime t = LocalTime.parse(time);
        if (t.isBefore(LocalTime.parse(openingHour)) ||
                t.isAfter(LocalTime.parse(closingHour))) {
            return false;
        }
        return true;
    }

    /**
     * Check if there are available times for reservation yet.
     * @return true if there are no reservation times available.
     */
    public boolean isFull() {
        return reservations.size() == hoursAvailable;
    }

    /**
     * Check whether the reservation time has already been booked.
     * @param time reservation time
     * @return false if: time is not legal;
     * there are no hours left in the day; time has already been booked.
     */
    public boolean isReservationTimeFree(String time) {
        if (isFull() || !isReservationTimeLegal(time)) {
            return false;
        }
        return !reservations.containsKey(time);
    }

    /**
     * Insert a new reservation into the timetable.
     * @param time reservation time
     * @param userId reservation user
     * @return false if time is not ok (out of working hours bounds).
     */
    public void insertReservation(String time, String userId)
            throws IllegalArgumentException {
        if (!isReservationTimeFree(time)) {
            throw new IllegalArgumentException("time is illegal or reservation not empty.");
        }
        // Insert the reservation into the map
        reservations.put(time, userId);
    }

    // TODO: retrieve full user, not only his ID
    public String getReservation(String time) {
        return reservations.get(time);
    }

    /**
     * Collect all the reservations time into a list.
     * @return a list which elements are reservations times
     */
    public List<String> timesToList() {
        List<String> result = new ArrayList<>();

        String currentTime = openingHour;
        while (LocalTime.parse(currentTime).isBefore(LocalTime.parse(closingHour))) {
            if (getReservation(currentTime) != null) {
                // There is a reservations at the current time; add it to the list.
                result.add(currentTime);
            }
            // Increment currentTime of one hour
            currentTime = LocalTime.parse(currentTime).plusHours(1).toString();
        }
        return result;
    }

    /**
     * Convert the timetable into a list.
     * @param reservationTimes A list containing reservation times of one day
     * @return the timetable as a list
     */
    public List<String> toList(List<String> reservationTimes) {
        List<String> result = new ArrayList<>();

        for (String time : reservationTimes) {
            // Surely there is a reservation at 'time'
            result.add(getReservation(time));
        }

        return result;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("openingHour", openingHour);
        result.put("closingHour", closingHour);
        result.put("hoursAvailable", hoursAvailable);
        result.put("reservations", reservations);

        return result;
    }
}
