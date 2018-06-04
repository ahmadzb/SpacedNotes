package com.diplinkblaze.spacednote.labels;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.universal.fragments.AveFragment;
import com.diplinkblaze.spacednote.universal.util.AveComponentSet;
import com.diplinkblaze.spacednote.universal.util.AveUtil;
import com.diplinkblaze.spacednote.R;

import java.util.ArrayList;

import data.database.OpenHelper;
import data.model.label.Label;
import data.model.label.LabelCatalog;
import exceptions.NotRealizedException;

public class LabelAveActivity extends BaseActivity implements AveFragment.OnFragmentInteractionListener {

    private static final String TAG_AVE = "ave";
    private static final String KEY_LABEL_ID = "labelId";

    public static Intent getIntent(@Nullable Label label, Context context) {
        if (label != null && !label.isRealized()) {
            throw new NotRealizedException();
        }
        Intent intent = new Intent(context, LabelAveActivity.class);
        if (label != null) {
            intent.putExtra(KEY_LABEL_ID, label.getId());
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_ave);
        initializeViews();
    }

    private void initializeViews() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_AVE);
        if (fragment == null) {
            Label label = null;
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(KEY_LABEL_ID)) {
                label = LabelCatalog.getLabelById(getIntent().getLongExtra(KEY_LABEL_ID, 0), OpenHelper.getDatabase(this));
            }
            fragment = AveFragment.newInstance(AveUtil.Label.create(label));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.activity_label_ave_frame, fragment, TAG_AVE);
            transaction.commit();
        }
    }

    //============================================ Ave =============================================
    @Override
    public void onSaveResult(AveComponentSet componentSet) {
        AveUtil.Label.save(componentSet, OpenHelper.getDatabase(this), this);
    }

    @Override
    public boolean onEditPressed(AveComponentSet componentSet) {
        return false;
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
