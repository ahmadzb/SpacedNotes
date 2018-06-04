package com.diplinkblaze.spacednote.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;

import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

import data.database.OpenHelper;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.scheduler.RevisionCatalog;
import data.preference.ChronologyCatalog;
import util.datetime.format.DateTimeFormat;
import util.datetime.format.DateTimeFormatter;
import util.datetime.primitive.Representation;

public class TimelineFragment extends Fragment implements ContentUpdateListener {

    private static final String KEY_CURRENT_DATE = "currentDate";

    private static final String TAG_NOTE_LIST_FRAGMENT = "noteListFragment";

    private OnViewClickListener onViewClickListener = new OnViewClickListener();

    private LocalDate currentDate;

    public TimelineFragment() {
        // Required empty public constructor
    }

    public static TimelineFragment newInstance() {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentDate = Representation.toLocalDate(savedInstanceState.getInt(KEY_CURRENT_DATE));
        } else {
            currentDate = LocalDate.now(ChronologyCatalog.getCurrentChronology(getContext()));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_DATE, Representation.fromLocalDate(currentDate));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_timeline, container, false);
        initializeViews(contentView);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        NoteListFragment fragment = (NoteListFragment) getChildFragmentManager()
                .findFragmentByTag(TAG_NOTE_LIST_FRAGMENT);
        if (fragment == null) {
            resetNoteListFragment();
        }
        View previousButton = contentView.findViewById(R.id.fragment_timeline_page_previous);
        View nextButton = contentView.findViewById(R.id.fragment_timeline_page_next);
        previousButton.setOnClickListener(onViewClickListener);
        nextButton.setOnClickListener(onViewClickListener);
    }

    private void tryUpdateViews() {
        View contentView = getView();
        if (contentView != null) {
            updateViews(contentView);
        }
    }

    private void updateViews(View contentView) {
        TextView pageTitle = contentView.findViewById(R.id.fragment_timeline_page_title);
        LocalDate now = LocalDate.now(currentDate.getChronology().withZone(DateTimeZone.getDefault()));
        String text = DateTimeFormat.fullDate(getResources()).print(currentDate);
        if (currentDate.equals(now)) {
            text = text + " (" + getString(R.string.today) + ")";
        } else if (currentDate.equals(now.plusDays(1))) {
            text = text + " (" + getString(R.string.tomorrow) + ")";
        } else if (currentDate.equals(now.plusDays(-1))) {
            text = text + " (" + getString(R.string.yesterday) + ")";
        }
        pageTitle.setText(text);
    }

    @Override
    public void updateContent() {
        ContentUpdateUtil.updateContentChildren(this);
    }

    private void resetNoteListFragment() {
        NoteListFragment fragment = NoteListFragment.newInstance(new TimelineNoteSelector(currentDate));
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_timeline_page_frame, fragment, TAG_NOTE_LIST_FRAGMENT);
        transaction.commit();
    }

    private class OnViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fragment_timeline_page_previous) {
                currentDate = currentDate.plusDays(-1);
                tryUpdateViews();
                resetNoteListFragment();
            } else if (v.getId() == R.id.fragment_timeline_page_next) {
                currentDate  = currentDate.plusDays(1);
                tryUpdateViews();
                resetNoteListFragment();
            }
        }
    }

    private static class TimelineNoteSelector extends NoteSelector {

        int datePrimitive;

        public TimelineNoteSelector(LocalDate date) {
            this.datePrimitive = Representation.fromLocalDate(date);
        }

        @Override
        protected ArrayList<Note> getNotes(Context context, SQLiteDatabase readableDb) {
            LocalDate date = Representation.toLocalDate(datePrimitive);
            LocalDate now = LocalDate.now(date.getChronology().withZone(DateTimeZone.getDefault()));
            ArrayList<Note> revisionFutureNotes;
            if (date.isAfter(now)) {
                revisionFutureNotes = NoteCatalog.getNotesByRevisionFutureDate(date, readableDb, true);
            } else if (date.isBefore(now)) {
                revisionFutureNotes = new ArrayList<>();
            } else {
                revisionFutureNotes = NoteCatalog.getNotesByRevisionFutureRange(null, date, readableDb, true);
            }
            ArrayList<Note> notes = NoteCatalog.getNotesByRevisionPastDate(date, readableDb);
            notes.addAll(revisionFutureNotes);
            Collections.sort(notes, Note.revisionFutureDueDateThenNoteDateComparator());
            return notes;
        }

        @Override
        protected boolean shouldHighlightNote(Note note) {
            if (note.getRevisionFuture() == null) {
                return false;
            } else {
                return true;//TODO
            }
        }

        @Override
        protected void onNoteNextRevisionClicked(Note note, SQLiteDatabase readableDb) {
            note.setRevisionFuture(null);
        }
    }
}
