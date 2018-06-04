package data.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import data.database.file.FileOpenHelper;
import data.model.existence.Existence;
import data.model.existence.ExistenceCatalog;
import data.model.profiles.Profile;
import data.xml.log.operator.LogOperations;
import data.xml.port.IdProvider;
import data.xml.port.PortOperations;
import data.xml.profiles.ProfilesOperations;
import data.xml.progress.ProgressOperations;

/**
 * Created by Ahmad on 02/10/18.
 * All rights reserved.
 */

public class CaptureOperations {
    private static final String DATABASE_DIRECTORY = "database";

    public static File makeCapture(Context context) {
        long captureId = IdProvider.nextCaptureId(context);
        long cumulativeOperationCount = IdProvider.progressedCumulativeOperationCount(context);
        File capture = Captures.getCaptureFile(captureId, cumulativeOperationCount);
        ExistenceCatalog.addCaptureExistence(captureId, cumulativeOperationCount,
                FileOpenHelper.getDatabase(context), context);
        LogOperations.waitUntilNoOperation();
        ProgressOperations.waitUntilNoOperations();
        PortOperations.waitUntilNoOperations();
        ProfilesOperations.waitUntilNoOperations();
        createCapture(context, capture);
        return capture;
    }

    private static void createCapture(Context context, File captureFile) {
        ArrayList<Profile> profiles = ProfilesOperations.getProfiles(context);
        try {
            FileOutputStream outputStream = new FileOutputStream(captureFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

            //Databases
            {
                for (Profile profile : profiles) {
                    File database = Databases.getDatabaseByProfile(profile, context);
                    if (database.exists()) {
                        ZipEntry entry = new ZipEntry(
                                DATABASE_DIRECTORY + File.separator + database.getName());
                        putFileIntoZip(database, entry, zipOutputStream);
                    }
                }

                File fileDatabase = Databases.getDatabaseForFiles(context);
                if (fileDatabase.exists()) {
                    ZipEntry entry = new ZipEntry(
                            DATABASE_DIRECTORY + File.separator + fileDatabase.getName());
                    putFileIntoZip(fileDatabase, entry, zipOutputStream);
                }
            }
            //log directory
            {
                File logDir = Log.getRootDir();
                ZipEntry entry = new ZipEntry(logDir.getName());
                putFileIntoZip(logDir, entry, zipOutputStream);
            }
            //Port directory
            {
                File portDir = Port.getPortDir();
                ZipEntry entry = new ZipEntry(portDir.getName());
                putFileIntoZip(portDir, entry, zipOutputStream);
            }
            //Profiles
            {
                File profilesFile = Profiles.getFile(context);
                if (profilesFile.exists()) {
                    ZipEntry entry = new ZipEntry(profilesFile.getName());
                    putFileIntoZip(profilesFile, entry, zipOutputStream);
                }
            }
            //Progress
            {
                File progressFile = Progress.getFile(context);
                if (progressFile.exists()) {
                    ZipEntry entry = new ZipEntry(progressFile.getName());
                    putFileIntoZip(progressFile, entry, zipOutputStream);
                }
            }
            zipOutputStream.flush();
            zipOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void putFileIntoZip(File file, ZipEntry entry, ZipOutputStream zipOutputStream) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                putFileIntoZip(child, new ZipEntry(entry.getName() + File.separator + child.getName()), zipOutputStream);
            }
        } else {
            zipOutputStream.putNextEntry(entry);
            FileInputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = bufferedInputStream.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }
            bufferedInputStream.close();
        }
    }

    public static void replaceFromCapture(File capture, Context context) {
        try {
            InputStream inputStream = new FileInputStream(capture);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream);
            ZipEntry entry;
            while((entry = zipInputStream.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith(DATABASE_DIRECTORY + File.separator)) {
                    String databaseName = name.replace(DATABASE_DIRECTORY + File.separator, "");
                    File database = Databases.getDatabaseByName(databaseName, context);
                    writeFileFromZip(database, zipInputStream);
                } else if (name.startsWith(Log.getRootDir().getName())) {
                    File logFile = new File(Log.getRootDir().getParent(), name);
                    logFile.getParentFile().mkdirs();
                    writeFileFromZip(logFile, zipInputStream);
                } else if (name.startsWith(Port.getPortDir().getName())) {
                    File logFile = new File(Port.getPortDir().getParent(), name);
                    logFile.getParentFile().mkdirs();
                    writeFileFromZip(logFile, zipInputStream);
                } else if (name.equals(Contract.InternalDirectory.profilesFile)) {
                    File file = Profiles.getFile(context);
                    writeFileFromZip(file, zipInputStream);
                } else if (name.equals(Contract.InternalDirectory.progressFile)) {
                    File file = Progress.getFile(context);
                    writeFileFromZip(file, zipInputStream);
                }
            }
            zipInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFileFromZip(File file, ZipInputStream zipInputStream) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        int BUFFER = 1024;
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
            bufferedOutputStream.write(data, 0, count);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }

    public static void deleteCapture(long captureId, Context context) {
        SQLiteDatabase fileWritableDb = FileOpenHelper.getDatabase(context);
        Existence captureExistence = ExistenceCatalog.getCaptureExistence(captureId, fileWritableDb);
        File captureFile = Captures.getCaptureFile(captureId, captureExistence.getData1());
        captureFile.delete();
        ExistenceCatalog.setExistenceState(captureExistence, Existence.STATE_DELETE, fileWritableDb, context);
    }
}
