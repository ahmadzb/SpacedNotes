package data.sync;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nullable;

import data.model.profiles.Profile;
import util.Concurrent.TaskProgress;

/**
 * Created by Ahmad on 02/14/18.
 * All rights reserved.
 */

public interface SyncOperator {
    void requestSync(Context context, TaskProgress taskProgress) throws SignInException, SyncFailureException;

    void signOut(Context context, TaskProgress taskProgress) throws SignInException, SyncFailureException;

    void replaceFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException;

    void insertFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException;

    void downloadFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException;

    void deleteFile(SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException;

    ArrayList<SyncFile> listFiles(SyncFile parentDir, Context context, TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException;
}
