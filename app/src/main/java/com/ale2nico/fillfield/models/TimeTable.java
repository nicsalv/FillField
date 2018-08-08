package com.ale2nico.fillfield.models;

import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the timetable for a specific day
 * in which a Field can store its reservations.
 * So far, reservations are available only on an hour-basis,
 * which means that a 9.00-10.00 reservation is ok but
 * a 9.30-10.30 is not.
 */
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
    private Map<String, String> reservations;

    public TimeTable() {
        // Default constructor required for calls to DataSnapshot.getValue(TimeTable.class)
    }

    public TimeTable(String openingHour, String closingHour) {
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        reservations = new HashMap<>();

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
        if (!isFull() || !isReservationTimeLegal(time)) {
            return false;
        }
        return reservations.containsKey(time);
    }

    /**
     * Insert a new reservation into the timetable.
     * @param time reservation time
     * @param userId reservation user
     * @return false if time is not ok (out of working hours bounds).
     */
    public boolean insertReservation(String time, String userId)
            throws IllegalArgumentException {
        if (!isReservationTimeFree(time)) {
            throw new IllegalArgumentException("time is illegal or reservation not empty.");
        }
        // Insert the reservation into the map
        reservations.put(time, userId);

        return true;
    }

    // TODO: retrieve full user, not only his ID
    public String getReservation(String time) throws IllegalArgumentException {
        if (!isReservationTimeLegal(time)) {
            throw new IllegalArgumentException("getReservation: Time is illegal.");
        }
        return reservations.get(time);
    }
}
