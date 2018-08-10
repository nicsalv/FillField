package com.ale2nico.fillfield.firebaselisteners;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ale2nico.fillfield.FieldAdapter;
import com.ale2nico.fillfield.models.Field;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SearchChildEventListener implements ChildEventListener {

    private String searchQuery;

    private FieldAdapter fieldAdapter;

    private Context context;

    public SearchChildEventListener(String searchQuery, FieldAdapter fieldAdapter,Context context) {
        this.searchQuery = searchQuery;
        this.fieldAdapter = fieldAdapter;
        this.context = context;

    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Field field = dataSnapshot.getValue(Field.class);

        double lat = field.getLatitude();
        double lon = field.getLongitude();

        Address locality = null;

        List<Address> addresses = null;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        //try to get the location from lat e lon
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses.size() > 0)
            locality = addresses.get(0);

        String city = locality.getLocality();

        //check su query
        if (searchQuery != null) {
            if (field.getName().toLowerCase().contains(searchQuery.toLowerCase()) || city.toLowerCase().contains(searchQuery.toLowerCase())) {
                //add the field to results
                // Update RecyclerView
                fieldAdapter.getFields().add(field);
                fieldAdapter.getFieldsIds().add(dataSnapshot.getKey());
                fieldAdapter.notifyItemInserted(fieldAdapter.getFields().size() - 1);
            }
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
