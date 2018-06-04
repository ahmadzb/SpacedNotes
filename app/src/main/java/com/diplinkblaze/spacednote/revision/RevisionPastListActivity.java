package com.diplinkblaze.spacednote.revision;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.universal.fragments.CalendarFragment;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;

import data.database.OpenHelper;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.scheduler.RevisionCatalog;
import data.model.scheduler.RevisionPast;
import data.model.scheduler.Scheduler;
import data.preference.ChronologyCatalog;
import util.datetime.format.DateTimeFormat;
import util.datetime.format.DateTimeFormatter;
import util.datetime.primitive.Representation;

public class RevisionPastListActivity extends BaseActivity implements
        CalendarFragment.OnFragmentInteractionListener, ContentUpdateListener{

    private static final String TAG_FRAGMENT_CALENDAR = "fragmentCalendar";

    private static final String KEY_LIST_SUPPLIER = "listSupplier";
    private static final String KEY_NOTE = "note";

    private static final int LIST_SUPPLIER_NOTE = 0;

    private Note note;
    private int listSupplier;

    private Adapter adapter;

    public static Intent getIntentForNote(Note note, Context context) {
        Intent intent = new Intent(context, RevisionPastListActivity.class);
        intent.putExtra(KEY_LIST_SUPPLIER, LIST_SUPPLIER_NOTE);
        intent.putExtra(KEY_NOTE, note.getId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revision_past_list);
        listSupplier = getIntent().getIntExtra(KEY_LIST_SUPPLIER, -1);
        if (listSupplier == LIST_SUPPLIER_NOTE) {
            note = NoteCatalog.getNoteById(getIntent().getLongExtra(KEY_NOTE, -1),
                    OpenHelper.getDatabase(this));
        }
        initializeViews();

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.revision_past_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.revision_past_menu_add) {
            CalendarFragment.DateState dateState = new CalendarFragment.DateState();
            dateState.currentDate = LocalDate.now(ChronologyCatalog.getCurrentChronology(this));
            CalendarFragment calendarFragment = CalendarFragment.newInstance(
                    dateState, null, dateState.currentDate, null, null, this);
            calendarFragment.show(getSupportFragmentManager(), TAG_FRAGMENT_CALENDAR);
        }
        return super.onOptionsItemSelected(item);
    }


    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.activity_revision_past_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (listSupplier == LIST_SUPPLIER_NOTE) {
            SQLiteDatabase database = OpenHelper.getDatabase(this);
            ArrayList<RevisionPast> revisionPasts = RevisionCatalog.getRevisionPastsForNote(note, database);
            Collections.sort(revisionPasts, RevisionPast.getDateComparator());
            adapter = new Adapter(revisionPasts);
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateContent() {
        if (adapter != null) {
            SQLiteDatabase database = OpenHelper.getDatabase(this);
            ArrayList<RevisionPast> revisionPasts = RevisionCatalog.getRevisionPastsForNote(note, database);
            Collections.sort(revisionPasts, RevisionPast.getDateComparator());
            adapter.updateRevisionPasts(revisionPasts);
        }
    }

    @Override
    public void onDateSelected(CalendarFragment.DateState dateState, String tag, Bundle identifier) {
        CalendarFragment fragment = (CalendarFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_FRAGMENT_CALENDAR);
        fragment.dismiss();
        SQLiteDatabase database = OpenHelper.getDatabase(this);
        ArrayList<RevisionPast> revisionPasts = RevisionCatalog.getRevisionPastsForNote(note, database);
        int selectedDate = Representation.fromLocalDate(dateState.currentDate);
        boolean exists = false;
        for (RevisionPast revisionPast : revisionPasts) {
            if (revisionPast.getDate() == selectedDate) {
                exists = true;
            }
        }
        if (!exists) {
            RevisionPast newRevisionPast = RevisionPast.newInstance()
                    .setNoteId(note.getId())
                    .setDate(selectedDate)
                    .setInitialized(true);
            database.beginTransaction();
            RevisionCatalog.addRevisionPast(newRevisionPast, database, this);
            Scheduler.reappointFutureRevision(note, database, this);
            database.setTransactionSuccessful();
            database.endTransaction();
            updateContent();
            setResult(RESULT_OK);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        ArrayList<RevisionPast> revisionPasts;
        DateTimeFormatter formatter;

        public Adapter(ArrayList<RevisionPast> revisionPasts) {
            this.revisionPasts = revisionPasts;
            this.formatter = DateTimeFormat.fullDate(getResources());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.partial_revisionlist_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RevisionPast revisionPast = revisionPasts.get(position);
            holder.title.setText(formatter.print(
                    Representation.toLocalDate(revisionPast.getDate())));
            holder.remove.setTag(revisionPast);
        }

        @Override
        public int getItemCount() {
            return revisionPasts.size();
        }

        void updateRevisionPasts(ArrayList<RevisionPast> revisionPasts) {
            this.revisionPasts = revisionPasts;
            notifyDataSetChanged();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        ImageView remove;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.partial_revisionlist_item_text);
            remove = itemView.findViewById(R.id.partial_revisionlist_item_remove);
            remove.setOnClickListener(new OnRevisionRemoveClickListener());
        }
    }

    private class OnRevisionRemoveClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            new AlertDialog.Builder(RevisionPastListActivity.this)
                    .setMessage(R.string.sentence_delete_item_question)
                    .setTitle(R.string.delete)
                    .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RevisionPast revisionPast = (RevisionPast) v.getTag();
                            Context context = RevisionPastListActivity.this;
                            SQLiteDatabase database = OpenHelper.getDatabase(context);
                            database.beginTransaction();
                            RevisionCatalog.deleteRevisionPast(revisionPast, database, context);
                            Scheduler.reappointFutureRevision(note, database, context);
                            database.setTransactionSuccessful();
                            database.endTransaction();
                            updateContent();
                            setResult(RESULT_OK);
                        }
                    }).setNegativeButton(R.string.action_no, null).show();
        }
    }
}
