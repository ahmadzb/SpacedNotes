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
import data.model.schedule.Schedule;
import data.model.schedule.ScheduleCatalog;

public class ScheduleAveActivity extends BaseActivity implements AveFragment.OnFragmentInteractionListener{

    private static final String SCHEDULE_ID = "typeId";
    private static final String TAG_FRAGMENT = "fragment";

    public static Intent getIntent(@Nullable Schedule schedule, Context context) {
        Intent intent = new Intent(context, ScheduleAveActivity.class);
        if (schedule != null && schedule.isRealized()) {
            intent.putExtra(SCHEDULE_ID, schedule.getId());
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_ave);
        initializeViews();
    }

    private void initializeViews() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
        if (fragment == null) {
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(SCHEDULE_ID)) {
                Schedule schedule = ScheduleCatalog.getScheduleById(getIntent().getLongExtra(SCHEDULE_ID, 0), OpenHelper.getDatabase(this));
                fragment = AveFragment.newInstance(AveUtil.Schedule.create(schedule, getResources()));
            } else {
                fragment = AveFragment.newInstance(AveUtil.Type.create(getResources()));
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.root, fragment, TAG_FRAGMENT);
            transaction.commit();
        }
    }

    //====================================== Ave Fragment ==========================================
    @Override
    public void onSaveResult(AveComponentSet componentSet) {
        AveUtil.Schedule.save(componentSet, OpenHelper.getDatabase(this), this);
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
