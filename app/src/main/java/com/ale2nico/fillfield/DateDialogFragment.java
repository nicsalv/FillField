package com.ale2nico.fillfield;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Display a DatePickerDialog when the user clicks on the reserve button.
 */
public class DateDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get listener
        DatePickerDialog.OnDateSetListener dateSetListener
                = (DatePickerDialog.OnDateSetListener) getActivity();

        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog
        DatePickerDialog dateDialog
                = new DatePickerDialog(getActivity(), dateSetListener, year, month, day);

        // Set minimum selectable date to today
        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        return dateDialog;
    }
}
