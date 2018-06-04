package com.diplinkblaze.spacednote.universal.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.chrono.Holidays;
import org.joda.time.chrono.PersianChronology;
import util.datetime.format.DateTimeFormat;
import util.datetime.interval.IntervalUtils;

import util.TypeFaceUtils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarMonthFragment#newInstance} factory method to
 * createBudgets an instance of this fragment.
 */
public class CalendarMonthFragment extends Fragment {
    private static final String KEY_FIRST_DAY_OF_MONTH = "firstDayOfMonth";
    private static final String KEY_DATE_STATE_PREFIX = "dateStatePrefix";
    private static final String KEY_MIN_DAY = "minDay";
    private static final String KEY_MAX_DAY = "maxDay";

    private LocalDate firstDayOfMonth;
    private Integer minDay;
    private Integer maxDay;
    private CalendarFragment.DateState dateState;

    private OnCalendarCellClickListener calendarCellClickListener = new OnCalendarCellClickListener();
    private OnCalendarCellLongClickListener calendarCellLongClickListener = new OnCalendarCellLongClickListener();

    public CalendarMonthFragment() {
        // Required empty public constructor
    }

    public static CalendarMonthFragment newInstance(LocalDate firstDayOfMonth) {
        return newInstance(firstDayOfMonth, null, null);
    }

    public static CalendarMonthFragment newInstance(LocalDate firstDayOfMonth, @Nullable Integer minDay, @Nullable Integer maxDay) {
        CalendarMonthFragment fragment = new CalendarMonthFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_FIRST_DAY_OF_MONTH, firstDayOfMonth.withDayOfMonth(1));
        args.putSerializable(KEY_MIN_DAY, minDay);
        args.putSerializable(KEY_MAX_DAY, maxDay);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstDayOfMonth = (LocalDate) getArguments().getSerializable(KEY_FIRST_DAY_OF_MONTH);
        minDay = (Integer) getArguments().getSerializable(KEY_MIN_DAY);
        maxDay = (Integer) getArguments().getSerializable(KEY_MAX_DAY);

        retrieveCurrentDate();
        if (dateState == null) {
            if (savedInstanceState == null)
                throw new RuntimeException("there is no way to retrieve the current date");
            dateState = new CalendarFragment.DateState();
            dateState.copyFromBundle(savedInstanceState, KEY_DATE_STATE_PREFIX, getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_calendar_month, container, false);
        initializeViews(inflater, contentView);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(LayoutInflater inflater, View contentView) {
        /*GridLayout parent = contentView.findViewById(R.id.fragment_calendar_month_grid);
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 7; j++) {
                TextView textView = (TextView) inflater.inflate(R.layout.partial_calendar_month_cell, parent, false);
                PersianTypeFace.setTypefaceDefault(getResources().getAssets(), textView);

                int day = i * 7 + j;
                LocalDate modifiedDate = firstDayOfMonth.plusDays(
                        day - (firstDayOfMonth.getDayOfWeek() + 1) % 7);
                String dayText = PersianDateTimeFormat.forPattern("d", getResources()).print(modifiedDate);
                textView.setText(dayText);
                textView.setTag(modifiedDate);
                textView.setOnClickListener(calendarCellClickListener);


                if (modifiedDate.getMonthOfYear() == firstDayOfMonth.getMonthOfYear()) {
                    if (modifiedDate.getDayOfWeek() == 5 || Holidays.isPersianHoliday(modifiedDate))
                        textView.setTextColor(0xddff0000);
                    else
                        textView.setTextColor(0xdd000000);
                }
                else
                {
                    if (modifiedDate.getDayOfWeek() == 5 || Holidays.isPersianHoliday(modifiedDate))
                        textView.setTextColor(0x44ff0000);
                    else
                        textView.setTextColor(0x44000000);
                }


                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i + 1, 1, 1);
                params.columnSpec = GridLayout.spec(6 - j,  1, 1);
                parent.addView(textView, params);

                if (i == 0)
                {
                    TextView title = (TextView) inflater.inflate(R.layout.partial_calendar_month_cell, parent, false);
                    title.setText(PersianDateTimeFormat.forPattern("E", getResources()).print(modifiedDate));
                    PersianTypeFace.setTypefaceDefault(getResources().getAssets(), title);

                    if (modifiedDate.getDayOfWeek() == 5)
                        title.setTextColor(0xddff0000);
                    else
                        title.setTextColor(0xdd008aff);

                    title.setClickable(false);
                    params = new GridLayout.LayoutParams();
                    params.rowSpec = GridLayout.spec(0, 1, 1);
                    params.columnSpec = GridLayout.spec(6 - j, 1, 1);
                    parent.addView(title, params);
                }
            }*/

        LinearLayout parent = contentView.findViewById(R.id.fragment_calendar_month_layout);
        for (int i = -1; i < 6; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
            LinearLayout row = new LinearLayout(getContext());
            row.setLayoutParams(params);
            row.setOrientation(LinearLayout.HORIZONTAL);
            parent.addView(row);


            for (int j = 0; j <= 6; j++) {
                int day = i * 7 + j;
                int plusDays = day - (firstDayOfMonth.getDayOfWeek() + 1) % 7;
                LocalDate modifiedDate = firstDayOfMonth.plusDays(plusDays);

                if (i == -1) {
                    TextView title = (TextView) inflater.inflate(R.layout.partial_calendar_month_cell, parent, false);
                    title.setText(DateTimeFormat.forPattern("E", getResources()).print(modifiedDate));
                    TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), title);

                    if (modifiedDate.getDayOfWeek() == 5)
                        title.setTextColor(getResources().getColor(R.color.colorCalendarHoliday));
                    else
                        title.setTextColor(getResources().getColor(R.color.colorCalendarTitles));

                    title.setClickable(false);
                    row.addView(title);
                } else {
                    TextView textView = (TextView) inflater.inflate(R.layout.partial_calendar_month_cell, parent, false);
                    TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), textView);
                    String dayText = DateTimeFormat.forPattern("d", getResources()).print(modifiedDate);
                    textView.setText(dayText);
                    textView.setTag(modifiedDate);

                    if (minDay != null && minDay - 1 > plusDays || maxDay != null && maxDay - 1 < plusDays) {
                        textView.setClickable(false);
                        textView.setAlpha(0.3f);
                    } else {
                        textView.setOnClickListener(calendarCellClickListener);
                        textView.setOnLongClickListener(calendarCellLongClickListener);
                    }

                    row.addView(textView);
                }
            }
        }
    }

    private void tryUpdateViews() {
        View contentView = getView();
        if (contentView != null)
            updateViews(contentView);
    }

    private void updateViews(View contentView) {
        LinearLayout parent = contentView.findViewById(R.id.fragment_calendar_month_layout);
        for (int i = 1; i < parent.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) parent.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView cell = (TextView) row.getChildAt(j);
                LocalDate cellDate = (LocalDate) cell.getTag();
                if (!dateState.isPeriod && dateState.currentDate.equals(cellDate)) {
                    cell.setTextColor(getResources().getColor(R.color.colorCalendarSelected));
                } else if (dateState.isPeriod && dateState.compareEnabled &&
                        !dateState.from.isAfter(cellDate) && !dateState.to.isBefore(cellDate) &&
                        !dateState.fromCompare.isAfter(cellDate) && !dateState.toCompare.isBefore(cellDate)) {
                    cell.setTextColor(getResources().getColor(R.color.colorCalendarBothSelected));
                } else if (dateState.isPeriod &&
                        !dateState.from.isAfter(cellDate) && !dateState.to.isBefore(cellDate)) {
                    cell.setTextColor(getResources().getColor(R.color.colorCalendarSelected));
                } else if (dateState.isPeriod && dateState.compareEnabled &&
                        !dateState.fromCompare.isAfter(cellDate) && !dateState.toCompare.isBefore(cellDate)) {
                    cell.setTextColor(getResources().getColor(R.color.colorCalendarCompareSelected));
                } else if (cellDate.equals(LocalDate.now(PersianChronology.getInstance()))) {
                    cell.setTextColor(getResources().getColor(R.color.colorCalendarCurrent));
                } else if (cellDate.getMonthOfYear() == firstDayOfMonth.getMonthOfYear()) {
                    if (Holidays.isPersianHolidayOrWeekend(cellDate))
                        cell.setTextColor(getResources().getColor(R.color.colorCalendarHoliday));
                    else
                        cell.setTextColor(getResources().getColor(R.color.colorCalendarOrdinary));
                } else {
                    if (Holidays.isPersianHolidayOrWeekend(cellDate))
                        cell.setTextColor(getResources().getColor(R.color.colorCalendarHolidayLight));
                    else
                        cell.setTextColor(getResources().getColor(R.color.colorCalendarOrdinaryLight));
                }
            }
        }
    }

    private void onCellSelected(View cellView) {
        LocalDate selectedDate = (LocalDate) cellView.getTag();
        if (dateState.isPeriod) {
            Interval oldPeriod = IntervalUtils.toInterval(dateState.from, dateState.to);
            boolean dateChanged = false;
            if (selectedDate.isAfter(dateState.to)) {
                dateState.to = selectedDate;
                dateChanged = true;
            } else if (selectedDate.equals(dateState.to)) {
                dateState.from = selectedDate;
                dateChanged = true;
            } else if (selectedDate.isBefore(dateState.from)) {
                dateState.from = selectedDate;
                dateChanged = true;
            } else if (selectedDate.equals(dateState.from)) {
                dateState.to = selectedDate;
                dateChanged = true;
            } else {
                PopupMenu menu = new PopupMenu(getContext(), cellView, Gravity.START);
                menu.getMenu().add(0, 0, 0, R.string.universal_from_here);
                menu.getMenu().add(0, 1, 1, R.string.universal_to_here);
                menu.setOnMenuItemClickListener(new OnCellMenuItemClickListener(selectedDate));
                menu.show();
            }
            if (dateChanged) {
                if (dateState.hasCompare) {
                    Interval newPeriod = IntervalUtils.toInterval(dateState.from, dateState.to);
                    Interval oldCompare = IntervalUtils.toInterval(dateState.fromCompare, dateState.toCompare);
                    Interval newCompare = CalendarFragment.makeNewCompare(oldPeriod, newPeriod, oldCompare);
                    dateState.fromCompare = newCompare.getStart().toLocalDate();
                    dateState.toCompare = newCompare.getEnd().toLocalDate();
                }
                onDateSelected();
            }
        } else {
            dateState.currentDate = selectedDate;
            onDateSelected();
        }
    }

    private boolean onCellLongSelected(View cellView) {
        LocalDate selectedDate = (LocalDate) cellView.getTag();
        if (dateState.isPeriod && dateState.hasCompare && dateState.compareEnabled) {
            boolean compareDateChanged = false;
            if (selectedDate.isAfter(dateState.toCompare)) {
                dateState.toCompare = selectedDate;
                compareDateChanged = true;
            } else if (selectedDate.equals(dateState.toCompare)) {
                dateState.fromCompare = selectedDate;
                compareDateChanged = true;
            } else if (selectedDate.isBefore(dateState.fromCompare)) {
                dateState.fromCompare = selectedDate;
                compareDateChanged = true;
            } else if (selectedDate.equals(dateState.fromCompare)) {
                dateState.toCompare = selectedDate;
                compareDateChanged = true;
            } else {
                PopupMenu menu = new PopupMenu(getContext(), cellView, Gravity.START);
                menu.getMenu().add(0, 0, 0, R.string.universal_from_here);
                menu.getMenu().add(0, 1, 1, R.string.universal_to_here);
                menu.setOnMenuItemClickListener(new OnCellLongMenuItemClickListener(selectedDate));
                menu.show();
            }
            if (compareDateChanged) {
                onDateSelected();
            }
            return true;
        } else
            return false;
    }

    private boolean onDateMenuFromSelected(LocalDate selectedDate) {
        Interval oldPeriod = IntervalUtils.toInterval(dateState.from, dateState.to);
        dateState.from = selectedDate;
        if (dateState.hasCompare) {
            Interval newPeriod = IntervalUtils.toInterval(dateState.from, dateState.to);
            Interval oldCompare = IntervalUtils.toInterval(dateState.fromCompare, dateState.toCompare);
            Interval newCompare = CalendarFragment.makeNewCompare(oldPeriod, newPeriod, oldCompare);
            dateState.fromCompare = newCompare.getStart().toLocalDate();
            dateState.toCompare = newCompare.getEnd().toLocalDate();
        }
        onDateSelected();
        return true;
    }

    private boolean onDateMenuToSelected(LocalDate selectedDate) {
        Interval oldPeriod = IntervalUtils.toInterval(dateState.from, dateState.to);
        dateState.to = selectedDate;
        if (dateState.hasCompare) {
            Interval newPeriod = IntervalUtils.toInterval(dateState.from, dateState.to);
            Interval oldCompare = IntervalUtils.toInterval(dateState.fromCompare, dateState.toCompare);
            Interval newCompare = CalendarFragment.makeNewCompare(oldPeriod, newPeriod, oldCompare);
            dateState.fromCompare = newCompare.getStart().toLocalDate();
            dateState.toCompare = newCompare.getEnd().toLocalDate();
        }
        onDateSelected();
        return true;
    }

    private boolean onDateMenuFromLongSelected(LocalDate selectedDate) {
        dateState.fromCompare = selectedDate;
        onDateSelected();
        return true;
    }

    private boolean onDateMenuToLongSelected(LocalDate selectedDate) {
        dateState.toCompare = selectedDate;
        onDateSelected();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        dateState.addToBundle(outState, KEY_DATE_STATE_PREFIX);
    }

    // ==================================== Communication Parent =====================================
    public void changeDateState(CalendarFragment.DateState dateState) {
        this.dateState = dateState;
        tryUpdateViews();
    }

    private void onDateSelected() {
        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof OnFragmentInteractionListener)
            ((OnFragmentInteractionListener) parent).onDateSelected(dateState);
        else {
            Activity parentActivity = getActivity();
            if (parentActivity != null && parentActivity instanceof OnFragmentInteractionListener)
                ((OnFragmentInteractionListener) parentActivity).onDateSelected(dateState);
            else
                throw new RuntimeException("parent has not implemented OnFragmentInteractionListener");
        }
    }

    private void retrieveCurrentDate() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            dateState = ((OnFragmentInteractionListener) getParentFragment()).getDateState();
        else if (getActivity() instanceof OnFragmentInteractionListener)
            dateState = ((OnFragmentInteractionListener) getActivity()).getDateState();
        else
            throw new RuntimeException("parent has not implemented OnFragmentInteractionListener");
    }

    public interface OnFragmentInteractionListener {
        public void onDateSelected(CalendarFragment.DateState dateState);

        public CalendarFragment.DateState getDateState();
    }

    //======================================== Listeners ===========================================

    private class OnCalendarCellClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            onCellSelected(view);
        }
    }

    private class OnCalendarCellLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            return onCellLongSelected(v);
        }
    }

    private class OnCellMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        LocalDate selectedDate;

        public OnCellMenuItemClickListener(LocalDate selectedDate) {
            this.selectedDate = selectedDate;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == 0)
                return onDateMenuFromSelected(selectedDate);
            else if (item.getItemId() == 1)
                return onDateMenuToSelected(selectedDate);
            else
                throw new RuntimeException("given item is not recognized");
        }
    }

    private class OnCellLongMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        LocalDate selectedDate;

        public OnCellLongMenuItemClickListener(LocalDate selectedDate) {
            this.selectedDate = selectedDate;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == 0)
                return onDateMenuFromLongSelected(selectedDate);
            else if (item.getItemId() == 1)
                return onDateMenuToLongSelected(selectedDate);
            else
                throw new RuntimeException("given item is not recognized");
        }
    }
}
