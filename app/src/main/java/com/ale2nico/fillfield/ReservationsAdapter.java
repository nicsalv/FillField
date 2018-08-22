package com.ale2nico.fillfield;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ale2nico.fillfield.models.TimeTable;
import com.ale2nico.fillfield.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder> {

    private static final String TAG = "ReservationsAdapter";

    // Widgets reference so as to show loadings and empty view when needed
    private RecyclerView mReservationsRecycler;
    private ProgressBar loadingProgressBar;
    private TextView emptyTextView;

    private ReservationsFragment.OnContactButtonClickListener contactButtonClickListener;

    // Reference to the calendar into the database
    private DatabaseReference calendarRef;
    private ValueEventListener calendarListener;

    // List of times at which exists a reservation
    private List<String> reservationTimes = new ArrayList<>();

    // List of reservations on the selected day
    private List<String> reservations = new ArrayList<>();

    public ReservationsAdapter(String fieldKey, String selectedDate,
                               final ProgressBar loadingProgressBar, final TextView emptyTextView,
                               RecyclerView mReservationsRecycler,
                               ReservationsFragment.OnContactButtonClickListener contactButtonListener) {
        // Specific listener for retrieving reservation on a selected date
        calendarListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Store timetable inside the adapter list
                TimeTable currentTimeTable = dataSnapshot.getValue(TimeTable.class);

                // Show reservations list if there is at least a reservation on the selected date
                if (currentTimeTable != null) {
                    reservationTimes = currentTimeTable.timesToList();
                    reservations = currentTimeTable.toList(reservationTimes);

                    getUsersInfo();
                } else {
                    // Empty the two lists
                    reservationTimes = new ArrayList<>();
                    reservations = new ArrayList<>();

                    // Show empty view only
                    loadingProgressBar.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);

                    // Notify the recycler to update its content
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "It wasn't possible retrieve timetable for the selected date");
            }
        };

        // Get reference to widgets
        this.mReservationsRecycler = mReservationsRecycler;
        this.loadingProgressBar = loadingProgressBar;
        this.emptyTextView = emptyTextView;
        this.contactButtonClickListener = contactButtonListener;

        // This reference points to the timetable of the selected date of the selected field
        calendarRef = FirebaseDatabase.getInstance().getReference()
                .child("agenda").child(fieldKey).child("calendar").child(selectedDate);

        // Attach listener to database reference
        calendarRef.addListenerForSingleValueEvent(calendarListener);
    }

    public void setSelectedDate(String selectedDate) {
        // Now this reference has to point to the timetable of the selected date
        calendarRef = calendarRef.getParent().child(selectedDate);
        calendarRef.addListenerForSingleValueEvent(calendarListener);
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout from the 'reservation_item' layout
        View reservationItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reservation_item, parent, false);

        return new ReservationViewHolder(reservationItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        // Retrieve reservation info from the adapter list
        String reservationTime = reservationTimes.get(position).substring(0, 2); // Don't display minutes
        String reservationUser = reservations.get(position).split(",")[0];
        String reservationUserEmail = null;
        if(reservations.get(position).contains(",")) {
            reservationUserEmail = reservations.get(position).split(",")[1];
        }

        // Bind info to the view
        holder.reservationStartTime.setText(reservationTime);
        holder.reservationUser.setText(reservationUser);

        String finalReservationUserEmail = reservationUserEmail;
        holder.contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Write email to user
                contactButtonClickListener.onContactButtonClick(finalReservationUserEmail,
                        holder.contactButton.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    private void getUsersInfo() {
        for (final String userKey : reservations) {
            // Reference to the user into the database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(userKey);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User reservationUser = dataSnapshot.getValue(User.class);

                    // Get name and surname
                    String completeName
                            = reservationUser.getSurname() + " " + reservationUser.getName() + ","
                            + reservationUser.getEmail();

                    // Get email
                    String email = reservationUser.getEmail();

                    // Update reservation info
                    reservations.set(reservations.indexOf(userKey), completeName);

                    // Hide progress bar and show the list
                    mReservationsRecycler.setVisibility(View.VISIBLE);
                    loadingProgressBar.setVisibility(View.GONE);

                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "Cannot get reservation user info.");
                }
            });
        }
    }

    public class ReservationViewHolder extends RecyclerView.ViewHolder {

        // References to the views into the 'reservation_item' layout
        public TextView reservationStartTime;
        public TextView reservationUser;
        public Button contactButton;

        public ReservationViewHolder(View view) {
            super(view);

            // Get references to views
            reservationStartTime = view.findViewById(R.id.reservation_start_time);
            reservationUser = view.findViewById(R.id.reservation_user);
            contactButton = view.findViewById(R.id.contact_button);
        }

    }

}
