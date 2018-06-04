package data.xml.port;

/**
 * Created by Ahmad on 02/17/18.
 * All rights reserved.
 */

public class PortContract {
    public static final String itemName = "Port";

    public static final String port = "port";
    public static final String lastPortName = "lastPortName";
    public static final String isActive = "isActive";

    public static final String lastLocalCaptureId = "lastLocalCaptureId";
    public static final String lastLocalProfileId = "lastLocalProfileId";

    public static class LastLocalIds {
        public static final String itemName = "LastIds";
        public static final String forProfileId = "forProfileId";

        public static final String noteId = "noteId";
        public static final String noteDataId = "noteDataId";
        public static final String typeId = "typeId";
        public static final String typeElementId = "typeElementId";
        public static final String pictureId = "pictureId";
        public static final String labelId = "labelId";
        public static final String labelListId = "labelListId";
        public static final String scheduleId = "scheduleId";
        public static final String occurrenceId = "occurrenceId";
        public static final String revisionPastId = "revisionPastId";
    }

    public static class LogMetadata {
        public static final String itemName = "LogMetadata";

        public static final String lastOperationId = "lastOperationId";
        public static final String lastOperationTime = "lastOperationTime";
        public static final String currentLogIndex = "currentLogIndex";

        public static class Log {
            public static final String itemName = "Log";

            public static final String index = "index";
            public static final String fromOperationId = "fromOperationId";
            public static final String toOperationId = "toOperationId";
            public static final String fromOperationTime = "fromOperationTime";
            public static final String toOperationTime = "toOperationTime";
        }
    }
}
