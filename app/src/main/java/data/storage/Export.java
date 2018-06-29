package data.storage;

import android.content.Context;
import android.os.Environment;

import org.joda.time.LocalDate;

import java.io.File;

import data.preference.ChronologyCatalog;
import util.datetime.format.DateTimeFormat;

/**
 * Created by Ahmad on 06/29/18.
 * All rights reserved.
 */
public class Export {
    public static File getExportDirectory() {
        String directoryName = Contract.ExternalExportDirectory.directoryName;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                Contract.ExternalDirectory.directoryName + File.separator + directoryName;
        File file = new File(path);
        file.mkdirs();
        return file;
    }

    public static File getPdfFilesDirectory() {
        File file = new File(getExportDirectory(), Contract.ExternalExportDirectory.PdfFiles.directoryName);
        file.mkdirs();
        return file;
    }

    public static File getPDFFile(Context context, long noteId) {
        LocalDate date = new LocalDate(ChronologyCatalog.getCurrentChronology(context));
        String datePrefix = DateTimeFormat.forPattern("yyyy_MM_dd_", context.getResources()).print(date);
        return new File(getPdfFilesDirectory(), datePrefix + noteId + ".pdf");
    }
}
