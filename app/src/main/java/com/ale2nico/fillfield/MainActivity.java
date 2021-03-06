package com.ale2nico.fillfield;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.ale2nico.fillfield.HomeFragment.OnFieldClickListener;
import com.ale2nico.fillfield.MyFieldsFragment.OnReservationsButtonClickedListener;
import com.ale2nico.fillfield.models.Field;
import com.ale2nico.fillfield.models.FieldAgenda;
import com.ale2nico.fillfield.models.Reservation;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements OnFieldClickListener,
        OnReservationsButtonClickedListener,
        DatePickerDialog.OnDateSetListener, OnTimeDialogClickListener,
        OnMapReadyCallback, ReservationsFragment.OnContactButtonClickListener,
        ReservationsFragment.ShareImageClickListener{

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // Request login code
    public static final int REQUEST_USER_LOGIN = 1;

    public static final String HOME_FRAGMENT = "HOME_FRAGMENT";
    public static final String PROFILE_FRAGMENT = "PROFILE_FRAGMENT";
    public static final String FAVOURITES_FRAGMENT = "FAVOURITES_FRAGMENT";
    public static final String MY_RESERVATIONS_FRAGMENT = "MY RESERVATIONS FRAGMENT";
    public static final String MY_FIELDS_FRAGMENT = "MY FIELDS FRAGMENT";
    public static final String SEARCHING_FRAGMENT = "SEARCHING_FRAGMENT";
    public static final String RESERVATIONS_FRAGMENT = "RESERVATIONS FRAGMENT";
    public static final String MAP_FRAGMENT = "MAP FRAGMENT";

    private String activeFragmentTag = HOME_FRAGMENT;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Firebase User
    private FirebaseUser user;

    // Firebase Database
    private DatabaseReference mDatabase;

    // BottomNavigation
    BottomNavigationView navigation;

    // These variables contain reservation state.
    private Field selectedField;
    private String selectedFieldKey;
    private String selectedDate;
    private String selectedTime;

    // Key string for reservation state savings during config changes
    private static final String KEY_SELECTED_FIELDKEY = "selectedFieldKey";
    private static final String KEY_SELECTED_DATE = "selectedDate";
    private static final String KEY_SELECTED_TIME = "selectedTime";

    private static final String KEY_ACTIVE_FRAGMENT = "activeFragmentTag";


    // References to map elements
    public SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private double selectedFieldLat;
    private double selectedFieldLng;
    private String selectedFieldName;

    // Key string for saving marker in the map
    private static final String KEY_SELECTED_FIELD_LAT = "selectedFieldLat";
    private static final String KEY_SELECTED_FIELD_LNG = "selectedFieldLng";
    private static final String KEY_SELECTED_FIELD_NAME = "selectedFieldName";


    private Location lastKnownLocation;
    private static final int MY_PERMISSIONS_REQUEST_LOCALIZATION = 200;
    private LocationManager locationManager;

    // Save last know location so as to supply it to HomeFragment when needed
    private double lastKnownLat;
    private double lastKnownLng;

    // Key String for last known coordinates
    private static final String KEY_LAST_KNOWN_LAT = "lastKnownLat";
    private static final String KEY_LAST_KNOWN_LNG = "lastKnownLng";

    // Acquire a reference to the system Location Manager
    String locationProvider;

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
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if(mMapFragment != null){
                FragmentTransaction fragmentTransaction =
                        getSupportFragmentManager().beginTransaction();
                fragmentTransaction.remove(mMapFragment).commit();
            }
            switch (item.getItemId()) {

                case R.id.navigation_home:
                    // Replace the current fragment in the 'fragment_container'
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // There are not permissions
                        activeFragmentTag = HOME_FRAGMENT;
                        transaction.replace(R.id.fragment_container, new HomeFragment(), HOME_FRAGMENT);
                        View view = findViewById(R.id.list);

                        if (view != null) {
                            view.setVisibility(View.VISIBLE);
                        }

                    }else {
                        //permissions are granted
                        checkPermissionsAndFindPosition();

                        // Start HomeFragment with last known coordinates
                        HomeFragment homeFragment
                                = HomeFragment.newInstance(lastKnownLat, lastKnownLng);
                        activeFragmentTag = HOME_FRAGMENT;
                        transaction.replace(R.id.fragment_container, homeFragment, HOME_FRAGMENT);
                    }

                    break;
                case R.id.navigation_favourites_fields:
                    // Replace the current fragment in the 'fragment_container'
                    activeFragmentTag = FAVOURITES_FRAGMENT;
                    transaction.replace(R.id.fragment_container, new FavouritesFragment(), FAVOURITES_FRAGMENT);
                    break;
                case R.id.navigation_profile:
                    activeFragmentTag = PROFILE_FRAGMENT;
                    transaction.replace(R.id.fragment_container, new ProfileFragment(), PROFILE_FRAGMENT);
                    break;
                default:
                    return false;
            }

            // Clear the back stack
            FragmentManager fm = getSupportFragmentManager();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
            transaction.commit();
            return true;
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            lastKnownLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCALIZATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //Toast.makeText(getApplicationContext(), "ci sono i permessi", Toast.LENGTH_SHORT).show();
                    checkPermissionsAndFindPosition();

                    // Start HomeFragment with required arguments
                    HomeFragment homeFragment
                            = HomeFragment.newInstance(lastKnownLat, lastKnownLng);
                    activeFragmentTag = HOME_FRAGMENT;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, homeFragment, HOME_FRAGMENT)
                            .commit();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(getApplicationContext(), "non ci sono i permessi", Toast.LENGTH_SHORT).show();
                    View view = findViewById(R.id.list);

                    if (view != null) {
                        view.setVisibility(View.VISIBLE);
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.

        }

    }

    public void checkPermissionsAndFindPosition(){

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.GPS_PROVIDER;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCALIZATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }

        }else{
            //Toast.makeText(getApplicationContext(), "ci sono gia i permessi", Toast.LENGTH_SHORT).show();

            locationManager.requestLocationUpdates(locationProvider, 0,
                    0, mLocationListener);

            lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

            //Toast.makeText(getApplicationContext(), "Localization: "+lastKnownLocation.getLatitude()+", "+lastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            // Remove the listener previously added
            //locationManager.removeUpdates(mLocationListener);

            // Save last known coordinates
            lastKnownLat = lastKnownLocation.getLatitude();
            lastKnownLng = lastKnownLocation.getLongitude();

            //creation of SearchFragment with search_query argument
/*            HomeFragment homeFragment = new HomeFragment();
            Bundle args = new Bundle();

            if (lastKnownLocation != null ) {
                args.putDouble("ARG_LAT", lastKnownLocation.getLatitude());
                args.putDouble("ARG_LON", lastKnownLocation.getLongitude());
                homeFragment.setArguments(args);
            }


            // Replace the current fragment with the selected fragment --> showing result in a particular fragment
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, homeFragment).commit();
*/
        }

    }

    @Override
    protected void onPause() {
        if(locationManager != null) {
            locationManager.removeUpdates(mLocationListener);
        }
        super.onPause();
    }

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
        user = mAuth.getCurrentUser();

        // Get Firebase Database instance
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Register notification channel for API >=26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ale2nico.FillField", "FillField", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notification channel for FillField app");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Start notification push service
        startService(new Intent(this, PushService.class));


    }


    @Override
    protected void onDestroy() {
        // Stop the service: this cause the Broadcast receiver to restart service
        stopService(new Intent(this, PushService.class));

        super.onDestroy();

    }

    @Override
    protected void onStart() {
        super.onStart();
            // Load initial fragment only if there is a signed-in user
            if (user != null) {
                //new FindUserAccuracy().execute();
                // Acquire a reference to the system Location Manager
                checkPermissionsAndFindPosition();

            }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Sign-in through the LoginActivity
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(loginIntent, REQUEST_USER_LOGIN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Opening "My Reservations" or "My Fields" if user clicked on Notification
        if (user != null && getIntent().getStringExtra("notificationFragment") != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Load profile fragment
            activeFragmentTag = PROFILE_FRAGMENT;
            fragmentTransaction.replace(R.id.fragment_container, new ProfileFragment(), PROFILE_FRAGMENT);
            navigation.setSelectedItemId(R.id.navigation_profile);
            // Load My reservations fragment if user clicked on Reminder
            if (getIntent().getStringExtra("notificationFragment").equals("myReservationsFragment")) {
                MyReservationsFragment myReservationsFragment = new MyReservationsFragment();
                activeFragmentTag = MY_RESERVATIONS_FRAGMENT;
            }
            // Load My fields fragment if user clicked on Push notification
            else if (getIntent().getStringExtra("notificationFragment").equals("myFieldsFragment")) {
                MyFieldsFragment myFieldsFragment = new MyFieldsFragment();
                activeFragmentTag = MY_FIELDS_FRAGMENT;
            }
        }
        // Opening "My Fields" if user clicked on New Reservation Notification
        if (user != null && getIntent().getStringExtra("newReservation") != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Load profile fragment
            activeFragmentTag = PROFILE_FRAGMENT;
            fragmentTransaction.replace(R.id.fragment_container, new ProfileFragment(), PROFILE_FRAGMENT);
            navigation.setSelectedItemId(R.id.navigation_profile);
            // Load My fields fragment
            MyFieldsFragment myFieldsFragment = new MyFieldsFragment();
            activeFragmentTag = MY_FIELDS_FRAGMENT;
            fragmentTransaction.replace(R.id.fragment_container, myFieldsFragment)
                    .addToBackStack(null).commit();
        }

        // Opening correct fragment
        if (activeFragmentTag.equals(PROFILE_FRAGMENT)) {
            activeFragmentTag = PROFILE_FRAGMENT;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), PROFILE_FRAGMENT)
                    .commit();

        } else if (activeFragmentTag.equals(FAVOURITES_FRAGMENT)) {
            activeFragmentTag = FAVOURITES_FRAGMENT;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FavouritesFragment(), FAVOURITES_FRAGMENT)
                    .commit();

        } else if (activeFragmentTag.equals(MY_RESERVATIONS_FRAGMENT)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), PROFILE_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MyReservationsFragment(), RESERVATIONS_FRAGMENT);
        } else if (activeFragmentTag.equals(MY_FIELDS_FRAGMENT)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), PROFILE_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MyFieldsFragment(), MY_FIELDS_FRAGMENT)
                    .addToBackStack(null).commit();
        } else if (activeFragmentTag.equals(RESERVATIONS_FRAGMENT)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), PROFILE_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MyFieldsFragment(), MY_FIELDS_FRAGMENT)
                    .addToBackStack(null);
        }else if(activeFragmentTag.equals(HOME_FRAGMENT)) {
            HomeFragment homeFragment
                    = HomeFragment.newInstance(lastKnownLat, lastKnownLng);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment, HOME_FRAGMENT)
                    .commit();
        }





    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save currently active reservation, if any
        outState.putString(KEY_SELECTED_FIELDKEY, selectedFieldKey);
        outState.putString(KEY_SELECTED_DATE, selectedDate);
        outState.putString(KEY_SELECTED_TIME, selectedTime);

        // Save field coordinates for resuming marker on config changes
        outState.putDouble(KEY_SELECTED_FIELD_LAT, selectedFieldLat);
        outState.putDouble(KEY_SELECTED_FIELD_LNG, selectedFieldLng);
        outState.putString(KEY_SELECTED_FIELD_NAME, selectedFieldName);

        // Save last known coordinates
        outState.putDouble(KEY_LAST_KNOWN_LAT, lastKnownLat);
        outState.putDouble(KEY_LAST_KNOWN_LNG, lastKnownLng);

        //save the actual fragments tag
        outState.putString(KEY_ACTIVE_FRAGMENT, activeFragmentTag);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore currently active reservation
            selectedFieldKey = savedInstanceState.getString(KEY_SELECTED_FIELDKEY);
            selectedDate = savedInstanceState.getString(KEY_SELECTED_DATE);
            selectedTime = savedInstanceState.getString(KEY_SELECTED_TIME);

            // Restore currently active marker into the map
            selectedFieldLat = savedInstanceState.getDouble(KEY_SELECTED_FIELD_LAT);
            selectedFieldLng = savedInstanceState.getDouble(KEY_SELECTED_FIELD_LNG);
            selectedFieldName = savedInstanceState.getString(KEY_SELECTED_FIELD_NAME);

            // Restore last known coordinates
            lastKnownLat = savedInstanceState.getDouble(KEY_LAST_KNOWN_LAT);
            lastKnownLng = savedInstanceState.getDouble(KEY_LAST_KNOWN_LNG);

            activeFragmentTag = savedInstanceState.getString(KEY_ACTIVE_FRAGMENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            // Get currently signed-in user
            user = mAuth.getCurrentUser();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            //creation of SearchFragment with search_query argument
            SearchingFragment searchingFragment = new SearchingFragment();
            Bundle args = new Bundle();
            args.putString("ARG_SEARCH_QUERY", query);
            searchingFragment.setArguments(args);

            // Replace the current fragment with the selected fragment --> showing result in a particular fragment
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            activeFragmentTag = SEARCHING_FRAGMENT;
            transaction.replace(R.id.fragment_container, searchingFragment).addToBackStack(null);
            transaction.commit();

        }
    }

    @Override
    public void onMapButtonClicked(Field field) {
        // Start the map fragment
        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.setRetainInstance(true);
        activeFragmentTag = MAP_FRAGMENT;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mMapFragment)
                .addToBackStack(null)
                .commit();

        // Define the actual field that has triggered the event
        selectedFieldLat = field.getLatitude();
        selectedFieldLng = field.getLongitude();
        selectedFieldName = field.getName();

        // Open the map
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onReserveButtonClicked(final Field field, final String fieldKey) {
        // Save the selected field in order to be able to get it later
        this.selectedField = field;
        this.selectedFieldKey = fieldKey;

        // Show date picker for the reservation
        showDatePickerDialog();
    }

    public void showDatePickerDialog() {
        DialogFragment datePickerFragment = new DateDialogFragment();
        datePickerFragment.show(getSupportFragmentManager(), "datePickerDialog");
    }

    public void showTimePickerDialog() {
        DialogFragment timePickerFragment
                = TimeDialogFragment.newInstance(selectedDate, selectedFieldKey);
        timePickerFragment.show(getSupportFragmentManager(), "timePickerDialog");
    }

    /**
     * This method is called when the user selects a date inside the date picker.
     * It shows another dialog containing reservation times available for the field.
     * @param view The DatePicker displayed
     * @param year Reservation year selected
     * @param month Reservation month selected
     * @param dayOfMonth Reservation day selected
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Save selected date
        selectedDate = getDateFromPicker(year, month, dayOfMonth);

        // Show reservation times dialog with available times on the selected date
        showTimePickerDialog();
    }

    /**
     * Convert the date obtained from the DatePicker into a ISO-matching String.
     * @param year Year of the date
     * @param month Month of the date
     * @param dayOfMonth Day of the date
     * @return The date selected from the DatePicker as a String
     */
    public static String getDateFromPicker(int year, int month, int dayOfMonth) {
        String yearStr = Integer.toString(year);

        // Months are numbered from 0 to 11, that's why there's an eight and not a nine..
        String monthStr = month > 8 ?
                Integer.toString(month + 1) : "0" + Integer.toString(month + 1);

        String dayOfMonthStr = dayOfMonth > 9 ?
                Integer.toString(dayOfMonth) : "0" + Integer.toString(dayOfMonth);

        return yearStr + "-" + monthStr + "-" + dayOfMonthStr;
    }

    public void insertReservationIntoAgenda() {
        // Reference to the selected field agenda
        DatabaseReference fieldAgendaRef = mDatabase.child("agenda").child(selectedFieldKey);

        fieldAgendaRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                FieldAgenda fieldAgenda = mutableData.getValue(FieldAgenda.class);
                if (fieldAgenda == null) {
                    // fieldAgenda MUST never be null
                    return Transaction.success(mutableData);
                }

                // Insert reservation data into the current agenda
                fieldAgenda.insertReservation(selectedDate, selectedTime, user.getUid());

                mutableData.setValue(fieldAgenda);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b,
                                   @Nullable DataSnapshot dataSnapshot) {
                Log.d("insertReservation", "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    public void insertReservationIntoUser(Reservation newRes) {
        // Reference to the currently signed-in user's reservations
        DatabaseReference userRef = mDatabase.child("users").child(user.getUid())
                .child("reservations");

        // Add a new reservation node at /users/userId/reservation/resKey
        String resKey = userRef.push().getKey();
        Map<String, Object> reservationValues = newRes.toMap();

        // Construct a child update according to Firebase documentations
        Map<String, Object> reservationsUpdate = new HashMap<>();
        reservationsUpdate.put(resKey, reservationValues);

        // Insert the reservation under the user node
        userRef.updateChildren(reservationsUpdate);
    }

    public void insertReservation(Reservation newRes) {
        // Add the reservation into the selected field agenda
        insertReservationIntoAgenda();

        // Add the reservation both into the agenda and user node in the database
        insertReservationIntoUser(newRes);
    }

    @Override
    public void onReservationsButtonClicked(String fieldKey) {
        // The user selected a field for displaying reservations. Display the correct fragment.

        // Create fragment and give it an argument for the selected field
        ReservationsFragment reservationsFragment = ReservationsFragment.newInstance(fieldKey);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, reservationsFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    /**
     * This method is implemented here for allowing this activity obtain
     * the selected time from the {@link TimeDialogAdapter}.
     * It's defined in the {@link OnTimeDialogClickListener}.
     * @param selectedTime Selected time in the dialog
     */
    @Override
    public void onTimeDialogClick(String selectedTime) {
        this.selectedTime = selectedTime;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                // Construct a new reservation object
                Reservation newRes = new Reservation(selectedFieldKey, selectedDate, selectedTime);

                // Insert the reservation into the database
                insertReservation(newRes);

                // Send notification reminder
                sendNotification();

                // Show reservation completed snackbar
                showSnackbar();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                // Clear the reservation state
                selectedField = null;
                selectedFieldKey = null;
                selectedDate = null;
                selectedTime = null;
                break;
            default:
                break;
        }
    }

    public void showSnackbar() {
        // Construct the snackbar
        Snackbar reservationSnackbar = Snackbar.make(findViewById(R.id.main_activity_container),
                R.string.snackbar_reservation_done, Snackbar.LENGTH_LONG);

        // Attach listener to the action button
        reservationSnackbar.setAction(R.string.snackbar_view_action, (v) -> {
            // Start 'MyReservationFragment' so as to let user view his new reservation
            activeFragmentTag = MY_RESERVATIONS_FRAGMENT;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MyReservationsFragment())
                    .addToBackStack(null)
                    .commit();
        });
        // Show the snackbar
        reservationSnackbar.show();
    }

    /**
     *  Create and send immediately the notification
     *  This work both for API <26 and API >=26 because the Channel was created in the onCreate method
     */
    public void sendNotificationReminder(String channelId, String contentTitle, String contentText,
                                         Context packageContext, Class classContext, Class notificationReceiver,
                                         long delay, Integer notificationId) {

        // [START] Create notification and its settings
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(System.currentTimeMillis() + delay)
                .setAutoCancel(true);
        //[END] Create notification and its settings

        // Intent related to current context and class
        Intent intent = new Intent(packageContext, classContext);
        intent.putExtra("notificationFragment", "myReservationsFragment");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        // Pending intent for setting notification
        PendingIntent activityIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(activityIntent);

        // Build Notification
        Notification notification = mBuilder.build();

        // Schedule notification with two intents:
        // notificationIntent for attaching to the BroadcastReceiver
        Intent notificationIntent = new Intent(packageContext, notificationReceiver);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        notificationIntent.putExtra("reservationsFragment", "myReservationsFragment");

        // PendingIntent and AlarmManager for scheduling the notification at a specific time
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Do something for marshmallow and above versions
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        } else {
            // do something for phones running an SDK before marshmallow
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        }

    }

    public Date convertToDate(String dateString, String timeString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString + " " + timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate;
    }

    public void sendNotification() {
        // Calculate delay
        Date reservationDate = convertToDate(selectedDate, selectedTime);
        long currentTimeMillis = System.currentTimeMillis();
        long reservationTimeMillis = reservationDate.getTime();
        // Send the notification one hour before the reservation
        long delay = (reservationTimeMillis - 60*60*1000 ) - currentTimeMillis;
        if (delay > 0) {
            sendNotificationReminder("ale2nico.FillField", getResources().getString(R.string.remember_reservation),
                    getResources().getString(R.string.remember_reservation_text), getApplicationContext(), MainActivity.class,
                    NotificationReceiver.class, delay, new Random().nextInt(100));
        }
    }


        @Override
        public void onMapReady(GoogleMap map) {

            Toast.makeText(this, getResources().getString(R.string.googleMapTextDialog), Toast.LENGTH_LONG).show();

            mMap = map;

            String fieldName = "";
            Double lat = 0.0;
            Double lon = 0.0;

            // Get field info, they should always be available
            lat = selectedFieldLat;
            lon = selectedFieldLng;
            fieldName = selectedFieldName != null ? selectedFieldName : "Unknown field";

            // Add a marker on the field, and move the camera.
            LatLng location = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(location).title(fieldName));
            float zoomLevel = 12.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Marker"));
        }

        @Override
        public void onBackPressed() {
            if (getFragmentManager().getBackStackEntryCount() > 0 ){
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }

    @Override
    public void onContactButtonClick(String userEmail, int viewId) {
        // Open email app
        switch (viewId) {
            case R.id.my_res_contact_button:
                activeFragmentTag = MY_RESERVATIONS_FRAGMENT;
                break;
            case R.id.contact_button:
                activeFragmentTag = RESERVATIONS_FRAGMENT;
                break;
            default: break;

        }
        Intent contactIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", userEmail, null));

        startActivity(Intent.createChooser(contactIntent, getResources().getString(R.string.share_via)));
    }


    @Override
    public void onShareImageClick(String fieldName, String reservationDate, String reservationTime,
                                  String reservationPlace) {
        //TODO get correct position of the field
        activeFragmentTag = MY_RESERVATIONS_FRAGMENT;

        // Full string to send, including maps preview and plain text
        String uri = "http://maps.google.com/maps?q=" +
                reservationPlace.replace(" ", "+") + "\n\n" +
                String.format(getResources().getString(R.string.share_action_text),
                        fieldName, reservationDate, reservationTime);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_via)));
    }
}
