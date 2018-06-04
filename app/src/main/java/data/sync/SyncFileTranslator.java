package data.sync;

import android.content.Context;

import java.io.File;

import data.model.profiles.Profile;
import data.storage.Captures;
import data.storage.Pictures;
import util.Concurrent.TaskProgress;

/**
 * Created by Ahmad on 03/20/18.
 * All rights reserved.
 */

public class SyncFileTranslator {
    public static SyncFile getLogZipped(int port, int index) {
        SyncFile logDir = new SyncFile(SyncFileContract.Log.directoryName);
        SyncFile portDir = new SyncFile(logDir, SyncFileContract.Log.Port.directoryNamePrefix + port);
        SyncFile logFile = new SyncFile(portDir,
                SyncFileContract.Log.Port.logFilePrefix + index + SyncFileContract.Log.Port.logFileSuffix
                        + SyncFileContract.Log.Port.logFileSuffixZipped);
        return logFile;
    }

    public static SyncFile getPortZipped(int port) {
        SyncFile portDir = new SyncFile(SyncFileContract.Port.directoryName);
        SyncFile portFile = new SyncFile(portDir, SyncFileContract.Port.portPrefix + port +
                SyncFileContract.Port.portSuffix + SyncFileContract.Log.Port.logFileSuffixZipped);
        return portFile;
    }

    public static SyncFile getPicture(long profileId, long pictureId) {
        SyncFile picDir = new SyncFile(SyncFileContract.Picture.directoryNamePrefix + profileId);
        SyncFile picFile = new SyncFile(picDir, pictureId + SyncFileContract.Picture.pictureFileSuffix);
        return picFile;
    }

    public static SyncFile getCaptureDir() {
        SyncFile capDir = new SyncFile(SyncFileContract.Capture.directoryName);
        return capDir;
    }

    public static SyncFile getCapture(long captureId, long coc) {
        SyncFile capDir = getCaptureDir();
        SyncFile capFile = new SyncFile(capDir, Captures.getCaptureName(captureId, coc));
        return capFile;
    }
}
