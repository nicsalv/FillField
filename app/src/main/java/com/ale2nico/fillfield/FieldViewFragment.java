package com.ale2nico.fillfield;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.BaseCalendarEvent;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.github.tibolte.agendacalendarview.models.WeekItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FieldViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FieldViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FieldViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private AgendaCalendarView agendaCalendarView;

    private OnFragmentInteractionListener mListener;

    public FieldViewFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(getContext().getResources().getString(R.string.specific_field));
        return inflater.inflate(R.layout.fragment_field_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get a reference for the week view in the layout.
        agendaCalendarView = (AgendaCalendarView) getView().findViewById(R.id.agenda_calendar_view);
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        minDate.add(Calendar.MONTH, 0);
        minDate.set(Calendar.DAY_OF_MONTH, 1);
        maxDate.add(Calendar.YEAR, 1);

        // ***Example*** Add event to the list

        List<CalendarEvent> eventList = new ArrayList<>();
        Calendar startTime2 = Calendar.getInstance();
        Calendar endTime2 = Calendar.getInstance();
        startTime2.set(Calendar.HOUR_OF_DAY, 18);
        startTime2.set(Calendar.MINUTE, 0);
        endTime2.set(Calendar.HOUR_OF_DAY, 19);
        endTime2.set(Calendar.MINUTE, 0);
        BaseCalendarEvent event2 = new BaseCalendarEvent("Prenotazione: "+startTime2.get(Calendar.HOUR_OF_DAY) + "-"+endTime2.get(Calendar.HOUR_OF_DAY), "", "Dalvík",
                ContextCompat.getColor(this.getContext(), R.color.red), startTime2, endTime2, false);
        eventList.add(event2);
        Calendar startTime3 = Calendar.getInstance();
        Calendar endTime3 = Calendar.getInstance();
        startTime3.set(Calendar.HOUR_OF_DAY, 14);
        startTime3.set(Calendar.MINUTE, 0);
        endTime3.set(Calendar.HOUR_OF_DAY, 15);
        endTime3.set(Calendar.MINUTE, 0);
        BaseCalendarEvent event3 = new BaseCalendarEvent("Prenotazione: "+startTime3.get(Calendar.HOUR_OF_DAY) + "-"+endTime3.get(Calendar.HOUR_OF_DAY), "", "Dalvík",
                ContextCompat.getColor(this.getContext(), R.color.red), startTime3, endTime3, false);
        eventList.add(event3);
        // ***Example finished***
        //TODO: fill the calendar with the correct reservations for this field

        CalendarPickerController calendarPickerController = new CalendarPickerController() {
            @Override
            public void onDaySelected(DayItem dayItem) {
            }

            @Override
            public void onEventSelected(CalendarEvent event) {

            }

            @Override
            public void onScrollToDate(Calendar calendar) {

            }
        };

        agendaCalendarView.init(eventList, minDate, maxDate, Locale.getDefault(), calendarPickerController);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

    }
}
