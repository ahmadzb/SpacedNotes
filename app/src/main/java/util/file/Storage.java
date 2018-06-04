package util.file;

public class Storage {

    private static final String Authority = "com.zenahmad.spacednote";

    public static final String AppExternalFolderName = "ExpenseManager";

    public static final String ExportFolderName = "exports";
    public static final String ExcelExtension = "xls";
    public static final String ExcelName = "Excel_";

    public static final String BackupFolderName = "backups";
    public static final String BackupExtension = "emdb";
    public static final String BackupName = "EMBackup_";

    public static final String Info = "info.txt";
    public static final String Info_DateTime = "yyyy_MM_dd_HH_mm_ss";

    private static final String tempFolderName = "temp";

    public static String getAuthority() {
        return Authority;
    }

/*
    //========================================== Backup ============================================
    public static File makeBackup(Context context) {
        try {
            return makeBackup(context, getInfo());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static File makeBackup(Context context, String infoString) throws IOException {
        File tempDir;
        // Find database file:
        File database = context.getDatabasePath(OpenHelper.name);

        // Find or Create a temp directory to put backup files into it.
        {
            tempDir = new File(context.getCacheDir().getPath() + File.separator
                    + tempFolderName);
            FileUtil.deleteDirectory(tempDir);
            tempDir.mkdirs();

            // place required files to temp folder:
            FileUtil.copyFile(database, tempDir);
            File info = new File(tempDir.getPath() + File.separator + Info);
            FileWriter writer = new FileWriter(info);
            writer.write(infoString);
            writer.close();
        }

        // provide a name for backup file:
        String fileName = BackupName
                + DateTimeFormat.forPattern("yyMMdd_HHmm").print(
                DateTime.now()) + "." + BackupExtension;
        // make backup(zip file):
        Zip.zip(tempDir, context.getCacheDir(), fileName);
        File file = new File(context.getCacheDir().getPath() + File.separator + fileName);
        return file;
    }

    public static boolean trySaveBackup(Context context) {
        int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            return saveBackup(context);
        }
        return false;
    }

    public static boolean saveBackup(Context context) {
        File backupDir = getBackupExternalDirectory();
        if (backupDir != null) {
            try {
                File input = makeBackup(context);
                return FileUtil.saveFromStream(new FileInputStream(input), backupDir, input.getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean makeBackupAndSend(Context context) {
        try {
            File backup = makeBackup(context);

            if (backup == null)
                return false;
            sendFile(context, backup);
        } catch (Exception e) {
            Toast toast = Toast.makeText(context, e.getStackTrace().toString(),
                    Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
        return true;
    }

    public static void sendFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, getAuthority(), file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("application/emdb");

        List<ResolveInfo> resInfoList = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        String title = context.getString(R.string.universal_send_backup_via);
        Intent chooser = Intent.createChooser(intent, title);
        context.startActivity(chooser);
    }

    public static boolean receiveBackup(Context context, Uri uri)
    {
        File backup = null;
        if (uri == null)
            return false;
        try
        {
            ParcelFileDescriptor mInputPFD;
            try {
                // Get the content resolver instance for this context, and use
                // it to get a ParcelFileDescriptor for the file.
                mInputPFD = context.getContentResolver().openFileDescriptor(uri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("MainActivity", "File not found.");
                return false;
            }
            // Get a regular file descriptor for the file
            FileDescriptor fd = mInputPFD.getFileDescriptor();
            // Get a Cursor and then find the file name from it
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            String name = cursor.getString(nameIndex);

            FileUtil.saveFromStream(new FileInputStream(fd), context.getCacheDir(), name);
            backup = new File(context.getCacheDir().getPath() + File.separator + name);

            boolean backupCheck = checkBackup(backup);
            if (!backupCheck)
                backup = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (backup == null) {
            try {
                backup = new File(URI.create(uri.toString()));
                boolean backupCheck = Storage.checkBackup(backup);
                if (!backupCheck)
                    backup = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (backup == null) {
            return false;
        } else {
            File backupDir = getBackupExternalDirectory();
            if (backupDir != null) {
                FileUtil.copyFile(backup, backupDir, backup.getName());
            }
            return true;
        }
    }

    public static void restore(Context context, InputStream backup) {
        try {
            restorePrivate(context, backup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void restore(Context context, File backup) {
        try {
            restorePrivate(context, backup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void restorePrivate(Context context, File backup) throws IOException {
        restorePrivate(context, new FileInputStream(backup));
    }

    private static void restorePrivate(Context context, InputStream backup) throws IOException {
        OpenHelper.closeInstance();
        File tempDir;

        // Find or Create a temp directory to put backup files into it.
        {
            tempDir = new File(context.getCacheDir().getPath() + File.separator
                    + tempFolderName);
            FileUtil.deleteDirectory(tempDir);
            tempDir.mkdirs();
            // place required files to temp folder:
            Zip.unzip(tempDir, backup);
        }

        // copy backup database to current database:
        {
            File newDatabase = new File(tempDir.getPath() + File.separator
                    + OpenHelper.name);

            // Find database file and directory:
            File database = context.getDatabasePath(OpenHelper.name);
            File databaseDir = new File(database.getParent());
            FileUtil.copyFile(newDatabase, databaseDir);
        }

    }

    public static ArrayList<File> getBackupList(Context context) {

        // Find backup directory:
        File backupDir = getBackupExternalDirectory();
        if (backupDir != null) {
            //(Legacy) check for old backups:
            {
                File oldBackupDir = new File(context.getFilesDir().getPath() + File.separator + "backups");
                if (oldBackupDir.exists()) {
                    File[] files = oldBackupDir.listFiles();
                    if (files != null) {
                        ArrayList<File> list = new ArrayList<>(files.length);
                        for (File file : files)
                            if (file.isFile())
                                FileUtil.copyFile(file, backupDir, file.getName());
                    }
                    FileUtil.deleteDirectory(oldBackupDir);
                }
            }

            {
                File[] files = backupDir.listFiles();
                if (files != null) {
                    ArrayList<File> list = new ArrayList<File>(files.length);
                    for (File file : files)
                        if (file.isFile())
                            list.add(file);
                    return list;
                }
            }
        }
        return null;
    }

    public static void deleteBackup(File backup) {
        backup.delete();
    }

    public static BackupInfo getBackupInfo(File backup) {
        try {
            return getBackupInfoPrivate(backup);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getBackupPath(String name) {
        // Find backup directory:
        File backupDir = getBackupExternalDirectory();
        if (backupDir != null) {
            File file = new File(backupDir.getPath() + File.separator + name);
            if (file.exists())
                return file.getPath();
            else
                return null;
        } else
            return null;
    }

    private static byte[] buffer = new byte[0x4ff];

    private static BackupInfo getBackupInfoPrivate(File backup)
            throws IOException {
        BackupInfo info = new BackupInfo();

        ZipInputStream zipInStream = null;
        StringBuffer infoString = new StringBuffer();

        try {
            FileInputStream inStream = new FileInputStream(backup);
            zipInStream = new ZipInputStream(inStream);

            ZipEntry entry;
            while ((entry = zipInStream.getNextEntry()) != null) {

                try {
                    if (entry.getName().equals(Info)) {
                        int len;
                        while ((len = zipInStream.read(buffer)) > 0) {
                            for (int i = 0; i < len; i++)
                                infoString.append((char) buffer[i]);
                        }
                    }
                } finally {
                    zipInStream.closeEntry();
                }
            }
        } finally {
            if (zipInStream != null)
                zipInStream.close();
        }

        String infoS = infoString.toString();
        String[] values = infoS.split(",");
        info.date = DateTimeFormat.forPattern(Info_DateTime).parseDateTime(
                values[0]);
        info.version = (values.length <= 1) ? 1 : Integer.parseInt(values[1]);

        return info;
    }

    public static boolean checkBackup(File backup) {
        try {
            return checkBackup(new FileInputStream(backup));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkBackup(InputStream backup) {
        ZipInputStream zipInStream = null;
        DBChecker checker = new DBChecker();
        try {
            zipInStream = new ZipInputStream(backup);

            ZipEntry entry;
            while ((entry = zipInStream.getNextEntry()) != null)
                checker.Check(entry.getName());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity", "Invalid File.");
            return false;
        } finally {
            if (zipInStream != null)
                try {
                    zipInStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
        }
        return checker.isValid();
    }

    //========================================== General ===========================================
    public static File getExportExternalDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File exportDir = new File(Environment.getExternalStorageDirectory().getPath()
                    + File.separator + AppExternalFolderName + File.separator + ExportFolderName);
            exportDir.mkdirs();
            return exportDir;
        } else
            return null;
    }

    public static File getBackupExternalDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File exportDir = new File(Environment.getExternalStorageDirectory().getPath()
                    + File.separator + AppExternalFolderName + File.separator + BackupFolderName);
            exportDir.mkdirs();
            return exportDir;
        } else
            return null;
    }

    public static File getExternalDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File exportDir = new File(Environment.getExternalStorageDirectory().getPath()
                    + File.separator + AppExternalFolderName);
            exportDir.mkdirs();
            return exportDir;
        } else
            return null;
    }

    private static String getInfo() {
        String info = "";

        DateTime dt = DateTime.now();
        info += DateTimeFormat.forPattern(Info_DateTime).print(dt) + ",";
        info += OpenHelper.version + ",";
        return info;
    }

    public static class BackupInfo {
        public DateTime date;
        public int version;
    }

    private static class DBChecker {
        boolean nameFound;
        boolean infoFound;

        public DBChecker() {
            nameFound = false;
            infoFound = false;
        }

        public void Check(String value) {
            if (Info.equals(value))
                infoFound = true;
            else if (OpenHelper.name.equals(value))
                nameFound = true;
        }

        public boolean isValid() {
            return nameFound && infoFound;
        }
    }
*/
}
