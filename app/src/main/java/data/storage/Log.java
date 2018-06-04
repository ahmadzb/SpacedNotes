package data.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class Log {

    public static File getRootDir() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                Contract.ExternalDirectory.directoryName + File.separator +
                Contract.ExternalDirectory.Log.directoryName;
        File file = new File(path);
        file.mkdirs();
        return file;
    }

    public static File getPortDir(int port) {
        File dir =  new File(getRootDir(), Contract.ExternalDirectory.Log.Port.directoryNamePrefix + port);
        dir.mkdirs();
        return dir;
    }

    public static File getLogFile(int port, int index) {
        File dir = getPortDir(port);
        return new File(dir, Contract.ExternalDirectory.Log.Port.logFilePrefix + index +
                Contract.ExternalDirectory.Log.Port.logFileSuffix);
    }

    public static File getLogFile(int port, String fileName) {
        File dir = getPortDir(port);
        return new File(dir, fileName);
    }

    public static File getCacheLogFileZipped(Context context) {
        return new File(context.getCacheDir(), Contract.CacheDirectory.logFileZipped);
    }
}
