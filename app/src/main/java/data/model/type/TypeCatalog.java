package data.model.type;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import data.database.TypeOperations;

/**
 * Created by Ahmad on 01/09/18.
 * All rights reserved.
 */

public class TypeCatalog {
    //=========================================== Read =============================================
    public static int getCountAll(SQLiteDatabase readableDb) {
        return TypeOperations.getCountAll(readableDb);
    }

    public static int getCountAvailable(SQLiteDatabase readableDb) {
        return TypeOperations.getCountAvailable(readableDb);
    }

    public static Type getTypeById(long id, SQLiteDatabase readableDb) {
        return TypeOperations.getTypeById(id, readableDb);
    }

    public static ArrayList<Type> getTypes(SQLiteDatabase readableDb) {
        return TypeOperations.getTypes(readableDb);
    }

    public static ArrayList<Type> getTypesArchived(SQLiteDatabase readableDb) {
        return TypeOperations.getTypesArchived(readableDb);
    }
    public static ArrayList<Type> getTypesAvailable(SQLiteDatabase readableDb) {
        return TypeOperations.getTypesAvailable(readableDb);
    }

    public static ArrayList<Type> getTypes(String selection, SQLiteDatabase readableDb) {
        return TypeOperations.getTypes(selection, readableDb);
    }

    public static boolean hasRelatedItems(Type type, SQLiteDatabase readableDb) {
        return TypeOperations.hasRelatedItems(type, readableDb);
    }
    //========================================== Write =============================================
    public static long addType(Type type, SQLiteDatabase writableDb, Context context) {
        type.setId(TypeOperations.addType(type, writableDb, context));
        data.xml.log.operations.TypeOperations.addType(type, context);
        return type.getId();
    }

    public static int updateType(Type type, SQLiteDatabase writableDb, Context context) {
        int count = TypeOperations.updateType(type, writableDb);
        data.xml.log.operations.TypeOperations.updateType(type, context);
        return count;
    }

    public static int updateTypePosition(Type type, int position, SQLiteDatabase writableDb, Context context) {
        int count = TypeOperations.updateTypePosition(type, position, writableDb);
        data.xml.log.operations.TypeOperations.updateTypePosition(type, position, context);
        return count;
    }

    public static int updateTypeArchivedState(Type type, boolean isArchived, SQLiteDatabase writableDb, Context context) {
        int count = TypeOperations.updateTypeArchivedState(type, isArchived, writableDb);
        data.xml.log.operations.TypeOperations.updateTypeArchivedState(type, isArchived, context);
        return count;
    }

    public static int deleteType(Type type, SQLiteDatabase writableDb, Context context) {
        int count = TypeOperations.deleteType(type, writableDb);
        data.xml.log.operations.TypeOperations.deleteType(type, context);
        return count;
    }
}
