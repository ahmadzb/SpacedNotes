package util.file;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Ahmad on 12/11/17.
 * All rights reserved.
 */

public class DriveStorage {
    private static final String LOG_TAG = "expense manager";

    private static final String DRIVE_FOLDER_NAME = "Expense Manager Backups";
/*
    public static Task<MetadataBuffer> getBackupList(final AppCompatActivity activity, final DriveResourceClient driveResourceClient,
                                                     final DriveClient driveClient) {
        final Task<DriveFolder> folderTask = driveResourceClient.getRootFolder();
        return folderTask.continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                DriveFolder rootFolder = task.getResult();
                return driveResourceClient.listChildren(rootFolder);
            }
        }).continueWithTask(new Continuation<MetadataBuffer, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<MetadataBuffer> task) throws Exception {
                for (Metadata metadata : task.getResult()) {
                    if (isBackupFolder(metadata)) {
                        return driveResourceClient.listChildren(metadata.getDriveId().asDriveFolder());
                    }
                }
                return null;
            }
        });
    }

    private static GoogleSignInClient buildGoogleSignInClient(AppCompatActivity activity) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(activity, signInOptions);
    }

    public static void trySaveBackup(
            final AppCompatActivity activity, final OnSuccessListener<DriveFile> onSuccessListener,
            final OnFailureListener onFailureListener) {
            final GoogleSignInClient signInClient = buildGoogleSignInClient(activity);
            signInClient.silentSignIn().addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                @Override
                public void onSuccess(final GoogleSignInAccount googleSignInAccount) {
                    Log.i(LOG_TAG, "Success");
                    final DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(
                            activity, googleSignInAccount);
                    DriveClient driveClient = Drive.getDriveClient(activity, googleSignInAccount);
                    saveBackup(activity, driveResourceClient, driveClient, onSuccessListener, onFailureListener);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(LOG_TAG, "Failure");
                }
            });
    }

    public static void saveBackup(
            final Activity activity, final DriveResourceClient driveResourceClient,final DriveClient driveClient,
            final OnSuccessListener<DriveFile> onSuccessListener,final OnFailureListener onFailureListener) {

        final Task<DriveContents> createContentsTask = driveResourceClient.createContents();
        final Task<DriveFolder> folderTask = driveResourceClient.getRootFolder();
        folderTask.continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
            @Override
            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                return driveResourceClient.listChildren(task.getResult());
            }
        }).addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
            @Override
            public void onSuccess(final MetadataBuffer metadatas) {
                Tasks.whenAll(createContentsTask).continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(@NonNull Task<Void> task) throws Exception {
                        Task<DriveFile> driveFileTask = null;
                        if (metadatas.getCount() != 0) {
                            for (Metadata metadata : metadatas) {
                                if (isBackupFolder(metadata)) {
                                    try {
                                        driveFileTask = buildWriteBackupTaskAndNotifyUI(activity, driveResourceClient, metadata.getDriveId().asDriveFolder(),
                                                createContentsTask.getResult(), onSuccessListener, onFailureListener);
                                    } catch (IOException e) {
                                        Log.i(LOG_TAG, e.getMessage());
                                    }
                                }
                            }
                        }
                        if (driveFileTask == null) {
                            driveFileTask = createFolder(activity, driveResourceClient, driveClient)
                                    .continueWithTask(new Continuation<DriveFolder, Task<DriveFile>>() {
                                        @Override
                                        public Task<DriveFile> then(@NonNull Task<DriveFolder> task) throws Exception {
                                            return buildWriteBackupTaskAndNotifyUI(activity, driveResourceClient,
                                                    task.getResult(), createContentsTask.getResult(),
                                                    onSuccessListener, onFailureListener);
                                        }
                                    });
                        }
                        return null;
                    }
                });
            }
        });
    }

    private static Task<DriveFile> buildWriteBackupTaskAndNotifyUI(
            final Activity activity, final DriveResourceClient driveResourceClient,
             DriveFolder parent, DriveContents contents,
             final OnSuccessListener<DriveFile> onSuccessListener,final OnFailureListener onFailureListener) throws IOException {
        File backup = Storage.makeBackup(activity);
        InputStream inputStream = new FileInputStream(backup);
        OutputStream outputStream = contents.getOutputStream();
        FileUtil.copy(inputStream, outputStream);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(backup.getName())
                .setMimeType("application/emdb")
                .build();

        Task<DriveFile> driveFileTask = driveResourceClient.createFile(parent, changeSet, contents);
        if (onSuccessListener != null) {
            driveFileTask.addOnSuccessListener(onSuccessListener);
        }
        if (onFailureListener != null) {
            driveFileTask.addOnFailureListener(onFailureListener);
        }
        return driveFileTask;
    }

    private static Task<DriveFolder> createFolder(final Activity activity, final DriveResourceClient driveResourceClient,
                                           final DriveClient driveClient) {
        return driveResourceClient
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(DRIVE_FOLDER_NAME)
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .build();
                        return driveResourceClient.createFolder(parentFolder, changeSet);
                    }
                })
                .addOnSuccessListener(activity,
                        new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                                Log.i(LOG_TAG, "FOLDER_SUCCESS");
                            }
                        })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(LOG_TAG, "FOLDER_FAILURE");
                    }
                });
    }

    private static boolean isBackupFolder(Metadata metadata) {
        return metadata.isFolder() && DRIVE_FOLDER_NAME.equals(metadata.getTitle()) && !metadata.isTrashed();
    }
    */
}
