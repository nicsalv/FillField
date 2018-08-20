package com.ale2nico.fillfield;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.ale2nico.fillfield.models.FieldAgenda;
import com.ale2nico.fillfield.models.TimeTable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import java.util.ArrayList;
import java.util.List;

public class TimeDialogAdapter extends ArrayAdapter<String> {

    private static final String TAG = "TimeDialogAdapter";

    public TimeDialogAdapter(@NonNull Context context, int resource,
                             final String selectedDate, String fieldKey) {
        super(context, resource);

        // Reference to the field agenda on the database
        DatabaseReference mAgendaRef = FirebaseDatabase.getInstance().getReference()
                .child("agenda").child(fieldKey);

        // This listener checks on the database which hours are available on the selected date
        mAgendaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FieldAgenda mFieldAgenda = dataSnapshot.getValue(FieldAgenda.class);

                if (mFieldAgenda.getTimeTable(selectedDate) == null) {
                    // There are no reservation on this day, all times are available.

                    // Build available hours list and add them to the data set
                    List<String> availableHours = buildHoursList(mFieldAgenda.getOpeningHour(),
                            mFieldAgenda.getClosingHour(), new ArrayList<String>());

                    if (LocalDate.parse(selectedDate).isEqual(LocalDate.now())) {
                        // Selected date is today: remove past hours
                        removeTodayPastHours(availableHours);
                    }

                    // Add available hours to data set
                    addAll(availableHours);
                }
                else {
                    // There is at least one hour unavailable on the selected date
                    TimeTable timeTable = mFieldAgenda.getTimeTable(selectedDate);

                    // Build unavailable hours list
                    List<String> busyHours = buildBusyHoursList(timeTable);

                    // Build available hours list and add them to the data set
                    List<String> availableHours = buildHoursList(mFieldAgenda.getOpeningHour(),
                            mFieldAgenda.getClosingHour(), busyHours);

                    if (LocalDate.parse(selectedDate).isEqual(LocalDate.now())) {
                        // Selected date is today: remove past hours
                        removeTodayPastHours(availableHours);
                    }

                    // Add available hours to data set
                    addAll(availableHours);
                }

                // Notify the AdapterView that it should refresh itself
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Cannot retrieve available hours from Firebase");
            }
        });
    }

    /**
     * Builds a list with available reservation times based on the timetable
     * on the selected date.
     * @param openingHour Opening hour on selected date
     * @param closingHour Closing hour on selected date
     * @param busyHours Busy hour on selected date
     * @return a list with available reservation times on the selected date
     */
    public List<String> buildHoursList(String openingHour, String closingHour, List<String> busyHours) {
        List<String> result = new ArrayList<>();

        // Check available hours from opening time
        String currentHour = openingHour;

        while (LocalTime.parse(currentHour).isBefore(LocalTime.parse(closingHour))) {
            if (busyHours.indexOf(currentHour) == -1) {
                // currentHour is available, add it to result.
                result.add(currentHour);
            }

            // Increment currentHour of one
            currentHour = LocalTime.parse(currentHour).plusHours(1).toString();
        }

        return result;
    }

    /**
     * Builds a list with unavailable times on a timetable on a selected date.
     * @param timeTable Timetable of selected date
     * @return a list with unavailable times on a selected date
     */
    public List<String> buildBusyHoursList(TimeTable timeTable) {
        List<String> result = new ArrayList<>();

        // Check unavailable hours from opening time
        String currentHour = timeTable.getOpeningHour();

        while (LocalTime.parse(currentHour).isBefore(LocalTime.parse(timeTable.getClosingHour()))) {
            if (timeTable.getReservation(currentHour) != null) {
                // There is a reservation at this time, add it to result.
                result.add(currentHour);
            }

            // Increment current hour by one
            currentHour = LocalTime.parse(currentHour).plusHours(1).toString();
        }

        return result;
    }

    private void removeTodayPastHours(List<String> hoursList) {
        List<String> pastHours = new ArrayList<>();

        for (String time : hoursList) {
            if (LocalTime.parse(time).isBefore(LocalTime.now())) {
                // 'time' is past, so it has no sense to show it as available
                pastHours.add(time);
            }
        }
    }
}
