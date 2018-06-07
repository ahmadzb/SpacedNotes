package com.diplinkblaze.spacednote.note;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.contract.NewItemSupportListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NoteListActivity extends BaseActivity {

    private static final String TAG_LIST_FRAGMENT = "listFragment";
    private static final String KEY_NOTE_SELECTOR = "noteSelector";

    public static Intent getIntent(NoteSelector noteSelector, Context context) {
        Intent intent = new Intent(context, NoteListActivity.class);
        intent.putExtra(KEY_NOTE_SELECTOR, noteSelector);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() == null || !getIntent().getExtras().containsKey(KEY_NOTE_SELECTOR))
            throw new RuntimeException("Please use the intent maker method \"getIntent()\" instead");

        NoteSelector noteSelector = (NoteSelector) getIntent().getSerializableExtra(KEY_NOTE_SELECTOR);
        if (getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT) == null) {
            NoteListFragment fragment = NoteListFragment.newInstance(noteSelector);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.activity_note_list_frame, fragment, TAG_LIST_FRAGMENT);
            transaction.commit();
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new OnFabClicked());
        if (noteSelector.supportsNewNote()) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ContentUpdateUtil.updateContentChildren(this);
            setResult(RESULT_OK);
        }
    }

    class OnFabClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof NewItemSupportListener) {
                        ((NewItemSupportListener) fragment).newItem();
                    }
                }
            }
        }
    }
}
