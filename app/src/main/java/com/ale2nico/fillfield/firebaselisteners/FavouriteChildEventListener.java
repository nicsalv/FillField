package com.ale2nico.fillfield.firebaselisteners;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.ale2nico.fillfield.FieldAdapter;
import com.ale2nico.fillfield.models.Field;
import com.google.firebase.database.DataSnapshot;

public class FavouriteChildEventListener extends HomeChildEventListener {

    /**
     * Same constructor of the parent, but in Java constructors are not inherited.
     */
    public FavouriteChildEventListener(FieldAdapter fieldAdapter, ProgressBar progressBar) {
        this.fieldAdapter = fieldAdapter;
        this.progressBar = progressBar;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        Field field = dataSnapshot.getValue(Field.class);

        // Add the field to the displayed list only if it's marked as
        // favourite by the user
        if (field.getHearts().containsKey(fieldAdapter.getUid())) {

            // Update RecyclerView
            fieldAdapter.getFields().add(field);
            fieldAdapter.getFieldsIds().add(dataSnapshot.getKey());
            fieldAdapter.notifyItemInserted(fieldAdapter.getFields().size() - 1);

            // Hide progress bar
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
        Field field = dataSnapshot.getValue(Field.class);

        // User may have unstarred the field; if so, remove the field from the list.
        if (!field.getHearts().containsKey(fieldAdapter.getUid())) {

            // Field has changed and it's not starred by the user anymore.
            // Remove it from the list.
            String fieldKey = dataSnapshot.getKey();

            int fieldIndex = fieldAdapter.getFieldsIds().indexOf(fieldKey);
            if (fieldIndex > -1) {
                // Remove the field
                fieldAdapter.getFieldsIds().remove(fieldIndex);
                fieldAdapter.getFields().remove(fieldIndex);

                // Update RecyclerView
                fieldAdapter.notifyItemRemoved(fieldIndex);
            } else {
                Log.w(FieldAdapter.TAG, "onChildChanged:unknown_child:" + fieldKey);
            }
        }
    }
}
