package com.diplinkblaze.spacednote.universal.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.universal.util.ListData;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.chrono.PersianChronology;
import util.datetime.format.DateTimeFormat;
import util.datetime.format.DateTimeFormatter;

import util.Flags;
import util.TypeFaceUtils;


public class TabListFragment extends Fragment implements
        ListFragment.OnFragmentInteractionListener, ContentUpdateListener {
    private static final int MODE_FROM_TREE = 0;
    private static final int MODE_TIMELY = 1;

    private static final String KEY_MODE = "mode";
    private static final String KEY_TAG = "tag";

    //=========================  Timely Stuff ==================
    private static final String TAG_FUTURE = "tagFuture";

    private static final String KEY_PERIOD = "period";

    public static final int PERIOD_DAILY = 0;
    public static final int PERIOD_WEEKLY = 1;
    public static final int PERIOD_MONTHLY = 2;
    public static final int PERIOD_YEARLY = 3;

    private int periodType = PERIOD_MONTHLY;

    //======================== From Tree Stuff =================
    private ListData fromTreeData;

    //==========================================================
    private boolean isLayoutLoaded = false;
    private int mode;
    private Adapter mAdapter;

    public TabListFragment() {

    }

    public static TabListFragment newInstanceFromTree(String tag) {
        TabListFragment fragment = new TabListFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_MODE, MODE_FROM_TREE);
        args.putString(KEY_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    public static TabListFragment newInstanceTimely(int periodType, String tag) {
        TabListFragment fragment = new TabListFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_MODE, MODE_TIMELY);
        args.putInt(KEY_PERIOD, periodType);
        args.putString(KEY_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLayoutLoaded = false;
        mode = getArguments().getInt(KEY_MODE);
        if (mode == MODE_TIMELY) {
            periodType = getArguments().getInt(KEY_PERIOD);
        } else if (mode == MODE_FROM_TREE) {
            fromTreeData = retrieveFromTreeListData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isLayoutLoaded = false;
        View contentView = inflater.inflate(R.layout.fragment_universal_timely_list, container, false);
        initializeViews(contentView);
        updateViews(contentView);
        isLayoutLoaded = true;
        ContentUpdateUtil.updateContentChildren(this);
        return contentView;
    }

    private void initializeViews(View contentView) {
        ViewPager pager = contentView.findViewById(R.id.fragment_universal_timely_list_pager);
        mAdapter = new Adapter(getChildFragmentManager());
        pager.setAdapter(mAdapter);
        if (mode == MODE_TIMELY) {
            pager.setCurrentItem(1);
        }

        TabLayout tabLayout = contentView.findViewById(R.id.fragment_universal_timely_list_tab_layout);
        tabLayout.setupWithViewPager(pager);
        TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), tabLayout);
    }

    private void updateViews(View contentView) {

    }

    //========================================== Adapter ===========================================
    private class Adapter extends FragmentStatePagerAdapter {
        private static final int countTimely = 30;

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            String tag;
            if (mode == MODE_TIMELY) {
                if (position == 0) {
                    tag = TAG_FUTURE;
                } else {
                    LocalDate startDate = getStartDateByPosition(position);
                    tag = tagForPeriod(startDate);
                }
            } else if (mode == MODE_FROM_TREE) {
                tag = String.valueOf(fromTreeData.getItems().getChildAt(position).getEntityId());
            } else
                throw new RuntimeException("Mode is not recognized");
            //Fragment fragment = UniversalListSimpleFragment.newInstanceTimely(tag);
            Fragment fragment = ListFragment.newInstance(tag);
            return fragment;
        }

        @Override
        public int getCount() {
            if (mode == MODE_TIMELY) {
                return countTimely;
            } else if (mode == MODE_FROM_TREE) {
                return fromTreeData.getItems().getChildrenCountImmediate();
            } else
                throw new RuntimeException("Mode is not recognized");
        }

        public LocalDate getStartDateByPosition(int position) {
            LocalDate now = startPeriodNow();
            return plusPeriod(now, -position + 1);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (mode == MODE_TIMELY) {
                if (position == 0)
                    return getResources().getString(R.string.universal_future);
                else
                    return getPeriodTitle(getStartDateByPosition(position));
            } else if (mode == MODE_FROM_TREE) {
                return fromTreeData.getItems().getChildAt(position).getTitle(getResources());
            } else
                throw new RuntimeException("Mode is not recognized");
        }
    }

    //======================================= Period Factory =========================================
    private LocalDate plusPeriod(LocalDate date, int count) {
        if (periodType == PERIOD_DAILY) {
            return date.plusDays(count);
        } else if (periodType == PERIOD_WEEKLY) {
            return date.plusWeeks(count);
        } else if (periodType == PERIOD_MONTHLY) {
            return date.plusMonths(count);
        } else if (periodType == PERIOD_YEARLY) {
            return date.plusYears(count);
        } else
            throw new RuntimeException("periodType is invalid");
    }

    private LocalDate startPeriodNow() {
        LocalDate now = new LocalDate(PersianChronology.getInstance());
        if (periodType == PERIOD_DAILY) {
            return now;
        } else if (periodType == PERIOD_WEEKLY) {
            return now.withDayOfWeek(1);
        } else if (periodType == PERIOD_MONTHLY) {
            return now.withDayOfMonth(1);
        } else if (periodType == PERIOD_YEARLY) {
            return now.withDayOfYear(1);
        } else
            throw new RuntimeException("periodType is invalid");
    }

    private String getPeriodTitle(LocalDate startDate) {
        DateTimeFormatter formatter;
        if (periodType == PERIOD_DAILY) {
            formatter = DateTimeFormat.mediumDate(getResources());
        } else if (periodType == PERIOD_WEEKLY) {
            formatter = DateTimeFormat.forPattern("ww", getResources());
        } else if (periodType == PERIOD_MONTHLY) {
            formatter = DateTimeFormat.forPattern("yyyy MMMM", getResources());
        } else if (periodType == PERIOD_YEARLY) {
            formatter = DateTimeFormat.forPattern("yyyy", getResources());
        } else
            throw new RuntimeException("periodType is invalid");
        return formatter.print(startDate);
    }

    //========================================= Tag Factory ==========================================
    private String tagForPeriod(LocalDate startDate) {
        return startDate.getYear() + " " + startDate.getMonthOfYear() + " " + startDate.getDayOfMonth();
    }

    private LocalDate periodFromTag(String tag) {
        String[] dateSplit = tag.split(" ");
        LocalDate date = new LocalDate(Integer.parseInt(dateSplit[0]),
                Integer.parseInt(dateSplit[1]),
                Integer.parseInt(dateSplit[2]), PersianChronology.getInstance());
        return date;
    }

    //=================================== Communication Children ======================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        if (isLayoutLoaded) {
            if (mode == MODE_TIMELY) {
                if (TAG_FUTURE.equals(tag)) {
                    DateTime formDateTime = plusPeriod(startPeriodNow(), 1).toDateTime(
                            new LocalTime(0, 0, 0, 0, PersianChronology.getInstance()));

                    ListData data = retrieveTimelyListData(formDateTime, null);
                    if (Flags.hasFlags(data.getModeFlags(), ListData.MODE_FLAG_SWAPPABLE))
                        throw new RuntimeException("For now, swappable is not supported");
                    if (Flags.hasFlags(data.getModeFlags(), ListData.MODE_FLAG_MARKABLE))
                        throw new RuntimeException("For now, markable is not supported");
                    return data;
                } else {
                    LocalDate startDay = periodFromTag(tag);
                    LocalDate endDayPlusOne = plusPeriod(startDay, 1);
                    LocalTime midnight = new LocalTime(0, 0, 0, 0, PersianChronology.getInstance());
                    DateTime formDateTime = startDay.toDateTime(midnight);
                    DateTime toDateTime = endDayPlusOne.toDateTime(midnight).minusMillis(1);
                    ListData data = retrieveTimelyListData(formDateTime, toDateTime);
                    if (Flags.hasFlags(data.getModeFlags(), ListData.MODE_FLAG_SWAPPABLE))
                        throw new RuntimeException("For now, swappable is not supported");
                    if (Flags.hasFlags(data.getModeFlags(), ListData.MODE_FLAG_MARKABLE))
                        throw new RuntimeException("For now, markable is not supported");
                    return data;
                }
            } else if (mode == MODE_FROM_TREE) {
                long entityId = Long.parseLong(tag);
                return new ListData(fromTreeData.findItemByEntityId(entityId), fromTreeData.getModeFlags());
            } else
                throw new RuntimeException("periodType is invalid");
        } else
            return null;
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {
        //TODO
    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {
        onTabListItemSelected(entity);
    }

    @Override
    public void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, String tag, Bundle identifier) {
        onTabListActionSelected(infoRow, view, tag);
    }

    @Override
    public void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo, String tag, Bundle identifier) {

    }

    //==================================== Communication Parent =======================================

    @Override
    public void updateContent() {
        if (mode == MODE_FROM_TREE) {
            fromTreeData = retrieveFromTreeListData();
        }
        ContentUpdateUtil.updateContentChildren(this);
    }

    private ListData retrieveTimelyListData(DateTime startDateTime, DateTime endDateTime) {
        String tag = getArguments().getString(KEY_TAG);
        return ((OnFragmentInteractionListenerTimely) getListener())
                .retrieveTimelyListData(startDateTime, endDateTime, periodType, tag);
    }

    private ListData retrieveFromTreeListData() {
        String tag = getArguments().getString(KEY_TAG);
        return  ((OnFragmentInteractionListenerFromTree) getListener())
                .retrieveFromTreeListData(getContext(), tag);
    }


    private void onTabListItemSelected(ListData.Entity entity) {
        String tag = getArguments().getString(KEY_TAG);
        getListener().onTabListItemSelected(entity, tag);
    }

    private void onTabListActionSelected(ListData.InfoRow infoRow, View v, String childTag) {
        String parentTag = getArguments().getString(KEY_TAG);
        OnFragmentInteractionListener listener = getListener();
        if (mode == MODE_TIMELY) {
            if (TAG_FUTURE.equals(childTag)) {
                DateTime formDateTime = plusPeriod(startPeriodNow(), 1).toDateTime(
                        new LocalTime(0, 0, 0, 0, PersianChronology.getInstance()));
                ((OnFragmentInteractionListenerTimely) listener).onTabListActionSelected(
                        infoRow, v, formDateTime, null, parentTag
                );
            } else {
                LocalDate startDay = periodFromTag(childTag);
                LocalDate endDayPlusOne = plusPeriod(startDay, 1);
                LocalTime midnight = new LocalTime(0, 0, 0, 0, PersianChronology.getInstance());
                DateTime formDateTime = startDay.toDateTime(midnight);
                DateTime toDateTime = endDayPlusOne.toDateTime(midnight).minusMillis(1);
                ((OnFragmentInteractionListenerTimely) listener).onTabListActionSelected(
                        infoRow, v, formDateTime, toDateTime, parentTag
                );
            }
        } else if (mode == MODE_FROM_TREE) {
            long entityId = Long.parseLong(childTag);
            ((OnFragmentInteractionListenerFromTree) listener).onTabListActionSelected(entityId, parentTag);
        } else
            throw new RuntimeException("periodType is invalid");
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            return ((OnFragmentInteractionListener) getParentFragment());
        else if (getActivity() instanceof OnFragmentInteractionListener)
            return ((OnFragmentInteractionListener) getActivity());
        else
            throw new RuntimeException("Either parent fragment of activity must implement" +
                    "OnFragmentInteractionListener");
    }

    private interface OnFragmentInteractionListener {
        void onTabListItemSelected(ListData.Entity entity, String tag);
    }

    public interface OnFragmentInteractionListenerTimely extends OnFragmentInteractionListener{
        ListData retrieveTimelyListData(@Nullable DateTime startDateTime, @Nullable DateTime endDateTime, int periodType, String tag);
        void onTabListActionSelected(ListData.InfoRow infoRow, View v, @Nullable DateTime startDateTime, @Nullable DateTime endDateTime, String tag);
    }

    public interface OnFragmentInteractionListenerFromTree extends OnFragmentInteractionListener{
        ListData retrieveFromTreeListData(Context context, String tag);
        void onTabListActionSelected(long rootEntityId, String tag);
    }
}
