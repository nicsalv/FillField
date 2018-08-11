package com.ale2nico.fillfield;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale2nico.fillfield.HomeFragment.OnListFragmentInteractionListener;
import com.ale2nico.fillfield.dummy.DummyContent.DummyItem;
import com.ale2nico.fillfield.models.Field;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FieldAdapter extends RecyclerView.Adapter<FieldAdapter.FieldViewHolder> {

    public static final String TAG = "FieldAdapter";

    // Firebase Database references
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    // List of the fields stored into the database
    private List<String> mFieldsIds = new ArrayList<>();
    private List<Field> mFields = new ArrayList<>();

    // Interaction listener passes data to the hosting activity
    private final HomeFragment.OnListFragmentInteractionListener mListener;

    public FieldAdapter(DatabaseReference ref,
                        HomeFragment.OnListFragmentInteractionListener listener) {
        mDatabaseReference = ref;
        mListener = listener;
    }

    @Override
    @NonNull
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the 'field_card' layout inside a view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.field_card, parent, false);

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
        holder.bindToField(field, new View.OnClickListener() {
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
                    mListener.onListFragmentInteraction(null);
                }
            }
        });
        holder.action2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(null);
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

    public void setChildEventListener(ChildEventListener childEventListener){
        mChildEventListener = childEventListener;
        mDatabaseReference.addChildEventListener(childEventListener);
    }

    public List<String> getFieldsIds() {
        return mFieldsIds;
    }

    public List<Field> getFields() {
        return mFields;
    }

    public Integer getNumberOfFields(){
        return mFields.size();
    }

    @Override
    public int getItemCount() {
        return mFields.size();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
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
        // TODO: rename these buttons
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
            action1Button = (Button) view.findViewById(R.id.action_1_button);
            action2Button = (Button) view.findViewById(R.id.action_2_button);
        }

        public void bindToField(Field field, View.OnClickListener heartClickListener) {
            fieldNameTextView.setText(field.getName());
            fieldAddressTextView.setText(Double.toString(field.getLatitude()));
            if (field.getHeartsCount() > 0) {
                heartsCountTextView.setText(Integer.toString(field.getHeartsCount()));
            }
            // TODO: set all the field's fields

            heartImageView.setOnClickListener(heartClickListener);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + fieldNameTextView.getText() + "'";
        }
    }

}
