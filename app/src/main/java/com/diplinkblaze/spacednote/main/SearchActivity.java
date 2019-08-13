package com.diplinkblaze.spacednote.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.NoActionbarActivity;
import com.diplinkblaze.spacednote.note.NoteListActivity;
import com.diplinkblaze.spacednote.note.NoteSelector;
import com.diplinkblaze.spacednote.note.NoteSelectors;

public class SearchActivity extends NoActionbarActivity {

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initializeContent();
    }

    private void initializeContent() {
        View viewBack = findViewById(R.id.activity_search_back);
        View viewSearch = findViewById(R.id.activity_search_search);
        EditText viewText = findViewById(R.id.activity_search_text);
        viewBack.setOnClickListener(new OnBackClickListener());
        viewSearch.setOnClickListener(new OnSearchClickListener());
        viewText.setOnEditorActionListener(new OnEnterHitListener());
    }

    private void search(String keyword) {
        NoteSelector noteSelector = NoteSelectors.SearchNoteSelector.newInstance(keyword);
        Intent intent = NoteListActivity.getIntent(noteSelector, this);
        startActivity(intent);
    }

    private void back() {
        finish();
    }

    private class OnSearchClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            EditText editText = findViewById(R.id.activity_search_text);
            String keyword = editText.getText().toString();
            if (keyword != null && !keyword.isEmpty()) {
                search(keyword);
            }
        }
    }

    private class OnBackClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            back();
        }
    }

    private class OnEnterHitListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                String keyword = textView.getText().toString();
                if (keyword != null && !keyword.isEmpty()) {
                    search(keyword);
                    return true;
                }
            return false;
        }
    }
}
