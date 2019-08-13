package data.sync;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.IOException;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import data.database.file.FileOpenHelper;
import data.model.capture.CaptureCatalog;
import data.model.existence.Existence;
import data.model.existence.ExistenceCatalog;
import data.storage.Captures;
import data.storage.Contract;
import data.storage.Log;
import data.storage.Pictures;
import data.storage.Port;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;
import data.xml.port.IdProvider;
import data.xml.port.PortOperations;
import util.Concurrent.TaskProgress;
import util.file.Zip;

/**
 * Created by Ahmad on 02/11/18.
 * All rights reserved.
 */

public class SyncOperations {

    /**
     * Critic: updateCaptures and performNewOperationsFromLog lines are not related to sync method purpose,
     * if possible create a more general method for both offline and online sync and move these lines there
     */
    public static void sync(TaskProgress progress, Context context) throws SignInException,
            SyncFailureException, SyncCancelledException {
        CaptureCatalog.updateCaptures(context);
        LogOperations.performNewOperationsFromLog(context);
        try {
            SyncOperator operator = SyncOperators.getCurrentOperator(context);
            if (progress.isProgressCancelled()) throw new SyncCancelledException();
            operator.requestSync(context, progress);

            LogOperations.waitUntilNoOperation();
            TreeMap<Integer, data.xml.port.Port> remotePortMap = retrieveRemotePortMap(context, progress);
            updateLocalOperations(remotePortMap, progress, context);

            SQLiteDatabase fileWritableDb = FileOpenHelper.getDatabase(context);
            Collection<Existence> allDownloadExistences = ExistenceCatalog.getAllDownloadExistences(operator, fileWritableDb, context);
            Collection<Existence> allUploadExistences = ExistenceCatalog.getAllUploadExistences(operator, fileWritableDb);
            Collection<Existence> allDeleteExistences = ExistenceCatalog.getAllDeleteExistences(operator, fileWritableDb);
            if (allDownloadExistences.size() + allUploadExistences.size() + allDeleteExistences.size() != 0) {
                int existenceFlag = SyncOperators.getOperatorExistenceFlag(operator);
                {//PICTURES
                    syncPictures(allDownloadExistences, allUploadExistences, allDeleteExistences,
                            existenceFlag, progress, context, fileWritableDb);
                }
                {//CAPTURES
                    syncCaptures(allUploadExistences, allDeleteExistences, existenceFlag, progress,
                            context, fileWritableDb);
                }
            }

            LogOperations.waitUntilNoOperation();
            updateRemoteOperationFiles(remotePortMap, progress, context);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SyncFailureException();
        }
    }


    private static TreeMap<Integer, data.xml.port.Port> retrieveRemotePortMap(Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException, SyncCancelledException {
        TreeMap<Integer, data.xml.port.Port> remotePortMap = new TreeMap<>();
        boolean shouldContinue = true;
        for (int port = 0; port < PortOperations.getPortsCount() && shouldContinue; port++) {
            File remotePortFile = new File(context.getCacheDir(), Contract.CacheDirectory.SyncOperationsCachedPort);
            remotePortFile.delete();
            downloadRemotePort(remotePortFile, port, context, taskProgress);
            data.xml.port.Port remotePort = null;
            if (remotePortFile.exists()) {
                remotePort = PortOperations.connectionFor(remotePortFile, port).getPort();
                remotePortMap.put(port, remotePort);
            } else {
                shouldContinue = false;
            }
            if (taskProgress.isProgressCancelled()) throw new SyncCancelledException();
        }
        return remotePortMap;
    }

    private static void updateLocalOperations(TreeMap<Integer, data.xml.port.Port> remotePortMap,
                                              TaskProgress progress, Context context)
            throws IOException, SignInException, SyncFailureException {
        long remoteCumulativeOperationId = 0;
        long localCumulativeOperationId = IdProvider.progressedCumulativeOperationCount(context);
        for (data.xml.port.Port port : remotePortMap.values()) {
            remoteCumulativeOperationId += port.getLogMetadata().getLastOperationId() + 1;
        }
        if (remoteCumulativeOperationId - localCumulativeOperationId > CaptureCatalog.CAPTURE_RETRIEVAL_THRESHOLD) {
            ArrayList<SyncFile> remoteCaptureFiles = retrieveCaptureList(context, progress);
            long maxCOC = 0;
            SyncFile maxRemoteCaptureFile = null;
            for (SyncFile remoteCaptureFile : remoteCaptureFiles) {
                long remoteCOC = Captures.getCaptureCOCByFileName(remoteCaptureFile.getName());
                if (maxCOC < remoteCOC) {
                    maxRemoteCaptureFile = remoteCaptureFile;
                    maxCOC = remoteCOC;
                }
            }
            if (maxCOC - localCumulativeOperationId > CaptureCatalog.CAPTURE_RETRIEVAL_THRESHOLD) {
                long coc = maxCOC;
                long captureId = Captures.getCaptureIdByFileName(maxRemoteCaptureFile.getName());
                File captureFile = Captures.getCaptureFile(captureId, coc);
                if (!captureFile.exists()) {
                    downloadRemoteCapture(captureId, coc, context, progress);
                }
            }
            LogOperations.reloadLogDocuments();
            LogOperations.performNewOperationsFromLog(context);
        }

        for (int port = 0; port < PortOperations.getPortsCount(); port++) {
            data.xml.port.Port remotePort = remotePortMap.get(port);
            data.xml.port.Port localPort = PortOperations.connectionFor(port).getPort();
            //Send Progress
            {
                if (remotePort != null && remotePort.getLogMetadata().getLastOperationId() >= LogContract.StartOperationId ||
                        localPort.getLogMetadata().getLastOperationId() >= LogContract.StartOperationId) {
                    String status = "LC port" + port + ": Local opId: " + localPort.getLogMetadata().getLastOperationId() + ", Remote opId: ";
                    if (remotePort != null) {
                        status += remotePort.getLogMetadata().getLastOperationId();
                    } else {
                        status += "N/A";
                    }
                    progress.setStatus(status);
                }
            }

            if (remotePort != null) {
                long localLastOperationId = localPort.getLogMetadata().getLastOperationId();
                long remoteLastOperationId = remotePort.getLogMetadata().getLastOperationId();
                if (localLastOperationId < remoteLastOperationId) {
                    for (int i = localPort.getLogMetadata().getCurrentLogIndex(); i <= remotePort.getLogMetadata().getCurrentLogIndex(); i++) {
                        downloadRemoteLog(port, i, context, progress);
                    }
                }

                data.xml.port.Port currentPort = PortOperations.getCurrentPort(context);
                if (localLastOperationId < remoteLastOperationId ||
                        localLastOperationId == remoteLastOperationId &&
                                (currentPort == null || port != currentPort.getPort())) {
                    PortOperations.connectionFor(port).writePortAsync(remotePort);
                }
            }
        }

        LogOperations.reloadLogDocuments();
        LogOperations.performNewOperationsFromLog(context);
    }

    private static void updateRemoteOperationFiles(TreeMap<Integer, data.xml.port.Port> remotePortMap,
                                                   TaskProgress progress, Context context)
            throws IOException, SignInException, SyncFailureException, SyncCancelledException {
        for (int port = 0; port < PortOperations.getPortsCount(); port++) {
            if (progress.isProgressCancelled()) throw new SyncCancelledException();
            data.xml.port.Port remotePort = remotePortMap.get(port);
            File localPortFile = Port.getPortFile(port);
            data.xml.port.Port localPort = PortOperations.connectionFor(port).getPort();
            //Send Progress
            {
                if (remotePort != null && remotePort.getLogMetadata().getLastOperationId() >= LogContract.StartOperationId ||
                        localPort.getLogMetadata().getLastOperationId() >= LogContract.StartOperationId) {
                    String status = "RM port" + port + ": Local opId: " + localPort.getLogMetadata().getLastOperationId() + ", Remote opId: ";
                    if (remotePort != null) {
                        status += remotePort.getLogMetadata().getLastOperationId();
                    } else {
                        status += "N/A";
                    }
                    progress.setStatus(status);
                }
            }

            if (remotePort == null ||
                    localPort.getLogMetadata().getLastOperationId() > remotePort.getLogMetadata().getLastOperationId()) {
                int fromLogIndex;
                if (remotePort != null) {
                    fromLogIndex = remotePort.getLogMetadata().getCurrentLogIndex();
                } else {
                    fromLogIndex = LogContract.StartLogIndex;
                }
                for (int i = fromLogIndex; i <= localPort.getLogMetadata().getCurrentLogIndex(); i++) {
                    uploadRemoteLog(port, i, context, progress);
                }
            }

            data.xml.port.Port currentPort = PortOperations.getCurrentPort(context);
            if (remotePort == null ||
                    localPort.getLogMetadata().getLastOperationId() > remotePort.getLogMetadata().getLastOperationId() ||
                    localPort.getLogMetadata().getLastOperationId() == remotePort.getLogMetadata().getLastOperationId() &&
                            currentPort != null && port == currentPort.getPort() && !localPort.equals(remotePort)) {
                if (localPortFile.exists()) {
                    uploadRemotePort(port, context, progress);
                }
            }
        }
    }

    private static void syncPictures(Collection<Existence> allDownloadExistences,
                                     Collection<Existence> allUploadExistences,
                                     Collection<Existence> allDeleteExistences,
                                     int existenceFlag,
                                     TaskProgress progress,
                                     Context context,
                                     SQLiteDatabase fileWritableDb)
            throws SignInException, SyncFailureException, SyncCancelledException {
        ArrayList<Existence> uploadExistences = Existence.filterForPictures(allUploadExistences);
        ArrayList<Existence> downloadExistences = Existence.filterForPictures(allDownloadExistences);
        ArrayList<Existence> deleteExistences = Existence.filterForPictures(allDeleteExistences);
        progress.setStatus(uploadExistences.size() + " pictures to upload, " +
                downloadExistences.size() + " to download, " +
                deleteExistences.size() + " to delete");
        if (PortOperations.getCurrentPortConnectionIfExists(context) != null) {
            for (Existence existence : uploadExistences) {
                if (progress.isProgressCancelled()) throw new SyncCancelledException();
                long profileId = existence.getProfile();
                long pictureId = Existence.Pattern.Picture.getPictureId(existence.getPattern());
                boolean success = uploadRemotePicture(profileId, pictureId, context, progress);
                if (success) {
                    ExistenceCatalog.setExistenceFlag(existence, existenceFlag, fileWritableDb, context);
                } else {
                    progress.setStatus("local picture: profileId: " + profileId +
                            ", pictureId: " + pictureId + " not found");
                }
            }
        }
        for (Existence existence : downloadExistences) {
            if (progress.isProgressCancelled()) throw new SyncCancelledException();
            long profileId = existence.getProfile();
            long pictureId = Existence.Pattern.Picture.getPictureId(existence.getPattern());
            downloadRemotePicture(profileId, pictureId, context, progress);
        }

        if (PortOperations.getCurrentPortConnectionIfExists(context) != null) {
            for (Existence existence : deleteExistences) {
                if (progress.isProgressCancelled()) throw new SyncCancelledException();
                long profileId = existence.getProfile();
                long pictureId = Existence.Pattern.Picture.getPictureId(existence.getPattern());
                boolean success = deleteRemotePicture(profileId, pictureId, context, progress);
                if (success) {
                    ExistenceCatalog.removeExistenceFlag(existence, existenceFlag, fileWritableDb, context);
                }
            }
        }
    }

    private static void syncCaptures(Collection<Existence> allUploadExistences,
                                     Collection<Existence> allDeleteExistences,
                                     int existenceFlag,
                                     TaskProgress progress, Context context,
                                     SQLiteDatabase fileWritableDb)
            throws SignInException, SyncFailureException, SyncCancelledException {
        ArrayList<Existence> uploadExistences = Existence.filterForCaptures(allUploadExistences);
        ArrayList<Existence> deleteExistences = Existence.filterForCaptures(allDeleteExistences);

        if (PortOperations.getCurrentPortConnectionIfExists(context) != null) {
            for (Existence existence : uploadExistences) {
                if (progress.isProgressCancelled()) throw new SyncCancelledException();
                long captureId = Existence.Pattern.Capture.getCaptureId(existence.getPattern());
                long coc = existence.getData1();
                uploadRemoteCapture(captureId, coc, context, progress);
                ExistenceCatalog.setExistenceFlag(existence, existenceFlag, fileWritableDb, context);
            }
            for (Existence existence : deleteExistences) {
                if (progress.isProgressCancelled()) throw new SyncCancelledException();
                long captureId = Existence.Pattern.Capture.getCaptureId(existence.getPattern());
                long coc = existence.getData1();
                deleteRemoteCapture(captureId, coc, context, progress);
                ExistenceCatalog.removeExistenceFlag(existence, existenceFlag, fileWritableDb, context);
            }
        }
    }

    //==============================================================================================
    private static void downloadRemotePort(File portFile, int port, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        SyncFile portSyncFile = SyncFileTranslator.getPortZipped(port);
        File portFileZipped = Port.getCachePortFileZipped(context);
        portFileZipped.delete();
        SyncOperators.getCurrentOperator(context).downloadFile(portFileZipped, portSyncFile, context, taskProgress,
                "port(port: " + port + ")");
        if (portFileZipped.exists()) {
            try {
                Zip.unzipFile(portFileZipped, portFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new SyncFailureException();
            }
        }
    }

    private static void uploadRemotePort(int port, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        File portFile = Port.getPortFile(port);
        if (portFile.exists()) {
            try {
                File portFileZipped = Port.getCachePortFileZipped(context);
                portFileZipped.delete();
                Zip.zipFile(portFile, portFileZipped);
                SyncFile portSyncFile = SyncFileTranslator.getPortZipped(port);
                SyncOperators.getCurrentOperator(context).replaceFile(portFileZipped, portSyncFile, context, taskProgress,
                        "port(port: " + port + ")");
            } catch (IOException e) {
                e.printStackTrace();
                throw new SyncFailureException();
            }
        }
    }

    private static void downloadRemoteLog(int port, int index, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        File logFile = Log.getLogFile(port, index);
        SyncFile logSyncFile = SyncFileTranslator.getLogZipped(port, index);
        File logFileZipped = Log.getCacheLogFileZipped(context);
        logFileZipped.delete();
        SyncOperators.getCurrentOperator(context).downloadFile(logFileZipped, logSyncFile, context, taskProgress,
                "log(port: " + port + ", index: " + index + ")");
        if (logFileZipped.exists()) {
            try {
                Zip.unzipFile(logFileZipped, logFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new SyncFailureException();
            }
        }
    }

    private static void uploadRemoteLog(int port, int index, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        File logFile = Log.getLogFile(port, index);
        if (logFile.exists()) {
            try {
                File logFileZipped = Log.getCacheLogFileZipped(context);
                logFileZipped.delete();
                Zip.zipFile(logFile, logFileZipped);
                SyncFile logSyncFile = SyncFileTranslator.getLogZipped(port, index);
                SyncOperators.getCurrentOperator(context).replaceFile(logFileZipped, logSyncFile, context, taskProgress,
                        "log(port: " + port + ", index: " + index + ")");
            } catch (IOException e) {
                e.printStackTrace();
                throw new SyncFailureException();
            }
        }
    }


    private static void downloadRemotePicture(long profileId, long pictureId, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        File pictureFile = Pictures.getPictureFile(profileId, pictureId);
        SyncFile pictureSyncFile = SyncFileTranslator.getPicture(profileId, pictureId);
        SyncOperators.getCurrentOperator(context).downloadFile(pictureFile, pictureSyncFile, context, taskProgress,
                "picture(profileId: " + profileId + ", pictureId: " + pictureId + ")");
    }

    private static boolean uploadRemotePicture(long profileId, long pictureId, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        File pictureFile = Pictures.getPictureFile(profileId, pictureId);
        if (pictureFile.exists()) {
            SyncFile pictureSyncFile = SyncFileTranslator.getPicture(profileId, pictureId);
            SyncOperators.getCurrentOperator(context).insertFile(pictureFile, pictureSyncFile, context, taskProgress,
                    "picture(profileId: " + profileId + ", pictureId: " + pictureId + ")");
            return true;
        }
        return false;
    }

    private static boolean deleteRemotePicture(long profileId, long pictureId, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        SyncFile pictureSyncFile = SyncFileTranslator.getPicture(profileId, pictureId);
        SyncOperators.getCurrentOperator(context).deleteFile(pictureSyncFile, context, taskProgress,
                "picture(profileId: " + profileId + ", pictureId: " + pictureId + ")");
        return true;
    }

    private static void downloadRemoteCapture(long captureId, long coc, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        File captureFile = Captures.getCaptureFile(captureId, coc);
        SyncFile captureSyncFile = SyncFileTranslator.getCapture(captureId, coc);
        SyncOperators.getCurrentOperator(context).downloadFile(captureFile, captureSyncFile, context, taskProgress,
                "capture(captureId: " + captureId + ")");
    }

    private static void uploadRemoteCapture(long captureId, long coc, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        File captureFile = Captures.getCaptureFile(captureId, coc);
        if (captureFile.exists()) {
            SyncFile captureSyncFile = SyncFileTranslator.getCapture(captureId, coc);
            SyncOperators.getCurrentOperator(context).insertFile(captureFile, captureSyncFile, context, taskProgress,
                    "capture(captureId: " + captureId + ")");
        }
    }

    private static void deleteRemoteCapture(long captureId, long coc, Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        SyncFile captureSyncFile = SyncFileTranslator.getCapture(captureId, coc);
        SyncOperators.getCurrentOperator(context).deleteFile(captureSyncFile, context, taskProgress,
                "capture(captureId: " + captureId + ")");
    }

    private static ArrayList<SyncFile> retrieveCaptureList(Context context, TaskProgress taskProgress)
            throws SignInException, SyncFailureException {
        SyncFile captureDir = SyncFileTranslator.getCaptureDir();
        return SyncOperators.getCurrentOperator(context).listFiles(captureDir, context, taskProgress, "captureDir");
    }
}
