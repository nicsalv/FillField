package com.ale2nico.fillfield;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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

    // Reference to the dialog
    private AlertDialog timeDialog;

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

                    // Build available hours list
                    List<String> availableHours = buildHoursList(mFieldAgenda.getOpeningHour(),
                            mFieldAgenda.getClosingHour(), new ArrayList<String>(), selectedDate);

                    // Add available hours to data set
                    addAll(availableHours);
                }
                else {
                    // There is at least one hour unavailable on the selected date
                    TimeTable timeTable = mFieldAgenda.getTimeTable(selectedDate);

                    // Build unavailable hours list
                    List<String> busyHours = buildBusyHoursList(timeTable);

                    // Build available hours list
                    List<String> availableHours = buildHoursList(mFieldAgenda.getOpeningHour(),
                            mFieldAgenda.getClosingHour(), busyHours, selectedDate);

                    // Add available hours to data set
                    addAll(availableHours);
                }

                // Notify the AdapterView that it should refresh itself
                notifyDataSetChanged();

                if (!isEmpty()) {
                    // If there is at least one hour available, enable positive button
                    timeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    // Display a toast to inform that there are no hours available
                    Toast.makeText(getContext(), R.string.dialog_no_hours_available, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Cannot retrieve available hours from Firebase");
            }
        });
    }

    public void setTimeDialog(AlertDialog timeDialog) {
        this.timeDialog = timeDialog;
    }

    /**
     * Builds a list with available reservation times based on the timetable
     * on the selected date.
     * @param openingHour Opening hour on selected date
     * @param closingHour Closing hour on selected date
     * @param busyHours Busy hour on selected date
     * @return a list with available reservation times on the selected date
     */
    public List<String> buildHoursList(String openingHour, String closingHour,
                                       List<String> busyHours, String selectedDate) {
        List<String> result = new ArrayList<>();

        // Check if the reservation is for today
        boolean dateIsToday = LocalDate.parse(selectedDate).isEqual(LocalDate.now());

        // Check available hours from opening time
        String currentHour = openingHour;

        while (LocalTime.parse(currentHour).isBefore(LocalTime.parse(closingHour))) {
            if (busyHours.indexOf(currentHour) == -1) {
                // currentHour is available...
                if (!dateIsToday || LocalTime.parse(currentHour).isAfter(LocalTime.now())) {
                    // ...currentHour is still in the future, add it to result
                    result.add(currentHour);
                }
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
}
