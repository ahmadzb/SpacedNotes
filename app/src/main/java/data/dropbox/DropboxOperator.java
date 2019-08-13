package data.dropbox;

import android.content.Context;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderError;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
public class DropboxOperator implements SyncOperator {
    @Override
    public void requestSync(Context context, TaskProgress taskProgress) throws SignInException, SyncFailureException {
        //Irrelevant to dropbox api v2
    }

    @Override
    public void signOut(Context context, TaskProgress taskProgress) throws SignInException, SyncFailureException {
        try {
            taskProgress.setStatus("Attempting sign out");
            SyncPreferences.Dropbox.setToken(null, context);
            Authentication.signOut(context);
            taskProgress.setStatus("Sign out successful");
        } catch (DbxException e) {
            e.printStackTrace();
            taskProgress.setStatus("Sign out failed");
            throw new SyncFailureException();
        }
    }

    @Override
    public void replaceFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        DbxClientV2 client = Authentication.getSignIn(context).getClient();
        try {
            taskProgress.setStatus("Attempt to upload new " + fileDescription);
            InputStream in = new FileInputStream(file);
            FileMetadata metadata = client.files().uploadBuilder(getDropboxPath(syncFile))
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(in);
            taskProgress.setStatus(fileDescription + " uploaded");
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    @Override
    public void insertFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        DbxClientV2 client = Authentication.getSignIn(context).getClient();
        try {
            taskProgress.setStatus("Attempt to upload new " + fileDescription);
            InputStream in = new FileInputStream(file);
            FileMetadata metadata = client.files().uploadBuilder(getDropboxPath(syncFile))
                    .uploadAndFinish(in);
            taskProgress.setStatus(fileDescription + " uploaded");
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    @Override
    public void downloadFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        DbxClientV2 client = Authentication.getSignIn(context).getClient();
        try {
            taskProgress.setStatus("Trying to download dropbox " + fileDescription);
            DbxDownloader<FileMetadata> dbxDownloader = client.files().download(getDropboxPath(syncFile));
            dbxDownloader.download(new FileOutputStream(file));
            taskProgress.setStatus("dropbox " + fileDescription + " downloaded");
        } catch (DownloadErrorException e) {
            if (e.errorValue.isPath()) {
                taskProgress.setStatus("dropbox " + fileDescription + " not found");
            } else {
                e.printStackTrace();
                throw new SyncFailureException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    @Override
    public void deleteFile(SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        DbxClientV2 client = Authentication.getSignIn(context).getClient();
        try {
            taskProgress.setStatus("Deleting dropbox " + fileDescription);
            client.files().deleteV2(getDropboxPath(syncFile));
            taskProgress.setStatus("Dropbox " + fileDescription + " deleted");
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    @Override
    public ArrayList<SyncFile> listFiles(SyncFile parentDir, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        DbxClientV2 client = Authentication.getSignIn(context).getClient();
        ListFolderResult result;
        try {
            // Get files and folder metadata from Dropbox root directory
            result = client.files().listFolder(getDropboxPath(parentDir));
            while (true) {
                if (!result.getHasMore()) {
                    break;
                }

                result = client.files().listFolderContinue(result.getCursor());
            }
        } catch (ListFolderErrorException e) {
            if (e.errorValue.tag() == ListFolderError.Tag.PATH) {
                e.printStackTrace();
                taskProgress.setStatus("Dropbox path: \"" + getDropboxPath(parentDir) + "\" doesn't exist");
                return new ArrayList<SyncFile>();
            } else {
                e.printStackTrace();
                throw new SyncFailureException();
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
        List<Metadata> metadataList = result.getEntries();
        ArrayList<SyncFile> syncFiles = new ArrayList<>(metadataList.size());
        for (Metadata metadata : metadataList) {
            syncFiles.add(new SyncFile(parentDir, metadata.getName()));
        }
        return syncFiles;
    }

    private static String getDropboxPath(SyncFile syncFile) {
        if (syncFile.getParent() != null) {
            return getDropboxPath(syncFile.getParent()) + "/" + syncFile.getName();
        } else {
            return "/" + syncFile.getName();
        }
    }
}
