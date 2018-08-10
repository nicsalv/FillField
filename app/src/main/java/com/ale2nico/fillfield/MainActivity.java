package com.ale2nico.fillfield;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.ale2nico.fillfield.dummy.DummyContent;
import com.ale2nico.fillfield.models.Field;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnListFragmentInteractionListener,
                    MyBookingsFragment.OnListFragmentInteractionListener,
                    MyFieldsFragment.OnListFragmentInteractionListener,
                    FavouritesFragment.OnListFragmentInteractionListener {

    // Request login code
    public static final int REQUEST_USER_LOGIN = 1;

    // Field list state for the HomeFragment.
    // Used for tracking the latest scroll position.
    public static Parcelable homeFragmentListState = null;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Firebase User
    private FirebaseUser user;

    // Firebase Database
    private DatabaseReference mDatabase;


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

        Firebase.setAndroidContext(this);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Get Firebase Authentication and set listener on it
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthListener);

        // Get Firebase Database instance
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Register notification channel for API >=26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ale2nico.FillField", "FillField", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notification channel for FillField app");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

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
            String choosen = prefs.getString("home_spinner", "Home");
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
    public void onListFragmentInteraction(Field field) {

        // Send a notification
        sendNotification("ale2nico.FillField", "Ehi tu!",
                "Non avrai mica cliccato quel bottone.....", getApplicationContext(), this.getClass(),
                NotificationReceiver.class, 1000 , 0);
        sendNotificationToUser("bozzi.ale96@gmail.com", "Ciao");
        Toast.makeText(this, "Button pressed", Toast.LENGTH_SHORT).show();
    }

    /**
     *  Create and send immediately the notification
     *  This work both for API <26 and API >=26 because the Channel was created in the onCreate method
     */
    public void sendNotification(String channelId, String contentTitle, String contentText,
                                    Context packageContext, Class classContext, Class notificationReceiver,
                                    long delay, Integer notificationId) {

        // [START] Create notification and its settings
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        //[END] Create notification and its settings

        // Intent related to current context and class
        Intent intent = new Intent(packageContext, classContext)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Pending intent for setting notification
        PendingIntent activityIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(activityIntent);

        // Build Notification
        Notification notification = mBuilder.build();

        // Schedule notification with two intents:
        // notificationIntent for attaching to the BroadcastReceiver
        Intent notificationIntent = new Intent(packageContext, notificationReceiver);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);

        // PendingIntent and AlarmManager for scheduling the notification at a specific time
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);

    }

    public static void sendNotificationToUser(String user, final String message) {
        Firebase ref = new Firebase( "https://fillfield-bc48e.firebaseio.com/");
        final Firebase notifications = ref.child("notificationRequests");

        Map notification = new HashMap<>();
        notification.put("username", user);
        notification.put("message", message);

        notifications.push().setValue(notification);
    }


}
