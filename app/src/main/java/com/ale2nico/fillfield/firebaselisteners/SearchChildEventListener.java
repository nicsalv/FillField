package com.ale2nico.fillfield.firebaselisteners;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ale2nico.fillfield.FieldAdapter;
import com.ale2nico.fillfield.models.Field;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class SearchChildEventListener implements ChildEventListener {

    private String searchQuery;

    private FieldAdapter fieldAdapter;

    public SearchChildEventListener(String searchQuery, FieldAdapter fieldAdapter) {
        this.searchQuery = searchQuery;
        this.fieldAdapter = fieldAdapter;

    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Field field = dataSnapshot.getValue(Field.class);

        //check su query
        if(field.getName().contains(searchQuery)){
            //add the field to results
            // Update RecyclerView
            fieldAdapter.getFields().add(field);
            fieldAdapter.getFieldsIds().add(dataSnapshot.getKey());
            fieldAdapter.notifyItemInserted(fieldAdapter.getFields().size() - 1);
        }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
