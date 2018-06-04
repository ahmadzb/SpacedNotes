package com.diplinkblaze.spacednote.profile;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.universal.fragments.AveFragment;
import com.diplinkblaze.spacednote.universal.util.AveComponentSet;
import com.diplinkblaze.spacednote.universal.util.AveUtil;

import java.util.ArrayList;

import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;

public class ProfileAveActivity extends BaseActivity implements AveFragment.OnFragmentInteractionListener{

    private static final String TAG_AVE = "ave";
    private static final String KEY_PROFILE_ID = "forProfileId";

    public static Intent getIntentNew(Context context) {
        Intent intent = new Intent(context, ProfileAveActivity.class);
        return intent;
    }

    public static Intent getIntentEdit(Profile profile, Context context) {
        Intent intent = new Intent(context, ProfileAveActivity.class);
        intent.putExtra(KEY_PROFILE_ID, profile.getId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_ave);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_AVE);
        if (fragment == null) {
            Profile profile = null;
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(KEY_PROFILE_ID)) {
                profile = ProfileCatalog.getProfileById(getIntent().getLongExtra(KEY_PROFILE_ID, 0), this);
            }
            fragment = AveFragment.newInstance(AveUtil.Profile.create(profile, this));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.activity_profile_ave_frame, fragment, TAG_AVE);
            transaction.commit();
        }
    }

    //=========================================== AVE ==============================================
    @Override
    public void onSaveResult(AveComponentSet componentSet) {
        AveUtil.Profile.save(componentSet, getApplicationContext());
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
