package com.ale2nico.fillfield;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ale2nico.fillfield.firebaselisteners.SearchChildEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFieldClickListener} interface
 * to handle interaction events.
 * Use the {@link SearchingFragment#} factory method to
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

    private HomeFragment.OnFieldClickListener mListener;

    // Reference to the adapter observer
    FieldAdapter.FieldAdapterObserver fieldAdapterObserver;
    ProgressBar progressBar ;

    private TextView emptyTextView;


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
        view = rootView.findViewById(R.id.list);
        progressBar = rootView.findViewById(R.id.home_progress_bar);
        emptyTextView = rootView.findViewById(R.id.field_list_empty_text_view);


        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = rootView.getContext();
            mFieldsRecycler = (RecyclerView) view;
            mFieldsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            mFieldsRecycler.setVisibility(View.VISIBLE);
            //TODO: build an appropriate listener in order to pass it to the adapter

            mFieldAdapter = new FieldAdapter(mFieldsReference, mListener);

            // Register an observer for the adapter
            fieldAdapterObserver = mFieldAdapter.new FieldAdapterObserver(rootView);
            mFieldAdapter.registerAdapterDataObserver(fieldAdapterObserver);

            ChildEventListener childEventListener
                    = new SearchChildEventListener(searchQuery, mFieldAdapter, getContext(), progressBar);
            mFieldAdapter.setChildEventListener(childEventListener);
            mFieldsRecycler.setAdapter(mFieldAdapter);

            // Initial check in order to show empty view if there are no fields.
            if (isFavouriteListEmpty()) {
                new progressCheck(progressBar).execute();
                //showEmptyView(rootView);
            }

        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof HomeFragment.OnFieldClickListener) {
            mListener = (HomeFragment.OnFieldClickListener) context;
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

    private boolean isFavouriteListEmpty() {
        return mFieldAdapter.getItemCount() == 0;
    }

    private void showEmptyView(View rootView) {
        TextView emptyTextView = (TextView) rootView.findViewById(R.id.field_list_empty_text_view);
        emptyTextView.setVisibility(View.VISIBLE);
    }

    public class progressCheck  extends AsyncTask<String, String, String> {

        ProgressBar mProgressBar;

        public progressCheck(ProgressBar p){
            this.mProgressBar = p;
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressBar.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);



        }

    }
}





