package com.ale2nico.fillfield;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Dialog that shows available reservation times for the selected date.
 */
public class TimeDialogFragment extends DialogFragment {

    private static final String ARG_SELECTED_DATE = "selectedDate";
    private static final String ARG_FIELD_KEY = "fieldKey";

    // Reference to the alert dialog
    private AlertDialog timeDialog;

    // Selected date chosen by the user in the previous date picker dialog
    private String selectedDate;

    // Key of the field in the current reservation
    private String fieldKey;

    /**
     * Factory method for easy instantiation.
     * @param selectedDate Selected date in the date picker
     * @return a new instance of TimeDialogFragment
     */
    public static TimeDialogFragment newInstance(String selectedDate, String fieldKey) {
        TimeDialogFragment newFragment = new TimeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_DATE, selectedDate);
        args.putString(ARG_FIELD_KEY, fieldKey);
        newFragment.setArguments(args);
        return newFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get selected date and field key
        if (getArguments() != null) {
            selectedDate = getArguments().getString(ARG_SELECTED_DATE);
            fieldKey = getArguments().getString(ARG_FIELD_KEY);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get instance of hosting acitivty that is a listener for the dialog buttons
        final OnTimeDialogClickListener buttonsListener
                = (OnTimeDialogClickListener) getActivity();

        // Instantiate list adapter that will fill the dialog
        final TimeDialogAdapter timeDialogAdapter = new TimeDialogAdapter((Context) getActivity(),
                android.R.layout.simple_list_item_single_choice, selectedDate, fieldKey);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.dialog_time_title)
                .setPositiveButton(R.string.dialog_time_pos_button, buttonsListener)
                .setNegativeButton(R.string.dialog_time_neg_button, buttonsListener)
                .setSingleChoiceItems(timeDialogAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This call permits to pass the selected time to the hosting activity
                        buttonsListener.onTimeDialogClick(timeDialogAdapter.getItem(which));
                    }
                });

        // Save reference to dialog in order to modify it later
        timeDialog = builder.create();

        // Attach the dialog to the adapter so it can be modified later
        timeDialogAdapter.setTimeDialog(timeDialog);

        timeDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // Disable confirm button while adapter fetches available hours
                timeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        return timeDialog;
    }
}
