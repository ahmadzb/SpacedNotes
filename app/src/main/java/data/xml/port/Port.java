package data.xml.port;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.TreeMap;

import data.model.profiles.ProfileCatalog;
import data.xml.log.operator.LogContract;

/**
 * Created by Ahmad on 02/18/18.
 * All rights reserved.
 */

public class Port {
    public static final int idShiftCount = 3;

    private int port;
    private String lastAgentName;
    private boolean isActive;

    private long lastLocalCaptureId;
    private long lastLocalProfileId;

    private TreeMap<Long, LastLocalIds> lastLocalIdsMap;
    private LogMetadata logMetadata;

    private Port() {
        lastLocalIdsMap = new TreeMap<>();
    }

    public static Port newInstance(int port) {
        Port instance = new Port().setPort(port);
        instance.setLogMetadata(new LogMetadata());
        return instance;
    }

    public LastLocalIds getLastLocalIdsCurrentProfile(Context context) {
        return getLastLocalIds(ProfileCatalog.getCurrentProfile(context).getId());
    }

    @NonNull
    public LastLocalIds getLastLocalIds(long profileId) {
        LastLocalIds lastLocalIds = lastLocalIdsMap.get(profileId);
        if (lastLocalIds == null) {
            lastLocalIds = LastLocalIds.newInstance().setProfileId(profileId);
            lastLocalIdsMap.put(profileId, lastLocalIds);
        }
        return lastLocalIds;

    }

    public Iterable<LastLocalIds> getLastLocalIdsIterable() {
        return lastLocalIdsMap.values();
    }

    public int getPort() {
        return port;
    }

    public Port setPort(int port) {
        int mask = 0;
        for (int i = 0; i < idShiftCount; i++) {
            mask = (mask << 1) | 1;
        }
        if ((port | mask) != mask)
            throw new RuntimeException("given port " + port + " is too big, must not exceed " + mask);
        this.port = port;
        return this;
    }

    public String getLastAgentName() {
        return lastAgentName;
    }

    public void setLastAgentName(String lastAgentName) {
        this.lastAgentName = lastAgentName;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public long getLastLocalCaptureId() {
        return lastLocalCaptureId;
    }

    public Port setLastLocalCaptureId(long lastLocalCaptureId) {
        this.lastLocalCaptureId = lastLocalCaptureId;
        return this;
    }

    public long getLastLocalProfileId() {
        return lastLocalProfileId;
    }

    public Port setLastLocalProfileId(long lastLocalProfileId) {
        this.lastLocalProfileId = lastLocalProfileId;
        return this;
    }

    public LogMetadata getLogMetadata() {
        return logMetadata;
    }

    public void setLogMetadata(LogMetadata logMetadata) {
        this.logMetadata = logMetadata;
    }

    public boolean equals(Port second) {
        if (port != second.port)
            return false;
        if (lastAgentName != null && !lastAgentName.equals(second.lastAgentName))
            return false;
        if (second.lastAgentName != null && !second.lastAgentName.equals(lastAgentName))
            return false;
        if (isActive != second.isActive)
            return false;
        if (lastLocalCaptureId != second.lastLocalCaptureId)
            return false;
        if (lastLocalProfileId != second.lastLocalProfileId)
            return false;
        if (lastLocalIdsMap.size() != second.lastLocalIdsMap.size())
            return false;
        for (LastLocalIds lastLocalIds : lastLocalIdsMap.values()) {
            LastLocalIds secondLastLocalIds = second.lastLocalIdsMap.get(lastLocalIds.getProfileId());
            if (secondLastLocalIds == null)
                return false;
            if (!secondLastLocalIds.equals(lastLocalIds))
                return false;
        }
        if (!logMetadata.equals(second.logMetadata))
            return false;
        return true;
    }

    public static class LastLocalIds {
        private long profileId;

        private long lastNoteId;
        private long lastNoteDataId;
        private long lastTypeId;
        private long lastTypeElementId;
        private long lastPictureId;
        private long lastLabelId;
        private long lastLabelListId;
        private long lastScheduleId;
        private long lastOccurrenceId;
        private long lastRevisionPastId;

        private LastLocalIds() {

        }

        public static LastLocalIds newInstance() {
            return new LastLocalIds();
        }

        public long getProfileId() {
            return profileId;
        }

        public LastLocalIds setProfileId(long profileId) {
            this.profileId = profileId;
            return this;
        }

        public long getLastNoteId() {
            return lastNoteId;
        }

        public LastLocalIds setLastNoteId(long lastNoteId) {
            this.lastNoteId = lastNoteId;
            return this;
        }

        public long getLastNoteDataId() {
            return lastNoteDataId;
        }

        public LastLocalIds setLastNoteDataId(long lastNoteDataId) {
            this.lastNoteDataId = lastNoteDataId;
            return this;
        }

        public long getLastTypeId() {
            return lastTypeId;
        }

        public LastLocalIds setLastTypeId(long lastTypeId) {
            this.lastTypeId = lastTypeId;
            return this;
        }

        public long getLastTypeElementId() {
            return lastTypeElementId;
        }

        public LastLocalIds setLastTypeElementId(long lastTypeElementId) {
            this.lastTypeElementId = lastTypeElementId;
            return this;
        }

        public long getLastPictureId() {
            return lastPictureId;
        }

        public LastLocalIds setLastPictureId(long lastPictureId) {
            this.lastPictureId = lastPictureId;
            return this;
        }

        public long getLastLabelId() {
            return lastLabelId;
        }

        public LastLocalIds setLastLabelId(long lastLabelId) {
            this.lastLabelId = lastLabelId;
            return this;
        }

        public long getLastLabelListId() {
            return lastLabelListId;
        }

        public void setLastLabelListId(long lastLabelListId) {
            this.lastLabelListId = lastLabelListId;
        }

        public long getLastScheduleId() {
            return lastScheduleId;
        }

        public LastLocalIds setLastScheduleId(long lastScheduleId) {
            this.lastScheduleId = lastScheduleId;
            return this;
        }

        public long getLastOccurrenceId() {
            return lastOccurrenceId;
        }

        public LastLocalIds setLastOccurrenceId(long lastOccurrenceId) {
            this.lastOccurrenceId = lastOccurrenceId;
            return this;
        }

        public long getLastRevisionPastId() {
            return lastRevisionPastId;
        }

        public LastLocalIds setLastRevisionPastId(long lastRevisionPastId) {
            this.lastRevisionPastId = lastRevisionPastId;
            return this;
        }

        public LastLocalIds clone() {
            LastLocalIds clone = LastLocalIds.newInstance();
            clone.profileId = this.profileId;

            clone.lastNoteId = this.lastNoteId;
            clone.lastNoteDataId = this.lastNoteDataId;
            clone.lastTypeId = this.lastTypeId;
            clone.lastTypeElementId = this.lastTypeElementId;
            clone.lastPictureId = this.lastPictureId;
            clone.lastLabelId = this.lastLabelId;
            clone.lastScheduleId = this.lastScheduleId;
            clone.lastOccurrenceId = this.lastOccurrenceId;
            clone.lastRevisionPastId = this.lastRevisionPastId;
            return clone;
        }

        public boolean equals(LastLocalIds second) {
            boolean equals = true;
            equals = equals && profileId == second.profileId;
            equals = equals && lastNoteId == second.lastNoteId;
            equals = equals && lastNoteDataId == second.lastNoteDataId;
            equals = equals && lastTypeId == second.lastTypeId;
            equals = equals && lastTypeElementId == second.lastTypeElementId;
            equals = equals && lastPictureId == second.lastPictureId;
            equals = equals && lastLabelId == second.lastLabelId;
            equals = equals && lastLabelListId == second.lastLabelListId;
            equals = equals && lastScheduleId == second.lastScheduleId;
            equals = equals && lastOccurrenceId == second.lastOccurrenceId;
            equals = equals && lastRevisionPastId == second.lastRevisionPastId;
            return equals;
        }
    }

    public static class LogMetadata {
        private long lastOperationId;
        private long lastOperationTime;
        private int currentLogIndex;
        private ArrayList<Log> logs;

        public LogMetadata() {
            currentLogIndex = LogContract.StartLogIndex - 1;
            lastOperationId = LogContract.StartOperationId - 1;
            logs = new ArrayList<>();
        }

        public long getLastOperationId() {
            return lastOperationId;
        }

        public void setLastOperationId(long lastOperationId) {
            this.lastOperationId = lastOperationId;
        }

        public long getLastOperationTime() {
            return lastOperationTime;
        }

        public void setLastOperationTime(long lastOperationTime) {
            this.lastOperationTime = lastOperationTime;
        }

        public int getCurrentLogIndex() {
            return currentLogIndex;
        }

        public void setCurrentLogIndex(int currentLogIndex) {
            this.currentLogIndex = currentLogIndex;
        }

        public ArrayList<Log> getLogs() {
            return logs;
        }

        public void setLogs(ArrayList<Log> logs) {
            this.logs = logs;
        }

        public boolean equals(LogMetadata second) {
            boolean equals = true;
            equals = equals && lastOperationId == second.lastOperationId;
            equals = equals && lastOperationTime == second.lastOperationTime;
            equals = equals && currentLogIndex == second.currentLogIndex;
            for (Log log : logs) {
                boolean hasEqual = false;

                for (int i = 0; i < second.logs.size() && !hasEqual; i++) {
                    hasEqual = log.equals(second.logs.get(i));
                }

                equals = equals && hasEqual;
            }
            return equals;
        }

        public static class Log implements Comparable<Log> {
            private int index;
            private long fromOperationId;
            private long toOperationId;
            private long fromOperationTime;
            private long toOperationTime;

            public int getIndex() {
                return index;
            }

            public void setIndex(int index) {
                this.index = index;
            }

            public long getFromOperationId() {
                return fromOperationId;
            }

            public void setFromOperationId(long firstOperationId) {
                this.fromOperationId = firstOperationId;
            }

            public long getToOperationId() {
                return toOperationId;
            }

            public void setToOperationId(long toOperationId) {
                this.toOperationId = toOperationId;
            }

            public long getFromOperationTime() {
                return fromOperationTime;
            }

            public void setFromOperationTime(long fromOperationTime) {
                this.fromOperationTime = fromOperationTime;
            }

            public long getToOperationTime() {
                return toOperationTime;
            }

            public void setToOperationTime(long toOperationTime) {
                this.toOperationTime = toOperationTime;
            }

            @Override
            public int compareTo(@NonNull Log o) {
                return Long.compare(toOperationId, o.toOperationId);
            }

            public boolean equals(Log second) {
                boolean equals = true;
                equals = equals && index == second.index;
                equals = equals && fromOperationId == second.fromOperationId;
                equals = equals && toOperationId == second.toOperationId;
                equals = equals && fromOperationTime == second.fromOperationTime;
                equals = equals && toOperationTime == second.toOperationTime;
                return equals;
            }
        }
    }
}
