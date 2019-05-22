package data.pcloud;

import android.content.Context;


import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.Call;
import com.pcloud.sdk.DownloadOptions;
import com.pcloud.sdk.FileLink;
import com.pcloud.sdk.internal.ResponseAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import data.sync.SignInException;
import data.sync.SyncFailureException;
import data.sync.SyncFile;
import data.sync.SyncOperator;
import okhttp3.Request;
import okhttp3.Response;
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
