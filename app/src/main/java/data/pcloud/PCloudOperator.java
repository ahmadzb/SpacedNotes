package data.pcloud;

import android.content.Context;


import java.io.File;
import java.util.ArrayList;

import data.sync.SignInException;
import data.sync.SyncFailureException;
import data.sync.SyncFile;
import data.sync.SyncOperator;
import util.Concurrent.TaskProgress;

/**
 * Created by Ahmad on 05/21/18.
 * All rights reserved.
 */
public class PCloudOperator implements SyncOperator {



    @Override
    public void requestSync(Context context, TaskProgress taskProgress) throws SignInException, SyncFailureException {

    }

    @Override
    public void signOut(Context context, TaskProgress taskProgress) throws SignInException, SyncFailureException {

    }

    @Override
    public void replaceFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {

    }

    @Override
    public void insertFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {

    }

    @Override
    public void downloadFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {

    }

    @Override
    public void deleteFile(SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {

    }

    @Override
    public ArrayList<SyncFile> listFiles(SyncFile parentDir, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        return null;
    }

    private static String getPCloudPath(SyncFile syncFile) {
        if (syncFile.getParent() != null) {
            return getPCloudPath(syncFile.getParent()) + "/" + syncFile.getName();
        } else {
            return "/" + syncFile.getName();
        }
    }
}
