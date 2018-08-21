package com.ale2nico.fillfield;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ale2nico.fillfield.models.Field;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    // Display reservations on a specific day
    RecyclerView mReservationsRecycler;

    // Adapter that gets reservations info from the database
    private ReservationsAdapter mReservationsAdapter;

    // Progress Bar displayed during fields loading
    private ProgressBar loadingProgressBar;

    // Empty view displayed when the reservation list is empty on a selected date
    private TextView emptyTextView;

    // Reference to the calendar widget in order to be able to hide it
    private CalendarView calendarView;

    // Instance of the hosting activity in order to send email with contact button
    private OnContactButtonClickListener contactButtonListener;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactButtonClickListener) {
            contactButtonListener = (OnContactButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnReservationsButtonClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        contactButtonListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fieldKey = getArguments().getString(ARG_FIELD_KEY);
        }

        // Tell the activity that this fragment has a menu to show
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservations, container, false);

        // Get reference to the progress bar and the empty text view
        loadingProgressBar = view.findViewById(R.id.reservations_progress_bar);
        emptyTextView = view.findViewById(R.id.reservations_empty_view);

        // Set the listener for the calendar
        calendarView = view.findViewById(R.id.reservations_calendar);
        calendarView.setOnDateChangeListener(this);

        // Initializing RecyclerView and its layout
        mReservationsRecycler = view.findViewById(R.id.reservations_list);
        mReservationsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mReservationsRecycler.setVisibility(View.GONE);

        // Initialing adapter for the RecyclerView. At first it'll get reservations for today.
        mReservationsAdapter = new ReservationsAdapter(fieldKey, LocalDate.now().toString(),
                loadingProgressBar, emptyTextView, mReservationsRecycler, contactButtonListener);
        mReservationsRecycler.setAdapter(mReservationsAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get field name and set app bar title accordingly
        FirebaseDatabase.getInstance().getReference().child("fields")
                .child(fieldKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fieldName = dataSnapshot.child("name").getValue(String.class);

                // Set app bar title
                getActivity().setTitle(fieldName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("App bar title", "Can't print field name on the app bar.");
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu resource into layout
        inflater.inflate(R.menu.reservations_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_calendar_visibility:
                // Change calendar view visibility
                changeCalendarViewVisibility(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeCalendarViewVisibility(MenuItem item) {
        // Show/hide calendarView based on its current state

        if (calendarView.getVisibility() == View.GONE) {
            // Calendar is hidden. Show it.
            calendarView.setVisibility(View.VISIBLE);
            item.setTitle(R.string.hide_calendar_menu_item);
        }
        else if (calendarView.getVisibility() == View.VISIBLE) {
            // Calendar is displayed. Hide it.
            calendarView.setVisibility(View.GONE);
            item.setTitle(R.string.show_calendar_menu_item);
        }
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        // Hide both reservation list and empty view
        mReservationsRecycler.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        // Show progress bar
        loadingProgressBar.setVisibility(View.VISIBLE);

        // Cast the selected date as a String
        String selectedDate = MainActivity.getDateFromPicker(year, month, dayOfMonth);

        // Set selected date into the adapter
        mReservationsAdapter.setSelectedDate(selectedDate);
    }

    public interface OnContactButtonClickListener {

        public void onContactButtonClick(String userEmail);
    }

    public interface ShareImageClickListener {
        public void onShareImageClick(String fieldName, String reservationDay, String reservationTime);
    }
}
