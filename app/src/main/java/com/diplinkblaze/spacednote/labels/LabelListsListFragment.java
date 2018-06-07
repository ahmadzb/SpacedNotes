package com.diplinkblaze.spacednote.labels;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.ActivityRequestHostUtils;
import com.diplinkblaze.spacednote.contract.BackSupportListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.contract.NewItemSupportListener;
import com.diplinkblaze.spacednote.note.NoteListActivity;
import com.diplinkblaze.spacednote.note.NoteSelectors;
import com.diplinkblaze.spacednote.universal.fragments.ListFragment;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import java.util.ArrayList;
import java.util.List;

import data.database.OpenHelper;
import data.model.label.Label;
import data.model.label.LabelCatalog;
import data.model.label.LabelList;
import data.model.label.LabelListCatalog;

public class LabelListsListFragment extends Fragment implements ListFragment.OnFragmentInteractionListener,
        ContentUpdateListener, NewItemSupportListener, BackSupportListener {

    private static final String TAG_LIST = "list";

    private static final String KEY_LABEL_LIST_ID = "labelList.Id";

    private static final int ACTIVITY_REQUEST_AVE = 0;
    private static final int ACTIVITY_REQUEST_LABEL_CHOOSER = 1;
    private static final int ACTIVITY_REQUEST_NOTE_LIST = 2;

    public LabelListsListFragment() {
        // Required empty public constructor
    }

    public static LabelListsListFragment newInstance() {
        LabelListsListFragment fragment = new LabelListsListFragment();
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
        View contentView = inflater.inflate(R.layout.fragment_label_lists, container, false);
        initializeViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(TAG_LIST);
        if (fragment == null) {
            fragment = ListFragment.newInstance(TAG_LIST);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_label_lists_frame, fragment, TAG_LIST);
            transaction.commit();
        }

    }

    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }

    //=========================================== List =============================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
            return ListUtil.LabelList.createTree(OpenHelper.getDatabase(context), true);
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {
        ListUtil.LabelList.updatePositions(data, rootEntity, OpenHelper.getDatabase(getContext()), getContext());
    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {
        ListUtil.LabelList.LabelListEntity labelListEntity = (ListUtil.LabelList.LabelListEntity) entity;
        if (labelListEntity.getLabel() != null) {
            long labelId = labelListEntity.getLabel().getId();
            Intent intent = NoteListActivity.getIntent(
                    NoteSelectors.LabelNoteSelector.newInstance(labelId), getContext());
            int request = ACTIVITY_REQUEST_NOTE_LIST;
            if (getActivity() instanceof ActivityRequestHost) {
                ActivityRequestHost host = (ActivityRequestHost) getActivity();
                request = ActivityRequestHostUtils.toGlobalRequest(request, host, this);
            }
            startActivityForResult(intent, request);
        }
    }

    @Override
    public void onUniversalListItemMoreSelected(final ListData.Entity entity, View moreView, String tag, Bundle identifier) {
        PopupMenu popupMenu = new PopupMenu(getContext(), moreView);
        popupMenu.getMenuInflater().inflate(R.menu.label_list_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.label_list_popup_menu_edit) {
                    editItem(entity);
                    return true;
                } else if (item.getItemId() == R.id.label_list_popup_menu_labels) {
                    changeLabels(entity);
                    return true;
                } else if (item.getItemId() == R.id.label_list_popup_menu_delete) {
                    deleteItem(entity);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo, String tag, Bundle identifier) {

    }

    //======================================= User Interaction =====================================
    @Override
    public void newItem() {
        Intent intent = LabelListAveActivity.getIntent(null, getContext());
        int request = ACTIVITY_REQUEST_AVE;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(intent, request);
    }

    private void deleteItem(final ListData.Entity entity) {
        new AlertDialog.Builder(getContext()).setTitle(R.string.delete).setMessage(R.string.sentence_delete_item_question)
                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListUtil.LabelList.LabelListEntity labelListEntity = (ListUtil.LabelList.LabelListEntity) entity;
                        LabelListCatalog.deleteLabelList(labelListEntity.getLabelList(), OpenHelper.getDatabase(getContext()), getContext());
                        updateContent();
                    }
                }).setNegativeButton(R.string.action_no, null).show();
    }

    public void editItem(ListData.Entity entity) {
        ListUtil.LabelList.LabelListEntity labelListEntity = (ListUtil.LabelList.LabelListEntity) entity;
        LabelList labelList = labelListEntity.getLabelList();
        Intent intent = LabelListAveActivity.getIntent(labelList, getContext());
        int request = ACTIVITY_REQUEST_AVE;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(intent, request);
    }

    private void changeLabels(ListData.Entity entity) {
        ArrayList<Label> labels = LabelCatalog.getLabelsByLabelList(entity.getId(), OpenHelper.getDatabase(getContext()));
        ArrayList<Long> labelIds = new ArrayList<>(labels.size());
        for (Label label : labels)
            labelIds.add(label.getId());
        Bundle identifier = new Bundle();
        identifier.putLong(KEY_LABEL_LIST_ID, entity.getId());
        Intent intent = LabelChooserActivity.getIntent(labelIds, getContext(), identifier);

        int request = ACTIVITY_REQUEST_LABEL_CHOOSER;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(intent, request);
    }

    private void changeLabelsResult(Intent result) {
        ArrayList<Long> selection = LabelChooserActivity.getSelectionFromResult(result);
        Bundle identifier = LabelChooserActivity.getIdentifierFromResult(result);
        long labelListId = identifier.getLong(KEY_LABEL_LIST_ID);
        LabelList labelList = LabelList.newInstance().setId(labelListId);

        SQLiteDatabase database = OpenHelper.getDatabase(getContext());
        database.beginTransaction();
        LabelListCatalog.updateLabelListLabels(selection, labelList, database, getContext());
        database.setTransactionSuccessful();
        database.endTransaction();
        updateContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //ave
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
        //label chooser
        {
            int request = ACTIVITY_REQUEST_LABEL_CHOOSER;
            if (getActivity() instanceof ActivityRequestHost) {
                ActivityRequestHost host = (ActivityRequestHost) getActivity();
                request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
            }
            if (request == requestCode) {
                if (resultCode == Activity.RESULT_OK) {
                    changeLabelsResult(data);
                }
            }
        }
        //note list
        {
            int request = ACTIVITY_REQUEST_NOTE_LIST;
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

    @Override
    public boolean onBackPressed() {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof BackSupportListener) {
                    boolean back = ((BackSupportListener) fragment).onBackPressed();
                    if (back) return true;
                }
            }
        }
        return false;
    }
}
