package data.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Ahmad on 02/18/18.
 * All rights reserved.
 */

public class Port {
    public static File getPortDir() {
        String directoryName = Contract.ExternalDirectory.Port.directoryName;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                Contract.ExternalDirectory.directoryName + File.separator + directoryName;
        File file = new File(path);
        file.mkdirs();
        return file;
    }

    public static File getPortFile(int port) {
        File dir = getPortDir();
        return new File(dir, Contract.ExternalDirectory.Port.portPrefix + port +
                Contract.ExternalDirectory.Port.portSuffix);
    }

    public static File getCachePortFileZipped(Context context) {
        return new File(context.getCacheDir(), Contract.CacheDirectory.portFileZipped);
    }
}
