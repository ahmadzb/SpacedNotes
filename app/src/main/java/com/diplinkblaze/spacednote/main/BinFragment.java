package com.diplinkblaze.spacednote.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BackSupportListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.note.NoteListActivity;
import com.diplinkblaze.spacednote.note.NoteSelectors;
import com.diplinkblaze.spacednote.note.NoteViewActivity;
import com.diplinkblaze.spacednote.universal.fragments.ListFragment;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import java.util.List;

import data.database.OpenHelper;
import data.database.file.FileOpenHelper;
import data.model.note.Note;

public class BinFragment extends Fragment implements ContentUpdateListener, BackSupportListener,
        ListFragment.OnFragmentInteractionListener{

    private static final String TAG_LIST = "list";

    public BinFragment() {

    }

    public static BinFragment newInstance() {
        BinFragment fragment = new BinFragment();
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
        View contentView = inflater.inflate(R.layout.fragment_bin, container, false);
        initializeViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(TAG_LIST);
        if (fragment == null) {
            fragment = ListFragment.newInstance(TAG_LIST);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_bin_content, fragment, TAG_LIST);
            transaction.commit();
        }

    }

    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }
    //================================= Communication Children =====================================
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        return ListUtil.Deleted.create(context, OpenHelper.getDatabase(context));
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier) {
        if (entity instanceof ListUtil.Wrapper.WrapperEntity) {
            ListData.Entity innerEntity = ((ListUtil.Wrapper.WrapperEntity) entity).getInnerEntity();
            if (innerEntity instanceof ListUtil.Label.LabelEntity) {
                ListUtil.Label.LabelEntity labelEntity = (ListUtil.Label.LabelEntity) innerEntity;
                if (labelEntity.getLabel() != null) {
                    long labelId = labelEntity.getId();
                    Intent intent = NoteListActivity.getIntent(
                            NoteSelectors.LabelNoteSelector.newInstance(labelId), getContext());
                    startActivity(intent);
                }
            } else if (innerEntity instanceof ListUtil.Note.NoteEntity) {
                ListUtil.Note.NoteEntity noteEntity = (ListUtil.Note.NoteEntity) innerEntity;
                if (noteEntity.getNote() != null) {
                    long noteId = noteEntity.getId();
                    Intent intent = NoteViewActivity.getIntentReadOnly(noteId, getContext());
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onUniversalListItemMoreSelected(final ListData.Entity entity, View moreView, String tag, Bundle identifier) {
        PopupMenu popupMenu = new PopupMenu(getContext(), moreView);
        popupMenu.inflate(R.menu.bin_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.bin_menu_delete) {
                    onEntityDeleteClicked((ListUtil.Wrapper.WrapperEntity) entity);
                    return true;
                } else if (item.getItemId() == R.id.bin_menu_restore) {
                    onEntityRestoreClicked((ListUtil.Wrapper.WrapperEntity) entity);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, String tag, Bundle identifier) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo, String tag, Bundle identifier) {
        //Nothing relative here
    }

    private void onEntityDeleteClicked(final ListUtil.Wrapper.WrapperEntity deleteEntity) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_forever)
                .setMessage(R.string.sentence_delete_item_forever_question)
                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListUtil.Deleted.delete(deleteEntity, OpenHelper.getDatabase(getContext()),
                                FileOpenHelper.getDatabase(getContext()), getContext());
                        Toast.makeText(getContext(), R.string.sentence_item_deleted_forever, Toast.LENGTH_SHORT).show();
                        updateContent();
                    }
                }).setNegativeButton(R.string.action_no, null).show();
    }

    private void onEntityRestoreClicked(ListUtil.Wrapper.WrapperEntity restoreEntity) {
        ListUtil.Deleted.restore(restoreEntity, OpenHelper.getDatabase(getContext()), getContext());
        Toast.makeText(getContext(), R.string.sentence_item_restored, Toast.LENGTH_SHORT).show();
        updateContent();
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
