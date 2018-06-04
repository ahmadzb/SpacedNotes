package com.diplinkblaze.spacednote.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.draglistview.DragListView;

import java.io.File;
import java.util.ArrayList;

import data.database.OpenHelper;
import data.storage.Databases;
import data.storage.PictureOperations;
import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;
import data.storage.Pictures;
import util.file.FileUtil;

public class ProfileListFragment extends Fragment implements ContentUpdateListener {

    private static final int ACTIVITY_REQUEST_AVE = 0;

    private ArrayList<Profile> profiles;
    DragAdapter adapter;
    private boolean showArchived;

    public ProfileListFragment() {
        //Required empty public constructor
    }

    public static ProfileListFragment newInstance() {
        ProfileListFragment fragment = new ProfileListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        if (showArchived) {
            profiles = ProfileCatalog.getProfilesSorted(getContext());
        } else {
            profiles = ProfileCatalog.getProfilesNotArchivedSorted(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_profile_list, container, false);
        initializeViews(contentView);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        //List
        {
            adapter = new DragAdapter();
            DragListView listView = contentView.findViewById(R.id.fragment_profile_list_list_view);
            listView.setLayoutManager(new LinearLayoutManager(getContext()));
            listView.setAdapter(adapter, true);
            listView.setCanDragHorizontally(false);
            listView.setDragListListener(new DragListener());
        }

        //Lookup
        {
            OnLookupItemClicked lookupItemClicked = new OnLookupItemClicked();
            ImageView more = contentView.findViewById(R.id.fragment_profile_list_lookup_more);
            ImageView edit = contentView.findViewById(R.id.fragment_profile_list_lookup_edit);
            ImageView archived = contentView.findViewById(R.id.fragment_profile_list_lookup_visibility);
            ImageView dismiss = contentView.findViewById(R.id.fragment_profile_list_lookup_dismiss);
            more.setOnClickListener(lookupItemClicked);
            edit.setOnClickListener(lookupItemClicked);
            archived.setOnClickListener(lookupItemClicked);
            dismiss.setOnClickListener(lookupItemClicked);
        }

        //Footer
        {
            OnFooterItemClicked footerItemClicked = new OnFooterItemClicked();
            ImageView newProfile = contentView.findViewById(R.id.fragment_profile_list_footer_new);
            ImageView visibility = contentView.findViewById(R.id.fragment_profile_list_footer_visibility);
            newProfile.setOnClickListener(footerItemClicked);
            visibility.setOnClickListener(footerItemClicked);
        }
    }

    private void tryUpdateViews() {
        View contentView = getView();
        if (contentView != null) {
            updateViews(contentView);
        }
    }

    private void updateViews(View contentView) {
        //Lookup
        {
            View lookupView = contentView.findViewById(R.id.fragment_profile_list_lookup);
            if (lookupView.getVisibility() == View.VISIBLE) {
                long profileId = ((Profile) lookupView.getTag()).getId();
                Profile profile = ProfileCatalog.getProfileById(profileId, getContext());

                ImageView visibility = contentView.findViewById(R.id.fragment_profile_list_lookup_visibility);
                if (profile.isArchived())
                    visibility.setImageResource(R.drawable.ic_hidden);
                else
                    visibility.setImageResource(R.drawable.ic_visibility);

                TextView title = lookupView.findViewById(R.id.fragment_profile_list_lookup_title);
                title.setText(profile.getName());
            }
        }

        //Footer
        {
            ImageView visibility = contentView.findViewById(R.id.fragment_profile_list_footer_visibility);
            if (showArchived) {
                visibility.setImageResource(R.drawable.ic_archive_black_24dp);
            } else {
                visibility.setImageResource(R.drawable.ic_unarchive_black_24dp);
            }
        }
    }

    //=================================== Communication Parent =====================================
    @Override
    public void updateContent() {
        if (showArchived) {
            profiles = ProfileCatalog.getProfilesSorted(getContext());
        } else {
            profiles = ProfileCatalog.getProfilesNotArchivedSorted(getContext());
        }
        if (adapter != null) {
            adapter.updateContent();
        }
        tryUpdateViews();
    }

    private void onCurrentProfileChanged() {
        OpenHelper.closeInstance();
        getListener().onProfileChanged();
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener) {
            return (OnFragmentInteractionListener) getParentFragment();
        } else if (getActivity() instanceof OnFragmentInteractionListener) {
            return (OnFragmentInteractionListener) getActivity();
        }
        return null;
    }

    public interface OnFragmentInteractionListener {
        void onProfileChanged();
    }

    //========================================== Adapter ===========================================
    private class DragAdapter extends DragItemAdapter<Profile, ProfileViewHolder> {
        public DragAdapter() {
            setHasStableIds(true);
            setItemList(profiles);
        }

        @Override
        public long getItemId(int position) {
            return profiles.get(position).getId();
        }

        @Override
        public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View itemView = inflater.inflate(R.layout.partial_profilelist_item, parent, false);
            ProfileViewHolder holder = new ProfileViewHolder(itemView);
            holder.layout.setOnClickListener(new OnItemClicked());
            holder.info.setOnClickListener(new OnInfoClicked());
            return holder;
        }

        @Override
        public void onBindViewHolder(ProfileViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            Profile profile = profiles.get(position);
            holder.title.setText(profile.getName());
            holder.itemView.setBackgroundColor(profile.getColor());
            holder.layout.setTag(profile);
            holder.info.setTag(profile);
        }

        public void updateContent() {
            setItemList(profiles);
        }
    }

    private static class ProfileViewHolder extends DragItemAdapter.ViewHolder{
        View itemView;
        ViewGroup layout;
        TextView title;
        View info;

        private ProfileViewHolder(View itemView) {
            super(itemView, R.id.partial_profilelist_item_layout, true);
            this.itemView = itemView;
            this.layout = itemView.findViewById(R.id.partial_profilelist_item_layout);
            this.title = itemView.findViewById(R.id.partial_profilelist_item_title);
            this.info = itemView.findViewById(R.id.partial_profilelist_item_info);
        }
    }

    //========================================= Listeners ==========================================
    private void onNewProfileClicked() {
        Intent intent = ProfileAveActivity.getIntentNew(getContext());
        int request = ACTIVITY_REQUEST_AVE;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(intent, request);
    }

    private void onDeleteProfileClicked(final Profile profile) {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.sentence_delete_profile_question)
                .setTitle(R.string.delete)
                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OpenHelper.closeInstance();
                        ProfileCatalog.deleteProfile(profile, getContext());
                        Databases.deleteDatabaseAsync(profile, getContext());
                        PictureOperations.deleteProfileDirAsync(profile, getContext());
                        Toast.makeText(getContext(), R.string.sentence_profile_deleted, Toast.LENGTH_SHORT).show();
                        onCurrentProfileChanged();
                    }
                }).setNegativeButton(R.string.action_no, null)
                .setNeutralButton(R.string.action_yes_keep_photos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OpenHelper.closeInstance();
                        ProfileCatalog.deleteProfile(profile, getContext());
                        Databases.deleteDatabaseAsync(profile, getContext());
                        Toast.makeText(getContext(), R.string.sentence_profile_deleted_photos_kept, Toast.LENGTH_SHORT).show();
                        onCurrentProfileChanged();
                    }
                }).show();
    }

    private void onEditProfileClicked(Profile profile) {
        Intent intent = ProfileAveActivity.getIntentEdit(profile, getContext());
        startActivityForResult(intent, ACTIVITY_REQUEST_AVE);
    }

    private void onProfileArchiveStateClicked(Profile profile) {
        profile.setArchived(!profile.isArchived());
        ProfileCatalog.updateProfile(profile, getContext());
        updateContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Ave
        {
            int request = ACTIVITY_REQUEST_AVE;
            if (getActivity() instanceof ActivityRequestHost) {
                ActivityRequestHost host = (ActivityRequestHost) getActivity();
                request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
            }
            if (request == requestCode) {
                if (resultCode == Activity.RESULT_OK) {
                    updateContent();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class OnItemClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Profile profile = (Profile) v.getTag();
            ProfileCatalog.setCurrentProfile(profile, getContext());
            onCurrentProfileChanged();
        }
    }

    private class OnInfoClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Profile profile = (Profile) v.getTag();
            View contentView = getView();
            if (contentView != null) {
                View lookupLayout = contentView.findViewById(R.id.fragment_profile_list_lookup);
                lookupLayout.setTag(profile);
                lookupLayout.setVisibility(View.VISIBLE);
                updateViews(contentView);

                {
                    final TextView sizeText = contentView.findViewById(R.id.fragment_profile_list_lookup_size);
                    sizeText.setText("...");
                    final File databaseFile = Databases.getDatabaseByProfile(profile, getContext());
                    AsyncTask<Void, Void, Long> task = new AsyncTask<Void, Void, Long>() {
                        @Override
                        protected Long doInBackground(Void... params) {
                            return databaseFile.length();
                        }

                        @Override
                        protected void onPostExecute(Long bytes) {
                            sizeText.setText(FileUtil.humanReadableByteCount(bytes, false));
                        }
                    };
                    task.execute();
                }

                {
                    final TextView sizeTotalText = contentView.findViewById(R.id.fragment_profile_list_lookup_size_total);
                    sizeTotalText.setText("...");
                    final File pictures = Pictures.getProfileDir(profile);
                    AsyncTask<Void, Void, Long> task = new AsyncTask<Void, Void, Long>() {
                        @Override
                        protected Long doInBackground(Void... params) {
                            return FileUtil.directorySize(pictures);
                        }

                        @Override
                        protected void onPostExecute(Long bytes) {
                            sizeTotalText.setText(FileUtil.humanReadableByteCount(bytes, false));
                        }
                    };
                    task.execute();
                }

            }

        }
    }

    private class OnLookupItemClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            View contentView = getView();
            if (contentView == null) {
                return;
            }
            View lookupLayout = contentView.findViewById(R.id.fragment_profile_list_lookup);
            final Profile profile = (Profile) lookupLayout.getTag();
            if (v.getId() == R.id.fragment_profile_list_lookup_more) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.profile_lookup_more_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.profile_lookup_more_menu_delete) {
                            onDeleteProfileClicked(profile);
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            } else if (v.getId() == R.id.fragment_profile_list_lookup_edit) {
                onEditProfileClicked(profile);
            } else if (v.getId() == R.id.fragment_profile_list_lookup_visibility) {
                onProfileArchiveStateClicked(profile);
            } else if (v.getId() == R.id.fragment_profile_list_lookup_dismiss) {
                lookupLayout.setVisibility(View.GONE);
            }
        }
    }

    private class OnFooterItemClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fragment_profile_list_footer_new) {
                onNewProfileClicked();
            } else if (v.getId() == R.id.fragment_profile_list_footer_visibility) {
                showArchived = !showArchived;
                updateContent();
            }
        }
    }

    public class DragListener implements DragListView.DragListListener {
        @Override
        public void onItemDragStarted(int position) {

        }

        @Override
        public void onItemDragging(int itemPosition, float x, float y) {

        }

        @Override
        public void onItemDragEnded(int fromPosition, int toPosition) {
            ProfileCatalog.beginTransaction(getContext());
            for (int i = 0; i < profiles.size(); i++) {
                Profile profile = profiles.get(i);
                profile.setPosition(i);
                ProfileCatalog.updateProfile(profile, getContext());
            }
            ProfileCatalog.setTransactionSuccessful(getContext());
            ProfileCatalog.endTransaction(getContext());
        }
    }
}
