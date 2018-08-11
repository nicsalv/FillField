package com.ale2nico.fillfield.firebaselisteners;

import com.ale2nico.fillfield.FieldAdapter;
import com.ale2nico.fillfield.models.Field;
import com.google.firebase.database.DataSnapshot;

public class FavouriteChildEventListener extends HomeChildEventListener {

    /**
     * Same constructor of the parent, but in Java constructors are not inherited.
     */
    public FavouriteChildEventListener(FieldAdapter fieldAdapter) {
        this.fieldAdapter = fieldAdapter;
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
        }

    }
}
