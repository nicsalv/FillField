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
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ale2nico.fillfield.models.Field;
import com.ale2nico.fillfield.models.FieldAgenda;
import com.ale2nico.fillfield.models.Reservation;
import com.ale2nico.fillfield.models.TimeTable;
import com.ale2nico.fillfield.HomeFragment.OnFieldClickListener;
import com.ale2nico.fillfield.MyFieldsFragment.OnReservationsButtonClickedListener;

import com.firebase.client.Firebase;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
        implements OnFieldClickListener,
        OnReservationsButtonClickedListener,
        DatePickerDialog.OnDateSetListener, OnTimeDialogClickListener {

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

    // These variables contain reservation state.
    private Field selectedField;
    private String selectedFieldKey;
    private String selectedDate;
    private String selectedTime;

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
                    // Replace the current fragment in the 'fragment_container'
                    transaction.replace(R.id.fragment_container, new HomeFragment());
                    break;

                case R.id.navigation_search_fields:
                    transaction.replace(R.id.fragment_container, new SearchFragment());
                    onSearchRequested();
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
        // Opening "My Reservations" if user clicked on Notification Reminder
        if (user != null && getIntent().getStringExtra("reservationsFragment") != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Load profile fragment
            fragmentTransaction.replace(R.id.fragment_container,  new ProfileFragment());
            navigation.setSelectedItemId(R.id.navigation_profile);
            // Load My reservations fragment
            MyReservationsFragment myReservationsFragment = new MyReservationsFragment();
            fragmentTransaction.replace(R.id.fragment_container, myReservationsFragment)
                    .addToBackStack(null).commit();


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            // Get currently signed-in user
            user = mAuth.getCurrentUser();

            Map<String, Object> fieldUpdate = new HashMap<>();
            Map<String, Object> fieldAgendaUpdate = new HashMap<>();
            for( int i = 0 ; i < 10 ; i++) {
                Field field = new Field(user.getUid(), "Campo " + i, 44.0568495d, 8.1641088d, "08:00", "23:00");
                FieldAgenda fieldAgenda = new FieldAgenda(field.getOpeningHour(), field.getClosingHour());
                String key = mDatabase.child("fields").push().getKey();
                Map<String, Object> fieldValues = field.toMap();
                Map<String, Object> fieldAgendaValues = fieldAgenda.toMap();

                fieldUpdate.put("/fields/" + key, fieldValues);
                fieldAgendaUpdate.put("/agenda/" + key, fieldAgendaValues);
            }
            mDatabase.updateChildren(fieldUpdate);
            mDatabase.updateChildren(fieldAgendaUpdate);
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

            case "Search":
                SearchFragment searchFragment = new SearchFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, searchFragment).commit();
                navigation.setSelectedItemId(R.id.navigation_search_fields);
                break;

            case "Favourites":
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, new HomeFragment()).commit();
                navigation.setSelectedItemId(R.id.navigation_favourites_fields);
                break;
        }
    }

    @Override
    public void onNewIntent(Intent intent){
        setIntent(intent);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            //TODO: search the query on Firebase

            //creation of SearchFragment with search_query argument
            SearchFragment searchFragment = new SearchFragment();
            Bundle args = new Bundle();
            args.putString(SearchFragment.ARG_SEARCH_QUERY, query);
            searchFragment.setArguments(args);

            // Replace the current fragment with the selected fragment --> showing result in a particular fragment
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, searchFragment)
                    .addToBackStack(null);
            transaction.commit();

        }
    }

    @Override
    public void onMapButtonClicked(Field field) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);

        //Create the bundle
        Bundle bundle = new Bundle();

        //Add your data to bundle
        bundle.putDouble("EXTRA_LAT", field.getLatitude());
        bundle.putDouble("EXTRA_LON", field.getLongitude());
        bundle.putString("EXTRA_FIELD_NAME", field.getName());

        //Add the bundle to the intent
        intent.putExtras(bundle);

        //Fire that second activity
        startActivity(intent);
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
        String monthStr = month > 8 ? Integer.toString(month + 1) : "0" + Integer.toString(month + 1);

        String dayofMonthStr = dayOfMonth > 9 ? Integer.toString(dayOfMonth) : "0" + Integer.toString(dayOfMonth);

        return yearStr + "-" + monthStr + "-" + dayofMonthStr;
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

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                // Construct a new reservation object
                Reservation newRes = new Reservation(selectedFieldKey, selectedDate, selectedTime);

                // Insert the reservation into the database
                insertReservation(newRes);
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

    /**
     * This method is implemented here for allowing this activity to obtain
     * the selected time from the {@link TimeDialogAdapter}.
     * It's defined in the {@link OnTimeDialogClickListener}
     * @param selectedTime Selected time in the dialog
     */
    @Override
    public void onTimeDialogClick(String selectedTime) {
        this.selectedTime = selectedTime;
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
        intent.putExtra("reservationsFragment", "myReservationsFragment");
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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Do something for marshmallow and above versions
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,  futureInMillis, pendingIntent);
        } else{
            // do something for phones running an SDK before marshmallow
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        }

    }

    public static void sendNotificationToUser(String user, final String message) {
        Firebase ref = new Firebase( "https://fillfield-bc48e.firebaseio.com/");
        final Firebase notifications = ref.child("notificationRequests");

        Map notification = new HashMap<>();
        notification.put("username", user);
        notification.put("message", message);

        notifications.push().setValue(notification);
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
}
