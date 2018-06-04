package data.sync;

import data.storage.Pictures;

/**
 * Created by Ahmad on 03/20/18.
 * All rights reserved.
 */

public class SyncFileContract {
    public static class Log {
        public static final String directoryName = "log";

        public static class Port{
            public static final String directoryNamePrefix = "port";

            public static final String logFilePrefix = "log";
            public static final String logFileSuffix = ".xml";
            public static final String logFileSuffixZipped = ".zip";
            public static final String logMetadata = "metadata.xml";
        }
    }

    public static class Port {
        public static final String directoryName = "port";

        public static final String portPrefix = "port";
        public static final String portSuffix = ".xml";
    }

    public static class Picture {
        public static final String directoryNamePrefix = "pictures";
        public static final String pictureFileSuffix = Pictures.pictureSuffix;
    }

    public static class Capture {
        public static final String directoryName = "captures";

        public static final String captureFilePrefix = "capture";
        public static final String captureFileSuffix = ".zip";
    }
}
