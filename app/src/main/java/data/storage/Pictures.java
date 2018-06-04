package data.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;
import util.file.FileUtil;

/**
 * Created by Ahmad on 02/16/18.
 * All rights reserved.
 */

public class Pictures {
    public static final String pictureSuffix = ".jpg";

    public static File getSelectDir(Context context) {
        String path = context.getCacheDir().getAbsoluteFile() + File.separator + Contract.CacheDirectory.pictureSelectDirectory;
        File file = new File(path);
        file.mkdirs();
        return file;
    }

    public static File getCurrentProfileDir(Context context) {
        return getProfileDir(ProfileCatalog.getCurrentProfile(context));
    }

    public static File getProfileDir(Profile profile) {
        return getProfileDir(profile.getId());
    }

    public static File getProfileDir(Long profileId) {
        String directoryName = Contract.ExternalDirectory.Picture.directoryNamePrefix + profileId;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                Contract.ExternalDirectory.directoryName + File.separator + directoryName;
        File file = new File(path);
        file.mkdirs();
        return file;
    }

    public static File getPictureFile(Profile profile, long pictureId) {
        return getPictureFile(profile.getId(), pictureId);
    }

    public static File getPictureFile(long profileId, long pictureId) {
        File dir = getProfileDir(profileId);
        return new File(dir, pictureId + pictureSuffix);
    }
}
