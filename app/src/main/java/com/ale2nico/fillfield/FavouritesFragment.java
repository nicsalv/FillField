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
        View view = inflater.inflate(R.layout.fragment_field_list, container, false);

        // Initializing the layout
        if (view instanceof RecyclerView) {
            Context context = view.getContext();

            // Initializing RecyclerView and its layout
            mFieldsRecycler = (RecyclerView) view;
            mFieldsRecycler.setLayoutManager(new LinearLayoutManager(context));

            // Initializing adapter with listener
            mFieldAdapter = new FieldAdapter(mFieldsReference, mListener);
            ChildEventListener favoriteChildEventListener
                    = new FavouriteChildEventListener(mFieldAdapter);
            mFieldAdapter.setChildEventListener(favoriteChildEventListener);

            // Set the adapter for the recycler
            mFieldsRecycler.setAdapter(mFieldAdapter);
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
}
