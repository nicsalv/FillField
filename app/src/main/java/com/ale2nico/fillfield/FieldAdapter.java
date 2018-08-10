package com.ale2nico.fillfield;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ale2nico.fillfield.HomeFragment.OnListFragmentInteractionListener;
import com.ale2nico.fillfield.dummy.DummyContent.DummyItem;
import com.ale2nico.fillfield.models.Field;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;

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

    private final OnListFragmentInteractionListener mListener;

    public FieldAdapter(DatabaseReference ref, OnListFragmentInteractionListener listener) {
        mDatabaseReference = ref;
        mListener = listener;

        // Create child event listener
        // [START child_event_listener_recycler]
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new field has been added, add it to the displayed list
                Field field = dataSnapshot.getValue(Field.class);

                // Update RecyclerView
                mFieldsIds.add(dataSnapshot.getKey());
                mFields.add(field);
                notifyItemInserted(mFields.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A field has changed, use the key to determine if we are displaying
                // this field and if so displayed the changed field.
                Field changedField = dataSnapshot.getValue(Field.class);
                String fieldKey = dataSnapshot.getKey();

                int fieldIndex = mFieldsIds.indexOf(fieldKey);
                if (fieldIndex > -1) {
                    // Replace with the new data
                    mFields.set(fieldIndex,changedField);

                    // Update RecyclerView
                    notifyItemChanged(fieldIndex);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + fieldKey);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A field has been removed, use the key to determine if we are displaying
                // this field and if so remove it.
                String fieldKey = dataSnapshot.getKey();

                int fieldIndex = mFieldsIds.indexOf(fieldKey);
                if (fieldIndex > -1) {
                    // Remove field from both lists
                    mFieldsIds.remove(fieldIndex);
                    mFields.remove(fieldIndex);

                    // Update RecyclerView
                    notifyItemRemoved(fieldIndex);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // Do nothing because we don't expect a field to move position
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "FieldAdapter:onCancelled", databaseError.toException());
            }
        };

        ref.addChildEventListener(childEventListener);

        // Store reference to listener so it can be removed on app stop
        mChildEventListener = childEventListener;
    }

    @Override
    public FieldViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the 'field_card' layout inside a view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.field_card, parent, false);

        // Create a FieldViewHolder that holds the view
        return new FieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FieldViewHolder holder, final int position) {
        // Set the content inside the FieldViewHolder
        holder.fieldAddressTextView.setText(Double.toString(mFields.get(position).getLatitude()));
        holder.fieldNameTextView.setText(mFields.get(position).getName());

        // Set the listeners to the action buttons
        holder.action1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mFields.get(position));
                }
            }
        });
        holder.action2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mFields.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFields.size();
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
        public final ImageButton favoriteImageButton;
        public final TextView fieldAddressTextView;
        public final TextView fieldNameTextView;
        // TODO: rename these buttons
        public final Button action1Button;
        public final Button action2Button;

        public FieldViewHolder(View view) {
            super(view);
            // Get references of the 'view' so as to edit their contents later
            fieldView = view;
            favoriteImageButton = (ImageButton) view.findViewById(R.id.favourite_button);
            fieldAddressTextView = (TextView) view.findViewById(R.id.card_field_address);
            fieldNameTextView = (TextView) view.findViewById(R.id.card_field_name);
            action1Button = (Button) view.findViewById(R.id.action_1_button);
            action2Button = (Button) view.findViewById(R.id.action_2_button);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + fieldNameTextView.getText() + "'";
        }
    }
}
