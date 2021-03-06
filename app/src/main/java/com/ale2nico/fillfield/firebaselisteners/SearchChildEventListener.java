package com.ale2nico.fillfield.firebaselisteners;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import com.ale2nico.fillfield.FieldAdapter;
import com.ale2nico.fillfield.models.Field;
import com.google.firebase.database.DataSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SearchChildEventListener extends HomeChildEventListener {

    private String searchQuery;
    private Context context;
    private ProgressBar progressBar;
    int i = 0;

    public SearchChildEventListener(String searchQuery, FieldAdapter fieldAdapter, Context context, ProgressBar progressbar) {
        this.searchQuery = searchQuery;
        this.fieldAdapter = fieldAdapter;
        this.context = context;
        this.progressBar = progressbar;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Field field = dataSnapshot.getValue(Field.class);

        double lat = field.getLatitude();
        double lon = field.getLongitude();

        String city = "";

        Address locality = null;

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
            city = locality.getLocality();
        }

        Boolean cityFlag = false;

        if(city != null && searchQuery!=null){
            city = city.toLowerCase();
            if(city.contains(searchQuery.toLowerCase()))
                cityFlag = true;
            else
                cityFlag = false;

        }else {
            cityFlag = false;
        }

        if (i != 0){
            //hide progressBar
            progressBar.setVisibility(View.GONE);
        }

        //check su query
        if (searchQuery != null) {
            if (field.getName().toLowerCase().contains(searchQuery.toLowerCase()) || cityFlag) {
                //add the field to results
                // Update RecyclerView
                fieldAdapter.getFields().add(field);
                fieldAdapter.getFieldsIds().add(dataSnapshot.getKey());
                fieldAdapter.notifyItemInserted(fieldAdapter.getFields().size() - 1);

                //update counter for progressBar
                ++i;

            }
        }
    }

}
