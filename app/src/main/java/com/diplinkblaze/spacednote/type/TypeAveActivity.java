package com.diplinkblaze.spacednote.type;

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
import data.model.type.Type;
import data.model.type.TypeCatalog;

public class TypeAveActivity extends BaseActivity implements AveFragment.OnFragmentInteractionListener{

    private static final String TYPE_ID = "typeId";

    public static Intent getIntent(@Nullable Type type, Context context) {
        Intent intent = new Intent(context, TypeAveActivity.class);
        if (type != null && type.isRealized()) {
            intent.putExtra(TYPE_ID, type.getId());
        }
        return intent;
    }

    private static final String TAG_FRAGMENT = "fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_ave);
        initializeViews();
    }

    private void initializeViews() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
        if (fragment == null) {
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(TYPE_ID)) {
                Type type = TypeCatalog.getTypeById(getIntent().getLongExtra(TYPE_ID, 0), OpenHelper.getDatabase(this));
                fragment = AveFragment.newInstance(AveUtil.Type.create(type, getResources()));
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
        AveUtil.Type.save(componentSet, OpenHelper.getDatabase(this), this);
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
