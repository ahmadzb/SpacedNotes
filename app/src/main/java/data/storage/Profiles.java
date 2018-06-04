package data.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Ahmad on 02/08/18.
 * All rights reserved.
 */

public class Profiles {
    public static File getFile(Context context) {
        return new File(context.getFilesDir().getAbsolutePath() + File.separator +
                Contract.InternalDirectory.profilesFile);
    }
}
