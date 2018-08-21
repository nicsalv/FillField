package com.ale2nico.fillfield;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * This Fragment displays user's reservations.
 * Activities that contain this fragment must implement the
 * {@link ReservationsFragment.OnContactButtonClickListener} interface
 * to handle interaction events.
 */
public class MyReservationsFragment extends Fragment {

    // No arguments required (so far)

    // Instance of the hosting activity
    private ReservationsFragment.OnContactButtonClickListener contactButtonClickListener;
    private ReservationsFragment.ShareImageClickListener shareImageClickListener;

    public MyReservationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_reservations, container, false);

        // Get reference to views
        ProgressBar progressBar = view.findViewById(R.id.my_reservations_progress_bar);
        TextView emptyTextView = view.findViewById(R.id.my_reservations_empty_view);

        // Initialize recycler
        RecyclerView myResRecycler = view.findViewById(R.id.my_reservations_list);
        myResRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Initialize adapter
        MyReservationsAdapter myResAdapter
                = new MyReservationsAdapter(progressBar, emptyTextView,
                        contactButtonClickListener, shareImageClickListener);
        myResRecycler.setAdapter(myResAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set the appbar title
        getActivity().setTitle(getContext()
                .getResources().getString(R.string.my_reservations_fragment_title));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ReservationsFragment.OnContactButtonClickListener) {
            contactButtonClickListener = (ReservationsFragment.OnContactButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if (context instanceof ReservationsFragment.ShareImageClickListener) {
            shareImageClickListener = (ReservationsFragment.ShareImageClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        contactButtonClickListener = null;
        shareImageClickListener = null;
    }
}
