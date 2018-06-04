package com.diplinkblaze.spacednote.universal.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.universal.contract.AveContentChangeListener;

import org.joda.time.LocalTime;
import util.datetime.format.DateTimeFormat;

import java.util.ArrayList;

import util.Keyboard;
import util.TypeFaceUtils;


public class CalendarTimeFragment extends Fragment implements AveContentChangeListener {
    private static final String KEY_INITIAL_TIME = "initialTime";
    private static final String KEY_TAG = "tag";
    private static final String KEY_READ_ONLY = "readOnly";

    private static final String KEY_CURRENT_TIME = "currentTime";
    private static final String KEY_MODE = "mode";

    private static final int MODE_TEXT = 0;
    private static final int MODE_AUTO_COMPLETE = 1;

    private Adapter adapter;
    private LocalTime currentTime;
    private int mode;
    private boolean readOnly = false;

    private OnViewClickListener viewClickListener = new OnViewClickListener();

    public CalendarTimeFragment() {
        // Required empty public constructor
    }

    public static CalendarTimeFragment newInstance(LocalTime initialTime, String tag, boolean readOnly) {
        CalendarTimeFragment fragment = new CalendarTimeFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_INITIAL_TIME, initialTime);
        args.putString(KEY_TAG, tag);
        args.putBoolean(KEY_READ_ONLY, readOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            currentTime = (LocalTime) savedInstanceState.getSerializable(KEY_CURRENT_TIME);
            mode = savedInstanceState.getInt(KEY_MODE);
            readOnly = savedInstanceState.getBoolean(KEY_READ_ONLY);
        } else if (getArguments() != null) {
            currentTime = (LocalTime) getArguments().getSerializable(KEY_INITIAL_TIME);
            mode = MODE_TEXT;
            readOnly = getArguments().getBoolean(KEY_READ_ONLY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_calendar_time, container, false);
        initializeViews(contentView);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        TextView timeText = contentView.findViewById(R.id.fragment_calendar_time_text);
        timeText.setOnClickListener(viewClickListener);
        TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), timeText);

        AutoCompleteTextView autoCompleteTextView = contentView.findViewById(R.id.fragment_calendar_time_auto_complete);
        adapter = new Adapter();
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener(new OnListItemSelected());
        autoCompleteTextView.setOnFocusChangeListener(new OnAutoCompleteFocusChanged());
    }

    private void updateViews(View contentView) {
        TextView timeText = contentView.findViewById(R.id.fragment_calendar_time_text);
        View timeAutoComplete = contentView.findViewById(R.id.fragment_calendar_time_auto_complete);

        if (mode == MODE_AUTO_COMPLETE && !readOnly) {
            timeText.setVisibility(View.GONE);
            timeAutoComplete.setVisibility(View.VISIBLE);
        } else if (mode == MODE_TEXT || readOnly) {
            timeText.setText(DateTimeFormat.fullTime(getContext()).print(currentTime));
            timeText.setVisibility(View.VISIBLE);
            timeAutoComplete.setVisibility(View.GONE);
        } else
            throw new RuntimeException("mode is invalid");

        if (readOnly) {
            timeText.setClickable(false);
        } else {
            timeText.setClickable(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CURRENT_TIME, currentTime);
        outState.putInt(KEY_MODE, mode);
        outState.putBoolean(KEY_READ_ONLY, readOnly);
    }

    //======================================= Listeners =======================================


    private void onChangeTimeClick(View contentView) {
        mode = MODE_AUTO_COMPLETE;
        updateViews(contentView);
        AutoCompleteTextView timeAutoComplete = contentView.findViewById(R.id.fragment_calendar_time_auto_complete);
        timeAutoComplete.setText("");
        timeAutoComplete.requestFocus();
    }

    void listItemSelected(int position, View contentView) {
        currentTime = ((Time) adapter.getItem(position)).toLocalTime();
        mode = MODE_TEXT;
        updateViews(contentView);
        timeSelected();
        Keyboard.hide(getActivity(), this);
    }

    private class OnListItemSelected implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            listItemSelected(position, getView());
        }
    }

    private class OnViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.fragment_calendar_time_text)
                onChangeTimeClick(getView());
        }
    }

    private class OnAutoCompleteFocusChanged implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                Keyboard.show(getActivity(), CalendarTimeFragment.this);
            }
        }
    }

    //====================================== Communication Parent ===================================
    @Override
    public void aveStateChanged(boolean readOnly) {
        this.readOnly = readOnly;
        updateViews(getView());
    }

    public void aveNewContent(LocalTime newTime) {
        currentTime = newTime;
        updateViews(getView());
    }

    void timeSelected() {
        String tag = getArguments().getString(KEY_TAG);
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            ((OnFragmentInteractionListener) getParentFragment()).onTimeSelected(currentTime, tag);
        else if (getActivity() instanceof OnFragmentInteractionListener)
            ((OnFragmentInteractionListener) getActivity()).onTimeSelected(currentTime, tag);
        else
            throw new RuntimeException("Neither Parent Fragment nor Activity is an instance of " +
                    "OnFragmentInteractionListener");
    }

    public interface OnFragmentInteractionListener {
        void onTimeSelected(LocalTime time, String tag);
    }

    //============================================ Adapter ===========================================
    private class Adapter extends BaseAdapter implements Filterable {
        TimeFilter mFilter = new TimeFilter();

        ArrayList<Time> filterList;

        public Adapter() {

        }

        @Override
        public int getCount() {

            if (filterList != null)
                return filterList.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int i) {
            return filterList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return filterList.get(i).hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_universal, viewGroup, false);
                holder = new ViewHolder();
                view.setTag(holder);
                holder.title = view.findViewById(R.id.list_item_universal_title);
                TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), holder.title);
                view.findViewById(R.id.list_item_universal_icon).setVisibility(View.GONE);
                view.findViewById(R.id.list_item_universal_next).setVisibility(View.GONE);
                view.findViewById(R.id.list_item_universal_value).setVisibility(View.GONE);
                view.findViewById(R.id.list_item_universal_detail).setVisibility(View.GONE);
                view.findViewById(R.id.list_item_universal_footer).setVisibility(View.GONE);
            } else
                holder = (ViewHolder) view.getTag();

            holder.title.setText(DateTimeFormat.fullTime(getContext()).print(
                    filterList.get(i).toLocalTime()));

            return view;
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        private class TimeFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                if (charSequence != null) {
                    String number = charSequence.toString();
                    ArrayList<Time> filterList = Time.getInstancesFor(getContext(), number);
                    FilterResults results = new FilterResults();
                    results.values = filterList;
                    results.count = filterList.size();
                    return results;
                } else {
                    FilterResults results = new FilterResults();
                    results.values = null;
                    results.count = 0;
                    return results;
                }
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filterList = (ArrayList<Time>) filterResults.values;
                notifyDataSetChanged();
            }

        }

        private class ViewHolder {
            TextView title;
        }
    }

    public static class Time {
        public static final int HALF_DAY_AM = 1;
        public static final int HALF_DAY_PM = 2;
        public static final int HALF_DAY_24 = 3;

        private int hour;
        private int minute;
        private int halfDay;

        private Time() {

        }

        public static ArrayList<Time> getInstancesFor(Context context, String number) {
            ArrayList<Time> times = new ArrayList<Time>(4);
            if (number.length() <= 4) {
                boolean is24 = DateFormat.is24HourFormat(context);
                for (String clockNumber : getPossibleClockNumbers(number)) {
                    int hour = Integer.parseInt(clockNumber.substring(0, 2));
                    int minute = Integer.parseInt(clockNumber.substring(2, 4));
                    if (is24) {
                        if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                            Time time = new Time();
                            time.hour = hour;
                            time.minute = minute;
                            time.halfDay = HALF_DAY_24;
                            times.add(time);
                        }
                    } else {
                        if (hour >= 1 && hour <= 12 && minute >= 0 && minute <= 59) {
                            Time timeAm = new Time();
                            Time timePm = new Time();
                            timeAm.hour = hour;
                            timePm.hour = hour;
                            timeAm.minute = minute;
                            timePm.minute = minute;
                            timeAm.halfDay = HALF_DAY_AM;
                            timePm.halfDay = HALF_DAY_PM;
                            times.add(timeAm);
                            times.add(timePm);
                        }
                    }
                }
            }
            return times;
        }

        public LocalTime toLocalTime() {
            int hourOfDay;
            if (halfDay == HALF_DAY_24)
                hourOfDay = hour;
            else if (halfDay == HALF_DAY_AM)
                hourOfDay = hour % 12;
            else if (halfDay == HALF_DAY_PM)
                hourOfDay = (hour % 12) + 12;
            else
                throw new RuntimeException("Invalid halfDay, something must have gone wrong");

            LocalTime localTime = new LocalTime(hourOfDay, minute, 0, 0);
            return localTime;
        }

        public int toNumber() {
            int number = hour * 100 + minute;
            return number;
        }

        private static ArrayList<String> getPossibleClockNumbers(String number) {
            ArrayList<String> clockNumbers = new ArrayList<String>();
            if (number.length() <= 4 && number.length() > 0) {
                int zeros = 4 - number.length();
                for (int i = 0; i <= zeros; i++) {
                    String clockNumber = "";
                    for (int j = 0; j < i; j++)
                        clockNumber += "0";
                    clockNumber += number;
                    for (int j = i; j < zeros; j++)
                        clockNumber += "0";
                    clockNumbers.add(clockNumber);
                }
            }
            return clockNumbers;
        }
    }
}
