package com.ale2nico.fillfield;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ale2nico.fillfield.HomeFragment.OnFieldClickListener;
import com.ale2nico.fillfield.models.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.FieldViewHolder> {

    public static final String TAG = "FieldAdapter";

    // Firebase Database references
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    // List of the fields stored into the database
    private List<String> mFieldsIds = new ArrayList<>();
    private List<Field> mFields = new ArrayList<>();

    private Map<Field, String> fieldKeyMap = new HashMap<>();

    // Field images stored as byte array keyed by fieldKey
    private Map<String, ByteBuffer> mFieldImage = new HashMap<>();

    // Interaction listener passes data to the hosting activity
    private final OnFieldClickListener mListener;

    private Context context;

    private ProgressBar progressBar;


    public FieldAdapter(DatabaseReference ref,
                        OnFieldClickListener listener) {
        mDatabaseReference = ref;
        mListener = listener;
    }

    public void setProgressBar(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    @Override
    @NonNull
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the 'field_card' layout inside a view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.field_card, parent, false);

        //save the context
        this.context = parent.getContext();

        // Create a FieldViewHolder that holds the view
        return new FieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FieldViewHolder holder, final int position) {
        final String fieldKey = mFieldsIds.get(position);
        Field field = mFields.get(position);


        // Determine if the current user has liked this field and set UI accordingly
        if (field.getHearts().containsKey(getUid())) {
            holder.heartImageView.setImageResource(R.drawable.ic_favorite_red_24dp);
        } else {
            holder.heartImageView.setImageResource(R.drawable.ic_favorite_outline_red_24dp);
        }

        // Bind field to ViewHolder, setting OnClickListener for the heart button
        holder.bindToField(field, fieldKey, new View.OnClickListener() {
            @Override
            public void onClick(View heartView) {
                // Ref to the field in the database
                DatabaseReference fieldRef = mDatabaseReference.child(fieldKey);

                // Run transaction
                onHeartClicked(fieldRef);
            }
        });

        // Set the listeners to the action buttons
        holder.action1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onReserveButtonClicked(mFields.get(position), fieldKey);
                }
            }
        });
        holder.action2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onMapButtonClicked(mFields.get(position));
                }
            }
        });
    }

    private void onHeartClicked(DatabaseReference fieldRef) {
        fieldRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Field f = mutableData.getValue(Field.class);
                if (f == null) {
                    return Transaction.success(mutableData);
                }

                if (f.getHearts().containsKey(getUid())) {
                    // Unstar the field and remove self from hearts
                    f.setHeartsCount(f.getHeartsCount() - 1);
                    f.getHearts().remove(getUid());
                } else {
                    // Star the field and add self to hearts
                    f.setHeartsCount(f.getHeartsCount() + 1);
                    f.getHearts().put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(f);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b,
                                   @Nullable DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    public void setChildEventListener(ChildEventListener childEventListener) {
        mChildEventListener = childEventListener;
        mDatabaseReference.addChildEventListener(childEventListener);
    }

    public List<String> getFieldsIds() {
        return mFieldsIds;
    }

    public List<Field> getFields() {
        return mFields;
    }

    public Map<Field, String> getFieldKeyMap() {
        return fieldKeyMap;
    }

    @Override
    public int getItemCount() {
        return mFields.size();
    }

    public String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser()!= null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return "";
        }
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    public class FieldAdapterObserver extends RecyclerView.AdapterDataObserver {

        // Layout that contains the empty text
        private View layoutView;

        public FieldAdapterObserver(View layoutView) {
            this.layoutView = layoutView;
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            // Hide the empty view
            TextView emptyViewText = layoutView.findViewById(R.id.favourite_fields_empty_view);
            if( emptyViewText == null) {
                emptyViewText = layoutView.findViewById(R.id.field_list_empty_text_view);
            }
                emptyViewText.setVisibility(View.GONE);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            // Show empty view if there are no fields left in the list
            if (getItemCount() == 0) {
                TextView emptyTextView = (TextView) layoutView
                        .findViewById(R.id.favourite_fields_empty_view);
                emptyTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public class FieldViewHolder extends RecyclerView.ViewHolder {

        // Card
        public final View fieldView;

        // Elements of the card
        public final ImageView heartImageView;
        public final TextView heartsCountTextView;
        public final TextView fieldAddressTextView;
        public final TextView fieldNameTextView;
        public final TextView fieldSurfaceTextView;
        public final TextView fieldSizeTextView;
        public final TextView fieldPriceTextView;
        public final ImageView fieldImageView;
        public final Button action1Button;
        public final Button action2Button;

        public FieldViewHolder(View view) {
            super(view);
            // Get references of the 'view' so as to edit their contents later
            fieldView = view;
            heartImageView = (ImageView) view.findViewById(R.id.heart);
            heartsCountTextView = (TextView) view.findViewById(R.id.field_num_hearts);
            fieldAddressTextView = (TextView) view.findViewById(R.id.card_field_address);
            fieldNameTextView = (TextView) view.findViewById(R.id.card_field_name);
            fieldImageView = (ImageView) view.findViewById(R.id.card_field_image);
            fieldSurfaceTextView = (TextView) view.findViewById(R.id.field_surface);
            fieldSizeTextView = (TextView) view.findViewById(R.id.field_dimen);
            fieldPriceTextView = (TextView) view.findViewById(R.id.field_price);
            action1Button = (Button) view.findViewById(R.id.action_1_button);
            action2Button = (Button) view.findViewById(R.id.action_2_button);
        }

        private void bindToField(Field field, String fieldKey, View.OnClickListener heartClickListener) {
            // Download and set the picture of the field (done at first because it can take much time).
            downloadAndSetFieldImage(fieldKey);

            // Setting all field features
            fieldNameTextView.setText(field.getName());
            fieldAddressTextView.setText(field.getAddress());
            fieldSurfaceTextView.setText(field.getSurface());
            fieldSizeTextView.setText(field.getSize());
            fieldPriceTextView.setText(field.getPrice());

            //TODO: substitute this AsyncTask with the address obtained from db (field.getAddress)
            // new DiscoverAddress().execute(field.getLatitude(), field.getLongitude());

            // fieldAddressTextView.setText(String
            //        .format(Locale.getDefault(), "%f", field.getLatitude()));

            // Show hearts count only if it's greater than zero.
            if (field.getHeartsCount() > 0) {
                heartsCountTextView.setText(String
                        .format(Locale.getDefault(), "%d", field.getHeartsCount()));
            }

            heartImageView.setOnClickListener(heartClickListener);
        }

        private void downloadAndSetFieldImage(final String fieldKey) {
            // Check if the image has already been downloaded
            if (mFieldImage.containsKey(fieldKey)) {
                // No need to download, just set it into the view
                setFieldImage(mFieldImage.get(fieldKey).array());
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
                    mFieldImage.put(fieldKey, imageByteBuffer);

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
            fieldImageView.setBackground(null);
            fieldImageView.setImageBitmap(fieldImageBitmap);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + fieldNameTextView.getText() + "'";
        }


        class DiscoverAddress extends AsyncTask<Double, Void, String>{

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

                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

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
            protected void onPostExecute(String result) {
                fieldAddressTextView.setText(result);
            }
        }
    }
}
