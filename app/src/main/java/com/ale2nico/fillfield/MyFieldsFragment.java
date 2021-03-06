package com.ale2nico.fillfield;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * A fragment representing a list of fields owned by the signed-in user.
 */
public class MyFieldsFragment extends Fragment {

    // Get fields on Firebase and display them into the fragment
    private MyFieldsAdapter myFieldsAdapter;

    // Instance of the hosting activity
    private OnReservationsButtonClickedListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyFieldsFragment() {
    }

    @SuppressWarnings("unused")
    public static MyFieldsFragment newInstance() {
        MyFieldsFragment fragment = new MyFieldsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_fields_list, container, false);

        // Reference to the progress bar
        ProgressBar progressBar = view.findViewById(R.id.load_field_progress_bar);

        // Initializing RecyclerView and its layout
        RecyclerView mFieldsRecycler = view.findViewById(R.id.my_fields_list);
        mFieldsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Initializing adapter
        myFieldsAdapter = new MyFieldsAdapter(getContext(), mListener, view, progressBar);
        mFieldsRecycler.setAdapter(myFieldsAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set the appbar title
        getActivity().setTitle(getContext()
                .getResources().getString(R.string.my_fields_title));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReservationsButtonClickedListener) {
            mListener = (OnReservationsButtonClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnReservationsButtonClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnReservationsButtonClickedListener {
        void onReservationsButtonClicked(String fieldKey);
    }
}
