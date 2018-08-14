package com.ale2nico.fillfield;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ale2nico.fillfield.firebaselisteners.SearchChildEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnListFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // tag for debugging
    private static final String TAG = "SearchFragment";

    // key for saving the state of the recycler
    public static final String KEY_RECYCLER_STATE = "RECYCLER_STATE";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_SEARCH_QUERY = "search-query";

    // TODO: Rename and change types of parameters
    private String searchQuery;

    private Boolean flag;


    // Firebase References
    private DatabaseReference mFieldsReference;
    private DatabaseReference mUsersReference;


    // References to layout objects
    private RecyclerView mFieldsRecycler;
    private FieldAdapter mFieldAdapter;
    private Bundle mFieldsRecyclerState;
    // The layout manager is provided inside the 'onCreateView' method.
    // It depends on the number of column of the list.

    private HomeFragment.OnListFragmentInteractionListener mListener;


    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_QUERY, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            searchQuery = getArguments().getString(ARG_SEARCH_QUERY);
            flag = true;
        }else{
            flag = false;
        }

        // Reference to the 'fields' object stored in the database
        mFieldsReference = FirebaseDatabase.getInstance().getReference()
                .child("fields");
        // Reference to the 'users' object stored in the database
        mUsersReference = FirebaseDatabase.getInstance().getReference()
                .child("users");
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        View view;
        // Inflate the layout for this fragment
        getActivity().setTitle(getContext().getResources().getString(R.string.search_fragment_title));

        if(flag){
            rootView = inflater.inflate(R.layout.search_result, container, false);
            view = rootView.findViewById(R.id.result_list);

            // Set the adapter
            if (view instanceof RecyclerView) {
                Context context = rootView.getContext();
                mFieldsRecycler = (RecyclerView) view;
                mFieldsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                //TODO: build an appropriate listener in order to pass it to the adapter

                mFieldAdapter = new FieldAdapter(mFieldsReference, mListener);
                ChildEventListener childEventListener
                        = new SearchChildEventListener(searchQuery, mFieldAdapter, getContext());
                mFieldAdapter.setChildEventListener(childEventListener);
                mFieldsRecycler.setAdapter(mFieldAdapter);


            }

        }else {
            rootView = inflater.inflate(R.layout.search_informations, container, false);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof HomeFragment.OnListFragmentInteractionListener) {
            mListener = (HomeFragment.OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        // Save the state of the RecyclerView
        mFieldsRecyclerState = new Bundle();
        if (mFieldsRecycler != null) {
            mFieldsRecyclerState.putParcelable(KEY_RECYCLER_STATE,
                    mFieldsRecycler.getLayoutManager().onSaveInstanceState());
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Restore the state of the RecyclerView
        if (mFieldsRecyclerState != null) {
            Parcelable recyclerState = mFieldsRecyclerState.getParcelable(KEY_RECYCLER_STATE);
            mFieldsRecycler.getLayoutManager().onRestoreInstanceState(recyclerState);
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
        if(flag){
            mFieldAdapter.cleanupListener();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the options menu from XML
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                getActivity().onSearchRequested();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
