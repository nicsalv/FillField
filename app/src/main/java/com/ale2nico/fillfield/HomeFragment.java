package com.ale2nico.fillfield;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ale2nico.fillfield.firebaselisteners.HomeChildEventListener;
import com.ale2nico.fillfield.models.Field;
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A fragment representing a list of Fields.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFieldClickListener}
 * interface.
 */
public class HomeFragment extends Fragment implements SortedListAdapter.Callback{

    private static final String TAG = "HomeFragment";

    // Interaction listener that passes data to the hosting activity
    protected OnFieldClickListener mListener;

    // Firebase References
    protected DatabaseReference mFieldsReference;

    // References to layout objects
    protected RecyclerView mFieldsRecycler;
    protected FieldAdapter mFieldAdapter;

    private List<Field> mFields;
    private List<String> mFieldsId;

    private Double userLat;
    private Double userLon;





    // The layout manager is provided inside the 'onCreateView' method.
    // It depends on the number of column of the list.


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //check if there are argouments
        if(getArguments() != null) {
            userLat = getArguments().getDouble("ARG_LAT");
            userLon = getArguments().getDouble("ARG_LON");

            //Toast.makeText(getApplicationContext(), "LocalizationHome: "+userLat+", "+userLon, Toast.LENGTH_SHORT).show();


            //mFields = mFieldAdapter.getFields();
            //mFieldsId = mFieldAdapter.getFieldsIds();
        }

        // Reference to the 'fields' object stored in the database
        mFieldsReference = FirebaseDatabase.getInstance().getReference()
                .child("fields");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //check if there are argouments
        if(getArguments() == null) {
            View view2 = getActivity().findViewById(R.id.list);
            if(view2 != null)
                view2.setVisibility(View.VISIBLE);
        }

    }


    /*

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCALIZATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "ci sono i permessi", Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "non ci sono i permessi", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.

        }
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate fragment layout
        View view = inflater.inflate(R.layout.fragment_field_list, container, false);


        // Initializing the layout
        if (view instanceof RecyclerView) {
            Context context = view.getContext();

            // Initializing RecyclerView and its layout
            mFieldsRecycler = (RecyclerView) view;
            mFieldsRecycler.setLayoutManager(new WrapContentLinearLayoutManager(context));

            // Initializing adapter with listener
            mFieldAdapter = new FieldAdapter(mFieldsReference, mListener);
            ChildEventListener homeChildEventListener = new HomeChildEventListener(mFieldAdapter);
            mFieldAdapter.setChildEventListener(homeChildEventListener);

            // Set the adapter for the recycler
            mFieldsRecycler.setAdapter(mFieldAdapter);


            if(getArguments() != null){
                //start an Asynctask for ordering the list
                 Double[] params = new Double[2];
                 params[0] = userLat;
                 params[1] = userLon;

                new SortingList().execute(params);
            }



        }

        return view;
    }

    class SortingList extends AsyncTask<Double, Void, Boolean> {

        @SuppressLint("MissingPermission")
        @Override
        protected Boolean doInBackground(Double... params) {
            //sorting the list
            LatLng userPosition = new LatLng(params[0], params[1]);

            try{
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            List<Field> mFields = mFieldAdapter.getFields();

            Collections.sort(mFields, createComparator(userPosition));

            return true;
        }

        @Override
        protected void onPostExecute(Boolean b){
            mFieldAdapter.notifyDataSetChanged();
            mFieldsRecycler.setVisibility(View.VISIBLE);
        }

    }

    private static Comparator<Field> createComparator(LatLng p)
    {
        return new Comparator<Field>()
        {
            @Override
            public int compare(Field p0, Field p1)

            {

                float[] result1 = new float[1];
                Location.distanceBetween(p0.getLatitude(), p.latitude,
                        p0.getLongitude(), p.longitude, result1);

                float[] result2 = new float[1];
                Location.distanceBetween(p1.getLatitude(), p.latitude,
                        p1.getLongitude(), p.longitude, result2);

               // double ds0 = p0.distanceSq(finalP);
                //double ds1 = p1.distanceSq(finalP);
                return Float.compare(result1[0], result2[0]);
            }

        };
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set the appbar title
        getActivity().setTitle(getContext()
                .getResources().getString(R.string.app_name));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFieldClickListener) {
            mListener = (OnFieldClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Clean up fields listeners
        mFieldAdapter.cleanupListener();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditFinished() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFieldClickListener {

        /**
         * Fired when the user clicks on the 'reserve' button.
         * @param field The field whose reserve button was clicked
         * @param fieldKey Fieldkey whose reserve button was clicked
         */
        void onReserveButtonClicked(Field field, String fieldKey);

        /**
         * Fired when the user clicks on the 'map' button.
         * @param field The field whose map button was clicked
         */
        void onMapButtonClicked(Field field);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        super.onCreateOptionsMenu(menu,inflater);
    }


    /**
     *
     * @param models
     * @param query
     * @return a filtered list starting with models according to the query
     */
    private  List<Field> filter(List<Field> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<Field> filteredModelList = new ArrayList<>();
        for (int i=0; i<models.size(); ++i) {
            Field model = models.get(i);
            final String text = model.getName().toLowerCase();
            if (!text.contains(lowerCaseQuery)) {
                //need to remove this field
                filteredModelList.add(model);
            }
        }

        return  filteredModelList;
    }




}
