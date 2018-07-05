package com.diplinkblaze.spacednote.note;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.diplinkblaze.spacednote.notifications.ExportNotifications;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import data.database.OpenHelper;
import data.pdf.NoteToPdf;
import util.Concurrent.TaskProgress;

public class NoteToPdfService extends IntentService implements TaskProgress {

    private static final int BROADCAST_INTENT_EXPORT_STATE = 1;
    private static final String KEY_BROADCAST_INTENT_TAG = "broadcastIntentTag";
    private static final String INTENT_EXPORT_ACTION = "com.diplinkblaze.spacednote.noteToPdfIntent";

    private static final int MODE_SINGLE = 0;
    private static final int MODE_BATCH = 1;

    private static final String KEY_MODE = "mode";
    private static final String KEY_NOTE_ID = "noteID";
    private static final String KEY_NOTE_SELECTOR = "noteSelector";

    private static boolean isExportCancelled;
    private static ExportState exportState = new ExportState();

    public static Intent getIntent(long noteId, Context context) {
        Intent intent = new Intent(context, NoteToPdfService.class);
        intent.putExtra(KEY_MODE, MODE_SINGLE);
        intent.putExtra(KEY_NOTE_ID, noteId);
        return intent;
    }

    public static Intent getIntent(NoteSelector noteSelector, Context context) {
        Intent intent = new Intent(context, NoteToPdfService.class);
        intent.putExtra(KEY_MODE, MODE_BATCH);
        intent.putExtra(KEY_NOTE_SELECTOR, noteSelector);
        return intent;
    }

    public static IntentFilter getBroadcastIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_EXPORT_ACTION);
        return intentFilter;
    }

    public NoteToPdfService() {
        super("NoteToPdfService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        isExportCancelled = false;
        if (intent != null) {
            int mode = intent.getIntExtra(KEY_MODE, -1);
            if (mode == MODE_SINGLE) {
                exportState.setExportState(ExportState.EXPORT_STATE_RUNNING);
                onExportStateChanged();
                ExportNotifications.dismissExportFailureNotification(this);
                long noteId = intent.getLongExtra(KEY_NOTE_ID, -1);
                if (noteId >= 0) {
                    File destination = NoteToPdf.noteToPdf(noteId, OpenHelper.getDatabase(this), this, this);
                    if (destination == null) {
                        exportState.setExportState(ExportState.EXPORT_STATE_CANCELLED);
                    } else if (destination.exists()) {
                        exportState.setExportState(ExportState.EXPORT_STATE_FINISHED);
                    } else {
                        exportState.setExportState(ExportState.EXPORT_STATE_FAILURE);
                    }
                    onExportStateChanged();
                }
            } else if (mode == MODE_BATCH) {
                ExportNotifications.dismissExportFailureNotification(this);
                //TODO
            }
        }
        ExportNotifications.dismissExportNoteToPdfNotification(this);
    }

    private void onExportStateChanged() {
        Intent intent = new Intent();
        intent.setAction(INTENT_EXPORT_ACTION);
        intent.putExtra(KEY_BROADCAST_INTENT_TAG, BROADCAST_INTENT_EXPORT_STATE);
        sendBroadcast(intent);
        ExportNotifications.postNoteToPdfNotification(this);
    }

    @Override
    public void setStatus(String status) {
        getExportState().addLog(status);
        onExportStateChanged();
    }

    @Override
    public void setStatus(int statusResId) {
        setStatus(getString(statusResId));
    }

    @Override
    public boolean isProgressCancelled() {
        return isExportCancelled;
    }

    public static synchronized boolean isExportCancelled() {
        return isExportCancelled;
    }

    public static synchronized void cancelExport() {
        isExportCancelled = true;
        exportState.setExportState(ExportState.EXPORT_STATE_CANCEL_ATTEMPT);
    }

    public static synchronized ExportState getExportState() {
        return exportState;
    }

    public static boolean isBroadcastIntentExportState(Intent intent) {
        boolean result = false;
        if (intent != null && intent.getExtras() != null) {
            int tag = intent.getIntExtra(KEY_BROADCAST_INTENT_TAG, -1);
            result = tag == BROADCAST_INTENT_EXPORT_STATE;
        }
        return result;
    }

    public static class ExportState {
        public static final int EXPORT_STATE_IDLE = 0;
        public static final int EXPORT_STATE_RUNNING = 1;
        public static final int EXPORT_STATE_FINISHED = 2;
        public static final int EXPORT_STATE_CANCEL_ATTEMPT = 3;
        public static final int EXPORT_STATE_CANCELLED = 4;
        public static final int EXPORT_STATE_FAILURE = 5;

        private static final int LogListThreshold = 200;

        private LinkedList<String> logList = new LinkedList<>();
        private int logState = EXPORT_STATE_IDLE;

        private synchronized void addLog(String log) {
            logList.addFirst(log);
            while (logList.size() > LogListThreshold) {
                logList.removeLast();
            }
        }

        public synchronized void clearLogList() {
            logList.clear();
        }

        public synchronized ArrayList<String> getLogListCapture() {
            return new ArrayList<>(logList);
        }

        private synchronized void setExportState(int logState) {
            this.logState = logState;
        }

        public synchronized int getExportState() {
            return logState;
        }
    }
}
