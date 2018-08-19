package com.ale2nico.fillfield;

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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ale2nico.fillfield.dummy.DummyContent;
import com.ale2nico.fillfield.models.Field;
import com.ale2nico.fillfield.models.FieldAgenda;
import com.ale2nico.fillfield.models.TimeTable;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalTime;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnListFragmentInteractionListener,
                    MyReservationsFragment.OnListFragmentInteractionListener,
                    FieldViewFragment.OnFragmentInteractionListener,
                    MyFieldsFragment.OnListFragmentInteractionListener,
                    OnMapReadyCallback {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // Request login code
    public static final int REQUEST_USER_LOGIN = 1;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Firebase User
    private FirebaseUser user;

    // Firebase Database
    private DatabaseReference mDatabase;

    //BottomNavigation
    BottomNavigationView navigation;

    public MapFragment mMapFragment;

    private GoogleMap mMap;
    private Field actualMapField;

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
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();

            if(mMapFragment != null){
                Toast.makeText(getApplicationContext(), "bonaa", Toast.LENGTH_LONG);
                android.app.FragmentTransaction fragmentTransaction =
                        getFragmentManager().beginTransaction();
                fragmentTransaction.remove(mMapFragment).commit();
            }

            switch (item.getItemId()) {

                case R.id.navigation_home:
                    // Replace the current fragment in the 'fragment_container'
                    transaction.replace(R.id.fragment_container, new HomeFragment());
                    break;
                case R.id.navigation_favourites_fields:
                    // Replace the current fragment in the 'fragment_container'
                    transaction.replace(R.id.fragment_container, new FavouritesFragment());
                    break;
                case R.id.navigation_profile:
                    transaction.replace(R.id.fragment_container, new ProfileFragment());
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
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Place the initial fragment into the activity (the HomeFragment).
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // Load initial fragment only if there is a signed-in user
            if (user != null) {
                loadFragmentFromSharedPreferences();
            }
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
            fragmentTransaction.replace(R.id.fragment_container,  new ProfileFragment());
            navigation.setSelectedItemId(R.id.navigation_profile);
            // Load My reservations fragment if user clicked on Reminder
            if (getIntent().getStringExtra("notificationFragment").equals("myReservationsFragment")) {
                MyReservationsFragment myReservationsFragment = new MyReservationsFragment();
                fragmentTransaction.replace(R.id.fragment_container, myReservationsFragment)
                        .addToBackStack(null).commit();
            }
            // Load My fields fragment if user clicked on Push notification
            else if (getIntent().getStringExtra("notificationFragment").equals("myFieldsFragment")) {
                MyFieldsFragment myFieldsFragment = new MyFieldsFragment();
                fragmentTransaction.replace(R.id.fragment_container, myFieldsFragment)
                        .addToBackStack(null).commit();
            }

        }
        // Opening "My Fields" if user clicked on New Reservation Notification
        if (user != null && getIntent().getStringExtra("newReservation") != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Load profile fragment
            fragmentTransaction.replace(R.id.fragment_container,  new ProfileFragment());
            navigation.setSelectedItemId(R.id.navigation_profile);
            // Load My fields fragment
            MyFieldsFragment myFieldsFragment = new MyFieldsFragment();
            fragmentTransaction.replace(R.id.fragment_container, myFieldsFragment)
                    .addToBackStack(null).commit();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            // Get currently signed-in user
            user = mAuth.getCurrentUser();
        }
    }

    private void loadFragmentFromSharedPreferences() {
        // Get shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // If there isn't any preference, load the HomeFragment
        String chosen = prefs.getString("home_spinner", "Home");

        switch (chosen) {
            case "Home":
                // Replace the current fragment in the 'fragment_container'
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, new HomeFragment()).commit();
                navigation.setSelectedItemId(R.id.navigation_home);
                break;
            case "Favourites":
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, new HomeFragment()).commit();
                navigation.setSelectedItemId(R.id.navigation_favourites_fields);
                break;
        }
    }

    @Override
    public void onListFragmentInteraction(final Field field, final String fieldKey, int id) {
        switch (id){
            case R.id.action_1_button:

                // [START] Reservation
                final DatabaseReference mFieldAgendaRef = mDatabase.child("agenda").child(fieldKey);
                mFieldAgendaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(FieldAgenda.class) == null) {
                            mDatabase.child("agenda").child(fieldKey)
                                    .setValue(new FieldAgenda(field.getOpeningHour(), field.getClosingHour()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // This array stores the Date in the first cell, and the Time in the second one
                final String[] reservationDateTime = new String[2];

                // Set listener for DatePickerDialog
                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(final DatePicker view, int year, int month, int dayOfMonth) {
                        // Save the chosen Date in the array
                        reservationDateTime[0] = getDateFromPicker(year, month + 1, dayOfMonth);

                        final Calendar reservationDay = Calendar.getInstance();
                        reservationDay.set(year, month, dayOfMonth);
                        // Create dialog for selecting reservation time
                        final AlertDialog.Builder reservationDialogBuilder = new AlertDialog.Builder(MainActivity.this,
                                R.style.Theme_AppCompat_Light_Dialog);
                        // [START] Create centered custom title for dialog
                        TextView title = new TextView(MainActivity.this);
                        title.setText(R.string.reservation_time);
                        title.setGravity(Gravity.CENTER);
                        title.setTextSize(20);
                        title.setTextColor(Color.BLACK);
                        reservationDialogBuilder.setCustomTitle(title);
                        // [END] Create centered custom title for dialog

                        // [START] Get free hours
                        List<String> hours = new ArrayList<>();
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                                R.layout.reservation_dialog, hours);
                        getFreeHoursFromDatabase(adapter, hours, fieldKey, reservationDateTime[0]);
                        // [END] Get free hours

                        //Set dialog with correct hours
                        reservationDialogBuilder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {

                            private int colorOrg = 0x00000000;
                            private int colorSelected = 0xFF00FF00;
                            private View previousView;


                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked on entry, save the choice
                                ListView listView = ((AlertDialog) dialog).getListView();
                                // Needed in case of scrolling listView
                                final int firstListItemPosition = listView.getFirstVisiblePosition();

                                reservationDateTime[1] = adapter.getItem(which).toString();
                                // Change background color of previously selected item
                                if(previousView != null) {
                                    previousView.setBackgroundColor(colorOrg);
                                }

                                // Change background color of selected item
                                listView.getChildAt(which - firstListItemPosition).setBackgroundColor(colorSelected);
                                previousView = listView.getChildAt(which-firstListItemPosition);
                            }
                        });


                        // Set confirm and cancel button for reservation
                        reservationDialogBuilder.setPositiveButton(R.string.confirm_reservation, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO add reservation and send notification
                                // Get reservation data
                                String date = reservationDateTime[0];
                                String time = reservationDateTime[1];
                                onInsertedReservation(mFieldAgendaRef, date, time, user.getUid());

                                // Reservation done, send notification

                                // Calculate delay
                                Date reservationDate = convertToDate(reservationDateTime[0], reservationDateTime[1] );
                                long currentTimeMillis = System.currentTimeMillis();
                                long reservationTimeMillis = reservationDate.getTime();
                                // Send the notification one hour before the reservation
                                long delay = (reservationTimeMillis - 60*60*1000 ) - currentTimeMillis;
                                if (delay > 0) {
                                    sendNotificationReminder("ale2nico.FillField", getResources().getString(R.string.remember_reservation),
                                            getResources().getString(R.string.remember_reservation_text), getApplicationContext(), MainActivity.class,
                                            NotificationReceiver.class, delay, new Random().nextInt(1000));
                                }
                                Toast.makeText(getApplicationContext(), R.string.reservation_success, Toast.LENGTH_LONG).show();
                            }
                        })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });

                        // Show the dialog for selecting reservation hour
                        reservationDialogBuilder.show();
                    }
                };


                // Creation of calendar for today date and DatePickerDialog
                Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, 0,
                        onDateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();

                //  [END] Reservation

                break;
            case R.id.action_2_button:

                //let's start the map Fragment
                mMapFragment = MapFragment.newInstance();
                android.app.FragmentTransaction fragmentTransaction =
                        getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, mMapFragment).addToBackStack(null);
                fragmentTransaction.commit();

                //Define the actual field that have triggered the event
                actualMapField = field;

                //let's open the map
                mMapFragment.getMapAsync(this);

                break;


        }

    }

    // TODO: remove this method
    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

       // THIS GOES TO MY RESERVATIONS RECYCLER VIEW
        Double latitude = 44.054932231450536;
        Double longitude = 8.212966918945312;
        String fieldName = "Da Rossi";
        String reservationTime = "19:00";
        // Full string to send, including maps preview and plain text
        String uri = "http://maps.google.com/maps?q=" +
                latitude + ","+longitude + "\n\n" +
                String.format(getResources().getString(R.string.share_action_text),
                      fieldName, reservationTime);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }


    @Override
    public void onNewIntent(Intent intent){
        setIntent(intent);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
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
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, searchingFragment).addToBackStack(null);
            transaction.commit();

        }
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
        notificationId = notificationId + 1;
        Intent notificationIntent = new Intent(packageContext, notificationReceiver);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        notificationIntent.putExtra("reservationsFragment", "myReservationsFragment");

        // PendingIntent and AlarmManager for scheduling the notification at a specific time
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Do something for marshmallow and above versions
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,  futureInMillis, pendingIntent);
        } else{
            // do something for phones running an SDK before marshmallow
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        }

    }


    //Convert the date into a string that matches the pattern requested from LocalTime
    public String getDateFromPicker(int year, int month, int dayOfMonth) {
       String yearStr = Integer.toString(year);
       String monthStr = month > 9 ? Integer.toString(month) : "0" + Integer.toString(month);
       String dayofMonthStr = dayOfMonth > 9 ? Integer.toString(dayOfMonth) : "0" + Integer.toString(dayOfMonth);

       return yearStr + "-" + monthStr + "-" + dayofMonthStr;
    }

    public void getFreeHoursFromDatabase(final ArrayAdapter<String> adapter, final List<String> freeHours, final String fieldKey, final String date) {
        DatabaseReference mAgendaRef = mDatabase.child("agenda").child(fieldKey);

        mAgendaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FieldAgenda mFieldAgenda = dataSnapshot.getValue(FieldAgenda.class);
                // All hours are available
                if (mFieldAgenda.getTimeTable(date) == null) {
                    freeHours.addAll(buildHoursList(mFieldAgenda.getOpeningHour(),
                            mFieldAgenda.getClosingHour(), new ArrayList<String>()));
                }
                // One or more hours already reserved
                else {
                    TimeTable timeTable = mFieldAgenda.getTimeTable(date);
                    List<String> busyHours = buildBusyHoursList(timeTable);
                    freeHours.addAll(buildHoursList(timeTable.getOpeningHour(),
                            timeTable.getClosingHour(), busyHours));

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // Construct free hours for one day
    public List<String> buildHoursList(String openingHour, String closingHour, List<String> busyHours) {
        String currentHour = openingHour;
        List<String> freeHours = new ArrayList<>();
        while (LocalTime.parse(currentHour).isBefore(LocalTime.parse(closingHour))) {
            if (busyHours.indexOf(currentHour) == -1) {
                freeHours.add(currentHour);
            }
            currentHour = LocalTime.parse(currentHour).plusHours(1).toString();
        }
        return freeHours;
    }

    // Detect which hours are already reserved
    public List<String> buildBusyHoursList(TimeTable timeTable) {
        List<String> busyHours = new ArrayList<>();
        String currentHour = timeTable.getOpeningHour();
        while (LocalTime.parse(currentHour).isBefore(LocalTime.parse(timeTable.getClosingHour()))) {
            if (timeTable.getReservation(currentHour) != null) {
                busyHours.add(currentHour);
            }
            currentHour = LocalTime.parse(currentHour).plusHours(1).toString();
        }
        return busyHours;
    }

    public void onInsertedReservation(final DatabaseReference fieldAgendaRef, final String date, final String time, final String userId) {
        fieldAgendaRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                FieldAgenda fieldAgenda = mutableData.getValue(FieldAgenda.class);
                if (fieldAgenda == null) {
                    return Transaction.success(mutableData);
                }

                // Insert reservation data
                fieldAgenda.insertReservation(date, time, userId);

                mutableData.setValue(fieldAgenda);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                Log.d("INSERT RESERVATION", "postTransaction:onComplete:" + databaseError);
            }
        });
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


        @Override
        public void onMapReady(GoogleMap map) {

            new AlertDialog.Builder(this).setMessage(R.string.googleMapTextDialog)
                    .show();

            mMap = map;

            String fieldName = "";
            Double lat = 0.0;
            Double lon = 0.0;

            if(actualMapField != null) {
                lat = actualMapField.getLatitude();
                lon = actualMapField.getLongitude();
                fieldName = actualMapField.getName();
            }

            // Add a marker on the field, and move the camera.
            LatLng location = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(location).title("Field: "+fieldName));
            float zoomLevel = 10.0f;
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
}
