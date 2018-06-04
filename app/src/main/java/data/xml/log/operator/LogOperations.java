package data.xml.log.operator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeMap;

import data.database.OpenHelper;
import data.database.file.FileOpenHelper;
import data.model.capture.CaptureCatalog;
import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;
import data.storage.Captures;
import data.xml.log.operations.ExistenceOperations;
import data.xml.log.operations.LabelListOperations;
import data.xml.log.operations.LabelOperations;
import data.xml.log.operations.NoteElementOperations;
import data.xml.log.operations.NoteOperations;
import data.xml.log.operations.OccurrenceOperations;
import data.xml.log.operations.PictureOperations;
import data.xml.log.operations.ProfileOperations;
import data.xml.log.operations.RevisionOperations;
import data.xml.log.operations.ScheduleOperations;
import data.xml.log.operations.TypeElementOperations;
import data.xml.log.operations.TypeOperations;
import data.xml.port.IdProvider;
import data.xml.port.Port;
import data.xml.port.PortOperations;
import data.xml.progress.ProgressOperations;
import data.xml.util.DocumentInitializer;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class LogOperations {
    private static LinkedList<AddOperationTask> addOperationTaskQueue = new LinkedList<>();
    private static final Observer addOperationTaskQueueLock = new Observer();
    private static final Observer addOperationTaskLock = new Observer();
    private static final Observer waiter = new Observer();

    public static void waitUntilNoOperation() {
        while (addOperationTaskQueueSize() != 0) {
            synchronized (waiter) {
                try {
                    waiter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void reloadLogDocuments() {
        LogDocuments.reloadLogDocuments();
    }

    private static int addOperationTaskQueueSize() {
        int size;
        synchronized (addOperationTaskQueueLock) {
            size = addOperationTaskQueue.size();
        }
        return size;
    }

    //==================================================
    public static boolean performNewOperationsFromLog(Context context) {
        //Checking capture files:
        File earliestCapture = Captures.getEarliestCapture();
        if (earliestCapture != null && earliestCapture.exists()) {
            long currentCOC = IdProvider.progressedCumulativeOperationCount(context);
            long captureCOC = Captures.getCaptureCOCByFileName(earliestCapture.getName());
            if (captureCOC - currentCOC > CaptureCatalog.CAPTURE_RETRIEVAL_THRESHOLD) {
                CaptureCatalog.replaceFromCapture(earliestCapture, context);
            }
        }
        //performing Operations from log:
        TreeMap<Long, SQLiteDatabase> databaseProfileMap = new TreeMap<>();
        boolean result = false;

        ProgressOperations.Progress progress = ProgressOperations.getProgress(context);

        OperationCatalog.DocumentCache documentCache = OperationCatalog.DocumentCache.newInstance();
        //Finding earliest operation time
        long minTime = Long.MAX_VALUE;
        {
            int portCount = PortOperations.getPortsCount();
            for (int port = 0; port < portCount; port++) {
                long lastPerformedOperationId = progress.getLastPerformedOperationId(port);
                OperationCatalog.Filter filter = OperationCatalog.Filter.newInstance();
                filter.setFromId(lastPerformedOperationId + 1);
                filter.setToId(lastPerformedOperationId + 1);
                ArrayList<Operation> operations = OperationCatalog.getOperations(port, filter, documentCache);
                if (operations.size() != 0) {
                    if (operations.get(0).getTime() < minTime) {
                        minTime = operations.get(0).getTime();
                    }
                }
            }
        }
        //Performing Operations
        if (minTime == Long.MAX_VALUE) {
            //No unperformed operations
        } else {
            ArrayList<Operation> allOperationsToPerform = new ArrayList<>();
            int portCount = PortOperations.getPortsCount();
            for (int port = 0; port < portCount; port++) {
                OperationCatalog.Filter filter = OperationCatalog.Filter.newInstance();
                filter.setFromId(progress.getLastPerformedOperationId(port) + 1);
                filter.setFromTime(minTime);
                filter.setFromResolution(OperationCatalog.Filter.RESOLUTION_OR);
                allOperationsToPerform.addAll(OperationCatalog.getOperations(port, filter, documentCache));
            }
            Collections.sort(allOperationsToPerform, new Operation.OperationTimeComparator());
            SQLiteDatabase fileDatabase = FileOpenHelper.getDatabase(context);
            try {
                for (Operation operation : allOperationsToPerform) {
                    performOperation(operation, context, databaseProfileMap, fileDatabase);
                    progress.setLastPerformedOperationId(operation.getId(), operation.getPort());
                }
                ProgressOperations.writeProgress(progress, context, true);
            } catch (DataConversionException e) {
                throw new RuntimeException(e);
            }
        }

        for (SQLiteDatabase database : databaseProfileMap.values()) {
            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
        }
        return result;
    }

    private static void performOperation(Operation operation, Context context,
                                         TreeMap<Long, SQLiteDatabase> databaseProfileMap, SQLiteDatabase fileWritableDb)
            throws DataConversionException {
        SQLiteDatabase writableDb = null;
        if (!operation.getOperator().equals(LogContract.Operators.profile)) {
            writableDb = databaseProfileMap.get(operation.getProfileId());
            if (writableDb == null) {
                writableDb = OpenHelper.instantiateDatabaseForProfile(Profile.newInstance().setId(operation.getProfileId()), context);
                writableDb.beginTransaction();
                databaseProfileMap.put(operation.getProfileId(), writableDb);
            }
        }
        switch (operation.getOperator()) {
            case LogContract.Operators.label: {
                LabelOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.labelList: {
                LabelListOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.note: {
                NoteOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, fileWritableDb, context, operation.getProfileId());
            }
            break;
            case LogContract.Operators.noteElement: {
                NoteElementOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.occurrence: {
                OccurrenceOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.picture: {
                PictureOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.profile: {
                ProfileOperations.performOperation(operation.getElement(), operation.getTime(), context);
            }
            break;
            case LogContract.Operators.revision: {
                RevisionOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.schedule: {
                ScheduleOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.type: {
                TypeOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.typeElement: {
                TypeElementOperations.performOperation(operation.getElement(), operation.getTime(), writableDb, context);
            }
            break;
            case LogContract.Operators.existence: {
                ExistenceOperations.performOperation(operation.getElement(), operation.getTime(), fileWritableDb);
            }
            break;
            default: {
                throw new RuntimeException("Operator " + operation.getOperator() + " was not found");
            }
        }

    }

    //==================================================
    private static void addOperation(Element element, String operator, Context context) {
        AddOperationTask addOperationTask = new AddOperationTask(element, operator);
        boolean writeLogOperations;
        synchronized (addOperationTaskQueueLock) {
            addOperationTaskQueue.addLast(addOperationTask);
            writeLogOperations = addOperationTaskQueue.size() == 1;
        }
        if (writeLogOperations) {
            writeLogOperations(context);
        }
    }

    private static void writeLogOperations(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (addOperationTaskLock) {
                    try {
                        long opsTime;
                        PortOperations.PortConnection portConnection;
                        Port port;
                        Port.LogMetadata metadata;
                        ProgressOperations.Progress progress;
                        Document document;
                        Element logElement;
                        //Initializing
                        {
                            opsTime = System.currentTimeMillis();
                            portConnection = PortOperations.getCurrentPortConnection(context);
                            portConnection.lockConnection();
                            port = portConnection.getPort();
                            metadata = port.getLogMetadata();
                            //Updating metadata
                            if (LogDocuments.doesExceedSizeLimit(port.getPort(), metadata.getCurrentLogIndex())) {
                                metadata.setCurrentLogIndex(metadata.getCurrentLogIndex() + 1);
                                Port.LogMetadata.Log log = new Port.LogMetadata.Log();
                                log.setFromOperationId(metadata.getLastOperationId() + 1);
                                log.setFromOperationTime(opsTime);
                                log.setIndex(metadata.getCurrentLogIndex());
                                metadata.getLogs().add(log);
                            }
                            progress = ProgressOperations.getProgress(context);
                            document = LogDocuments.getDocument(port.getPort(), metadata.getCurrentLogIndex(),
                                    makeLogInitializer(metadata));
                            logElement = document.getRootElement();
                        }
                        //Performing some checks:
                        {
                            if (metadata.getLastOperationId() != progress.getLastPerformedOperationId(port.getPort())) {
                                throw new RuntimeException("Cannot add a new operation, logMetadata and saved operation" +
                                        " ids are out of sync. logMetadata: " + metadata.getLastOperationId() +
                                        " saved: " + progress.getLastPerformedOperationId(port.getPort()));
                            }
                            if (metadata.getLastOperationId() !=
                                    logElement.getAttribute(LogContract.LogRoot.toOperationId).getLongValue()) {
                                throw new RuntimeException("Cannot add a new operation, logMetadata and log operation" +
                                        " ids are out of sync" + metadata.getLastOperationId() +
                                        " log: " + logElement.getAttribute(LogContract.LogRoot.toOperationId).getLongValue());
                            }
                        }
                        //Main task
                        boolean hasTask = true;
                        while (hasTask) {
                            AddOperationTask task;
                            synchronized (addOperationTaskQueueLock) {
                                task = addOperationTaskQueue.getFirst();
                            }

                            long operationId = metadata.getLastOperationId() + 1;
                            metadata.setLastOperationId(operationId);
                            metadata.setLastOperationTime(opsTime);

                            Element operation = new Element(LogContract.Operation.itemName);
                            operation.setAttribute(LogContract.Operation.id, String.valueOf(operationId));
                            operation.setAttribute(LogContract.Operation.time, String.valueOf(metadata.getLastOperationTime()));
                            operation.setAttribute(LogContract.Operation.profileId,
                                    String.valueOf(ProfileCatalog.getCurrentProfile(context).getId()));
                            operation.setAttribute(LogContract.Operation.operator, task.operator);
                            operation.addContent(task.element);
                            logElement.addContent(operation);

                            synchronized (addOperationTaskQueueLock) {
                                addOperationTaskQueue.removeFirst();
                                hasTask = addOperationTaskQueue.size() != 0;
                            }
                        }
                        //Updating metadata
                        for (Port.LogMetadata.Log log : metadata.getLogs()) {
                            if (log.getIndex() == metadata.getCurrentLogIndex()) {
                                log.setToOperationId(metadata.getLastOperationId());
                                log.setToOperationTime(metadata.getLastOperationTime());
                            }
                        }
                        //Writing documents
                        {
                            portConnection.writePortAsync(port);
                            portConnection.unlockConnection();
                            progress.setLastPerformedOperationId(metadata.getLastOperationId(), port.getPort());
                            ProgressOperations.writeProgress(progress, context, false);
                            logElement.setAttribute(LogContract.LogRoot.toOperationId, String.valueOf(metadata.getLastOperationId()));
                            LogDocuments.writeDocument(document, port.getPort(), metadata.getCurrentLogIndex());
                        }
                    } catch (JDOMException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (waiter) {
                        waiter.notifyAll();
                    }
                }
            }
        });
        thread.start();
    }

    private static DocumentInitializer makeLogInitializer(final Port.LogMetadata metadata) {
        return new DocumentInitializer() {
            @Override
            public Document initializeDocument() {
                Element root = new Element(LogContract.LogRoot.root);
                root.setAttribute(LogContract.LogRoot.toOperationId, String.valueOf(metadata.getLastOperationId()));
                root.setAttribute(LogContract.LogRoot.fromOperationId, String.valueOf(metadata.getLastOperationId() + 1));
                Document document = new Document(root);
                return document;
            }
        };
    }

    //==================================================
    public static void addLabelListOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.labelList, context);
    }

    public static void addLabelOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.label, context);
    }

    public static void addNoteElementOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.noteElement, context);
    }

    public static void addTypeElementOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.typeElement, context);
    }

    public static void addNoteOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.note, context);
    }

    public static void addTypeOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.type, context);
    }

    public static void addScheduleOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.schedule, context);
    }

    public static void addRevisionOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.revision, context);
    }

    public static void addOccurrenceOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.occurrence, context);
    }

    public static void addPictureOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.picture, context);
    }

    public static void addProfileOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.profile, context);
    }

    public static void addExistenceOperation(Element element, Context context) {
        addOperation(element, LogContract.Operators.existence, context);
    }

    private static class AddOperationTask {
        final Element element;
        final String operator;

        public AddOperationTask(Element element, String operator) {
            this.element = element;
            this.operator = operator;
        }
    }

    private static class Observer {
    }
}
