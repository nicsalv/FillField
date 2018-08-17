package com.ale2nico.fillfield;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale2nico.fillfield.models.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyFieldsAdapter extends RecyclerView.Adapter<MyFieldsAdapter.MyFieldViewHolder> {

    private static final String TAG = "MyFieldsAdapter";

    // List of the fields stored into the database
    private List<String> myFieldsIds = new ArrayList<>();
    private List<Field> myFields = new ArrayList<>();

    // Field images stored as byte array keyed by fieldKey
    private Map<String, ByteBuffer> myFieldsImage = new HashMap<>();

    // Instance of the hosting activity
    private MyFieldsFragment.OnReservationsButtonClickedListener mReservationButtonListener;

    // Used into the AsyncTask
    Context mContext;

    public MyFieldsAdapter(Context context,
                           MyFieldsFragment.OnReservationsButtonClickedListener listener) {
        mContext = context;
        mReservationButtonListener = listener;
        // This listener gains all the fields that the currently signed-in user owns.
        ValueEventListener myFieldsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Add every field that the user owns into the list
                for (DataSnapshot fieldSnap : dataSnapshot.getChildren()) {
                    String currentFieldKey = fieldSnap.getKey();
                    Field currentField = fieldSnap.getValue(Field.class);

                    if (currentField.getUserId().equals(getUid())) {
                        // Signed-in user owns this field. Add it to the list.
                        myFieldsIds.add(currentFieldKey);
                        myFields.add(myFieldsIds.indexOf(currentFieldKey), currentField);
                        notifyItemInserted(myFieldsIds.indexOf(currentFieldKey));
                        // TODO: remove empty view
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "It wasn't possible to get user's fields.");
            }
        };

        // Reference to the 'fields' node into the database
        DatabaseReference mFieldsReference;
        mFieldsReference = FirebaseDatabase.getInstance().getReference().child("fields");

        // Add the listener to the reference
        mFieldsReference.addListenerForSingleValueEvent(myFieldsListener);
    }

    @NonNull
    @Override
    public MyFieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the field card
        View fieldCard = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_field_card, parent, false);

        return new MyFieldViewHolder(fieldCard);
    }

    @Override
    public void onBindViewHolder(@NonNull MyFieldViewHolder holder, int position) {
        final String fieldKey = myFieldsIds.get(position);
        Field field = myFields.get(position);

        holder.bindToField(field, fieldKey);
    }

    @Override
    public int getItemCount() {
        return myFields.size();
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public class MyFieldViewHolder extends RecyclerView.ViewHolder {

        // View elements of the card
        public ImageView myFieldImage;
        public TextView myFieldAddress;
        public TextView myFieldName;
        public Button viewReservationButton;
        public TextView myFieldPrice;
        public TextView myFieldSurface;
        public TextView myFieldSize;

        public MyFieldViewHolder(View v) {
            super(v);

            myFieldImage = v.findViewById(R.id.card_my_field_image);
            myFieldAddress = v.findViewById(R.id.card_my_field_address);
            myFieldName = v.findViewById(R.id.card_my_field_name);
            viewReservationButton = v.findViewById(R.id.view_reservations);
            myFieldPrice = v.findViewById(R.id.my_field_price);
            myFieldSurface = v.findViewById(R.id.my_field_surface);
            myFieldSize = v.findViewById(R.id.my_field_size);
        }

        private void bindToField(Field field, final String fieldKey) {
            // Download and set the picture of the field (done at first because it can take much time).
            downloadAndSetFieldImage(fieldKey);

            // Find a human-readable addres to display into the card
            new DiscoverAddress().execute(field.getLatitude(), field.getLongitude());

            // Add listener to the reservations button
            viewReservationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mReservationButtonListener != null) {
                        mReservationButtonListener.onReservationsButtonClicked(fieldKey);
                    }
                }
            });

            // TODO: set all the field's fields
            myFieldName.setText(field.getName());
        }

        private void downloadAndSetFieldImage(final String fieldKey) {
            // Check if the image has already been downloaded
            if (myFieldsImage.containsKey(fieldKey)) {
                // No need to download, just set it into the view
                setFieldImage(myFieldsImage.get(fieldKey).array());
                return;
            }

            // Get reference to the image on the cloud
            StorageReference fieldImageRef
                    = FirebaseStorage.getInstance().getReference().child(fieldKey + ".jpg");

            // Declare maximum size for download, otherwise it could fill the memory and
            // make the app crash.
            final long ONE_MEGABYTE = 1024 * 1024;

            // Start the image download with the method 'getBytes' and set
            // it into the ImageView once it has finished.
            fieldImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] fieldImageBytes) {
                    // Store the image into the adapter so as to avoid downloading it again
                    ByteBuffer imageByteBuffer = ByteBuffer.wrap(fieldImageBytes);
                    myFieldsImage.put(fieldKey, imageByteBuffer);

                    setFieldImage(fieldImageBytes);
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Firebase Cloud Storage",
                            "A problem occured when trying to fetch image.");
                }
            });
        }

        private void setFieldImage(byte[] fieldImageBytes) {
            // Generate a bitmap image from the byte array
            Bitmap fieldImageBitmap = BitmapFactory
                    .decodeByteArray(fieldImageBytes, 0, fieldImageBytes.length);

            // Set the image into the field card after having removed the grey background
            myFieldImage.setBackground(null);
            myFieldImage.setImageBitmap(fieldImageBitmap);
        }

        class DiscoverAddress extends AsyncTask<Double, Void, String> {

            Double lat;
            Double lon;

            @Override
            protected String doInBackground(Double... params) {

                lat = params[0];
                lon = params[1];

                //A complete address -> addresses[0]
                String addressLine = null;

                //obtained by splitting addressLine
                String address = null;

                //Long vector filled by geocoder Object
                Address locality = null;

                //A list of possible addresses
                List<Address> addresses = null;

                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

                //try to get the location from lat e lon
                try {
                    addresses = geocoder.getFromLocation(lat, lon, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addresses.size() > 0) {
                    locality = addresses.get(0);
                    addressLine = locality.getAddressLine(0);
                    String[] addressLineSplitted = addressLine.split(",");
                    address = addressLineSplitted[2];
                }


                return address;
            }

            @Override
            protected void onPostExecute(String result){
                myFieldAddress.setText(result);
            }
        }

    }
}
