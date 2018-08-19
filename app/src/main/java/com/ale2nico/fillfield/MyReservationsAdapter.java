package com.ale2nico.fillfield;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale2nico.fillfield.models.Field;
import com.ale2nico.fillfield.models.Reservation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyReservationsAdapter
        extends RecyclerView.Adapter<MyReservationsAdapter.MyReservationViewHolder> {

    private static final String TAG = "MyReservationsAdapter";

    // Field key needed to retrieve information about a field inside a reservation
    private String currentFieldKey;

    // Reservation data set: it doesn't contain field info
    List<Reservation> reservations = new ArrayList<>();

    // Fields inside reservations
    Map<String, Field> reservationFields = new HashMap<>();

    public MyReservationsAdapter() {
        // Reference to the user into the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid()).child("reservations");

        // Get reservations with a listener
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot reservationSnap : dataSnapshot.getChildren()) {
                    Reservation currentRes = reservationSnap.getValue(Reservation.class);

                    // Add the reservation to the list
                    reservations.add(currentRes);

                    // Get field info of current reservation
                    getReservationFieldInfo(currentRes.getFieldKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getReservationFieldInfo(final String fieldKey) {
        // Reference to the field into the database
        DatabaseReference fieldRef = FirebaseDatabase.getInstance().getReference()
                .child("fields").child(fieldKey);

        fieldRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Field field = dataSnapshot.getValue(Field.class);

                // Add the field to the data set so as to be displayed by the adapter
                reservationFields.put(fieldKey, field);

                // Now both reservation and field are ready to display,
                // so we can notify the adapter to update its content.
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Cannot retrieve field information inside reservation");
            }
        });
    }

    @NonNull
    @Override
    public MyReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MyReservationViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public class MyReservationViewHolder extends RecyclerView.ViewHolder {

        // References to views into 'my_reservation_item' layout
        public TextView myResDate;
        public TextView myResTime;
        public TextView myResPlace;
        public ImageView myResShareImageView;
        public TextView myResFieldName;

        public MyReservationViewHolder(View view) {
            super(view);

            // Get references to views
            myResDate = view.findViewById(R.id.my_res_date_text);
            myResTime = view.findViewById(R.id.my_res_time_text);
            myResPlace = view.findViewById(R.id.my_res_where_text);
            myResShareImageView = view.findViewById(R.id.my_res_share_image);
            myResFieldName = view.findViewById(R.id.my_res_field_name);
        }
    }
}
