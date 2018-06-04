package data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import data.model.type.Type;
import data.xml.port.IdProvider;
import exceptions.InvalidCursorException;
import exceptions.NotRealizedException;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */
public class TypeOperations {
    //=========================================== Read =============================================
    public static int getCountAll(SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Type.table, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static int getCountAvailable(SQLiteDatabase readableDb) {
        String selection = Contract.Type.isArchived + " = 0";
        Cursor cursor = readableDb.query(Contract.Type.table, null, selection, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static Type getTypeById(long id, SQLiteDatabase readableDb) {
        String selection = Contract.Type.id + " = " + id;
        ArrayList<Type> types = getTypes(selection, readableDb);
        if (types.size() == 0) {
            return null;
        } else {
            return types.get(0);
        }
    }

    public static ArrayList<Type> getTypes(SQLiteDatabase readableDb) {
        return getTypes(null, readableDb);
    }

    public static ArrayList<Type> getTypesArchived(SQLiteDatabase readableDb) {
        String selection = Contract.Type.isArchived + " = 1";
        return getTypes(selection, readableDb);
    }
    public static ArrayList<Type> getTypesAvailable(SQLiteDatabase readableDb) {
        String selection = Contract.Type.isArchived + " = 0";
        return getTypes(selection, readableDb);
    }

    public static ArrayList<Type> getTypes(String selection, SQLiteDatabase readableDb) {
        Cursor cursor = readableDb.query(Contract.Type.table, null, selection, null, null, null, null);
        ArrayList<Type> types = retrieveTypes(cursor);
        cursor.close();
        return types;
    }

    public static ArrayList<Type> retrieveTypes(Cursor cursor) {
        int indexId = cursor.getColumnIndex(Contract.Type.id);
        int indexTitle = cursor.getColumnIndex(Contract.Type.title);
        int indexColor = cursor.getColumnIndex(Contract.Type.color);
        int indexPosition = cursor.getColumnIndex(Contract.Type.position);
        int indexIsArchived = cursor.getColumnIndex(Contract.Type.isArchived);

        if (indexId < 0)
            throw new InvalidCursorException();
        ArrayList<Type> types = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Type type = Type.newInstance();
            type.setRealized(true);
            type.setInitialized(true);
            type.setId(cursor.getLong(indexId));
            if (indexTitle >= 0) {
                type.setTitle(cursor.getString(indexTitle));
            }
            if (indexColor >= 0) {
                type.setColor(cursor.getInt(indexColor));
            }
            if (indexPosition >= 0) {
                type.setPosition(cursor.getInt(indexPosition));
            }
            if (indexIsArchived >= 0) {
                type.setArchived(cursor.getInt(indexIsArchived) == 1);
            }
            types.add(type);
        }
        return types;
    }

    public static boolean hasRelatedItems(Type type, SQLiteDatabase readableDb) {
        boolean hasRelatedItems = false;
        hasRelatedItems = hasRelatedItems || NoteOperations.hasRelatedNotes(type, readableDb);
        return hasRelatedItems;
    }

    //========================================== Write =============================================
    private static ContentValues getContentValues(Type type) {
        ContentValues values = new ContentValues();

        values.put(Contract.Type.title, type.getTitle());
        values.put(Contract.Type.color, type.getColor());
        values.put(Contract.Type.position, type.getPosition());
        values.put(Contract.Type.isArchived, type.isArchived()? 1 : 0);

        return values;
    }

    public static long addType(Type type, SQLiteDatabase writableDb, Context context) {
        ContentValues values = getContentValues(type);
        long id;
        if (type.isRealized()) {
            values.put(Contract.Type.id, type.getId());
            id = writableDb.replace(Contract.Type.table, null, values);
        } else {
            values.put(Contract.Type.id, IdProvider.nextTypeId(context));
            id = writableDb.insert(Contract.Type.table, null, values);
        }
        return id;
    }

    public static int updateType(Type type, SQLiteDatabase writableDb) {
        if (!type.isRealized() || !type.isInitialized())
            throw new NotRealizedException();
        String selection = Contract.Type.id + " = " + type.getId();
        return writableDb.update(Contract.Type.table, getContentValues(type), selection, null);
    }

    public static int updateTypePosition(Type type, int position, SQLiteDatabase writableDb) {
        ContentValues values = new ContentValues();
        values.put(Contract.Type.position, position);
        String selection = Contract.Type.id + " = " + type.getId();
        int count = writableDb.update(Contract.Type.table, values, selection, null);
        return count;
    }

    public static int updateTypeArchivedState(Type type, boolean isArchived, SQLiteDatabase writableDb) {
        ContentValues values = new ContentValues();
        values.put(Contract.Type.isArchived, isArchived);
        String selection = Contract.Type.id + " = " + type.getId();
        int count = writableDb.update(Contract.Type.table, values, selection, null);
        return count;
    }

    public static int deleteType(Type type, SQLiteDatabase writableDb) {
        String selection = Contract.Type.id + " = " + type.getId();
        return writableDb.delete(Contract.Type.table, selection, null);
    }
}
