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

import com.ale2nico.fillfield.dummy.DummyContent;
import com.ale2nico.fillfield.models.Field;
import com.ale2nico.fillfield.models.FieldAgenda;
import com.ale2nico.fillfield.models.TimeTable;
import com.firebase.client.Firebase;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnListFragmentInteractionListener,
                    MyReservationsFragment.OnListFragmentInteractionListener,
                    MyFieldsFragment.OnListFragmentInteractionListener,
                    FieldViewFragment.OnFragmentInteractionListener {

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
                        .add(R.id.fragment_container, new HomeFragment());
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
                                sendNotification("ale2nico.FillField", "Prenotazione",
                                        "Non te lo prenoto quel campo, maledetto", getApplicationContext(), this.getClass(),
                                        NotificationReceiver.class, 0, 0);
                                sendNotificationToUser("bozzi.ale96@gmail.com", "Ciao");
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

                break;

        }

        sendNotification("ale2nico.FillField", "Ehi tu!",
                "Non avrai mica cliccato quel bottone.....", getApplicationContext(), this.getClass(),
                NotificationReceiver.class, 0 , 0);
        sendNotificationToUser("bozzi.ale96@gmail.com", "Ciao");

    }

    // TODO: remove this method
    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

        // Passing to the FieldViewFragment

            FieldViewFragment fieldViewFragment = new FieldViewFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fieldViewFragment)
                    .addToBackStack(null)
                    .commit();
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
}
