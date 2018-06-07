package com.diplinkblaze.spacednote.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.checker.ImplicitChecker;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.BackSupportListener;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.contract.DeliverySupportListener;
import com.diplinkblaze.spacednote.contract.NewItemSupportListener;
import com.diplinkblaze.spacednote.labels.LabelListsListFragment;
import com.diplinkblaze.spacednote.labels.LabelsListFragment;
import com.diplinkblaze.spacednote.note.NoteListFragment;
import com.diplinkblaze.spacednote.note.NoteSelectors;
import com.diplinkblaze.spacednote.note.TimelineFragment;
import com.diplinkblaze.spacednote.profile.ProfileListFragment;
import com.diplinkblaze.spacednote.schedule.ScheduleListFragment;
import com.diplinkblaze.spacednote.type.TypeListFragment;

import java.util.List;
import java.util.TreeMap;

import data.database.OpenHelper;
import data.database.file.FileOpenHelper;
import util.TypeFaceUtils;

public class MainActivity extends BaseActivity
        implements MainDrawerFragment.OnFragmentInteractionListener, ContentUpdateListener,
        ActivityRequestHost {

    private static final String TAG_NAV_VIEW = "tagNavView";
    private static final String TAG_CURRENT_CONTENT = "tagCurrentContent";

    private static final int activityRequestShift = 4;
    private static final int ACTIVITY_REQUEST_PREFIX_SELF = 1;
    private static final int ACTIVITY_REQUEST_PREFIX_NAV_FRAGMENT = 2;
    private static final int ACTIVITY_REQUEST_PREFIX_TYPE = 3;
    private static final int ACTIVITY_REQUEST_PREFIX_SCHEDULE = 4;
    private static final int ACTIVITY_REQUEST_PREFIX_LABEL = 5;
    private static final int ACTIVITY_REQUEST_PREFIX_LABEL_LIST = 6;
    private static final int ACTIVITY_REQUEST_PREFIX_NOTES = 7;
    private static final int ACTIVITY_REQUEST_PREFIX_PROFILE = 8;
    private static final int ACTIVITY_REQUEST_PREFIX_TIMELINE = 9;

    private static final String KEY_CURRENT_CONTENT = "keyCurrentContent";

    private static final String KEY_INTENT_CONTENT = "keyIntentContent";
    private static final String KEY_INTENT_DELIVERY = "keyIntentDelivery";

    public static final int CONTENT_TIME_LINE = 1;
    public static final int CONTENT_LABEL_LISTS = 2;
    public static final int CONTENT_LABELS = 3;
    public static final int CONTENT_NOTES = 4;
    public static final int CONTENT_SCHEDULE = 5;
    public static final int CONTENT_TYPES = 6;
    public static final int CONTENT_BIN = 7;
    public static final int CONTENT_INVALID = 0;
    public static final int CONTENT_DEFAULT = CONTENT_TIME_LINE;

    private int currentContent = CONTENT_INVALID;
    private int requestContent = CONTENT_DEFAULT;
    private TreeMap<Integer, String> contentsTitle;
    private Bundle pendingDelivery = null;

    private OnViewClickListener viewClickListener = new OnViewClickListener();

    public static Intent getIntent(Context context) {
        OpenHelper.getDatabase(context);
        FileOpenHelper.getDatabase(context);
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }

    //======================================= Life cycle ===========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View contentView = getContentView();
        if (contentView instanceof DrawerLayout) {
            DrawerLayout drawer = (DrawerLayout) contentView;
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            drawer.addDrawerListener(new MainDrawerListener());
            toggle.syncState();
        }

        if (savedInstanceState != null) {
            currentContent = savedInstanceState.getInt(KEY_CURRENT_CONTENT);
            requestContent = currentContent;
        } else if (getIntent().getExtras() != null) {
            if (getIntent().getIntExtra(KEY_INTENT_CONTENT, -1) != -1) {
                requestContent = getIntent().getIntExtra(KEY_INTENT_CONTENT, -1);
            }
            if (getIntent().getBundleExtra(KEY_INTENT_DELIVERY) != null) {
                pendingDelivery = getIntent().getBundleExtra(KEY_INTENT_DELIVERY);
            }
        }

        initializeViews();
        updateViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean result = ImplicitChecker.runCheck(this, true);
        if (result) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

    }

    private void initializeViews() {
        View contentView = getContentView();
        TypeFaceUtils.setTypefaceDefaultCascade(getAssets(), contentView);

        if (getSupportFragmentManager().findFragmentByTag(TAG_NAV_VIEW) == null) {
            MainDrawerFragment fragment = MainDrawerFragment.newInstance(requestContent);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.activity_main_nav_view, fragment, TAG_NAV_VIEW);
            transaction.commit();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(viewClickListener);
    }

    private void updateViews() {
        Fragment fragment = null;
        if (currentContent != requestContent) {
            if (requestContent == CONTENT_TIME_LINE) {
                fragment = TimelineFragment.newInstance();
            } else if (requestContent == CONTENT_LABEL_LISTS) {
                fragment = LabelListsListFragment.newInstance();
            } else if (requestContent == CONTENT_LABELS) {
                fragment = LabelsListFragment.newInstance();
            } else if (requestContent == CONTENT_NOTES) {
                fragment = NoteListFragment.newInstance(new NoteSelectors.NotesNotDeletedSelector());
            } else if (requestContent == CONTENT_SCHEDULE) {
                fragment = ScheduleListFragment.newInstance();
            } else if (requestContent == CONTENT_TYPES) {
                fragment = TypeListFragment.newInstance();
            } else if (requestContent == CONTENT_BIN) {
                fragment = BinFragment.newInstance();
            } else
                throw new RuntimeException("Given request was not recognized");
            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_main_frame, fragment, TAG_CURRENT_CONTENT)
                        .commit();
            }
            currentContent = requestContent;
        }

        if (fragment == null) {
            fragment = getSupportFragmentManager().findFragmentByTag(TAG_CURRENT_CONTENT);
        }

        if (fragment != null) {
            View appBarFrame = findViewById(R.id.activity_main_app_bar_layout);
            float elevation = getResources().getDimension(R.dimen.app_bar_elevation);
            if (fragment instanceof LabelListsListFragment ||
                    fragment instanceof TypeListFragment ||
                    fragment instanceof TimelineFragment ||
                    fragment instanceof BinFragment) {
                appBarFrame.setElevation(0);
            } else {
                appBarFrame.setElevation(elevation);
            }
        }
        if (fragment != null) {
            //fab
            {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                if (fragment instanceof NewItemSupportListener) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }

            //delivery
            {
                if (pendingDelivery != null && fragment instanceof DeliverySupportListener) {
                    ((DeliverySupportListener) fragment).receiveDelivery(pendingDelivery);
                }
                pendingDelivery = null;
            }
        }

        //Title
        {
            if (contentsTitle != null) {
                String title = contentsTitle.get(currentContent);
                if (title == null) {
                    title = getString(R.string.app_name);
                }
                getSupportActionBar().setTitle(title);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_CONTENT, currentContent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //TODO

        return super.onOptionsItemSelected(item);
    }

    private View getContentView() {
        View contentView = findViewById(R.id.activity_main_root_drawer);
        if (contentView == null) {
            contentView = findViewById(R.id.activity_main_root_linear);
        }
        if (contentView == null) {
            throw new RuntimeException("Root layout was not found");
        }
        return contentView;
    }

    //================================== Communication Children ====================================
    //Activity Request host
    @Override
    public int getRequestPrefix(Fragment child) {
        if (child instanceof TypeListFragment) {
            return ACTIVITY_REQUEST_PREFIX_TYPE;
        } else if (child instanceof MainDrawerFragment) {
            return ACTIVITY_REQUEST_PREFIX_NAV_FRAGMENT;
        } else if (child instanceof ScheduleListFragment) {
            return ACTIVITY_REQUEST_PREFIX_SCHEDULE;
        } else if (child instanceof LabelsListFragment) {
            return ACTIVITY_REQUEST_PREFIX_LABEL;
        } else if (child instanceof LabelListsListFragment) {
            return ACTIVITY_REQUEST_PREFIX_LABEL_LIST;
        } else if (child instanceof NoteListFragment) {
            return ACTIVITY_REQUEST_PREFIX_NOTES;
        } else if (child instanceof ProfileListFragment) {
            return ACTIVITY_REQUEST_PREFIX_PROFILE;
        }else if (child instanceof TimelineFragment) {
            return ACTIVITY_REQUEST_PREFIX_TIMELINE;
        }
        return 0;
    }

    @Override
    public int getRequestShift() {
        return activityRequestShift;
    }

    //Nav List
    @Override
    public void onContentChangeRequest(int content) {
        //Drawer
        {
            View contentView = getContentView();
            if (contentView instanceof DrawerLayout) {
                DrawerLayout drawer = (DrawerLayout) contentView;
                drawer.closeDrawers();
            }
        }
        //Content
        {
            requestContent = content;
            updateViews();
        }
    }

    @Override
    public void setContentsTitle(TreeMap<Integer, String> titles) {
        this.contentsTitle = titles;
        updateViews();
    }

    @Override
    public void onCurrentProfileChanged() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //Activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ContentUpdateUtil.updateContentChildren(this);
        }
    }

    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }

    //====================================== User Interaction =====================================
    @Override
    public void onBackPressed() {
        View contentView = getContentView();
        if (contentView instanceof DrawerLayout) {
            DrawerLayout drawer = (DrawerLayout) contentView;
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return;
            }
        }
        {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            if (fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if (fragment instanceof BackSupportListener && !(fragment instanceof MainDrawerFragment)) {
                        boolean result = ((BackSupportListener) fragment).onBackPressed();
                        if (result) return;
                    }
                }

                for (Fragment fragment : fragmentList) {
                    if (fragment instanceof BackSupportListener && fragment instanceof MainDrawerFragment) {
                        boolean result = ((BackSupportListener) fragment).onBackPressed();
                        if (result) return;
                    }
                }
            }
        }
        super.onBackPressed();
    }

    private void onFabClicked() {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CURRENT_CONTENT);
        if (fragment != null && fragment instanceof NewItemSupportListener) {
            ((NewItemSupportListener) fragment).newItem();
        }

    }

    private class OnViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fab) {
                onFabClicked();
            }
        }
    }

    private class MainDrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {
            MainDrawerFragment fragment = (MainDrawerFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_NAV_VIEW);
            fragment.resetCurrentLayout();
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }
}
