package com.ale2nico.fillfield.firebaselisteners;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ale2nico.fillfield.FieldAdapter;
import com.ale2nico.fillfield.models.Field;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class HomeChildEventListener implements ChildEventListener {

    private FieldAdapter fieldAdapter;

    public HomeChildEventListener(FieldAdapter fieldAdapter) {
        this.fieldAdapter = fieldAdapter;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

        // A new field has been added, add it to the displayed list
        Field field = dataSnapshot.getValue(Field.class);

        // Update RecyclerView
        fieldAdapter.getFields().add(field);
        fieldAdapter.getFieldsIds().add(dataSnapshot.getKey());
        fieldAdapter.notifyItemInserted(fieldAdapter.getFields().size() - 1);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

        // A field has changed, use the key to determine if we are displaying
        // this field and if so displayed the changed field.
        Field changedField = dataSnapshot.getValue(Field.class);
        String fieldKey = dataSnapshot.getKey();

        int fieldIndex = fieldAdapter.getFieldsIds().indexOf(fieldKey);
        if (fieldIndex > -1) {
            // Replace with the new data
            fieldAdapter.getFields().set(fieldIndex,changedField);

            // Update RecyclerView
            fieldAdapter.notifyItemChanged(fieldIndex);
        } else {
            Log.w(FieldAdapter.TAG, "onChildChanged:unknown_child:" + fieldKey);
        }
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        Log.d(FieldAdapter.TAG, "onChildRemoved:" + dataSnapshot.getKey());

        // A field has been removed, use the key to determine if we are displaying
        // this field and if so remove it.
        String fieldKey = dataSnapshot.getKey();

        int fieldIndex = fieldAdapter.getFieldsIds().indexOf(fieldKey);
        if (fieldIndex > -1) {
            // Remove field from both lists
            fieldAdapter.getFieldsIds().remove(fieldIndex);
            fieldAdapter.getFields().remove(fieldIndex);

            // Update RecyclerView
            fieldAdapter.notifyItemRemoved(fieldIndex);
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
        Log.d(FieldAdapter.TAG, "onChildMoved:" + dataSnapshot.getKey());

        // Do nothing because we don't expect a field to move position
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.w(FieldAdapter.TAG, "FieldAdapter:onCancelled", databaseError.toException());
    }
}
