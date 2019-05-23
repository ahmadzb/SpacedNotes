package com.diplinkblaze.spacednote.main;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.ActivityRequestHostUtils;
import com.diplinkblaze.spacednote.contract.BackSupportListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.profile.ProfileListFragment;

import java.util.ArrayList;
import java.util.TreeMap;

import data.drive.DriveOperator;
import data.dropbox.DropboxOperator;
import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;
import data.pcloud.PCloudOperator;
import data.preference.ContentPreferences;
import data.preference.SyncPreferences;
import data.sync.SyncOperator;
import data.sync.SyncOperators;
import util.Colors;
import util.TypeFaceUtils;


public class MainDrawerFragment extends Fragment implements BackSupportListener, ContentUpdateListener,
        ProfileListFragment.OnFragmentInteractionListener {
    private static final int LAYOUT_ITEMS = 0;
    private static final int LAYOUT_PROFILE = 1;

    private static final String KEY_CURRENT_LAYOUT = "currentLayout";
    private static final String KEY_CURRENT_CONTENT = "currentContent";
    private static final String KEY_SHOW_MORE_SYNC = "showMoreSync";


    private static final String TAG_PROFILES = "profiles";

    private static final int ACTIVITY_REQUEST_SETTINGS = 0;
    private static final int ACTIVITY_REQUEST_SYNC = 1;

    private OnViewClickedListener mViewClickedListener = new OnViewClickedListener();

    private int currentContent = MainActivity.CONTENT_DEFAULT;
    private int currentLayout = LAYOUT_ITEMS;
    private boolean showMoreSyncs;

    public MainDrawerFragment() {
        // Required empty public constructor
    }

    public static MainDrawerFragment newInstance(int initialContent) {
        MainDrawerFragment fragment = new MainDrawerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_CURRENT_CONTENT, initialContent);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentContent = savedInstanceState.getInt(KEY_CURRENT_CONTENT);
            currentLayout = savedInstanceState.getInt(KEY_CURRENT_LAYOUT);
            showMoreSyncs = savedInstanceState.getBoolean(KEY_SHOW_MORE_SYNC);
        } else {
            currentContent = getArguments().getInt(KEY_CURRENT_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_main_drawer, container, false);
        initializeViews(contentView);
        updateViews(contentView);
        setContentsTitle(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), contentView);

        //Header
        {
            ViewGroup headerLayout = contentView.findViewById(R.id.fragment_main_drawer_header_frame);
            TextView headerTitle = contentView.findViewById(R.id.fragment_main_drawer_header_title);
            headerTitle.setOnClickListener(mViewClickedListener);
        }

        //Items
        {
            LinearLayout itemsLayout = contentView.findViewById(R.id.fragment_main_drawer_layout_items);

            int inactiveColor = 0x88000000;

            for (int i = 0; i < itemsLayout.getChildCount(); i++) {
                View view = itemsLayout.getChildAt(i);
                //check if this view is a menu item
                if (view.getTag() instanceof String) {

                    String tag = (String) view.getTag();
                    String contentMenuItem = getString(R.string.tag_nav_menu_item);
                    String openableMenuItem = getString(R.string.tag_nav_menu_item_openable);
                    if (tag.equals(contentMenuItem) || tag.equals(openableMenuItem)) {
                        view.setOnClickListener(mViewClickedListener);
                    }
                    if (tag.equals(openableMenuItem)) {
                        ImageView iconImageView = view.findViewWithTag(getString(R.string.tag_nav_menu_icon));
                        iconImageView.setColorFilter(inactiveColor, PorterDuff.Mode.MULTIPLY);
                        view.setOnClickListener(mViewClickedListener);
                    }
                }
            }

            if (currentLayout == LAYOUT_ITEMS) {
                itemsLayout.setVisibility(View.VISIBLE);
            } else {
                itemsLayout.setVisibility(View.GONE);
            }
        }

        //Profiles
        {
            Fragment fragment = getChildFragmentManager().findFragmentByTag(TAG_PROFILES);
            if (fragment == null) {
                fragment = ProfileListFragment.newInstance();
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_main_drawer_content_frame, fragment, TAG_PROFILES);
                if (currentLayout != LAYOUT_PROFILE) {
                    transaction.hide(fragment);
                }
                transaction.commit();
            }
        }
        //Sync more
        {
            View moreView = contentView.findViewById(R.id.fragment_main_drawer_sync_more);
            moreView.setOnClickListener(mViewClickedListener);
        }
    }

    private void tryUpdateViews() {
        View contentView = getView();
        if (contentView != null) {
            updateViews(contentView);
        }
    }

    private void updateViews(View contentView) {
        //Header
        {
            Profile currentProfile = ProfileCatalog.getCurrentProfile(getContext());
            ViewGroup headerLayout = contentView.findViewById(R.id.fragment_main_drawer_header_frame);
            TextView headerTitle = contentView.findViewById(R.id.fragment_main_drawer_header_title);
            headerTitle.setText(currentProfile.getName());
            headerLayout.setBackgroundColor(currentProfile.getColor());
        }

        //items
        {
            LinearLayout itemsLayout = contentView.findViewById(R.id.fragment_main_drawer_layout_items);

            int activeColor = Colors.getPrimaryColor(getContext());
            int inactiveColor = 0x88000000;
            int inactiveTextColor = getResources().getColor(R.color.colorText);
            int backgroundActivated = getResources().getColor(R.color.colorBackgroundLightActivated);
            int transparent = 0x0;

            for (int i = 0; i < itemsLayout.getChildCount(); i++) {
                View view = itemsLayout.getChildAt(i);
                //check if this view is a content menu item
                if (view.getTag() instanceof String &&
                        view.getTag().equals(getString(R.string.tag_nav_menu_item))) {

                    ImageView iconImageView = view.findViewWithTag(getString(R.string.tag_nav_menu_icon));
                    TextView textView = view.findViewWithTag(getString(R.string.tag_nav_menu_text));

                    if (currentContent == getContentByViewId(view.getId())) {
                        view.setBackgroundColor(backgroundActivated);
                        iconImageView.setColorFilter(activeColor, PorterDuff.Mode.MULTIPLY);
                        textView.setTextColor(activeColor);
                    } else {
                        view.setBackgroundColor(transparent);
                        iconImageView.setColorFilter(inactiveColor, PorterDuff.Mode.MULTIPLY);
                        textView.setTextColor(inactiveTextColor);
                    }
                }
            }
        }
        //Sync visibilities
        {
            ArrayList<View> syncItems = new ArrayList<>();
            syncItems.add(contentView.findViewById(R.id.fragment_main_drawer_sync_drive));
            syncItems.add(contentView.findViewById(R.id.fragment_main_drawer_sync_dropbox));
            syncItems.add(contentView.findViewById(R.id.fragment_main_drawer_sync_pcloud));
            View moreView = contentView.findViewById(R.id.fragment_main_drawer_sync_more);
            if (showMoreSyncs) {
                moreView.setVisibility(View.GONE);
                for (View syncItem : syncItems) {
                    syncItem.setVisibility(View.VISIBLE);
                }
            } else {
                moreView.setVisibility(View.VISIBLE);
                SyncOperator currentSyncOperator = SyncOperators.getCurrentOperator(getContext());
                for (View syncItem : syncItems) {
                    boolean isVisible = false;
                    isVisible = isVisible || currentSyncOperator instanceof DriveOperator &&
                            syncItem.getId() == R.id.fragment_main_drawer_sync_drive;
                    isVisible = isVisible || currentSyncOperator instanceof DropboxOperator &&
                            syncItem.getId() == R.id.fragment_main_drawer_sync_dropbox;
                    isVisible = isVisible || currentSyncOperator instanceof PCloudOperator &&
                            syncItem.getId() == R.id.fragment_main_drawer_sync_pcloud;
                    syncItem.setVisibility(isVisible? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_CONTENT, currentContent);
        outState.putInt(KEY_CURRENT_LAYOUT, currentLayout);
        outState.putBoolean(KEY_SHOW_MORE_SYNC, showMoreSyncs);
    }

    //======================================= Listeners =========================================
    private void onHeaderClicked() {
        if (currentLayout == LAYOUT_ITEMS) {
            setCurrentLayout(LAYOUT_PROFILE);
        } else if (currentLayout == LAYOUT_PROFILE) {
            setCurrentLayout(LAYOUT_ITEMS);
        }
    }

    private void setCurrentLayout(int layout) {
        currentLayout = layout;
        Fragment fragment = getChildFragmentManager().findFragmentByTag(TAG_PROFILES);
        View contentView = getView();
        if (fragment != null && contentView != null) {
            View itemsLayout = contentView.findViewById(R.id.fragment_main_drawer_layout_items);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            if (currentLayout == LAYOUT_PROFILE) {
                transaction.show(fragment);
                itemsLayout.setVisibility(View.GONE);
            } else if (currentLayout == LAYOUT_ITEMS) {
                transaction.hide(fragment);
                itemsLayout.setVisibility(View.VISIBLE);
            }
            transaction.commit();
        }
    }

    private void menuContentItemSelected(int content, boolean notifyParent) {
        currentContent = content;
        tryUpdateViews();
        if (notifyParent) {
            contentChangeRequest(content);
        }
    }

    private void menuSettingsSelected() {
//        Intent intent = SettingsActivity.generateIntent(getContext());
//        startActivityForResult(intent, getRequest(ACTIVITY_REQUEST_SETTINGS));
    }

    private void menuSyncDriveSelected() {
        SyncPreferences.setCurrentSyncOperatorDrive(getContext());
        int request = ActivityRequestHostUtils.toGlobalRequest(ACTIVITY_REQUEST_SYNC,
                (ActivityRequestHost) getActivity(), this);
        startActivityForResult(SyncActivity.getIntent(getContext()), request);
        showMoreSyncs = false;
        tryUpdateViews();
    }

    private void menuSyncDropboxSelected() {
        SyncPreferences.setCurrentSyncOperatorDropbox(getContext());
        int request = ActivityRequestHostUtils.toGlobalRequest(ACTIVITY_REQUEST_SYNC,
                (ActivityRequestHost) getActivity(), this);
        startActivityForResult(SyncActivity.getIntent(getContext()), request);
        showMoreSyncs = false;
        tryUpdateViews();
    }

    private void menuSyncPCloudSelected() {
        SyncPreferences.setCurrentSyncOperatorPCloud(getContext());
        int request = ActivityRequestHostUtils.toGlobalRequest(ACTIVITY_REQUEST_SYNC,
                (ActivityRequestHost) getActivity(), this);
        startActivityForResult(SyncActivity.getIntent(getContext()), request);
        showMoreSyncs = false;
        tryUpdateViews();
    }

    private void moreSyncSelected() {
        showMoreSyncs = true;
        tryUpdateViews();
    }

    private class OnViewClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int content = getContentByViewId(v.getId());
            if (content != MainActivity.CONTENT_INVALID) {
                menuContentItemSelected(content, true);
            } else if (v.getId() == R.id.fragment_main_drawer_settings) {
                menuSettingsSelected();
            } else if (v.getId() == R.id.fragment_main_drawer_sync_drive) {
                menuSyncDriveSelected();
            } else if (v.getId() == R.id.fragment_main_drawer_sync_dropbox) {
                menuSyncDropboxSelected();
            } else if (v.getId() == R.id.fragment_main_drawer_sync_pcloud) {
                menuSyncPCloudSelected();
            } else if (v.getId() == R.id.fragment_main_drawer_header_title) {
                onHeaderClicked();
            } else if (v.getId() == R.id.fragment_main_drawer_sync_more) {
                moreSyncSelected();
            }
        }
    }

    //===================================== Content Util =========================================
    private int getContentByViewId(int viewId) {
        switch (viewId) {
            case R.id.fragment_main_drawer_timeline:
                return MainActivity.CONTENT_TIME_LINE;
            case R.id.fragment_main_drawer_label_lists:
                return MainActivity.CONTENT_LABEL_LISTS;
            case R.id.fragment_main_drawer_labels:
                return MainActivity.CONTENT_LABELS;
            case R.id.fragment_main_drawer_notes:
                return MainActivity.CONTENT_NOTES;
            case R.id.fragment_main_drawer_schedule:
                return MainActivity.CONTENT_SCHEDULE;
            case R.id.fragment_main_drawer_types:
                return MainActivity.CONTENT_TYPES;
            case R.id.fragment_main_drawer_bin:
                return MainActivity.CONTENT_BIN;
            default:
                return MainActivity.CONTENT_INVALID;
        }
    }

    private void findContentsTitle(TreeMap<Integer, String> contentTitles, View view) {
        if (view.getTag() instanceof String &&
                view.getTag().equals(getString(R.string.tag_nav_menu_item))) {
            int content = getContentByViewId(view.getId());
            TextView textView = view.findViewWithTag(getString(R.string.tag_nav_menu_text));
            contentTitles.put(content, textView.getText().toString());
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++)
                findContentsTitle(contentTitles, vg.getChildAt(i));
        }
    }

    //================================== Communication Parent ======================================
    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
        View contentView = getView();
        if (contentView != null) {
            updateViews(contentView);
        }
    }

    //Parent to This
    @Override
    public boolean onBackPressed() {
        if (currentContent != MainActivity.CONTENT_DEFAULT) {
            menuContentItemSelected(MainActivity.CONTENT_DEFAULT, true);
            return true;
        }
        return false;
    }

    public void resetCurrentLayout() {
        setCurrentLayout(LAYOUT_ITEMS);
        showMoreSyncs = false;
        tryUpdateViews();
    }

    //This to parent
    private void updateParentContent() {
        if (getParentFragment() instanceof ContentUpdateListener)
            ((ContentUpdateListener) getParentFragment()).updateContent();
        else if (getActivity() instanceof ContentUpdateListener)
            ((ContentUpdateListener) getActivity()).updateContent();
    }

    private void setContentsTitle(View contentView) {
        TreeMap<Integer, String> contentTitles = new TreeMap<>();
        findContentsTitle(contentTitles, contentView);
        getListener().setContentsTitle(contentTitles);
    }

    private void contentChangeRequest(int content) {
        getListener().onContentChangeRequest(content);
    }

    private void onCurrentProfileChanged() {
        getListener().onCurrentProfileChanged();
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            return ((OnFragmentInteractionListener) getParentFragment());
        else if (getActivity() instanceof OnFragmentInteractionListener)
            return ((OnFragmentInteractionListener) getActivity());
        else
            throw new RuntimeException("Either parent fragment or activity should implement" +
                    " OnFragmentInteractionListener");
    }

    public interface OnFragmentInteractionListener {
        void onContentChangeRequest(int content);

        void setContentsTitle(TreeMap<Integer, String> titles);

        void onCurrentProfileChanged();
    }

    //================================= Communication Children =====================================

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ActivityRequestHostUtils.toGlobalRequest(ACTIVITY_REQUEST_SETTINGS,
                (ActivityRequestHost) getActivity(), this)) {
            updateParentContent();
        } else if (requestCode == ActivityRequestHostUtils.toGlobalRequest(ACTIVITY_REQUEST_SYNC,
                (ActivityRequestHost) getActivity(), this)) {
            updateParentContent();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onProfileChanged() {
        onCurrentProfileChanged();
    }
}
