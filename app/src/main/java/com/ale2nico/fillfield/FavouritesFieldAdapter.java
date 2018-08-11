package com.ale2nico.fillfield;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale2nico.fillfield.FavouritesFragment.OnListFragmentInteractionListener;
import com.ale2nico.fillfield.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FavouritesFieldAdapter extends RecyclerView.Adapter<FavouritesFieldAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public FavouritesFieldAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.field_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.fieldPositionTextView.setText(mValues.get(position).id);
        holder.fieldTitleTextView.setText(mValues.get(position).content);

        // Set the listeners to the action buttons
        holder.action1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        holder.action2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Card
        public final View fieldView;

        // Elements of the card
        public final ImageView favoriteImageButton;
        public final TextView fieldPositionTextView;
        public final TextView fieldTitleTextView;
        // TODO: rename these buttons
        public final Button action1Button;
        public final Button action2Button;

        // Contains the data that will fill the view
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            // Get references of the 'view' so as to edit their contents later
            fieldView = view;
            favoriteImageButton = (ImageView) view.findViewById(R.id.heart);
            fieldPositionTextView = (TextView) view.findViewById(R.id.card_field_address);
            fieldTitleTextView = (TextView) view.findViewById(R.id.card_field_name);
            action1Button = (Button) view.findViewById(R.id.action_1_button);
            action2Button = (Button) view.findViewById(R.id.action_2_button);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + fieldPositionTextView.getText() + "'";
        }
    }
}
