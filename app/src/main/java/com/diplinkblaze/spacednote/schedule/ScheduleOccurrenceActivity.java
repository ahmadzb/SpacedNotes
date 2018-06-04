package com.diplinkblaze.spacednote.schedule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.universal.fragments.ListFragment;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import data.database.OpenHelper;
import data.model.schedule.Occurrence;
import data.model.schedule.OccurrenceCatalog;
import data.model.schedule.Schedule;
import data.model.schedule.ScheduleCatalog;
import data.model.scheduler.RevisionCatalog;
import data.model.scheduler.Scheduler;

public class ScheduleOccurrenceActivity extends BaseActivity implements
        ListFragment.OnFragmentInteractionListener, ContentUpdateListener {

    private static final String SCHEDULE_ID = "scheduleId";

    private static final String TAG_LIST_FRAGMENT = "listFragment";

    private static final int ACTIVITY_REQUEST_AVE = 0;
    private static final int ACTIVITY_REQUEST_AVE_SELF = 1;

    private Schedule schedule;

    public static Intent getIntent(Schedule schedule, Context context) {
        Intent intent = new Intent(context, ScheduleOccurrenceActivity.class);
        intent.putExtra(SCHEDULE_ID, schedule.getId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        schedule = ScheduleCatalog.getScheduleById(
                getIntent().getLongExtra(SCHEDULE_ID, 0),
                OpenHelper.getDatabase(this));

        ListFragment listFragment = (ListFragment) getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT);
        if (listFragment != null) {
            listFragment.updateContent();
        }

        setContentView(R.layout.activity_schedule_occurrence);
        initializeViews();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(schedule.getTitle());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeViews() {
        OnViewClickListener viewClickListener = new OnViewClickListener();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(viewClickListener);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT);
        if (fragment == null) {
            fragment = ListFragment.newInstance(TAG_LIST_FRAGMENT);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.activity_schedule_occurrence_frame, fragment, TAG_LIST_FRAGMENT);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedule_occurrence_menu, menu);
        if (schedule.getOccurrencesCount() == 0) {
            menu.removeItem(R.id.schedule_occurrence_menu_delete_last);
        }
        if (RevisionCatalog.hasFutureRevision(schedule, OpenHelper.getDatabase(this))) {
            menu.removeItem(R.id.schedule_occurrence_menu_delete_self);
        } else {
            menu.removeItem(R.id.schedule_occurrence_menu_merge);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.schedule_occurrence_menu_edit) {
            Intent intent = ScheduleAveActivity.getIntent(schedule, this);
            startActivityForResult(intent, ACTIVITY_REQUEST_AVE_SELF);
        } else if (item.getItemId() == R.id.schedule_occurrence_menu_delete_last) {
            deleteLastOccurrence();
        } else if (item.getItemId() == R.id.schedule_occurrence_menu_merge) {
            mergeSchedule();
        } else if (item.getItemId() == R.id.schedule_occurrence_menu_delete_self) {
            deleteSelfSchedule();
        }
        return super.onOptionsItemSelected(item);
    }
    //======================================= List Fragment ========================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        if (schedule == null)
            return null;
        else
            return ListUtil.ScheduleOccurrence.create(schedule, OpenHelper.getDatabase(this));
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {
        ListUtil.ScheduleOccurrence.OccurrenceEntity occurrenceEntity =
                (ListUtil.ScheduleOccurrence.OccurrenceEntity) entity;
        Intent intent = ScheduleOccurrenceAveActivity.getIntent(occurrenceEntity.getOccurrence(), schedule, this);
        startActivityForResult(intent, ACTIVITY_REQUEST_AVE);
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

    //======================================= Content Update =======================================
    @Override
    public void updateContent() {
        schedule = ScheduleCatalog.getScheduleById(schedule.getId(), OpenHelper.getDatabase(this));
        invalidateOptionsMenu();
        ContentUpdateUtil.updateContentChildren(this);
    }

    //====================================== User Interaction ======================================
    private class OnViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fab) {
                onFabClicked();
            }
        }
    }

    private void onFabClicked() {
        Intent intent = ScheduleOccurrenceAveActivity.getIntent(null, schedule, this);
        startActivityForResult(intent, ACTIVITY_REQUEST_AVE);
    }

    private void deleteLastOccurrence() {
        schedule = ScheduleCatalog.getScheduleById(schedule.getId(), OpenHelper.getDatabase(this));//Double check
        int count = schedule.getOccurrencesCount();
        if (count != 0) {
            Occurrence occurrence = schedule.getOccurrenceByNumber(count - 1);
            OccurrenceCatalog.deleteOccurrence(occurrence, OpenHelper.getDatabase(this), this);
            updateContent();
            setResult(RESULT_OK);
        }
    }

    private void mergeSchedule() {
        //TODO
    }

    private void deleteSelfSchedule() {
        new AlertDialog.Builder(this).setTitle(R.string.delete)
                .setMessage(R.string.sentence_delete_schedule_question)
                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ScheduleCatalog.deleteSchedule(schedule, OpenHelper.getDatabase(getApplicationContext()), getApplicationContext());
                        Toast.makeText(getApplicationContext(), "Schedule deleted successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }).setNegativeButton(R.string.action_no, null).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST_AVE) {
            if (resultCode == RESULT_OK) {
                updateContent();
                setResult(RESULT_OK);
            }
        } else if (requestCode == ACTIVITY_REQUEST_AVE_SELF) {
            if (resultCode == RESULT_OK) {
                updateContent();
                setResult(RESULT_OK);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
