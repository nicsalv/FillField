package com.ale2nico.fillfield;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a list of fields owned by the signed-in user.
 */
public class MyFieldsFragment extends Fragment {

    private MyFieldsAdapter myFieldsAdapter;

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

        // Initializing RecyclerView and its layout
        RecyclerView mFieldsRecycler = view.findViewById(R.id.my_fields_list);
        mFieldsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Initializing adapter
        myFieldsAdapter = new MyFieldsAdapter(getContext(), mListener);
        mFieldsRecycler.setAdapter(myFieldsAdapter);

        // Initial check in order to show empty view if there are no favourite fields.
        if (isFavouriteListEmpty()) {
            showEmptyView(view);
        }

        return view;
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

    private boolean isFavouriteListEmpty() {
        return myFieldsAdapter.getItemCount() == 0;
    }

    private void showEmptyView(View rootView) {
        TextView emptyTextView = (TextView) rootView.findViewById(R.id.my_fields_list_empty_view);
        emptyTextView.setVisibility(View.VISIBLE);
    }

    public interface OnReservationsButtonClickedListener {
        void onReservationsButtonClicked(String fieldKey);
    }
}
