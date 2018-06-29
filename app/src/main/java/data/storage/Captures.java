package data.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ahmad on 02/16/18.
 * All rights reserved.
 */

public class Captures {
    public static File getDirectory() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                Contract.ExternalDirectory.directoryName + File.separator +
                Contract.ExternalDirectory.Capture.directoryName;
        File file = new File(path);
        file.mkdirs();
        return file;
    }

    public static File getCaptureFile(long id, long coc) {
        File captureDir = Captures.getDirectory();
        String capturePath = captureDir.getAbsolutePath() + File.separator + getCaptureName(id, coc);
        return new File(capturePath);
    }

    public static String getCaptureName(long captureId, long coc) {
        return Contract.ExternalDirectory.Capture.captureFilePrefixId + captureId +
                Contract.ExternalDirectory.Capture.captureFileCOC + coc +
                Contract.ExternalDirectory.Capture.captureFileSuffix;
    }

    public static long getCaptureIdByFileName(String captureFileName) {
        String idPrefix = Contract.ExternalDirectory.Capture.captureFilePrefixId;
        return retrieveNumberAfterText(captureFileName, idPrefix);
    }

    public static long getCaptureCOCByFileName(String captureFileName) {
        String cocPrefix = Contract.ExternalDirectory.Capture.captureFileCOC;
        return retrieveNumberAfterText(captureFileName, cocPrefix);
    }

    private static long retrieveNumberAfterText(String fullText, String text) {
        String sub = fullText.substring(fullText.indexOf(text) + text.length());
        long number = 0;
        while (sub.length() != 0 && sub.charAt(0) >= '0' && sub.charAt(0) <= '9') {
            number = number * 10 + sub.charAt(0) - '0';
            sub = sub.substring(1);
        }
        return number;
    }

    public static File getEarliestCapture() {
        File[] possibleCaptures = getDirectory().listFiles();
        if (possibleCaptures != null) {
            ArrayList<File> actualCaptures = new ArrayList<>(possibleCaptures.length);
            for (File file : possibleCaptures) {
                if (isCaptureFile(file))
                    actualCaptures.add(file);
            }

            long maxCOC = 0;
            File maxCaptureFile = null;
            for (File captureFile : actualCaptures) {
                long captureCOC = Captures.getCaptureCOCByFileName(captureFile.getName());
                if (maxCOC < captureCOC) {
                    maxCaptureFile = captureFile;
                    maxCOC = captureCOC;
                }
            }
            return maxCaptureFile;
        } else {
            return null;
        }
    }

    public static boolean isCaptureFile(File file) {
        if (file == null || !file.exists())
            return false;
        if (!file.getName().startsWith(Contract.ExternalDirectory.Capture.captureFilePrefixId))
            return false;
        if (!file.getName().endsWith(Contract.ExternalDirectory.Capture.captureFileSuffix))
            return false;
        if (!file.getName().contains(Contract.ExternalDirectory.Capture.captureFileCOC))
            return false;
        return true;
    }
}
