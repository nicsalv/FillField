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


public class ProfileFragment extends Fragment implements View.OnClickListener {

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private FirebaseUser user;
    private Button signOutButton;
    private TextView nameSurname;
    private CircleImageView profileImage;
    private TextView contactUs;
    private ImageView backgroundImageView;

    // For pre-Lollipop devices
    private TextView myFields;
    private TextView myBookings;
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

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
        myFields.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_fields_icon), null, null, null);
        myBookings.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_book_black_24dp), null, null, null);

        //Listeners
        myFields.setOnClickListener(this);
        myBookings.setOnClickListener(this);

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
            case R.id.sign_out:
                signOut();
               break;
        }
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
