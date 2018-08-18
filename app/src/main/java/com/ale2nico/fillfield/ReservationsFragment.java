package com.ale2nico.fillfield;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import org.threeten.bp.LocalDate;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReservationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReservationsFragment extends Fragment
        implements CalendarView.OnDateChangeListener {

    // Key String for storing fieldKey
    private static final String ARG_FIELD_KEY = "fieldKey";

    // Display reservations for this field only
    private String fieldKey;

    // Adapter that gets reservations info from the database
    private ReservationsAdapter mReservationsAdapter;

    public ReservationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fieldKey Key of the field stored into the database.
     * @return A new instance of fragment ReservationsFragment.
     */
    public static ReservationsFragment newInstance(String fieldKey) {
        ReservationsFragment fragment = new ReservationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FIELD_KEY, fieldKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fieldKey = getArguments().getString(ARG_FIELD_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservations, container, false);

        // Set the listener for the calendar
        CalendarView calendar = view.findViewById(R.id.reservations_calendar);
        calendar.setOnDateChangeListener(this);

        // Initializing RecyclerView and its layout
        RecyclerView mReservationsRecycler = view.findViewById(R.id.reservations_list);
        mReservationsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Initialing adapter for the RecyclerView. At first it'll get reservations for today.
        mReservationsAdapter = new ReservationsAdapter(fieldKey, LocalDate.now().toString());
        mReservationsRecycler.setAdapter(mReservationsAdapter);

        return view;
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        // Cast the selected date as a String
        String selectedDate = MainActivity.getDateFromPicker(year, month, dayOfMonth);

        // Set selected date into the adapter
        mReservationsAdapter.setSelectedDate(selectedDate);
    }
}
