package com.diplinkblaze.spacednote.universal.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;

import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import data.preference.ChronologyCatalog;
import util.datetime.format.DateTimeFormat;
import util.datetime.format.DateTimeFormatter;
import util.datetime.interval.IntervalUtils;

import java.util.List;

import util.Keyboard;
import util.TypeFaceUtils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * createBudgets an instance of this fragment.
 */
public class CalendarFragment extends DialogFragment implements CalendarMonthFragment.OnFragmentInteractionListener {
    private static final String KEY_MIN_DATE = "minDate";
    private static final String KEY_MAX_DATE = "maxDate";
    private static final String KEY_DATE_STATE_PREFIX = "dateStatePrefix";
    private static final String KEY_TAG = "tag";
    private static final String KEY_IDENTIFIER = "identifier";

    private OnViewCLickListener viewCLickListener = new OnViewCLickListener();
    private OnViewCheckListener viewCheckListener = new OnViewCheckListener();

    private PagerAdapter adapter;
    private LocalDate minDate;
    private LocalDate maxDate;
    private DateState dateState;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance(String tag, Bundle identifier, Context context) {
        return newInstance(null, null, null, tag, identifier, context);
    }

    public static CalendarFragment newInstance(@Nullable DateState initialDateState, String tag, Bundle identifier, Context context) {
        return newInstance(initialDateState, null, null, tag, identifier, context);
    }

    public static CalendarFragment newInstance(@Nullable DateState initialDateState, LocalDate minDate, String tag, Bundle identifier, Context context) {
        return newInstance(initialDateState, minDate, null, tag, identifier, context);
    }

    public static CalendarFragment newInstance(@Nullable DateState initialDateState, LocalDate minDate, LocalDate maxDate, String tag, Bundle identifier, Context context) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        if (minDate != null)
            args.putSerializable(KEY_MIN_DATE, minDate);
        if (maxDate != null)
            args.putSerializable(KEY_MAX_DATE, maxDate);
        if (initialDateState == null) {
            initialDateState = new DateState();
            initialDateState.currentDate = LocalDate.now(ChronologyCatalog.getCurrentChronology(context));
        }
        initialDateState.addToBundle(args, KEY_DATE_STATE_PREFIX);
        args.putString(KEY_TAG, tag);
        args.putBundle(KEY_IDENTIFIER, identifier);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        minDate = (LocalDate) getArguments().getSerializable(KEY_MIN_DATE);
        maxDate = (LocalDate) getArguments().getSerializable(KEY_MAX_DATE);
        if (savedInstanceState != null) {
            dateState = new DateState();
            dateState.copyFromBundle(savedInstanceState, KEY_DATE_STATE_PREFIX, getContext());
        } else {
            dateState = new DateState();
            dateState.copyFromBundle(getArguments(), KEY_DATE_STATE_PREFIX, getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_calendar, container, false);
        initializeViews(inflater, contentView);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(LayoutInflater inflater, View contentView) {
        ViewPager pager = contentView.findViewById(R.id.fragment_calendar_pager);
        adapter = new PagerAdapter(getChildFragmentManager(), minDate, maxDate);
        pager.setAdapter(adapter);
        if (dateState.isPeriod)
            pager.setCurrentItem(adapter.getPositionForDate(dateState.from));
        else
            pager.setCurrentItem(adapter.getPositionForDate(dateState.currentDate));
        pager.addOnPageChangeListener(new PagerPageChangedListener());

        TextView month = contentView.findViewById(R.id.fragment_calendar_month);
        TextView year = contentView.findViewById(R.id.fragment_calendar_year);
        TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), month);
        TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), year);

        EditText editYear = contentView.findViewById(R.id.fragment_calendar_edit_year);
        month.setOnClickListener(viewCLickListener);
        year.setOnClickListener(viewCLickListener);
        editYear.setOnEditorActionListener(new OnEditYearAction());

        //Period
        {
            if (dateState.isPeriod) {
                ViewStub periodStub = contentView.findViewById(R.id.fragment_calendar_period);
                View periodPartial = periodStub.inflate();
                TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), periodPartial);
                View periodText = periodPartial.findViewById(R.id.partial_calendar_period_text);
                View periodSave = periodPartial.findViewById(R.id.partial_calendar_period_save);
                periodText.setOnClickListener(viewCLickListener);
                periodSave.setOnClickListener(viewCLickListener);

                View periodCompare = periodPartial.findViewById(R.id.partial_calendar_period_compare_layout);
                if (dateState.hasCompare) {
                    View periodCompareText = periodPartial.findViewById(R.id.partial_calendar_period_compare_text);
                    CheckBox periodCompareCheck = periodPartial.findViewById(R.id.partial_calendar_period_compare_check);
                    periodCompareText.setOnClickListener(viewCLickListener);
                    periodCompareCheck.setOnCheckedChangeListener(viewCheckListener);
                    periodCompare.setVisibility(View.VISIBLE);
                } else {
                    periodCompare.setVisibility(View.GONE);
                }
            }
        }
    }

    private void updateViews(View contentView) {
        ViewPager pager = contentView.findViewById(R.id.fragment_calendar_pager);
        TextView month = contentView.findViewById(R.id.fragment_calendar_month);
        TextView year = contentView.findViewById(R.id.fragment_calendar_year);
        LocalDate firstDayOfMonth = adapter.getFirstDayOfMonthForPosition(pager.getCurrentItem());
        month.setText(DateTimeFormat.forPattern("MMM", getResources()).print(firstDayOfMonth));
        year.setText(DateTimeFormat.forPattern("yyyy", getResources()).print(firstDayOfMonth));

        if (dateState.isPeriod) {
            DateTimeFormatter formatter = DateTimeFormat.mediumDate(getResources());
            String periodText = IntervalUtils.makeTextForInterval(
                    new Interval(dateState.from.toDateTimeAtStartOfDay(), dateState.to.toDateTimeAtStartOfDay()),
                    getResources());
            TextView periodTextView = contentView.findViewById(R.id.partial_calendar_period_text);
            periodTextView.setText(periodText);
            if (dateState.hasCompare) {
                String compareText = IntervalUtils.makeTextForInterval(
                        new Interval(dateState.fromCompare.toDateTimeAtStartOfDay(), dateState.toCompare.toDateTimeAtStartOfDay()),
                        getResources());
                TextView compareTextView = contentView.findViewById(R.id.partial_calendar_period_compare_text);
                compareTextView.setText(compareText);
                CheckBox compareEnabled = contentView.findViewById(R.id.partial_calendar_period_compare_check);
                if (dateState.compareEnabled) {
                    compareEnabled.setChecked(true);
                    compareTextView.setEnabled(true);
                } else {
                    compareEnabled.setChecked(false);
                    compareTextView.setEnabled(false);
                }
            }
        }
    }

    private void editYear(View contentView) {

        //Edit Year:
        {
            TextView year = contentView.findViewById(R.id.fragment_calendar_year);
            EditText editYear = contentView.findViewById(R.id.fragment_calendar_edit_year);
            year.setVisibility(View.GONE);
            editYear.setVisibility(View.VISIBLE);
            editYear.setImeOptions(EditorInfo.IME_ACTION_DONE);
            editYear.requestFocus();
            Keyboard.show(getActivity(), this);
        }
    }

    private void editYearCommit(View contentView) {
        //commit year:
        {
            TextView year = contentView.findViewById(R.id.fragment_calendar_year);
            EditText editYear = contentView.findViewById(R.id.fragment_calendar_edit_year);

            try {
                ViewPager pager = contentView.findViewById(R.id.fragment_calendar_pager);

                int newYear = Integer.parseInt(editYear.getText().toString());
                LocalDate newDate = adapter.getFirstDayOfMonthForPosition(pager.getCurrentItem()).withYear(newYear);

                pager.setCurrentItem(adapter.getPositionForDate(newDate));
            } catch (NumberFormatException e) {
                //Nothing to do about it for now!
            }

            year.setVisibility(View.VISIBLE);
            editYear.setVisibility(View.GONE);
            updateViews(contentView);
            Keyboard.hide(getActivity(), this);
        }

    }

    private void editMonth() {
        View contentView = getView();
        if (contentView == null)
            return;

        DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("MMMM", getResources());
        View monthText = contentView.findViewById(R.id.fragment_calendar_month);
        PopupMenu menu = new PopupMenu(getContext(), monthText, Gravity.START);
        LocalDate sample = LocalDate.now(ChronologyCatalog.getCurrentChronology(getContext()));
        for (int i = 1; i <= 12; i++) {
            menu.getMenu().add(0, i, 0, monthFormatter.print(sample.withMonthOfYear(i)));
        }
        menu.setOnMenuItemClickListener(new OnMonthTextMenuItemClicked());
        menu.show();
    }

    private void onPeriodTextClicked() {
        View contentView = getView();
        if (contentView == null)
            return;

        View periodText = contentView.findViewById(R.id.partial_calendar_period_text);
        PopupMenu menu = new PopupMenu(getContext(), periodText, Gravity.START);
        menu.getMenuInflater().inflate(R.menu.calendar_period_menu, menu.getMenu());
        if (dateState.type == DateState.TYPE_PAST) {
            menu.getMenu().removeGroup(R.id.calendar_period_menu_group_future);
            menu.getMenu().removeGroup(R.id.calendar_period_menu_group_now);
        } else if (dateState.type == DateState.TYPE_FUTURE) {
            menu.getMenu().removeGroup(R.id.calendar_period_menu_group_past);
            menu.getMenu().removeGroup(R.id.calendar_period_menu_group_now);
        } else if (dateState.type == DateState.TYPE_NOT_FUTURE) {
            menu.getMenu().removeGroup(R.id.calendar_period_menu_group_future);
        } else if (dateState.type == DateState.TYPE_NOT_PAST) {
            menu.getMenu().removeGroup(R.id.calendar_period_menu_group_past);
        }
        menu.setOnMenuItemClickListener(new OnPeriodTextMenuItemClicked());
        menu.show();
    }

    private boolean onPeriodMenuItemClicked(int id) {
        if (dateState.isPeriod) {
            int interval;
            if (id == R.id.calendar_period_menu_last_day) {
                interval = IntervalUtils.INTERVAL_LAST_DAY;
            } else if (id == R.id.calendar_period_menu_last_7_days) {
                interval = IntervalUtils.INTERVAL_LAST_7_DAYS;
            } else if (id == R.id.calendar_period_menu_last_week) {
                interval = IntervalUtils.INTERVAL_LAST_WEEK;
            } else if (id == R.id.calendar_period_menu_last_30_days) {
                interval = IntervalUtils.INTERVAL_LAST_30_DAYS;
            } else if (id == R.id.calendar_period_menu_last_month) {
                interval = IntervalUtils.INTERVAL_LAST_MONTH;
            } else if (id == R.id.calendar_period_menu_last_year) {
                interval = IntervalUtils.INTERVAL_LAST_YEAR;
            } else if (id == R.id.calendar_period_menu_next_day) {
                interval = IntervalUtils.INTERVAL_NEXT_DAY;
            } else if (id == R.id.calendar_period_menu_next_7_days) {
                interval = IntervalUtils.INTERVAL_NEXT_7_DAYS;
            } else if (id == R.id.calendar_period_menu_next_week) {
                interval = IntervalUtils.INTERVAL_NEXT_WEEK;
            } else if (id == R.id.calendar_period_menu_next_30_days) {
                interval = IntervalUtils.INTERVAL_NEXT_30_DAYS;
            } else if (id == R.id.calendar_period_menu_next_month) {
                interval = IntervalUtils.INTERVAL_NEXT_MONTH;
            } else if (id == R.id.calendar_period_menu_next_year) {
                interval = IntervalUtils.INTERVAL_NEXT_YEAR;
            } else if (id == R.id.calendar_period_menu_this_day) {
                interval = IntervalUtils.INTERVAL_THIS_DAY;
            } else if (id == R.id.calendar_period_menu_this_week) {
                interval = IntervalUtils.INTERVAL_THIS_WEEK;
            } else if (id == R.id.calendar_period_menu_this_month) {
                interval = IntervalUtils.INTERVAL_THIS_MONTH;
            } else if (id == R.id.calendar_period_menu_this_year) {
                interval = IntervalUtils.INTERVAL_THIS_YEAR;
            } else
                throw new RuntimeException("given id is invalid");
            Interval oldPeriod = IntervalUtils.toInterval(dateState.from, dateState.to);
            Interval newPeriod = IntervalUtils.makeInterval(interval);
            dateState.from = new LocalDate(newPeriod.getStartMillis(), ChronologyCatalog.getCurrentChronology(getContext()));
            dateState.to = new LocalDate(newPeriod.getEndMillis(), ChronologyCatalog.getCurrentChronology(getContext()));
            if (dateState.hasCompare) {
                Interval oldCompare = IntervalUtils.toInterval(dateState.fromCompare, dateState.toCompare);
                Interval newCompare = makeNewCompare(oldPeriod, newPeriod, oldCompare);
                dateState.fromCompare = newCompare.getStart().toLocalDate();
                dateState.toCompare = newCompare.getEnd().toLocalDate();
            }
            onDateChange();
            View contentView = getView();
            if (contentView != null) {
                ViewPager pager = contentView.findViewById(R.id.fragment_calendar_pager);
                pager.setCurrentItem(adapter.getPositionForDate(dateState.from));
                updateViews(contentView);
            }
            return true;
        }
        return false;
    }

    private void onCompareTextClicked() {
        View contentView = getView();
        if (contentView == null)
            return;

        View compareText = contentView.findViewById(R.id.partial_calendar_period_compare_text);
        PopupMenu menu = new PopupMenu(getContext(), compareText, Gravity.START);
        menu.getMenuInflater().inflate(R.menu.calendar_compare_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new OnCompareTextMenuItemClicked());
        menu.show();
    }

    private void onCompareCheckClicked(boolean isChecked) {
        View contentView = getView();
        if (contentView == null)
            return;
        dateState.compareEnabled = isChecked;
        onDateChange();
        updateViews(contentView);
    }

    private boolean onCompareMenuItemClicked(int id) {
        if (dateState.isPeriod) {
            if (id == R.id.calendar_compare_menu_previous_period) {
                Interval previous = IntervalUtils.getPreviousInterval(dateState.from, dateState.to);
                dateState.fromCompare = previous.getStart().toLocalDate();
                dateState.toCompare = previous.getEnd().toLocalDate();
            } else if (id == R.id.calendar_compare_menu_previous_year) {
                dateState.fromCompare = dateState.from.minusYears(1);
                dateState.toCompare = dateState.to.minusYears(1);
            } else
                throw new RuntimeException("given id is invalid");
            onDateChange();
            View contentView = getView();
            if (contentView != null) {
                updateViews(contentView);
            }
            return true;
        }
        return false;
    }

    private void onPeriodSaveClicked() {
        dateSelected();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        dateState.addToBundle(outState, KEY_DATE_STATE_PREFIX);
    }

    //================================ Listener Classes ========================================
    private class OnViewCLickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.fragment_calendar_year)
                editYear(getView());
            else if (view.getId() == R.id.fragment_calendar_month) {
                editMonth();
            } else if (view.getId() == R.id.partial_calendar_period_text) {
                onPeriodTextClicked();
            } else if (view.getId() == R.id.partial_calendar_period_save) {
                onPeriodSaveClicked();
            } else if (view.getId() == R.id.partial_calendar_period_compare_text) {
                onCompareTextClicked();
            }
        }
    }

    private class OnViewCheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.partial_calendar_period_compare_check)
                onCompareCheckClicked(isChecked);
        }
    }

    private class OnEditYearAction implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE)
                editYearCommit(getView());
            return false;
        }
    }

    private class OnPeriodTextMenuItemClicked implements PopupMenu.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return onPeriodMenuItemClicked(item.getItemId());
        }
    }

    private class OnCompareTextMenuItemClicked implements PopupMenu.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return onCompareMenuItemClicked(item.getItemId());
        }
    }

    private class OnMonthTextMenuItemClicked implements PopupMenu.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int monthOfYear = item.getItemId();
            if (monthOfYear < 1 || monthOfYear > 12)
                throw new RuntimeException("item id is invalid");
            View contentView = getView();
            if (contentView != null) {
                ViewPager pager = contentView.findViewById(R.id.fragment_calendar_pager);
                LocalDate current = adapter.getFirstDayOfMonthForPosition(pager.getCurrentItem());
                pager.setCurrentItem(adapter.getPositionForDate(current.withMonthOfYear(monthOfYear)));
                return true;
            }
            return false;
        }
    }

    private class PagerPageChangedListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (getView() != null)
                updateViews(getView());
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    // =================================== Communication Children ====================================
    @Override
    public void onDateSelected(DateState dateState) {
        this.dateState = dateState;
        onDateChange();
        View contentView = getView();
        if (contentView != null) {
            updateViews(contentView);
        }
    }

    private void onDateChange() {
        if (!dateState.isPeriod) {
            dateSelected();
        }
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof CalendarMonthFragment)
                    ((CalendarMonthFragment) fragment).changeDateState(dateState);
            }
        }
    }

    @Override
    public DateState getDateState() {
        return dateState;
    }

    // ==================================== Communication Parent =====================================
    private void dateSelected() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof OnFragmentInteractionListener)
            ((OnFragmentInteractionListener) parent).onDateSelected(dateState, tag, identifier);
        else {
            Activity parentActivity = getActivity();
            if (parentActivity != null && parentActivity instanceof OnFragmentInteractionListener)
                ((OnFragmentInteractionListener) parentActivity).onDateSelected(dateState, tag, identifier);
            else
                throw new RuntimeException("No parent has implemented OnFragmentInteractionListener");
        }
    }

    public interface OnFragmentInteractionListener {
        public void onDateSelected(DateState dateState, String tag, Bundle identifier);
    }


    //===================================== Classes ============================================
    private class PagerAdapter extends FragmentStatePagerAdapter {
        private static final int pagerSize = 100000;
        private int startOffset = 0;
        private int maxLimit = pagerSize;

        public PagerAdapter(FragmentManager fm, @Nullable LocalDate minDate, @Nullable LocalDate maxDate) {
            super(fm);
            if (minDate != null)
                startOffset = minDate.getYear() * 12 + minDate.getMonthOfYear() - 1;
            if (maxDate != null)
                maxLimit = maxDate.getYear() * 12 + maxDate.getMonthOfYear() - 1;
        }

        @Override
        public Fragment getItem(int position) {
            LocalDate date = getFirstDayOfMonthForPosition(position);
            Integer minDay = null;
            Integer maxDay = null;
            if (minDate != null)
                minDay = Days.daysBetween(date, minDate).getDays() + 1;
            if (maxDate != null)
                maxDay = Days.daysBetween(date, maxDate).getDays() + 1;

            return CalendarMonthFragment.newInstance(getFirstDayOfMonthForPosition(position), minDay, maxDay);
        }

        @Override
        public int getCount() {
            return maxLimit - startOffset + 1;
        }

        public LocalDate getFirstDayOfMonthForPosition(int position) {
            position = getCount() - 1 - position;
            position = startOffset + position;
            int year = position / 12;
            int month = (position % 12) + 1;
            LocalDate localDate = new LocalDate(year, month, 1, ChronologyCatalog.getCurrentChronology(getContext()));
            return localDate;
        }

        public int getPositionForDate(LocalDate date) {
            int position = date.getYear() * 12 + date.getMonthOfYear() - 1;
            position = position - startOffset;
            position = getCount() - 1 - position;
            return position;
        }
    }


    public static class DateState {
        private static final String KEY_CURRENT_DATE = "currentDate";
        private static final String KEY_IS_PERIOD = "isPeriod";
        private static final String KEY_FROM = "from";
        private static final String KEY_TO = "to";
        private static final String KEY_HAS_COMPARE = "hasCompare";
        private static final String KEY_COMPARE_ENABLED = "compareEnabled";
        private static final String KEY_FROM_COMPARE = "fromCompare";
        private static final String KEY_TO_COMPARE = "toCompare";
        private static final String KEY_TYPE = "type";

        public static final int TYPE_PAST = 1;
        public static final int TYPE_FUTURE = 2;
        public static final int TYPE_ALL = 3;
        public static final int TYPE_NOT_PAST = 4;
        public static final int TYPE_NOT_FUTURE = 5;

        public LocalDate currentDate;
        public boolean isPeriod;
        public LocalDate from;
        public LocalDate to;
        public boolean hasCompare;
        public boolean compareEnabled;
        public LocalDate fromCompare;
        public LocalDate toCompare;
        public int type;

        public void addToBundle(Bundle bundle, String prefix) {
            if (currentDate != null)
                bundle.putLong(prefix + KEY_CURRENT_DATE, currentDate.toDateTimeAtStartOfDay().getMillis());
            bundle.putBoolean(prefix + KEY_IS_PERIOD, isPeriod);
            bundle.putBoolean(prefix + KEY_HAS_COMPARE, hasCompare);
            bundle.putBoolean(prefix + KEY_COMPARE_ENABLED, compareEnabled);
            if (from != null)
                bundle.putLong(prefix + KEY_FROM, from.toDateTimeAtStartOfDay().getMillis());
            if (to != null)
                bundle.putLong(prefix + KEY_TO, to.toDateTimeAtStartOfDay().getMillis());
            if (fromCompare != null)
                bundle.putLong(prefix + KEY_FROM_COMPARE, fromCompare.toDateTimeAtStartOfDay().getMillis());
            if (toCompare != null)
                bundle.putLong(prefix + KEY_TO_COMPARE, toCompare.toDateTimeAtStartOfDay().getMillis());
            bundle.putInt(KEY_TYPE, type);
        }

        public void copyFromBundle(Bundle bundle, String prefix, Context context) {
            if (bundle.containsKey(prefix + KEY_CURRENT_DATE))
                currentDate = new LocalDate(bundle.getLong(prefix + KEY_CURRENT_DATE), ChronologyCatalog.getCurrentChronology(context));
            isPeriod = bundle.getBoolean(prefix + KEY_IS_PERIOD);
            hasCompare = bundle.getBoolean(prefix + KEY_HAS_COMPARE);
            compareEnabled = bundle.getBoolean(prefix + KEY_COMPARE_ENABLED);
            if (bundle.containsKey(prefix + KEY_FROM))
                from = new LocalDate(bundle.getLong(prefix + KEY_FROM), ChronologyCatalog.getCurrentChronology(context));
            if (bundle.containsKey(prefix + KEY_TO))
                to = new LocalDate(bundle.getLong(prefix + KEY_TO), ChronologyCatalog.getCurrentChronology(context));
            if (bundle.containsKey(prefix + KEY_FROM_COMPARE))
                fromCompare = new LocalDate(bundle.getLong(prefix + KEY_FROM_COMPARE), ChronologyCatalog.getCurrentChronology(context));
            if (bundle.containsKey(prefix + KEY_TO_COMPARE))
                toCompare = new LocalDate(bundle.getLong(prefix + KEY_TO_COMPARE), ChronologyCatalog.getCurrentChronology(context));
            type = bundle.getInt(KEY_TYPE);
        }
    }

    public static Interval makeNewCompare(Interval oldPeriod, Interval newPeriod, Interval oldCompare) {

        boolean isComparePrevious = IntervalUtils.equalLocalDates(
                IntervalUtils.getPreviousInterval(oldPeriod), oldCompare);
        boolean isCompareLastYear = IntervalUtils.equalLocalDates(
                new Interval(oldPeriod.getStart().minusYears(1), oldPeriod.getEnd().minusYears(1)),
                oldCompare
        );
        if (isComparePrevious)
            return IntervalUtils.getPreviousInterval(newPeriod);
        else if (isCompareLastYear)
            return new Interval(newPeriod.getStart().minusYears(1), newPeriod.getEnd().minusYears(1));
        else
            return oldCompare;
    }
}
