package com.ale2nico.fillfield;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ale2nico.fillfield.firebaselisteners.FavouriteChildEventListener;
import com.google.firebase.database.ChildEventListener;

/**
 * A fragment representing a list of Fields that are marked
 * as favourite by the user.
 * <p/>
 * Activities containing this fragment MUST implement the
 * {@link com.ale2nico.fillfield.HomeFragment.OnFieldClickListener}
 * interface.
 */
public class FavouritesFragment extends HomeFragment {

    private static final String TAG = "FavouritesFragment";

    private ProgressBar progressBar;

    // Reference to the adapter observer
    FieldAdapter.FieldAdapterObserver fieldAdapterObserver;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavouritesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate fragment layout
        View view = inflater.inflate(R.layout.fragment_favourites_list, container, false);

        // Reference to progress bar
        progressBar = view.findViewById(R.id.favourite_progress_bar);

        // Initializing RecyclerView and its layout
        mFieldsRecycler = view.findViewById(R.id.favourite_fields_list);
        mFieldsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Initializing adapter
        mFieldAdapter = new FieldAdapter(mFieldsReference, mListener);

        // Initialize an observer for the adapter: it'll be registered in onStart().
        fieldAdapterObserver = mFieldAdapter.new FieldAdapterObserver(view);

        // Attach a listener to the adapter for communicating with Firebase
        ChildEventListener favoriteChildEventListener
                = new FavouriteChildEventListener(mFieldAdapter, progressBar);
        mFieldAdapter.setChildEventListener(favoriteChildEventListener);

        // Set the adapter for the recycler
        mFieldsRecycler.setAdapter(mFieldAdapter);

        // Restore list state after one seconds so as to
        // permit the adapter to gain all the data again.
        new Handler().postDelayed(() -> {
            if (savedInstanceState != null) {
                // Restore recycler state
                mFieldsRecycler.getLayoutManager()
                        .onRestoreInstanceState(savedInstanceState.getParcelable(KEY_RECYCLER_STATE));
            }
        }, 1000);

        // Show empty view if in five seconds we didn't manage to get any favourite fields.
        new Handler().postDelayed(() -> {
            if (isFavouriteListEmpty()) {
                showEmptyView(view);
            }
        }, 5000);

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
    public void onStart() {
        super.onStart();

        // Register the adapter observer
        mFieldAdapter.registerAdapterDataObserver(fieldAdapterObserver);
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
        // Hide progress bar first
        progressBar.setVisibility(View.GONE);

        // Then show empty view
        TextView emptyTextView = (TextView) rootView.findViewById(R.id.favourite_fields_empty_view);
        emptyTextView.setVisibility(View.VISIBLE);
    }
}
