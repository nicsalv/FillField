package com.ale2nico.fillfield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.ale2nico.fillfield.dummy.DummyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnListFragmentInteractionListener {

    // Request login code
    public static final int REQUEST_USER_LOGIN = 1;

    // Field list state for the HomeFragment.
    // Used for tracking the latest scroll position.
    public static Parcelable homeFragmentListState = null;

    // Firebase Authentication
    FirebaseAuth mAuth;

    // Firebase User
    FirebaseUser user;

    //BottomNavigation
    BottomNavigationView navigation;

    // Listens for actually signed-out user
    private FirebaseAuth.AuthStateListener mAuthListener
            = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            // Check whether the user is signed-in or not
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                // Sign-in through the LoginActivity
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, REQUEST_USER_LOGIN);
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // Replace the current fragment with the selected fragment
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            switch (item.getItemId()) {

                case R.id.navigation_home:
                    // Create a new HomeFragment with the latest scroll position
                    HomeFragment homeFragment = new HomeFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(HomeFragment.ARG_SCROLL_POSITION, homeFragmentListState);
                    homeFragment.setArguments(args);

                    // Replace the current fragment in the 'fragment_container'
                    transaction.replace(R.id.fragment_container, homeFragment);
                    break;

                case R.id.navigation_search_fields:
                    transaction.replace(R.id.fragment_container, new SearchFragment());
                    break;
                case R.id.navigation_favourites_fields:
                    transaction.replace(R.id.fragment_container, new FavouritesFragment());
                    break;
                case R.id.navigation_profile:
                    transaction.replace(R.id.fragment_container, new ProfileFragment());
                    break;
                default:
                    return false;
            }
            transaction.commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Get Firebase Authentication and set listener on it
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthListener);

        // Place the initial fragment into the activity (the HomeFragment).
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // SharedPreferences to load correct fragment
            // Then add the fragment to the "fragment_container" FrameLayout
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String choosen = prefs.getString("home_spinner", "none");
            switch (choosen) {
                case "Home":
                    // Create a new HomeFragment with the latest scroll position
                    HomeFragment homeFragment = new HomeFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(HomeFragment.ARG_SCROLL_POSITION, homeFragmentListState);
                    homeFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, homeFragment).commit();
                    navigation.setSelectedItemId(R.id.navigation_home);
                    break;
                case "Search":
                    SearchFragment searchFragment = new SearchFragment();
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, searchFragment).commit();
                    navigation.setSelectedItemId(R.id.navigation_search_fields);
                    break;
                case "Favourites":
                    FavouritesFragment favouritesFragment = new FavouritesFragment();
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, favouritesFragment).commit();
                    navigation.setSelectedItemId(R.id.navigation_favourites_fields);
                    break;
            }
            Toast.makeText(this, "Pref:" + choosen , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // [START Check sign-in and execute it if needed]
        // Get currently signed-in user
        user = mAuth.getCurrentUser();
        if (user == null) {
            // Start LoginActivity to sign-in the user
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, REQUEST_USER_LOGIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Get currently signed-in user
        if (resultCode == RESULT_OK) {
            user = mAuth.getCurrentUser();
        }
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        Toast.makeText(this, "Button pressed", Toast.LENGTH_SHORT).show();
    }
}
