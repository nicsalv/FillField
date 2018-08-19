package com.ale2nico.fillfield;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ale2nico.fillfield.firebaselisteners.HomeChildEventListener;
import com.ale2nico.fillfield.models.Field;
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Fields.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class HomeFragment extends Fragment implements SortedListAdapter.Callback{

    private static final String TAG = "HomeFragment";

    // Interaction listener that passes data to the hosting activity
    protected HomeFragment.OnListFragmentInteractionListener mListener;

    // Firebase References
    protected DatabaseReference mFieldsReference;

    // References to layout objects
    protected RecyclerView mFieldsRecycler;
    protected FieldAdapter mFieldAdapter;
    private List<Field> mFields;
    private List<String> mFieldsId;


    // The layout manager is provided inside the 'onCreateView' method.
    // It depends on the number of column of the list.


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Reference to the 'fields' object stored in the database
        mFieldsReference = FirebaseDatabase.getInstance().getReference()
                .child("fields");
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
            mFieldsRecycler.setLayoutManager(new WrapContentLinearLayoutManager(context));

            // Initializing adapter with listener
            mFieldAdapter = new FieldAdapter(mFieldsReference, mListener);
            ChildEventListener homeChildEventListener = new HomeChildEventListener(mFieldAdapter);
            mFieldAdapter.setChildEventListener(homeChildEventListener);

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
                .getResources().getString(R.string.app_name));
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
    public void onStop() {
        super.onStop();

        // Clean up fields listeners
        mFieldAdapter.cleanupListener();
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditFinished() {

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
        void onListFragmentInteraction(Field field, int id);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        super.onCreateOptionsMenu(menu,inflater);
    }


    /**
     *
     * @param models
     * @param query
     * @return a filtered list starting with models according to the query
     */
    private  List<Field> filter(List<Field> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<Field> filteredModelList = new ArrayList<>();
        for (int i=0; i<models.size(); ++i) {
            Field model = models.get(i);
            final String text = model.getName().toLowerCase();
            if (!text.contains(lowerCaseQuery)) {
                //need to remove this field
                filteredModelList.add(model);
            }
        }

        return  filteredModelList;
    }




}
