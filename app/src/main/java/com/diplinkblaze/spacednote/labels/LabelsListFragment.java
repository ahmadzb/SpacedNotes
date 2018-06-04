package com.diplinkblaze.spacednote.labels;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.contract.NewItemSupportListener;
import com.diplinkblaze.spacednote.note.NoteListActivity;
import com.diplinkblaze.spacednote.note.NoteSelector;
import com.diplinkblaze.spacednote.note.NoteSelectors;
import com.diplinkblaze.spacednote.universal.fragments.AveFragment;
import com.diplinkblaze.spacednote.universal.fragments.ListFragment;
import com.diplinkblaze.spacednote.universal.util.AveComponentSet;
import com.diplinkblaze.spacednote.universal.util.AveUtil;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import java.util.ArrayList;

import data.database.OpenHelper;
import data.model.label.Label;
import data.model.label.LabelCatalog;

public class LabelsListFragment extends Fragment implements ListFragment.OnFragmentInteractionListener,
        AveFragment.OnFragmentInteractionListener, ContentUpdateListener, NewItemSupportListener {

    private static final String TAG_LIST = "list";
    private static final String TAG_AVE = "ave";
    private static final int ACTIVITY_REQUEST_AVE = 0;

    public LabelsListFragment() {
        // Required empty public constructor
    }

    public static LabelsListFragment newInstance() {
        LabelsListFragment fragment = new LabelsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_label_list, container, false);
        initializeViews(view);
        return view;
    }

    private void initializeViews(View contentView) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(TAG_LIST);
        if (fragment == null) {
            fragment = ListFragment.newInstance(TAG_LIST);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_labels_list_frame, fragment, TAG_LIST);
            transaction.commit();
        }
    }

    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }

    //========================================= List ==============================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        return ListUtil.Label.create(OpenHelper.getDatabase(context));
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {
        ListUtil.Label.LabelEntity labelEntity = (ListUtil.Label.LabelEntity) entity;
        NoteSelector noteSelector = NoteSelectors.LabelNoteSelector.newInstance(labelEntity.getLabel().getId());
        Intent intent = NoteListActivity.getIntent(noteSelector, getContext());
        startActivity(intent);
    }

    @Override
    public void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView, String tag, Bundle identifier) {
        ListUtil.Label.LabelEntity labelEntity = (ListUtil.Label.LabelEntity) entity;
        AveComponentSet componentSet = AveUtil.Label.create(labelEntity.getLabel());
        componentSet.setStateView();
        AveFragment fragment = AveFragment.newInstance(componentSet);
        fragment.show(getChildFragmentManager(), TAG_AVE);
    }

    @Override
    public void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo, String tag, Bundle identifier) {

    }

    //========================================== Ave ===============================================
    @Override
    public void onSaveResult(AveComponentSet componentSet) {

    }

    @Override
    public boolean onEditPressed(AveComponentSet componentSet) {
        Label label = LabelCatalog.getLabelById(componentSet.id, OpenHelper.getDatabase(getContext()));
        Intent intent = LabelAveActivity.getIntent(label, getContext());
        int request = ACTIVITY_REQUEST_AVE;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(intent, request);
        onFinish(false);
        return true;
    }

    @Override
    public void onFinish(boolean success) {
        AveFragment fragment = (AveFragment) getChildFragmentManager().findFragmentByTag(TAG_AVE);
        if (fragment != null)
            fragment.dismiss();
    }

    @Override
    public void onMenuClicked(int itemId, AveComponentSet componentSet) {
        if (itemId == R.id.label_menu_delete) {
            final Label label = Label.newInstance().setId(componentSet.id);
            LabelCatalog.markLabelAsDeleted(label, OpenHelper.getDatabase(getContext()), getContext());
            updateContent();
            View view = getView();
            if (view != null) {
                Snackbar.make(view, R.string.sentence_item_moved_to_bin, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LabelCatalog.markLabelAsNotDeleted(label, OpenHelper.getDatabase(getContext()), getContext());
                                updateContent();
                            }
                        }).show();
            }
        }
        onFinish(false);
    }

    @Override
    public ArrayList<AveComponentSet.ViewComponent> getViewComponents(AveComponentSet componentSet) {
        return null;
    }

    //======================================= User Interaction =====================================
    @Override
    public void newItem() {
        Intent intent = LabelAveActivity.getIntent(null, getContext());
        int request = ACTIVITY_REQUEST_AVE;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(intent, request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Ave
        {
            if (resultCode == Activity.RESULT_OK) {
                int request = ACTIVITY_REQUEST_AVE;
                if (getActivity() instanceof ActivityRequestHost) {
                    ActivityRequestHost host = (ActivityRequestHost) getActivity();
                    request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
                    if (request == requestCode) {
                        updateContent();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
