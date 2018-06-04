package data.storage;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import data.model.note.Note;
import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;
import util.file.FileUtil;

/**
 * Created by Ahmad on 01/24/18.
 * All rights reserved.
 */

public class PictureOperations {
    private static final String pictureSuffix = Pictures.pictureSuffix;

    public static void clearSelectDir(Context context) {
        File select = Pictures.getSelectDir(context);
        FileUtil.deleteDirectory(select);
    }

    public static void deleteProfileDir(Profile profile, Context context) {
        FileUtil.deleteDirectory(Pictures.getProfileDir(profile));
    }

    public static void deleteProfileDirAsync(Profile profile, Context context) {
        final File profileDir = Pictures.getProfileDir(profile);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.deleteDirectory(profileDir);
            }
        });
        thread.start();
    }

    /**
     * @param note
     * @param context
     * @param readableDb
     * @return Select folder file names mapped to picture ids, null if one or more files aren't cached
     */
    public static TreeMap<Long, File> copyToSelectDir(Note note, Context context, SQLiteDatabase readableDb) {
        TreeMap<Long, File> map = new TreeMap<>();

        File picturesDir = Pictures.getCurrentProfileDir(context);
        File selectDir = Pictures.getSelectDir(context);

        ArrayList<Long> ids = data.database.PictureOperations.getPictureIdsByNote(note, readableDb);
        ArrayList<File> copies = newPictures(selectDir, pictureSuffix, ids.size());

        for (int i = 0; i < ids.size(); i++) {
            long id = ids.get(i);
            File file = new File(picturesDir, id + pictureSuffix);
            if (file.exists()) {
                File copy = copies.get(i);
                FileUtil.copyFile(file, selectDir, copy.getName());
                map.put(id, copy);
            }
        }
        return map;
    }

    public static ArrayList<File> saveToSelectDir(Context context, Intent intent) {

        ArrayList<Bitmap> bitmaps;
        if (intent.getClipData() != null) {
            bitmaps = new ArrayList<>(intent.getClipData().getItemCount());

            //Get Bitmaps
            {
                try {
                    for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                        Uri uri = intent.getClipData().getItemAt(i).getUri();
                        bitmaps.add(MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            bitmaps = new ArrayList<>();

            //Get Bitmaps
            {
                try {
                    Uri uri = intent.getData();
                    bitmaps.add(MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ArrayList<File> newPictures;

        int imageQualityPercentage = 100;
        //Determine image quality
        {
            Profile currentProfile = ProfileCatalog.getCurrentProfileIfExist(context);
            if (currentProfile != null) {
                imageQualityPercentage = currentProfile.getImageQualityPercentage();
            }
        }

        //Save Bitmaps
        {


            newPictures = newPictures(Pictures.getSelectDir(context), pictureSuffix, bitmaps.size());
            for (int i = 0; i < bitmaps.size(); i++) {
                Bitmap bmp = bitmaps.get(i);
                File file = newPictures.get(i);
                //---
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    if (imageQualityPercentage != 100) {
                        int maxWidth = getMaxWidthByQualityPercentage(imageQualityPercentage);
                        int width = bmp.getWidth();
                        if (maxWidth < width) {
                            int dstWidth = maxWidth;
                            int dstHeight = bmp.getHeight() * maxWidth / width;
                            bmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
                        }
                    }
                    bmp.compress(Bitmap.CompressFormat.JPEG, imageQualityPercentage, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //---
            }
        }
        return newPictures;
    }

    public static void submitPicture(File picture, long pictureId, Context context) {
        File picturesDir = Pictures.getCurrentProfileDir(context);
        FileUtil.copyFile(picture, picturesDir, pictureId + pictureSuffix);
    }

    public static void deletePicture(long pictureId, Context context) {
        File picturesDir = Pictures.getCurrentProfileDir(context);
        File file = new File(picturesDir.getAbsolutePath() + File.separator + pictureId + pictureSuffix);
        if (file.exists()) {
            file.delete();
        }
    }

    //======================================= Util =================================================

    /**
     * Create unique file references for new pictures to save in
     *
     * @param directory
     * @param suffix
     * @param count
     * @return
     */
    private static ArrayList<File> newPictures(File directory, String suffix, int count) {
        ArrayList<String> names;
        {
            String[] n = directory.list();
            names = new ArrayList<String>(n.length);
            for (String name : n) {
                names.add(name);
            }
        }
        ArrayList<String> newNames = new ArrayList<>(count);
        while (newNames.size() < count) {
            String name = RandomStringUtils.randomAlphabetic(12) + suffix;
            if (!names.contains(name) && !newNames.contains(name))
                newNames.add(name);
        }
        ArrayList<File> files = new ArrayList<>(count);
        for (String name : newNames) {
            files.add(new File(directory.getAbsolutePath() + File.separator + name));
        }
        return files;
    }

    private static int getMaxWidthByQualityPercentage(int qualityPercentage) {
        int lowestMaxWidth = 100;
        int highestMaxWidth = 4000;
        int c = (highestMaxWidth - lowestMaxWidth) / 100;
        return lowestMaxWidth + c * qualityPercentage;
    }
}
