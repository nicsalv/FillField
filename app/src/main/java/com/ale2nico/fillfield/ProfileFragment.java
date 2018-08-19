package com.ale2nico.fillfield;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment implements View.OnClickListener, Spinner.OnItemSelectedListener {

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private FirebaseUser user;
    private Button signOutButton;
    private TextView nameSurname;
    private CircleImageView profileImage;
    private Spinner homeSpinner;
    private SharedPreferences savedValues;
    private TextView contactUs;
    private ImageView backgroundImageView;

    // For pre-Lollipop devices
    private TextView myFields;
    private TextView myBookings;
    private TextView settings;
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        savedValues = PreferenceManager.getDefaultSharedPreferences(getActivity());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(getContext().getResources().getString(R.string.my_profile));
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sign out text view and its listener
        signOutButton = (Button) getView().findViewById(R.id.sign_out);
        signOutButton.setOnClickListener(this);

        //Get user's name, surname and profile picture;
        nameSurname = (TextView) getView().findViewById((R.id.name_surname));
        nameSurname.setText(user.getDisplayName());
        profileImage = (CircleImageView) getView().findViewById(R.id.profile_image);
        backgroundImageView = (ImageView) view.findViewById(R.id.profile_background);

        //Contact us
        contactUs = (TextView) getView().findViewById(R.id.contact_us);
        contactUs.setMovementMethod(LinkMovementMethod.getInstance());

        // For pre-Lollipop devices
        myFields = getView().findViewById(R.id.my_fields);
        myBookings = getView().findViewById(R.id.my_reservations);
        settings = getView().findViewById(R.id.settings);
        myFields.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_fields_icon), null, null, null);
        myBookings.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_book_black_24dp), null, null, null);
        settings.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_settings_black_24dp), null, null, null);

        //Listeners
        myFields.setOnClickListener(this);
        myBookings.setOnClickListener(this);
        settings.setOnClickListener(this);

        // Set the background image
        Glide.with(this)
                .load(R.drawable.background_profile)
                .into(backgroundImageView);

        //User's image for profile pic
        Glide.with(this)
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.no_profile_pic)
                        .error(R.drawable.no_profile_pic))
                .into(profileImage);

       //Set spinner for preference
       homeSpinner = (Spinner) getView().findViewById(R.id.home_screen_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                Objects.requireNonNull(getActivity()), R.array.home_screen_preference, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        homeSpinner.setAdapter(adapter);
        String choosen = savedValues.getString("home_spinner", "none");

        if(choosen.equals("Home") || choosen.equals("none"))
            homeSpinner.setSelection(0);
        if (choosen.equals("Search"))
            homeSpinner.setSelection(1);
        if (choosen.equals("Favourites"))
            homeSpinner.setSelection(2);

        homeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_fields:
                MyFieldsFragment myFieldsFragment = new MyFieldsFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myFieldsFragment).addToBackStack(null).commit();
                break;
            case R.id.my_reservations:
                MyReservationsFragment myReservationsFragment = new MyReservationsFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myReservationsFragment).addToBackStack(null).commit();
                break;
            case R.id.settings:
                break;
            case R.id.sign_out:
                signOut();
               break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = (String) homeSpinner.getItemAtPosition(position);
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit();
        prefs.putString("home_spinner", selectedItem);
        prefs.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    /**
     * Sign-out from app
     */
    private void signOut() {
        //Alert to confirm sign-out
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.sign_out_message)
                .setTitle(R.string.sign_out_title);
        //Creating buttons for confirm or cancel
        builder.setPositiveButton(R.string.sign_out_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked confirm button
                mAuth.signOut();
                GoogleSignIn.getClient(getActivity(),
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build())
                        .signOut();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
