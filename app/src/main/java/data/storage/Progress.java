package data.storage;

import android.content.Context;

import java.io.File;

/**
 * Created by Ahmad on 02/10/18.
 * All rights reserved.
 */

public class Progress {
    public static File getFile(Context context) {
        return new File(context.getFilesDir().getAbsolutePath() + File.separator +
                Contract.InternalDirectory.progressFile);
    }
}
