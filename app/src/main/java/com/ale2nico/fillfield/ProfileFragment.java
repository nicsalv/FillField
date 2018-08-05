package com.ale2nico.fillfield;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment implements View.OnClickListener {

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private FirebaseUser user;
    private TextView signOutTextView;
    private TextView nameSurname;
    private CircleImageView profileImage;
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sign out text view and its listener
        signOutTextView = (TextView) getView().findViewById(R.id.sign_out);
        signOutTextView.setOnClickListener(this);

        //Get user's name, surname and profile picture;
        nameSurname = (TextView) getView().findViewById((R.id.name_surname));
        nameSurname.setText(user.getDisplayName());
        profileImage = (CircleImageView) getView().findViewById(R.id.profile_image);
       /*
       //CANNOT RETRIEVE PROFILE PIC?!
        Uri uri = user.getPhotoUrl();
        profileImage.setImageURI(uri);
        Log.d("Photourl", user.getPhotoUrl().toString());
        */
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_fields:
                break;
            case R.id.settings:
                break;
            case R.id.sign_out:
                //Alert to confirm sign-out
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.sign_out_message)
                        .setTitle(R.string.sign_out_title);
                //Creating buttons for confirm or cancel
                builder.setPositiveButton(R.string.sign_out_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked confirm button
                        mAuth.signOut();
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
               /* mAuth.signOut();
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivity(loginIntent);
                */
               break;
        }
    }
}
