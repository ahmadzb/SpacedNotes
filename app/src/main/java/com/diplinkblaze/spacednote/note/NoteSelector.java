package com.diplinkblaze.spacednote.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

import data.model.note.Note;

/**
 * Created by Ahmad on 01/21/18.
 * All rights reserved.
 */
public abstract class NoteSelector implements Serializable {
    protected abstract ArrayList<Note> getNotes(Context context, SQLiteDatabase readableDb);

    protected boolean shouldHighlightNote(Note note) {
        return false;
    }

    protected void onNoteNextRevisionClicked(Note note, SQLiteDatabase readableDb) {

    }

    protected boolean supportsNewNote() {
        return true;
    }

    protected ArrayList<Long> initializeLabels() {
        return null;
    }
}