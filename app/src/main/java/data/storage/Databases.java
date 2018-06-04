package data.storage;

import android.content.Context;

import java.io.File;

import data.database.OpenHelper;
import data.database.file.FileOpenHelper;
import data.model.profiles.Profile;

/**
 * Created by Ahmad on 02/01/18.
 * All rights reserved.
 */

public class Databases {
    public static File getDatabaseByName(String name, Context context) {
        return context.getDatabasePath(name);
    }

    public static File getDatabaseByProfile(Profile profile, Context context) {
        return context.getDatabasePath(OpenHelper.getDatabaseName(profile));
    }

    public static File getDatabaseForFiles(Context context) {
        return context.getDatabasePath(FileOpenHelper.nameDatabase);
    }

    public static void deleteDatabaseAsync(Profile profile, Context context) {
        final File database = getDatabaseByProfile(profile, context);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                database.delete();
            }
        });
        thread.start();
    }

    public static void deleteDatabase(Profile profile, Context context) {
        final File database = getDatabaseByProfile(profile, context);
        database.delete();
    }
}
