package util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {
    private static byte[] buffer = new byte[0x4ff];

    public static void zipFile(File file, File toZipFile) throws IOException {
        FileEntry fileEntry = new FileEntry(file);

        ZipOutputStream zipOutStream = null;
        try {
            FileOutputStream outStream = new FileOutputStream(toZipFile);
            zipOutStream = new ZipOutputStream(outStream);

            FileInputStream input = null;
            try {
                zipOutStream.putNextEntry(fileEntry.getEntry());
                input = new FileInputStream(fileEntry.getFile());

                int len = 0;
                while ((len = input.read(buffer)) > 0) {
                    zipOutStream.write(buffer, 0, len);
                }
            } finally {
                if (input != null)
                    input.close();
                zipOutStream.closeEntry();
            }
        } finally {
            if (zipOutStream != null)
                zipOutStream.close();
        }
    }

    public static void unzipFile(File zipFile, File toFile) throws IOException{
        ZipInputStream zipInStream = null;

        try {
            zipInStream = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry entry;
            int count = 0;
            while ((entry = zipInStream.getNextEntry()) != null) {
                count++;
                if (count > 1) {
                    throw new RuntimeException("zip file has more than one entries");
                }
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(toFile);

                    int len;
                    while ((len = zipInStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, len);
                    }
                } finally {
                    if (outStream != null)
                        outStream.close();
                    zipInStream.closeEntry();
                }
            }
        } finally {
            if (zipInStream != null)
                zipInStream.close();
        }
    }

    public static void zipDir(File contentDir, File toDir, String fileName) throws IOException {
        zipDir(contentDir, new File(toDir.getPath() + File.separator + fileName));
    }

    public static void zipDir(File contentDir, File toFile) throws IOException {
        Collection<FileEntry> list = listFileEntrys(contentDir);

        ZipOutputStream zipOutStream = null;
        try {
            FileOutputStream outStream = new FileOutputStream(toFile);
            zipOutStream = new ZipOutputStream(outStream);

            for (FileEntry fileEntry : list) {
                FileInputStream input = null;
                try {
                    zipOutStream.putNextEntry(fileEntry.getEntry());
                    input = new FileInputStream(fileEntry.getFile());

                    int len = 0;
                    while ((len = input.read(buffer)) > 0) {
                        zipOutStream.write(buffer, 0, len);
                    }
                } finally {
                    if (input != null)
                        input.close();
                    zipOutStream.closeEntry();
                }
            }

        } finally {
            if (zipOutStream != null)
                zipOutStream.close();
        }
    }

    public static void unzipDir(File outDir, File zipFile) throws IOException {
        unzipDir(outDir, new FileInputStream(zipFile));
    }

    public static void unzipDir(File outDir, InputStream zipInputStream) throws IOException {
        ZipInputStream zipInStream = null;

        try {
            zipInStream = new ZipInputStream(zipInputStream);

            ZipEntry entry;
            while ((entry = zipInStream.getNextEntry()) != null) {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(
                            outDir + File.separator + entry.getName());

                    int len;
                    while ((len = zipInStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, len);
                    }
                } finally {
                    if (outStream != null)
                        outStream.close();
                    zipInStream.closeEntry();
                }
            }
        } finally {
            if (zipInStream != null)
                zipInStream.close();
        }
    }

    public static void unzipDir(File outDir, File zipFile, Collection<ZipEntry> list) throws IOException {
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            for (ZipEntry entry : list) {
                FileOutputStream outStream = null;
                InputStream inStream = null;
                try {
                    outStream = new FileOutputStream(
                            outDir + File.separator + entry.getName());
                    inStream = zip.getInputStream(entry);

                    int len;
                    while ((len = inStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, len);
                    }
                } finally {
                    if (outStream != null)
                        outStream.close();
                    if (inStream != null)
                        inStream.close();
                }
            }
        } finally {
            if (zip != null)
                zip.close();
        }
    }

    private static Collection<FileEntry> listFileEntrys(File contentDir) {
        if (contentDir == null)
            return null;

        LinkedList<FileEntry> list = new LinkedList<FileEntry>();
        for (File file : contentDir.listFiles()) {
            list.add(new FileEntry(file));
        }
        return list;
    }

    public static class FileEntry {
        private File file;
        private ZipEntry buffer;

        public FileEntry() {

        }

        public FileEntry(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
            this.buffer = null;
        }

        public ZipEntry getEntry() {
            if (file == null)
                return null;

            if (buffer == null)
                buffer = new ZipEntry(file.getName());

            return buffer;
        }
    }
}
