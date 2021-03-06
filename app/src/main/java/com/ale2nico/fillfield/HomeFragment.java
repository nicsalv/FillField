package com.ale2nico.fillfield;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
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
import android.widget.ProgressBar;

import com.ale2nico.fillfield.firebaselisteners.HomeChildEventListener;
import com.ale2nico.fillfield.models.Field;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Fields.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFieldClickListener}
 * interface.
 */
public class HomeFragment extends Fragment {

    // Arguments for last known location coordinates
    private static final String ARG_LAST_KNOWN_LAT = "lastKnownLat";
    private static final String ARG_LAST_KNOWN_LNG = "lastKnownLng";

    private static final String TAG = "HomeFragment";

    protected static final String KEY_RECYCLER_STATE = "recyclerState";

    // Interaction listener that passes data to the hosting activity
    protected OnFieldClickListener mListener;

    // Firebase References
    protected DatabaseReference mFieldsReference;

    // References to layout objects
    protected RecyclerView mFieldsRecycler;
    protected FieldAdapter mFieldAdapter;

    private List<Field> mFields;
    private List<String> mFieldsId;

    // These coordinates are supplied on instantiation
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

    public static HomeFragment newInstance(double lastKnownLat, double lastKnownLng) {
        // Construct a new HomeFragment with required arguments
        HomeFragment newFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAST_KNOWN_LAT, lastKnownLat);
        args.putDouble(ARG_LAST_KNOWN_LNG, lastKnownLng);
        newFragment.setArguments(args);

        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(getArguments() != null) {
            // Get last known location
            userLat = getArguments().getDouble(ARG_LAST_KNOWN_LAT);
            userLon = getArguments().getDouble(ARG_LAST_KNOWN_LNG);
        }

        // Reference to the 'fields' object stored in the database
        mFieldsReference = FirebaseDatabase.getInstance().getReference()
                .child("fields");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if(getArguments() == null) {
            View view2 = view.findViewById(R.id.list);
            if(view2 != null)
                view2.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate fragment layout
        View view = inflater.inflate(R.layout.fragment_field_list, container, false);
        RecyclerView recycler = view.findViewById(R.id.list);

        // Initializing the layout
        if (recycler instanceof RecyclerView) {
            Context context = view.getContext();

            // Reference to the progress bar
            ProgressBar progressBar = view.findViewById(R.id.home_progress_bar);

            // Initializing RecyclerView and its layout
            mFieldsRecycler = recycler;
            mFieldsRecycler.setLayoutManager(new WrapContentLinearLayoutManager(context));

            // Initializing adapter with listener
            mFieldAdapter = new FieldAdapter(mFieldsReference, mListener);
            ChildEventListener homeChildEventListener
                    = new HomeChildEventListener(mFieldAdapter, progressBar);
            mFieldAdapter.setChildEventListener(homeChildEventListener);

            // Set the adapter for the recycler
            mFieldsRecycler.setAdapter(mFieldAdapter);

            new Handler().postDelayed(() -> {
                if (savedInstanceState != null) {
                    // Restore recycler state
                    mFieldsRecycler.getLayoutManager()
                            .onRestoreInstanceState(savedInstanceState.getParcelable(KEY_RECYCLER_STATE));
                }
            }, 1000);

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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            List<Field> mFields = mFieldAdapter.getFields();
            mFieldsId = mFieldAdapter.getFieldsIds();
            Map<Field, String>  fieldKeyMap = mFieldAdapter.getFieldKeyMap();


            Collections.sort(mFields, createComparator(userPosition));

            for (int i = 0; i < mFields.size(); ++i){
                String currentFieldKey = fieldKeyMap.get(mFields.get(i));
                int currentFieldKeyIndex = mFieldsId.indexOf(currentFieldKey);
                String removedFieldKey = mFieldsId.get(i);
                mFieldsId.set(i, currentFieldKey);
                mFieldsId.set(currentFieldKeyIndex, removedFieldKey);
            }



            return true;
        }

        @Override
        protected void onPostExecute(Boolean b){
            mFieldAdapter.notifyDataSetChanged();
            mFieldsRecycler.setVisibility(View.VISIBLE);
        }

    }

    private  Comparator<Field> createComparator(LatLng p)
    {
        return new Comparator<Field>()
        {
            @Override
            public int compare(Field p0, Field p1)

            {

                float[] result1 = new float[1];
                Location.distanceBetween(p.latitude, p.longitude,
                        p0.getLatitude(), p0.getLongitude(), result1);

                float[] result2 = new float[1];
                Location.distanceBetween(p.latitude, p.longitude,
                        p1.getLatitude(), p1.getLongitude(), result2);

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
        // Save recycler state
        Parcelable recyclerState = mFieldsRecycler.getLayoutManager()
                .onSaveInstanceState();
        outState.putParcelable(KEY_RECYCLER_STATE, recyclerState);

        super.onSaveInstanceState(outState);
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
}
