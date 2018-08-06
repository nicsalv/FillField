package com.ale2nico.fillfield;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ale2nico.fillfield.dummy.DummyContent;
import com.ale2nico.fillfield.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Fields.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class HomeFragment extends Fragment {

    private static final String LOG_TAG = "HomeFragment";

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static final String ARG_SCROLL_POSITION = "field-list-scroll-position";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    // References to layout objects
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    // The layout manager is provided inside the 'onCreateView' method.
    // It depends on the number of column of the list.


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {

    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static HomeFragment newInstance(int columnCount, int scrollPosition) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_SCROLL_POSITION, scrollPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_field_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                // Narrow screen, single-column list
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                // Wide screen, multi-column list
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new FieldRecyclerViewAdapter(DummyContent.ITEMS, mListener);
            mRecyclerView.setAdapter(mAdapter);
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
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
    public void onPause() {
        Log.d(LOG_TAG, "onPause called.");

        // Save the current scroll position into the arguments of the fragments
        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            // Reference to the layout manager attached to the RecyclerView
            // TODO: remove cast if you'll decide to mantain only LinearLayout
            LinearLayoutManager mLayoutManager
                    = (LinearLayoutManager) mRecyclerView.getLayoutManager();

            // Save the field list state, especially the scroll position
            Parcelable listState = mLayoutManager.onSaveInstanceState();

            // Save the field list state inside the hosting activity
            MainActivity.homeFragmentListState = listState;
        }

        super.onPause();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        // Restore the field list state, especially the scroll position
        Parcelable listState = getArguments().getParcelable(ARG_SCROLL_POSITION);
        mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
