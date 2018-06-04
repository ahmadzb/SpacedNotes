package com.diplinkblaze.spacednote.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.contract.NewItemSupportListener;
import com.diplinkblaze.spacednote.universal.fragments.ListFragment;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import data.database.OpenHelper;
import data.model.schedule.Schedule;

public class ScheduleListFragment extends Fragment implements ListFragment.OnFragmentInteractionListener,
        NewItemSupportListener, ContentUpdateListener{

    public static final String TAG_LIST_FRAGMENT = "listFragment";

    private static final int ACTIVITY_REQUEST_AVE = 0;
    private static final int ACTIVITY_REQUEST_SCHEDULE = 1;

    public ScheduleListFragment() {
        // Required empty public constructor
    }

    public static ScheduleListFragment newInstance() {
        ScheduleListFragment fragment = new ScheduleListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        initializeViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT);
        if (fragment == null) {
            fragment = ListFragment.newInstance(TAG_LIST_FRAGMENT);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_schedule_list_frame, fragment, TAG_LIST_FRAGMENT);
            transaction.commit();
        }
    }

    public interface OnFragmentInteractionListener {

    }

    //================================== Content Update Listener ===================================
    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }

    //======================================= List Fragment ========================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        return ListUtil.Schedule.create(OpenHelper.getDatabase(context));
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {
        SQLiteDatabase database = OpenHelper.getDatabase(getContext());
        database.beginTransaction();
        ListUtil.Schedule.updatePositions(data, rootEntity, database, getContext());
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {
        ListUtil.Schedule.ScheduleEntity scheduleEntity = (ListUtil.Schedule.ScheduleEntity) entity;
        Schedule schedule = scheduleEntity.getSchedule();
        Intent intent = ScheduleOccurrenceActivity.getIntent(schedule, getContext());
        startActivityForResult(intent, ACTIVITY_REQUEST_SCHEDULE);
    }

    @Override
    public void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo, String tag, Bundle identifier) {

    }

    //====================================== New Item Support ======================================
    @Override
    public void newItem() {
        int request = ACTIVITY_REQUEST_AVE;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(ScheduleAveActivity.getIntent(null, getContext()), request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            //Ave
            {
                int request = ACTIVITY_REQUEST_AVE;
                if (getActivity() instanceof ActivityRequestHost) {
                    ActivityRequestHost host = (ActivityRequestHost) getActivity();
                    request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
                }
                if (requestCode == request) {
                    updateContent();
                }
            }
            //Type
            {
                int request = ACTIVITY_REQUEST_SCHEDULE;
                if (getActivity() instanceof ActivityRequestHost) {
                    ActivityRequestHost host = (ActivityRequestHost) getActivity();
                    request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
                }
                if (requestCode == request) {
                    updateContent();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
