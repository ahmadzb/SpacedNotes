package com.diplinkblaze.spacednote.note;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;

import java.io.Serializable;

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


        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() == null || !getIntent().getExtras().containsKey(KEY_NOTE_SELECTOR))
            throw new RuntimeException("Please use the intent maker method \"getIntent()\" instead");

        if (getSupportFragmentManager().findFragmentByTag(TAG_LIST_FRAGMENT) == null) {
            NoteSelector noteSelector = (NoteSelector) getIntent().getSerializableExtra(KEY_NOTE_SELECTOR);
            NoteListFragment fragment = NoteListFragment.newInstance(noteSelector);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.activity_note_list_frame, fragment, TAG_LIST_FRAGMENT);
            transaction.commit();
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
        }
    }
}
