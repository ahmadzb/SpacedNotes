package data.drive;

import android.content.Context;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import data.sync.SignInException;
import data.sync.SyncFailureException;
import data.sync.SyncFile;
import data.sync.SyncOperator;
import util.Concurrent.TaskProgress;
import util.file.FileUtil;

/**
 * Created by Ahmad on 02/14/18.
 * All rights reserved.
 */

public class DriveOperator implements SyncOperator {

    @Override
    public void requestSync(Context context, TaskProgress taskProgress) throws SignInException, SyncFailureException {
        Task<Void> task = Authentication.getSignIn(context).getClient().requestSync();
        try {
            Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    @Override
    public void signOut(Context context, TaskProgress taskProgress) throws SyncFailureException {
        try {
            taskProgress.setStatus("Attempting sign out");
            Authentication.signOut(context);
            taskProgress.setStatus("Sign out successful");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            taskProgress.setStatus("Sign out failed");
            throw new SyncFailureException();
        }
    }

    @Override
    public void replaceFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        Authentication.SignIn signIn = Authentication.getSignIn(context);

        DriveFile driveFile = seekSyncFile(syncFile, signIn, taskProgress, fileDescription);
        if (driveFile != null) {
            uploadFile(file, driveFile, signIn, taskProgress, fileDescription);
        } else {
            insertFile(file, syncFile, context, taskProgress, fileDescription);
        }
    }

    @Override
    public void insertFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        Authentication.SignIn signIn = Authentication.getSignIn(context);

        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setCustomProperty(Contract.CustomPropertyCategory,
                        Contract.CATEGORY_PREFIX_FILE + syncFile.getParentHierarchyString())
                .setTitle(syncFile.getName())
                .build();

        DriveFolder parentFolder = getParentFolder(signIn, syncFile);
        if (parentFolder == null) {
            parentFolder = createParentFolder(signIn, syncFile);
        }
        uploadFile(file, parentFolder, metadataChangeSet, signIn, taskProgress, fileDescription);
    }

    @Override
    public void downloadFile(File file, SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        Authentication.SignIn signIn = Authentication.getSignIn(context);

        DriveFile driveFile = seekSyncFile(syncFile, signIn, taskProgress, fileDescription);
        if (driveFile != null) {
            downloadFile(file, driveFile, signIn, taskProgress, fileDescription);
        }
    }

    @Override
    public void deleteFile(SyncFile syncFile, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        Authentication.SignIn signIn = Authentication.getSignIn(context);

        DriveFile driveFile = seekSyncFile(syncFile, signIn, taskProgress, fileDescription);
        if (driveFile != null) {
            deleteResource(driveFile, signIn, taskProgress, fileDescription);
        }
    }

    @Override
    public ArrayList<SyncFile> listFiles(SyncFile parentDir, Context context, TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        Authentication.SignIn signIn = Authentication.getSignIn(context);

        Task<MetadataBuffer> seekQueryTask = signIn.getResourceClient().query(new Query.Builder()
                .addFilter(Filters.eq(Contract.CustomPropertyCategory,
                        Contract.CATEGORY_PREFIX_FILE + parentDir.getHierarchyString())).build());

        ArrayList<SyncFile> syncFiles;
        //Seek
        try {
            Metadata meta = null;
            taskProgress.setStatus("Seeking drive file list " + fileDescription);
            Tasks.await(seekQueryTask);
            MetadataBuffer metadataBuffer = seekQueryTask.getResult();
            syncFiles = new ArrayList<>(metadataBuffer.getCount());
            for (int i = 0; i < metadataBuffer.getCount(); i++) {
                meta = metadataBuffer.get(i);
                syncFiles.add(parentDir.getChild(meta.getTitle()));
            }
            metadataBuffer.release();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
        taskProgress.setStatus(syncFiles.size() + " drive " + fileDescription + " list items found");
        return syncFiles;
    }

    //========================================== Util ==============================================
    private void downloadFile(File file, DriveFile driveFile, Authentication.SignIn signIn,
                              TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException {
        try {
            taskProgress.setStatus("Downloading drive " + fileDescription);
            Task<DriveContents> contentsTask = signIn.getResourceClient().openFile(
                    driveFile, DriveFile.MODE_READ_ONLY);
            Tasks.await(contentsTask);
            DriveContents contents = contentsTask.getResult();
            FileOutputStream outputStream = new FileOutputStream(file);
            InputStream inputStream = contents.getInputStream();
            FileUtil.copy(inputStream, outputStream);
            taskProgress.setStatus("Drive " + fileDescription + " downloaded");
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }


    private void uploadFile(File file, DriveFile driveFile, Authentication.SignIn signIn,
                            TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException {
        try {
            taskProgress.setStatus("Attempt to upload new " + fileDescription);
            Task<DriveContents> contentsTask = signIn.getResourceClient().openFile(
                    driveFile, DriveFile.MODE_WRITE_ONLY);
            Tasks.await(contentsTask);
            DriveContents contents = contentsTask.getResult();
            FileInputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = contents.getOutputStream();
            FileUtil.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            Task<Void> commitTask =
                    signIn.getResourceClient().commitContents(contents, null);
            Tasks.await(commitTask);
            taskProgress.setStatus(fileDescription + " sent for upload");
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    private void uploadFile(File file, DriveFolder parentFolder,
                            MetadataChangeSet metadataChangeSet, Authentication.SignIn signIn,
                            TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException {
        try {
            taskProgress.setStatus("Attempt to upload new " + fileDescription);
            Task<DriveContents> contentsTask = signIn.getResourceClient().createContents();
            Tasks.await(contentsTask);
            DriveContents contents = contentsTask.getResult();
            OutputStream outputStream = contents.getOutputStream();
            InputStream inputStream = new FileInputStream(file);
            FileUtil.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            Task<DriveFile> task = signIn.getResourceClient().createFile(
                    parentFolder, metadataChangeSet, contents);
            Tasks.await(task);
            taskProgress.setStatus(fileDescription + " sent for upload");
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    private void deleteResource(DriveResource driveResource, Authentication.SignIn signIn,
                                TaskProgress taskProgress, String fileDescription) throws SignInException, SyncFailureException {
        try {
            taskProgress.setStatus("Trashing drive " + fileDescription);
            Task<Void> task = signIn.getResourceClient().trash(driveResource);
            Tasks.await(task);
            taskProgress.setStatus("Drive " + fileDescription + " Trashed");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    //===================
    private DriveFile seekSyncFile(SyncFile syncFile, Authentication.SignIn signIn,
                                   TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException {

        Task<MetadataBuffer> seekQueryTask = signIn.getResourceClient().query(new Query.Builder()
                .addFilter(Filters.eq(Contract.CustomPropertyCategory,
                        Contract.CATEGORY_PREFIX_FILE + syncFile.getParentHierarchyString()))
                .addFilter(Filters.eq(SearchableField.TITLE, syncFile.getName())).build());

        DriveId driveId = seekDriveId(seekQueryTask, taskProgress, fileDescription);
        if (driveId == null)
            return null;
        else
            return driveId.asDriveFile();
    }

    private DriveId seekDriveId(Task<MetadataBuffer> seekQueryTask, TaskProgress taskProgress, String fileDescription)
            throws SignInException, SyncFailureException {
        DriveId driveId = null;
        //Seek
        try {
            Metadata meta = null;
            taskProgress.setStatus("Seeking drive " + fileDescription);
            Tasks.await(seekQueryTask);
            MetadataBuffer metadataBuffer = seekQueryTask.getResult();
            for (int i = 0; i < metadataBuffer.getCount() && meta == null; i++) {
                meta = metadataBuffer.get(i);
                driveId = meta.getDriveId();
            }
            metadataBuffer.release();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
        if (driveId == null) {
            taskProgress.setStatus("drive " + fileDescription + " not found");
        } else {
            taskProgress.setStatus("drive " + fileDescription + " found");
        }
        return driveId;
    }


    //===================
    private DriveFolder getParentFolder(Authentication.SignIn signIn, SyncFile syncFile) throws SyncFailureException {
        try {
            Task<MetadataBuffer> task = signIn.getResourceClient().query(new Query.Builder()
                    .addFilter(Filters.eq(Contract.CustomPropertyCategory,
                            Contract.CATEGORY_PREFIX_FOLDER + syncFile.getParentHierarchyString()))
                    .addFilter(Filters.eq(SearchableField.TITLE, syncFile.getName())).build());
            Tasks.await(task);
            MetadataBuffer metadataBufferLog = task.getResult();

            DriveFolder folder = null;
            for (int i = 0; i < metadataBufferLog.getCount() && folder == null; i++) {
                if (metadataBufferLog.get(i).isFolder()) {
                    folder = metadataBufferLog.get(i).getDriveId().asDriveFolder();
                }
            }
            metadataBufferLog.release();
            return folder;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    private DriveFolder createParentFolder(final Authentication.SignIn signIn, SyncFile syncFile) throws SyncFailureException {
        try {
            DriveFolder mainFolder = getMainFolder(signIn);

            if (mainFolder == null) {
                mainFolder = createMainFolder(signIn);
            }
            ArrayList<SyncFile> parentHierarchy = syncFile.getParentHierarchy();
            DriveFolder parentFolder = mainFolder;
            for (SyncFile parent : parentHierarchy) {
                DriveFolder existingFolder = getParentFolder(signIn, parent);
                if (existingFolder != null) {
                    parentFolder = existingFolder;
                } else {
                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setCustomProperty(Contract.CustomPropertyCategory,
                                    Contract.CATEGORY_PREFIX_FOLDER + parent.getParentHierarchyString())
                            .setTitle(parent.getName())
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .build();
                    Task<DriveFolder> task = signIn.getResourceClient().createFolder(parentFolder, metadataChangeSet);
                    Tasks.await(task);
                    parentFolder = task.getResult();
                }
            }
            return parentFolder;
        } catch (InterruptedException | ExecutionException e) {
            throw new SyncFailureException();
        }
    }

    private DriveFolder getMainFolder(Authentication.SignIn signIn) throws SyncFailureException {
        try {
            Task<MetadataBuffer> task = signIn.getResourceClient().query(new Query.Builder().addFilter(
                    Filters.eq(Contract.CustomPropertyCategory, Contract.CATEGORY_MAIN_FOLDER)).build());
            Tasks.await(task);
            MetadataBuffer metadataBufferLog = task.getResult();

            DriveFolder folder = null;
            for (int i = 0; i < metadataBufferLog.getCount() && folder == null; i++) {
                if (metadataBufferLog.get(i).isFolder()) {
                    folder = metadataBufferLog.get(i).getDriveId().asDriveFolder();
                }
            }
            metadataBufferLog.release();
            return folder;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }

    private DriveFolder createMainFolder(Authentication.SignIn signIn) throws SyncFailureException {
        try {
            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                    .setCustomProperty(Contract.CustomPropertyCategory, Contract.CATEGORY_MAIN_FOLDER)
                    .setTitle(Contract.directoryName)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .build();

            Task<DriveFolder> rootFolderTask = signIn.getResourceClient().getRootFolder();
            Tasks.await(rootFolderTask);
            DriveFolder rootFolder = rootFolderTask.getResult();
            Task<DriveFolder> task = signIn.getResourceClient().createFolder(rootFolder, metadataChangeSet);
            Tasks.await(task);
            return task.getResult();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }
}

