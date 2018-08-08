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

    // Working hours of a specific day
    private LocalTime openingHour;
    private LocalTime closingHour;

    // Hours left to complete the day
    private int hoursAvailable;

    // Reservations of the day
    private Map<LocalTime, String> reservations;

    public TimeTable() {
        // Default constructor required for calls to DataSnapshot.getValue(TimeTable.class)
    }

    public TimeTable(LocalTime openingHour, LocalTime closingHour) {
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        reservations = new HashMap<>();

        // Initial calculation of the amount of hours available in this day
        hoursAvailable = closingHour.minus(openingHour.getHour(), ChronoUnit.HOURS).getHour();
    }

    public int getHoursAvailable() {
        return hoursAvailable;
    }

    public LocalTime getOpeningHour() {
        return openingHour;
    }

    public LocalTime getClosingHour() {
        return closingHour;
    }

    public Map<LocalTime, String> getReservations() {
        return reservations;
    }

    /**
     * Check whether the time is between the working hours.
     * @param time time of the reservation
     * @return true if time is between the working hours.
     */
    public boolean isReservationTimeLegal(CharSequence time) {
        LocalTime t = LocalTime.parse(time);
        if (t.isBefore(openingHour) || t.isAfter(closingHour)) {
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
    public boolean isReservationTimeFree(CharSequence time) {
        if (!isFull() || !isReservationTimeLegal(time)) {
            return false;
        }
        LocalTime t = LocalTime.parse(time);
        return reservations.containsKey(t);
    }

    /**
     * Insert a new reservation into the timetable.
     * @param time reservation time
     * @param userId reservation user
     * @return false if time is not ok (out of working hours bounds).
     */
    public boolean insertReservation(CharSequence time, String userId)
            throws IllegalArgumentException {
        if (!isReservationTimeFree(time)) {
            throw new IllegalArgumentException("time is illegal or reservation not empty.");
        }
        // Insert the reservation into the map
        LocalTime timeKey = LocalTime.parse(time);
        reservations.put(timeKey, userId);

        return true;
    }

    // TODO: retrieve full user, not only his ID
    public String getReservation(CharSequence time) throws IllegalArgumentException {
        if (!isReservationTimeLegal(time)) {
            throw new IllegalArgumentException("getReservation: Time is illegal.");
        }
        return reservations.get(LocalTime.parse(time));
    }
}
