package com.ale2nico.fillfield;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ale2nico.fillfield.dummy.DummyContent;
import com.ale2nico.fillfield.dummy.DummyContent.DummyItem;
import com.ale2nico.fillfield.firebaselisteners.FavouriteChildEventListener;
import com.ale2nico.fillfield.firebaselisteners.HomeChildEventListener;
import com.google.firebase.database.ChildEventListener;

/**
 * A fragment representing a list of Fields that are marked
 * as favourite by the user.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FavouritesFragment extends HomeFragment {

    private static final String TAG = "FavouritesFragment";

    // Reference to the adapter observer
    FieldAdapter.FieldAdapterObserver fieldAdapterObserver;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavouritesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate fragment layout
        View view = inflater.inflate(R.layout.fragment_favourites_list, container, false);

        // Initializing RecyclerView and its layout
        mFieldsRecycler = view.findViewById(R.id.favourite_fields_list);
        mFieldsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Initializing adapter
        mFieldAdapter = new FieldAdapter(mFieldsReference, mListener);

        // Register an observer for the adapter
        fieldAdapterObserver = mFieldAdapter.new FieldAdapterObserver(view);
        mFieldAdapter.registerAdapterDataObserver(fieldAdapterObserver);

        // Attach a listener to the adapter for communicating with Firebase
        ChildEventListener favoriteChildEventListener
                = new FavouriteChildEventListener(mFieldAdapter);
        mFieldAdapter.setChildEventListener(favoriteChildEventListener);

        // Set the adapter for the recycler
        mFieldsRecycler.setAdapter(mFieldAdapter);

        // Initial check in order to show empty view if there are no favourite fields.
        if (isFavouriteListEmpty()) {
            showEmptyView(view);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set the appbar title
        getActivity().setTitle(getContext()
                .getResources().getString(R.string.favourite_fragment_title));
    }

    @Override
    public void onStop() {
        // Unregister the adapter observer
        mFieldAdapter.unregisterAdapterDataObserver(fieldAdapterObserver);

        super.onStop();
    }

    private boolean isFavouriteListEmpty() {
        return mFieldAdapter.getItemCount() == 0;
    }

    private void showEmptyView(View rootView) {
        TextView emptyTextView = (TextView) rootView.findViewById(R.id.favourite_fields_empty_view);
        emptyTextView.setVisibility(View.VISIBLE);
    }
}
