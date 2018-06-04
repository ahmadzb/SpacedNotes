package data.xml.port;

import android.content.Context;

import data.model.profiles.ProfileCatalog;
import data.xml.progress.ProgressOperations;

/**
 * Created by Ahmad on 02/18/18.
 * All rights reserved.
 */

public class IdProvider {
    public static synchronized long nextProfileId(Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        long localId = port.getLastLocalProfileId() + 1;
        port.setLastLocalProfileId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextCaptureId(Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        long localId = port.getLastLocalCaptureId() + 1;
        port.setLastLocalCaptureId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextNoteId(Context context) {
        return nextNoteId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextNoteId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastNoteId() + 1;
        lastLocalIds.setLastNoteId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextNoteDataId(Context context) {
        return nextNoteDataId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextNoteDataId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastNoteDataId() + 1;
        lastLocalIds.setLastNoteDataId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextTypeId(Context context) {
        return nextTypeId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextTypeId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastTypeId() + 1;
        lastLocalIds.setLastTypeId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextTypeElementId(Context context) {
        return nextTypeElementId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextTypeElementId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastTypeElementId() + 1;
        lastLocalIds.setLastTypeElementId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextPictureId(Context context) {
        return nextPictureId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextPictureId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastPictureId() + 1;
        lastLocalIds.setLastPictureId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextLabelId(Context context) {
        return nextLabelId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextLabelId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastLabelId() + 1;
        lastLocalIds.setLastLabelId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextLabelListId(Context context) {
        return nextLabelListId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextLabelListId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastLabelListId() + 1;
        lastLocalIds.setLastLabelListId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextScheduleId(Context context) {
        return nextScheduleId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextScheduleId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastScheduleId() + 1;
        lastLocalIds.setLastScheduleId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long nextOccurrenceId(Context context) {
        return nextOccurrenceId(ProfileCatalog.getCurrentProfile(context).getId(), context);
    }

    public static synchronized long nextOccurrenceId(long profileId, Context context) {
        PortOperations.PortConnection portConnection = PortOperations.getCurrentPortConnection(context);
        portConnection.lockConnection();
        Port port = portConnection.getPort();
        if (port == null)
            throw new RuntimeException("Current port is null, cannot get new id");
        Port.LastLocalIds lastLocalIds = port.getLastLocalIds(profileId);
        long localId = lastLocalIds.getLastOccurrenceId() + 1;
        lastLocalIds.setLastOccurrenceId(localId);
        portConnection.writePortAsync(port);
        portConnection.unlockConnection();
        return PortOperations.getGlobalId(localId, port);
    }

    public static synchronized long progressedCumulativeOperationCount(Context context) {
        long cumulativeOperationCount = 0;
        ProgressOperations.Progress progress = ProgressOperations.getProgress(context);
        for (int i = 0; i < PortOperations.getPortsCount(); i++) {
            cumulativeOperationCount += progress.getLastPerformedOperationId(i) + 1;
        }
        return cumulativeOperationCount;
    }
}
