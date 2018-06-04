package com.diplinkblaze.spacednote.universal.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.chrono.PersianChronology;

import util.TypeFaceUtils;

public class DateTimeChooserFragment extends BottomSheetDialogFragment implements
        CalendarFragment.OnFragmentInteractionListener, CalendarTimeFragment.OnFragmentInteractionListener{

    private static final String KEY_TAG = "tag";
    private static final String KEY_IDENTIFIER = "identifier";
    private static final String KEY_DATETIME = "datetime";
    private static final String KEY_MIN_DATETIME = "minDatetime";
    private static final String KEY_MAX_DATETIME = "maxDatetime";

    private static final String TAG_CALENDAR_DATE = "calendarDate";
    private static final String TAG_CALENDAR_TIME = "calendarTime";

    private DateTime dateTime;

    public DateTimeChooserFragment() {
        // Required empty public constructor
    }

    public static DateTimeChooserFragment newInstance(
            @Nullable LocalDate minDate, @Nullable LocalDate maxDate, String tag, Bundle identifier) {
        return newInstance(DateTime.now(PersianChronology.getInstance()), minDate, maxDate, tag, identifier);
    }

    public static DateTimeChooserFragment newInstance
            (DateTime initialDate, @Nullable LocalDate minDate, @Nullable LocalDate maxDate,
             String tag, Bundle identifier) {
        DateTimeChooserFragment fragment = new DateTimeChooserFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TAG, tag);
        args.putLong(KEY_DATETIME, initialDate.getMillis());
        if (minDate != null) {
            args.putLong(KEY_MIN_DATETIME, minDate.toDateTimeAtStartOfDay().getMillis());
        }
        if (maxDate != null) {
            args.putLong(KEY_MAX_DATETIME, maxDate.toDateTimeAtStartOfDay().getMillis());
        }
        args.putBundle(KEY_IDENTIFIER, identifier);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            dateTime = new DateTime(savedInstanceState.getLong(KEY_DATETIME), PersianChronology.getInstance());
        } else {
            dateTime = new DateTime(getArguments().getLong(KEY_DATETIME), PersianChronology.getInstance());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_date_time_chooser, container, false);
        initializeViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        //Save button:
        {
            TextView selectButton = contentView.findViewById(R.id.fragment_date_time_chooser_select);
            selectButton.setOnClickListener(new OnSaveSelected());
            TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), selectButton);
        }

        //Calendar date:
        {
            Fragment fragment = getChildFragmentManager().findFragmentByTag(TAG_CALENDAR_DATE);
            if (fragment == null) {
                CalendarFragment.DateState dateState = new CalendarFragment.DateState();
                dateState.currentDate = dateTime.toLocalDate();
                LocalDate minDate = null;
                LocalDate maxDate = null;
                if (getArguments().containsKey(KEY_MIN_DATETIME))
                    minDate = new LocalDate(getArguments().getLong(KEY_MIN_DATETIME), PersianChronology.getInstance());
                if (getArguments().containsKey(KEY_MAX_DATETIME))
                    maxDate = new LocalDate(getArguments().getLong(KEY_MAX_DATETIME), PersianChronology.getInstance());
                fragment = CalendarFragment.newInstance(dateState, minDate, maxDate, TAG_CALENDAR_DATE, null, getContext());
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_date_time_chooser_date_frame, fragment, TAG_CALENDAR_DATE);
                transaction.commit();
            }
        }

        //Calendar Time:
        {
            Fragment fragment = getChildFragmentManager().findFragmentByTag(TAG_CALENDAR_TIME);
            if (fragment == null) {
                fragment = CalendarTimeFragment.newInstance(dateTime.toLocalTime(), TAG_CALENDAR_TIME, false);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_date_time_chooser_time_frame, fragment, TAG_CALENDAR_TIME);
                transaction.commit();
            }
        }
    }

    //=================================== Communication Children ===================================
    @Override
    public void onTimeSelected(LocalTime time, String tag) {
        this.dateTime = this.dateTime.withTime(time);
    }

    @Override
    public void onDateSelected(CalendarFragment.DateState dateState, String tag, Bundle identifier) {
        this.dateTime = this.dateTime.withDate(dateState.currentDate);
    }

    //==================================== Communication Parent ====================================
    void onDateTimeChooserResult() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            ((OnFragmentInteractionListener) getParentFragment()).onDateTimeChooserResult(dateTime, tag, identifier);
        else if (getActivity() instanceof OnFragmentInteractionListener)
            ((OnFragmentInteractionListener) getActivity()).onDateTimeChooserResult(dateTime, tag, identifier);
    }


    public interface OnFragmentInteractionListener {
        void onDateTimeChooserResult(DateTime dateTime, String tag, Bundle identifier);
    }

    //====================================== User Interaction ======================================

    private class OnSaveSelected implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            onDateTimeChooserResult();
        }
    }

}
