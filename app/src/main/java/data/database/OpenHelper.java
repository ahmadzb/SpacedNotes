package data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;

/**
 * Created by Ahmad on 01/02/18.
 * All rights reserved.
 */

public class OpenHelper extends SQLiteOpenHelper {

    public static final int version = 1;
    public static final String namePrefix = "database";
    public static final String nameSuffix = ".db";

    private static OpenHelper mInstance;
    private static SQLiteDatabase mDatabase;

    private OpenHelper(Context context) {
        super(context, getDatabaseName(ProfileCatalog.getCurrentProfile(context)), null, version);
    }

    private OpenHelper(Profile profile, Context context) {
        super(context, getDatabaseName(profile), null, version);
    }

    public static String getDatabaseName(Profile profile) {
        return namePrefix + profile.getId() + nameSuffix;
    }

    public static OpenHelper getInstance(Context context) {
        if (mInstance == null)
            mInstance = new OpenHelper(context);
        return mInstance;
    }

    public static void closeInstance() {
        if (mInstance != null) {
            mDatabase.close();
            mInstance.close();
            mInstance = null;
            mDatabase = null;
        }
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (mDatabase == null)
            mDatabase = getInstance(context).getWritableDatabase();
        return mDatabase;
    }

    public static SQLiteDatabase instantiateDatabaseForProfile(Profile profile, Context context) {
        return new OpenHelper(profile, context).getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Tables.Note);
        db.execSQL(Tables.NoteData);
        db.execSQL(Tables.Type);
        db.execSQL(Tables.TypeElement);
        db.execSQL(Tables.Picture);
        db.execSQL(Tables.Label);
        db.execSQL(Tables.LabelList);
        db.execSQL(Tables.LabelListLabel);
        db.execSQL(Tables.LabelNote);
        db.execSQL(Tables.Schedule);
        db.execSQL(Tables.Occurrence);
        db.execSQL(Tables.ScheduleConversion);
        db.execSQL(Tables.RevisionFuture);
        db.execSQL(Tables.RevisionPast);

        db.execSQL(Indexes.Note.CreateDateDefinition);
        db.execSQL(Indexes.Note.ModifyDateDefinition);
        db.execSQL(Indexes.NoteData.NoteIdDefinition);
        db.execSQL(Indexes.LabelNote.LabelIdDefinition);
        db.execSQL(Indexes.RevisionFuture.DueDateDefinition);
        db.execSQL(Indexes.RevisionFuture.ScheduleId_NoteIdDefinition);
        db.execSQL(Indexes.RevisionPast.DateDefinition);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static class Tables {
        private static final String Note = "CREATE TABLE " + Contract.Note.table + "(" +
                Contract.Note.id + " INTEGER PRIMARY KEY," +
                Contract.Note.createDate + " INTEGER NOT NULL," +
                Contract.Note.modifyDate + " INTEGER NOT NULL," +
                Contract.Note.typeId + " INTEGER NOT NULL," +
                Contract.Note.displayTitleFront + " TEXT," +
                Contract.Note.displayDetailsFront + " TEXT," +
                Contract.Note.displayTitleBack + " TEXT," +
                Contract.Note.displayDetailsBack + " TEXT," +
                Contract.Note.deleted + " INTEGER)";

        private static final String NoteData = "CREATE TABLE " + Contract.NoteData.table + "(" +
                Contract.NoteData.id + " INTEGER PRIMARY KEY," +
                Contract.NoteData.noteId + " INTEGER NOT NULL," +
                Contract.NoteData.elementId + " INTEGER NOT NULL," +
                Contract.NoteData.groupId + " INTEGER NOT NULL," +
                Contract.NoteData.position + " INTEGER NOT NULL," +
                Contract.NoteData.pattern + " INTEGER NOT NULL," +
                Contract.NoteData.data1 + " INTEGER," +
                Contract.NoteData.data2 + " INTEGER," +
                Contract.NoteData.data3 + " TEXT," +
                Contract.NoteData.data4 + " TEXT)";

        private static final String Type = "CREATE TABLE " + Contract.Type.table + "(" +
                Contract.Type.id + " INTEGER PRIMARY KEY," +
                Contract.Type.title + " INTEGER NOT NULL," +
                Contract.Type.color + " INTEGER NOT NULL," +
                Contract.Type.position + " INTEGER NOT NULL," +
                Contract.Type.isArchived + " INTEGER NOT NULL)";

        private static final String TypeElement = "CREATE TABLE " + Contract.TypeElement.table + "(" +
                Contract.TypeElement.id + " INTEGER PRIMARY KEY," +
                Contract.TypeElement.typeId + " INTEGER NOT NULL," +
                Contract.TypeElement.title + " TEXT NOT NULL," +
                Contract.TypeElement.position + " INTEGER NOT NULL," +
                Contract.TypeElement.isArchived + " INTEGER NOT NULL," +
                Contract.TypeElement.sides + " INTEGER NOT NULL," +
                Contract.TypeElement.pattern + " INTEGER NOT NULL," +
                Contract.TypeElement.initialCopy + " INTEGER NOT NULL," +
                Contract.TypeElement.data1 + " INTEGER," +
                Contract.TypeElement.data2 + " INTEGER," +
                Contract.TypeElement.data3 + " TEXT," +
                Contract.TypeElement.data4 + " TEXT)";

        private static final String Picture = "CREATE TABLE " + Contract.Picture.table + "(" +
                Contract.Picture.pictureId + " INTEGER PRIMARY KEY," +
                Contract.Picture.noteId + " INTEGER)";

        private static final String Label = "CREATE TABLE " + Contract.Label.table + "(" +
                Contract.Label.id + " INTEGER PRIMARY KEY," +
                Contract.Label.title + " TEXT NOT NULL," +
                Contract.Label.deleted + " INTEGER)";

        private static final String LabelList = "CREATE TABLE " + Contract.LabelList.table + "(" +
                Contract.LabelList.id + " INTEGER PRIMARY KEY," +
                Contract.LabelList.title + " TEXT NOT NULL," +
                Contract.LabelList.color + " INTEGER NOT NULL," +
                Contract.LabelList.parentId + " INTEGER," +
                Contract.LabelList.position + " INTEGER NOT NULL)";

        private static final String LabelNote = " CREATE TABLE " + Contract.LabelNote.table + "(" +
                Contract.LabelNote.noteId + " INTEGER NOT NULL," +
                Contract.LabelNote.labelId + " INTEGER NOT NULL," +
                " PRIMARY KEY(" + Contract.LabelNote.noteId + "," + Contract.LabelNote.labelId + "))";

        private static final String LabelListLabel = " CREATE TABLE " + Contract.LabelListLabel.table + "(" +
                Contract.LabelListLabel.labelId + " INTEGER NOT NULL," +
                Contract.LabelListLabel.labelListId + " INTEGER NOT NULL," +
                Contract.LabelListLabel.position + " INTEGER NOT NULL," +
                " PRIMARY KEY(" + Contract.LabelListLabel.labelId + "," + Contract.LabelListLabel.labelListId + "))";

        private static final String Schedule = " CREATE TABLE " + Contract.Schedule.table + "(" +
                Contract.Schedule.id + " INTEGER PRIMARY KEY," +
                Contract.Schedule.title + " TEXT NOT NULL," +
                Contract.Schedule.color + " INTEGER NOT NULL," +
                Contract.Schedule.position + " INTEGER NOT NULL)";

        private static final String Occurrence = " CREATE TABLE " + Contract.Occurrence.table + "(" +
                Contract.Occurrence.id + " INTEGER PRIMARY KEY," +
                Contract.Occurrence.scheduleId + " INTEGER NOT NULL," +
                Contract.Occurrence.number + " INTEGER NOT NULL," +
                Contract.Occurrence.plusDays + " INTEGER NOT NULL)";

        private static final String ScheduleConversion = " CREATE TABLE " + Contract.ScheduleConversion.table + "(" +
                Contract.ScheduleConversion.fromOccurrenceId + " INTEGER NOT NULL," +
                Contract.ScheduleConversion.toScheduleId + " INTEGER NOT NULL," +
                Contract.ScheduleConversion.toOccurrenceNumber + " INTEGER NOT NULL," +
                " PRIMARY KEY(" + Contract.ScheduleConversion.fromOccurrenceId + "," +
                Contract.ScheduleConversion.toScheduleId + "))";

        private static final String RevisionPast = " CREATE TABLE " + Contract.RevisionPast.table + "(" +
                Contract.RevisionPast.noteId + " INTEGER NOT NULL," +
                Contract.RevisionPast.date + " INTEGER NOT NULL," +
                " PRIMARY KEY(" + Contract.RevisionPast.noteId + "," +
                Contract.RevisionPast.date + "))";

        private static final String RevisionFuture = " CREATE TABLE " + Contract.RevisionFuture.table + "(" +
                Contract.RevisionFuture.noteId + " INTEGER PRIMARY KEY," +
                Contract.RevisionFuture.scheduleId + " INTEGER NOT NULL," +
                Contract.RevisionFuture.dueDate + " INTEGER NOT NULL," +
                Contract.RevisionFuture.occurrenceNumber + " INTEGER NOT NULL)";
    }

    private static class Indexes {
        private static class Note {
            private static final String CreateDate = "Note_CreateDate";
            private static final String CreateDateDefinition = "CREATE INDEX " + CreateDate +
                    " ON " + Contract.Note.table + "(" + Contract.Note.createDate + ")";

            private static final String ModifyDate = "Note_ModifyDate";
            private static final String ModifyDateDefinition = "CREATE INDEX " + ModifyDate +
                    " ON " + Contract.Note.table + "(" + Contract.Note.modifyDate + ")";
        }

        private static class NoteData {
            private static final String NoteId = "NoteData_NoteId";
            private static final String NoteIdDefinition = "CREATE INDEX " + NoteId +
                    " ON " + Contract.NoteData.table + "(" + Contract.NoteData.noteId + ")";
        }


        private static class LabelNote {
            private static final String LabelId = "LabelNote_LabelId";
            private static final String LabelIdDefinition = "CREATE INDEX " + LabelId +
                    " ON " + Contract.LabelNote.table + "(" + Contract.LabelNote.labelId + ")";
        }

        private static class RevisionPast {
            private static final String Date = "RevisionPast_Date";
            private static final String DateDefinition = "CREATE INDEX " + Date +
                    " ON " + Contract.RevisionPast.table + "(" + Contract.RevisionPast.date + ")";
        }

        private static class RevisionFuture {
            private static final String ScheduleId_NoteId = "RevisionFuture_ScheduleId_NoteId";
            private static final String ScheduleId_NoteIdDefinition = "CREATE INDEX " + ScheduleId_NoteId +
                    " ON " + Contract.RevisionFuture.table +
                    "(" + Contract.RevisionFuture.scheduleId + "," + Contract.RevisionFuture.noteId + ")";

            private static final String DueDate = "RevisionFuture_DueDate";
            private static final String DueDateDefinition = "CREATE INDEX " + DueDate +
                    " ON " + Contract.RevisionFuture.table + "(" + Contract.RevisionFuture.dueDate + ")";
        }

    }
}
