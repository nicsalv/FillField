package com.ale2nico.fillfield;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ale2nico.fillfield.firebaselisteners.SearchChildEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnListFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private String searchQuery;

    private Boolean flag;

    // Firebase References
    private DatabaseReference mFieldsReference;
    private DatabaseReference mUsersReference;


    // References to layout objects
    private RecyclerView mFieldsRecycler;
    private FieldAdapter mFieldAdapter;
    // The layout manager is provided inside the 'onCreateView' method.
    // It depends on the number of column of the list.

    private HomeFragment.OnListFragmentInteractionListener mListener;


    public SearchingFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        searchQuery = getArguments().getString("ARG_SEARCH_QUERY");

        // Reference to the 'fields' object stored in the database
        mFieldsReference = FirebaseDatabase.getInstance().getReference()
                .child("fields");
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        View view;

        // Inflate the layout for this fragment
        getActivity().setTitle(getContext().getResources().getString(R.string.search_fragment_title));

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        mFieldAdapter.cleanupListener();


    }
/*
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
            case R.id.action_search:
                //getActivity().onSearchRequested();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */





}