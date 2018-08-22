package com.ale2nico.fillfield;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ale2nico.fillfield.models.Field;
import com.ale2nico.fillfield.models.Reservation;
import com.ale2nico.fillfield.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyReservationsAdapter
        extends RecyclerView.Adapter<MyReservationsAdapter.MyReservationViewHolder> {

    private static final String TAG = "MyReservationsAdapter";

    private ProgressBar progressBar;

    private ReservationsFragment.OnContactButtonClickListener contactButtonClickListener;

    private ReservationsFragment.ShareImageClickListener shareImageClickListener;
    // Reservation data set: it doesn't contain field info
    List<Reservation> reservations = new ArrayList<>();

    // Fields inside reservations
    Map<String, Field> reservationFields = new HashMap<>();
    Map<String, String> reservationOwnerEmails = new HashMap<>();

    public MyReservationsAdapter(final ProgressBar progressBar, final TextView emptyTextView,
                                 ReservationsFragment.OnContactButtonClickListener listener,
                                 ReservationsFragment.ShareImageClickListener shareListener) {
        // Reference to the user's reservations into the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(getUid()).child("reservations");

        // Get reservations with a listener
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot reservationSnap : dataSnapshot.getChildren()) {
                    Reservation currentRes = reservationSnap.getValue(Reservation.class);

                    // Indicates whether the reservation is past or not
                    boolean isPast = LocalDate.parse(currentRes.getDate()).isBefore(LocalDate.now())
                            || LocalDate.parse(currentRes.getDate()).isEqual(LocalDate.now())
                            && LocalTime.parse(currentRes.getTime()).isBefore(LocalTime.now());

                    if (!isPast) {
                        // Add the reservation to the data set
                        reservations.add(currentRes);

                        // Get current reservation field info
                        getReservationFieldInfo(currentRes.getFieldKey());
                    }
                }
                if (reservations.isEmpty()) {
                    // Hide progress bar and show empty view
                    progressBar.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Cannot retrieve reservation information from Firebase");
            }
        });

        this.progressBar = progressBar;
        this.contactButtonClickListener = listener;
        this.shareImageClickListener = shareListener;
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

                getOwnerInfo(fieldKey, field.getUserId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Cannot retrieve field information inside reservation");
            }
        });
    }

    private void getOwnerInfo(String fieldKey, String userKey) {
        // Database reference to the user
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userKey);

        // Get user info
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User ownerUser = dataSnapshot.getValue(User.class);

                // Store the user email into the map
                reservationOwnerEmails.put(fieldKey, ownerUser.getEmail());

                // Hide progress bar
                progressBar.setVisibility(View.GONE);

                // Now both reservation and field are ready to display,
                // so we can notify the adapter to update its content.
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @NonNull
    @Override
    public MyReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate 'my_reservation_item' layout into the holder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_reservation_item, parent, false);

        return new MyReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReservationViewHolder holder, int position) {
        // Bind reservation info through the reservation list
        holder.myResDate.setText(holder.formatDate(reservations.get(position).getDate()));
        holder.myResTime.setText(reservations.get(position).getTime());

        // The fieldKey allows us to obtain field info through the map data set
        String fieldKey = reservations.get(position).getFieldKey();
        Field field = reservationFields.get(fieldKey);

        // Set field features
        holder.myResPlace.setText(field.getAddress());
        holder.myResFieldName.setText(field.getName());
        holder.myResFieldSurface.setText(field.getSurface());
        holder.myResFieldSize.setText(field.getSize());
        holder.myResFieldPrice.setText(field.getPrice());

        holder.contactOwnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactButtonClickListener.onContactButtonClick(reservationOwnerEmails.get(fieldKey));
            }
        });
        holder.myResShareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImageClickListener.onShareImageClick((String) holder.myResFieldName.getText(),
                        (String) holder.myResDate.getText(), (String) holder.myResTime.getText());
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public class MyReservationViewHolder extends RecyclerView.ViewHolder {

        // References to views into 'my_reservation_item' layout
        public TextView myResDate;
        public TextView myResTime;
        public TextView myResPlace;
        public TextView myResFieldName;
        public TextView myResFieldSurface;
        public TextView myResFieldSize;
        public TextView myResFieldPrice;
        public Button contactOwnerButton;
        public ImageView myResShareImageView;

        public MyReservationViewHolder(View view) {
            super(view);

            // Get references to views
            myResDate = view.findViewById(R.id.my_res_date_text);
            myResTime = view.findViewById(R.id.my_res_time_text);
            myResPlace = view.findViewById(R.id.my_res_where_text);
            myResShareImageView = view.findViewById(R.id.my_res_share_image);
            myResFieldName = view.findViewById(R.id.my_res_field_name);
            myResFieldSurface = view.findViewById(R.id.my_res_field_surface);
            myResFieldSize = view.findViewById(R.id.my_res_field_size);
            myResFieldPrice = view.findViewById(R.id.my_res_field_price);
            contactOwnerButton = view.findViewById(R.id.my_res_contact_button);
            myResShareImageView = view.findViewById(R.id.my_res_share_image);

        }

        public String formatDate(String dateToFormat) {
            // Format date with a specified formatter
            LocalDate result = LocalDate.parse(dateToFormat);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

            return result.format(formatter);
        }
    }
}
