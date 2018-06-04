package com.diplinkblaze.spacednote.type;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.universal.fragments.AveFragment;
import com.diplinkblaze.spacednote.universal.fragments.ListFragment;
import com.diplinkblaze.spacednote.universal.util.AveComponentSet;
import com.diplinkblaze.spacednote.universal.util.AveUtil;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import java.util.ArrayList;

import data.database.OpenHelper;
import data.model.type.Element;
import data.model.type.ElementCatalog;
import data.model.type.Type;
import data.model.type.TypeCatalog;
import util.TypeFaceUtils;

public class TypeElementsActivity extends BaseActivity implements ListFragment.OnFragmentInteractionListener,
        ContentUpdateListener, AveFragment.OnFragmentInteractionListener{

    private static final int ACTIVITY_REQUEST_AVE = 0;
    private static final int ACTIVITY_REQUEST_AVE_SELF = 1;


    private static final String KEY_TYPE_ID = "TypeId";

    private static final String TAG_AVE = "ave";
    public static final String TAG_LIST_FRAGMENT_AVAILABLE = "listFragmentAvailable";
    public static final String TAG_LIST_FRAGMENT_ARCHIVED = "listFragmentArchived";

    private Adapter mAdapter;
    private Type type;

    public static Intent getIntent(Type type, Context context) {
        Intent intent = new Intent(context, TypeElementsActivity.class);
        intent.putExtra(KEY_TYPE_ID, type.getId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_elements);

        type = TypeCatalog.getTypeById(getIntent().getLongExtra(KEY_TYPE_ID, 0), OpenHelper.getDatabase(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(type.getTitle());

        initializeViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeViews() {
        OnViewClickListener viewClickListener = new OnViewClickListener();

        FloatingActionButton fabText = (FloatingActionButton) findViewById(R.id.fab_text);
        FloatingActionButton fabList = (FloatingActionButton) findViewById(R.id.fab_list);
        FloatingActionButton fabPictures = (FloatingActionButton) findViewById(R.id.fab_pictures);
        FloatingActionButton fabDivider = (FloatingActionButton) findViewById(R.id.fab_divider);

        fabText.setOnClickListener(viewClickListener);
        fabList.setOnClickListener(viewClickListener);
        fabPictures.setOnClickListener(viewClickListener);
        fabDivider.setOnClickListener(viewClickListener);

        ViewPager pager = (ViewPager) findViewById(R.id.activity_type_elements_pager);
        mAdapter = new Adapter(getSupportFragmentManager());
        pager.setAdapter(mAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_type_elements_tab_layout);
        tabLayout.setupWithViewPager(pager);
        TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), tabLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.type_menu, menu);

        if (type.isArchived())
            menu.removeItem(R.id.type_menu_archived_state);
        else {
            menu.removeItem(R.id.type_menu_available_state);
        }
        return true;
    }

    //===================================== User Interaction =======================================

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.type_menu_delete) {
            final SQLiteDatabase database = OpenHelper.getDatabase(this);
            if (TypeCatalog.hasRelatedItems(type, database)) {
                Toast.makeText(this, "Cannot delete, has related items", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this).setTitle(R.string.delete)
                        .setMessage(R.string.sentence_delete_item_question)
                        .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TypeCatalog.deleteType(type, database, getApplicationContext());
                                setResult(RESULT_OK);
                                finish();
                            }
                        }).setNegativeButton(R.string.action_no, null).show();

            }
            return true;
        } else if (item.getItemId() == R.id.type_menu_edit) {
            Intent intent = TypeAveActivity.getIntent(type, this);
            startActivityForResult(intent, ACTIVITY_REQUEST_AVE_SELF);
            setResult(RESULT_OK);
            return true;
        } else if (item.getItemId() == R.id.type_menu_archived_state) {
            final SQLiteDatabase database = OpenHelper.getDatabase(this);
            TypeCatalog.updateTypeArchivedState(type, true, database, getApplicationContext());
            Toast.makeText(this, R.string.sentence_archived_state_set, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            type = TypeCatalog.getTypeById(type.getId(), database);
            invalidateOptionsMenu();
            return true;
        } else if (item.getItemId() == R.id.type_menu_available_state) {
            final SQLiteDatabase database = OpenHelper.getDatabase(this);
            TypeCatalog.updateTypeArchivedState(type, false, database, getApplicationContext());
            Toast.makeText(this, R.string.sentence_available_state_set, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            type = TypeCatalog.getTypeById(type.getId(), database);
            invalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class OnViewClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fab_text) {
                onNewTextClicked();
            } else if (v.getId() == R.id.fab_list) {
                onNewListClicked();
            } else if (v.getId() == R.id.fab_pictures) {
                onNewPicturesClicked();
            } else if (v.getId() == R.id.fab_divider) {
                onNewDividerClicked();
            }
        }
    }

    private void onNewTextClicked() {
        Type type = Type.newInstance();
        type.setId(getIntent().getLongExtra(KEY_TYPE_ID, 0));
        Intent intent = TypeElementAveActivity.getIntentNewText(type, this);
        startActivityForResult(intent, ACTIVITY_REQUEST_AVE);

        FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        menu.collapse();
    }

    private void onNewListClicked() {
        Type type = Type.newInstance();
        type.setId(getIntent().getLongExtra(KEY_TYPE_ID, 0));
        Intent intent = TypeElementAveActivity.getIntentNewList(type, this);
        startActivityForResult(intent, ACTIVITY_REQUEST_AVE);

        FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        menu.collapse();
    }

    private void onNewPicturesClicked() {
        Type type = Type.newInstance();
        type.setId(getIntent().getLongExtra(KEY_TYPE_ID, 0));
        Intent intent = TypeElementAveActivity.getIntentNewPictures(type, this);
        startActivityForResult(intent, ACTIVITY_REQUEST_AVE);

        FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        menu.collapse();
    }

    private void onNewDividerClicked() {
        Type type = Type.newInstance();
        type.setId(getIntent().getLongExtra(KEY_TYPE_ID, 0));
        Intent intent = TypeElementAveActivity.getIntentNewDivider(type, this);
        startActivityForResult(intent, ACTIVITY_REQUEST_AVE);

        FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        menu.collapse();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_REQUEST_AVE) {
                updateContent();
            } else if (requestCode == ACTIVITY_REQUEST_AVE_SELF) {
                type = TypeCatalog.getTypeById(type.getId(), OpenHelper.getDatabase(this));
                getSupportActionBar().setTitle(type.getTitle());
                updateContent();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //======================================= List Fragment ========================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        Type type = Type.newInstance();
        type.setId(getIntent().getLongExtra(KEY_TYPE_ID, 0));

        if (TAG_LIST_FRAGMENT_AVAILABLE.equals(tag)) {
            return ListUtil.TypeElement.createAvailable(type, OpenHelper.getDatabase(this));
        } else if (TAG_LIST_FRAGMENT_ARCHIVED.equals(tag)) {
            return ListUtil.TypeElement.createArchived(type, OpenHelper.getDatabase(this));
        } else
            throw new RuntimeException("Tag was not recognized");
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity,
                                                String tag, Bundle identifier) {
        ListUtil.TypeElement.updatePositions(data, rootEntity, OpenHelper.getDatabase(this), this);
    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {
        Element element = ((ListUtil.TypeElement.ElementEntity) entity).getElement();
        AveComponentSet componentSet = AveUtil.TypeElement.create(element, getResources());
        componentSet.setStateView();
        AveFragment fragment = AveFragment.newInstance(componentSet);
        fragment.show(getSupportFragmentManager(), TAG_AVE);
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

    //====================================== Content Update ========================================
    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }


    //=========================================== Ave ==============================================
    @Override
    public void onSaveResult(AveComponentSet componentSet) {

    }

    @Override
    public boolean onEditPressed(AveComponentSet componentSet) {
        Element element = Element.newInstance();
        element.setId(componentSet.id);
        element.setRealized(true);
        Type type = Type.newInstance();
        type.setId(getIntent().getLongExtra(KEY_TYPE_ID, 0));
        type.setRealized(true);

        Intent intent = TypeElementAveActivity.getIntent(type, element, this);
        startActivityForResult(intent, ACTIVITY_REQUEST_AVE);
        onFinish(false);
        return true;
    }

    @Override
    public void onFinish(boolean success) {
        AveFragment fragment = (AveFragment) getSupportFragmentManager().findFragmentByTag(TAG_AVE);
        if (fragment != null)
            fragment.dismiss();
        if (success) {
            updateContent();
        }
    }

    @Override
    public void onMenuClicked(int itemId, AveComponentSet componentSet) {
        SQLiteDatabase database = OpenHelper.getDatabase(this);
        if (itemId == R.id.type_element_menu_archive_state) {
            Element element = Element.newInstance().setId(componentSet.id).setRealized(true);
            ElementCatalog.updateElementArchivedState(element, true, database, getApplicationContext());
            onFinish(true);
        } else if(itemId == R.id.type_element_menu_available_state) {
            Element element = Element.newInstance().setId(componentSet.id).setRealized(true);
            ElementCatalog.updateElementArchivedState(element, false, database, getApplicationContext());
            onFinish(true);
        } else if(itemId == R.id.type_element_menu_delete) {
            Element element = Element.newInstance().setId(componentSet.id).setRealized(true);
            if (ElementCatalog.hasRelatedItems(element, database)) {
                Toast.makeText(this, "Cannot delete, has related items", Toast.LENGTH_SHORT).show();
            } else {
                ElementCatalog.deleteElement(element, database, getApplicationContext());
                onFinish(true);
            }
        }
    }

    @Override
    public ArrayList<AveComponentSet.ViewComponent> getViewComponents(AveComponentSet componentSet) {
        return null;
    }

    //====================================== Pager Adapter =========================================
    private class Adapter extends FragmentStatePagerAdapter {

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                Fragment listFragment = ListFragment.newInstance(TAG_LIST_FRAGMENT_AVAILABLE);
                return listFragment;
            } else if (position == 1) {
                Fragment listFragment = ListFragment.newInstance(TAG_LIST_FRAGMENT_ARCHIVED);
                return listFragment;
            } else
                throw new RuntimeException("position is too big");
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.available);
            } else if (position == 1) {
                return getString(R.string.archived);
            } else
                throw new RuntimeException("position is too big");
        }
    }
}
