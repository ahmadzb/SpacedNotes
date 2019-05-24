package data.pcloud;

import android.content.Context;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.DataSink;
import com.pcloud.sdk.DataSource;
import com.pcloud.sdk.DownloadOptions;
import com.pcloud.sdk.FileLink;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFile;
import com.pcloud.sdk.RemoteFolder;
import com.pcloud.sdk.internal.networking.JSONParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import data.preference.SyncPreferences;
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
        //Irrelevant to PCloud API
    }

    @Override
    public void signOut(Context context, TaskProgress taskProgress) throws SignInException, SyncFailureException {
        try {
            taskProgress.setStatus("Attempting sign out");
            SyncPreferences.PCloud.setToken(null, context);
            Authentication.signOut(context);
            taskProgress.setStatus("Sign out successful");
        } catch (SignInException e) {
            e.printStackTrace();
            taskProgress.setStatus("Sign out failed");
            throw new SyncFailureException();
        }
    }

    @Override
    public void replaceFile(File localFile, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        insertFile(localFile, syncFile, context, taskProgress, fileDescription);
    }

    @Override
    public void insertFile(File localFile, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        ApiClient apiClient = Authentication.getSignIn(context).getClient();
        try {
            taskProgress.setStatus("Attempt to upload new " + fileDescription);

            while (true) {
                RemoteFile uploadedFile = null;
                try {
                    uploadedFile = apiClient.createFile(
                            getPCloudPath(syncFile.getParent()),
                            syncFile.getName(),
                            DataSource.create(localFile)).execute();
                } catch (ApiError e) {
                    if (e.errorCode() == 2005) {
                        try {
                            createFolderHierarchy(syncFile.getParent(), Authentication.getSignIn(context), taskProgress);
                        } catch (ApiError error) {
                            taskProgress.setStatus("PCloud upload \"" + fileDescription + "\" failed: " +
                                    e.errorMessage() + " Code: " + e.errorCode());
                            e.printStackTrace();
                            throw new SyncFailureException();
                        }
                    } else {
                        taskProgress.setStatus("PCloud upload \"" + fileDescription + "\" failed: " +
                                e.errorMessage() + " Code: " + e.errorCode());
                        e.printStackTrace();
                        throw new SyncFailureException();
                    }
                }
                if (uploadedFile != null)
                    break;
            }
            taskProgress.setStatus(fileDescription + " uploaded");
        } catch (IOException e) {
            taskProgress.setStatus("PCloud upload \"" + fileDescription + "\" failed: " + e.getMessage());
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    private void createFolderHierarchy(SyncFile folderHierarchy, Authentication.SignIn signIn, TaskProgress taskProgress) throws ApiError, SyncFailureException {
        if (folderHierarchy == null) {
            return;
        }

        ApiClient apiClient = signIn.getClient();

        try {
            try {
                apiClient.createFolder(getPCloudPath(folderHierarchy)).execute();
            } catch (ApiError e) {
                if (e.errorCode() == 2002) {
                    createFolderHierarchy(folderHierarchy.getParent(), signIn, taskProgress);
                    apiClient.createFolder(getPCloudPath(folderHierarchy)).execute();
                } else if (e.errorCode() == 2004) {
                    //directory already exist *Do nothing
                } else {
                    throw e;
                }
            }
        } catch (JSONParseException e) {
            //Do nothing
        } catch (IOException e) {
            taskProgress.setStatus("PCloud folder create failed: " + e.getMessage());
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    @Override
    public void downloadFile(File localFile, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        ApiClient apiClient = Authentication.getSignIn(context).getClient();

        try {
            taskProgress.setStatus("Trying to download pcloud " + fileDescription);
            FileLink fileLink = apiClient.createFileLink(getPCloudPath(syncFile), DownloadOptions.DEFAULT).execute();
            apiClient.download(fileLink, DataSink.create(localFile));
            taskProgress.setStatus("pcloud " + fileDescription + " downloaded");
        } catch (ApiError e) {
            if (e.errorCode() == 2002) {
                taskProgress.setStatus("pcloud " + fileDescription + " parent directory not found");
            } else {
                taskProgress.setStatus("pcloud " + fileDescription + ": Download Failed: " + e.errorMessage());
                e.printStackTrace();
                throw new SyncFailureException();
            }
        } catch (IOException e) {
            taskProgress.setStatus("pcloud " + fileDescription + ": Download Failed");
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    @Override
    public void deleteFile(SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        ApiClient apiClient = Authentication.getSignIn(context).getClient();

        try {
            taskProgress.setStatus("Deleting pcloud " + fileDescription);
            apiClient.deleteFile(getPCloudPath(syncFile)).execute();
            taskProgress.setStatus("pcloud " + fileDescription + " deleted");
        } catch (Exception e) {
            taskProgress.setStatus("pcloud " + fileDescription + " file delete failed");
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    @Override
    public ArrayList<SyncFile> listFiles(SyncFile parentDir, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        ApiClient apiClient = Authentication.getSignIn(context).getClient();

        //ListFolderResult result;
        try {
            // Get files and folder metadata from Dropbox root directory
            RemoteFolder folder = apiClient.listFolder(getPCloudPath(parentDir)).execute();
            ArrayList<SyncFile> syncFiles = new ArrayList<>(folder.children().size());
            for (RemoteEntry entry : folder.children()) {
                syncFiles.add(new SyncFile(parentDir, entry.name()));
            }
            return syncFiles;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    private static String getPCloudPath(SyncFile syncFile) {
        if (syncFile.getParent() != null) {
            return getPCloudPath(syncFile.getParent()) + "/" + syncFile.getName();
        } else {
            return "/" + syncFile.getName();
        }
    }


}
