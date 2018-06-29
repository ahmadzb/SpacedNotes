package data.storage;

import android.content.Context;

import java.io.File;

/**
 * Created by Ahmad on 01/26/18.
 * All rights reserved.
 */

public class Contract {
    public static class CacheDirectory {
        public static final String pictureSelectDirectory = "selectPictures";
        public static final String SyncOperationsCachedPort = "syncOperationsCachedPort";
        public static final String logFileZipped = "logFileZipped.zip";
        public static final String portFileZipped = "portFileZipped.zip";
    }

    public static class InternalDirectory {
        public static final String profilesFile = "profiles.xml";
        public static final String progressFile = "progress.xml";
    }

    public static class ExternalDirectory {
        public static final String directoryName = "SpacedNotes";

        public static class Picture {
            public static final String directoryNamePrefix = "pictures";
        }

        public static class Log {
            public static final String directoryName = "log";

            public static class Port {
                public static final String directoryNamePrefix = "port";

                public static final String logFilePrefix = "log";
                public static final String logFileSuffix = ".xml";
                public static final String logMetadata = "metadata.xml";
            }
        }

        public static class Port {
            public static final String directoryName = "port";

            public static final String portPrefix = "port";
            public static final String portSuffix = ".xml";
        }

        public static class Capture {
            public static final String directoryName = "captures";

            public static final String captureFilePrefixId = "cID";
            public static final String captureFileCOC = "COC";
            public static final String captureFileSuffix = ".zip";
        }
    }

    public static class ExternalExportDirectory {
        public static final String directoryName = "SpacedNotesExports";


        public static class PdfFiles {
            public static final String directoryName = "PdfFiles";

        }
    }
}
