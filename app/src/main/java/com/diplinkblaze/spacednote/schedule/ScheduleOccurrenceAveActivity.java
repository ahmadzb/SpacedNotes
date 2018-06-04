package com.diplinkblaze.spacednote.schedule;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.universal.fragments.AveFragment;
import com.diplinkblaze.spacednote.universal.util.AveComponentSet;
import com.diplinkblaze.spacednote.universal.util.AveUtil;

import java.util.ArrayList;

import data.database.OpenHelper;
import data.model.schedule.Occurrence;
import data.model.schedule.OccurrenceCatalog;
import data.model.schedule.Schedule;
import data.model.schedule.ScheduleCatalog;

public class ScheduleOccurrenceAveActivity extends BaseActivity implements AveFragment.OnFragmentInteractionListener{

    private static final String KEY_OCCURRENCE_ID = "occurrenceId";
    private static final String KEY_SCHEDULE_ID = "scheduleId";
    private static final String TAG_FRAGMENT = "fragment";

    private Schedule schedule;
    private Occurrence occurrence;

    public static Intent getIntent(@Nullable Occurrence occurrence, Schedule schedule, Context context) {
        Intent intent = new Intent(context, ScheduleOccurrenceAveActivity.class);
        if (occurrence != null && occurrence.isRealized()) {
            intent.putExtra(KEY_OCCURRENCE_ID, occurrence.getId());
        }
        if (schedule == null || !schedule.isRealized())
            throw new RuntimeException("Schedule must be non-null and realized");
        intent.putExtra(KEY_SCHEDULE_ID, schedule.getId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        schedule = ScheduleCatalog.getScheduleById(getIntent().getLongExtra(KEY_SCHEDULE_ID, 0),
                OpenHelper.getDatabase(this));
        if (getIntent().getExtras().containsKey(KEY_OCCURRENCE_ID)) {
            occurrence = OccurrenceCatalog.getOccurrenceById(
                    getIntent().getLongExtra(KEY_OCCURRENCE_ID, 0), true, OpenHelper.getDatabase(this));
        }

        setContentView(R.layout.activity_schedule_occurrence_ave);
        initializeViews();
    }

    private void initializeViews() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
        if (fragment == null) {
            fragment = AveFragment.newInstance(AveUtil.ScheduleOccurrence.create(
                    occurrence, schedule, OpenHelper.getDatabase(this), getResources()));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.root, fragment, TAG_FRAGMENT);
            transaction.commit();
        }
    }

    //====================================== Ave Fragment ==========================================
    @Override
    public void onSaveResult(AveComponentSet componentSet) {
        AveUtil.ScheduleOccurrence.save(componentSet, schedule, OpenHelper.getDatabase(this), this);
    }

    @Override
    public boolean onEditPressed(AveComponentSet componentSet) {
        return true;
    }

    @Override
    public void onFinish(boolean success) {
        if (success) {
            setResult(RESULT_OK);
        }
        finish();
    }

    @Override
    public void onMenuClicked(int itemId, AveComponentSet componentSet) {

    }

    @Override
    public ArrayList<AveComponentSet.ViewComponent> getViewComponents(AveComponentSet componentSet) {
        return null;
    }
}

