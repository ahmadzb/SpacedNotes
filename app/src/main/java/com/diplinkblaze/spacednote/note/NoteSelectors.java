package com.diplinkblaze.spacednote.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;

import data.model.label.Label;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.scheduler.RevisionCatalog;
import data.model.scheduler.RevisionFuture;
import util.datetime.primitive.Representation;

/**
 * Created by Ahmad on 01/21/18.
 * All rights reserved.
 */

public class NoteSelectors {

    public static class NotesNotDeletedSelector extends NoteSelector {

        @Override
        protected ArrayList<Note> getNotes(Context context, SQLiteDatabase readableDb) {
            ArrayList<Note> notes = NoteCatalog.getNotesNotDeleted(readableDb);
            Collections.sort(notes, Note.createDateComparator());
            return notes;
        }
    }

    public static class NotesDeletedSelector extends NoteSelector {

        @Override
        protected ArrayList<Note> getNotes(Context context, SQLiteDatabase readableDb) {
            ArrayList<Note> notes = NoteCatalog.getNotesDeleted(readableDb);
            Collections.sort(notes, Note.createDateComparator());
            return notes;
        }
    }

    public static class LabelNoteSelector extends NoteSelector {

        private long labelId;

        private LabelNoteSelector() {

        }

        public static LabelNoteSelector newInstance(long labelId) {
            LabelNoteSelector instance = new LabelNoteSelector();
            instance.labelId = labelId;
            return instance;
        }

        @Override
        protected ArrayList<Note> getNotes(Context context, SQLiteDatabase readableDb) {
            ArrayList<Note> notes = NoteCatalog.getNotesByLabel(labelId, readableDb, true);
            Collections.sort(notes, Note.createDateComparator());
            return notes;
        }

        @Override
        protected boolean shouldHighlightNote(Note note) {
            int today = Representation.fromLocalDate(LocalDate.now());
            return note.getRevisionFuture() != null && note.getRevisionFuture().getDueDate() <= today;
        }

        @Override
        protected void onNoteNextRevisionClicked(Note note, SQLiteDatabase readableDb) {
            RevisionFuture updatedRevisionFuture = RevisionCatalog.getRevisionFutureForNote(note, readableDb);
            note.setRevisionFuture(updatedRevisionFuture);
        }

        @Override
        protected ArrayList<Long> initializeLabels() {
            ArrayList<Long> labels = new ArrayList<>();
            labels.add(labelId);
            return labels;
        }
    }
}
