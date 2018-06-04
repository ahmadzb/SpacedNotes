package com.diplinkblaze.spacednote.labels;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.universal.fragments.ListFragment;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import java.io.Serializable;
import java.util.ArrayList;

import data.database.OpenHelper;

public class LabelChooserActivity extends BaseActivity implements ListFragment.OnFragmentInteractionListener,
ListFragment.OnFragmentMarkActionListener, ContentUpdateListener{

    private static final String TAG_LIST = "list";
    private static final String KEY_SELECTION = "selection";
    private static final String KEY_IDENTIFIER = "identifier";

    private ArrayList<Long> selection;

    public static Intent getIntent(@Nullable ArrayList<Long> selection, Context context, Bundle identifier) {
        Intent intent = new Intent(context, LabelChooserActivity.class);
        if (selection != null) {
            intent.putExtra(KEY_SELECTION, (Serializable) selection);
        }
        if (identifier != null) {
            intent.putExtra(KEY_IDENTIFIER, identifier);
        }
        return intent;
    }

    public static ArrayList<Long> getSelectionFromResult(Intent result) {
        if (result.getExtras() == null || !result.getExtras().containsKey(KEY_SELECTION))
            return null;
        return (ArrayList<Long>) result.getSerializableExtra(KEY_SELECTION);
    }

    public static Bundle getIdentifierFromResult(Intent result) {
        if (result.getExtras() == null || !result.getExtras().containsKey(KEY_IDENTIFIER))
            return null;
        return result.getBundleExtra(KEY_IDENTIFIER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_chooser);

        if (savedInstanceState != null) {
            selection = (ArrayList<Long>) savedInstanceState.getSerializable(KEY_SELECTION);
        } else if (getIntent().getExtras() != null) {
            selection = (ArrayList<Long>) getIntent().getExtras().getSerializable(KEY_SELECTION);
        }
        if (selection == null)
            selection = new ArrayList<>();
        updateContent();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_LIST);
        if (fragment == null) {
            fragment = ListFragment.newInstance(TAG_LIST);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.activity_label_chooser_frame, fragment, TAG_LIST);
            transaction.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SELECTION, selection);
    }

    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }

    //========================================== List ==============================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        if (selection == null)
            return null;
        ListData listData = ListUtil.Label.create(OpenHelper.getDatabase(context));
        listData.setMarkedItemsIds(selection);
        listData.setModeFlags(listData.getModeFlags() | ListData.MODE_FLAG_MARKABLE);
        listData.setMarkMode(ListData.MARK_ALWAYS_CHOOSER);
        return listData;
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {

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

    //======= Mark
    @Override
    public void onUniversalListItemsSelected(ArrayList<Long> ids, String tag, Bundle identifier) {
        Intent intent = new Intent();
        intent.putExtra(KEY_SELECTION, (Serializable) selection);
        if (getIntent().getExtras() != null &&  getIntent().getExtras().containsKey(KEY_IDENTIFIER)) {
            intent.putExtra(KEY_IDENTIFIER, getIntent().getBundleExtra(KEY_IDENTIFIER));
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void universalListDismissRequest(String tag, Bundle identifier) {
        finish();
    }

    @Override
    public void onUniversalListMarkedItemsChanged(ArrayList<Long> ids, String tag, Bundle identifier) {
        selection = ids;
    }
}
