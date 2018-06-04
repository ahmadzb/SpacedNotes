package com.diplinkblaze.spacednote.main;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;

import com.diplinkblaze.spacednote.notifications.SyncNotifications;

import java.util.ArrayList;
import java.util.LinkedList;

import data.sync.SignInException;
import data.sync.SyncCancelledException;
import data.sync.SyncFailureException;
import data.sync.SyncOperations;
import data.sync.SyncOperators;
import util.Concurrent.TaskProgress;

/**
 * Created by Ahmad on 05/26/18.
 * All rights reserved.
 */
public class SyncService extends IntentService implements TaskProgress {


    private static final int BROADCAST_INTENT_SYNC_STATE = 1;
    private static final String KEY_BROADCAST_INTENT_TAG = "broadcastIntentTag";

    private static final int SYNC_REQUEST_SYNC = 1;
    private static final int SYNC_REQUEST_SIGN_OUT = 2;
    private static final String KEY_SYNC_REQUEST = "syncRequest";

    private static final String INTENT_SYNC_ACTION = "com.diplinkblaze.spacednote.syncIntent";

    private static final SyncState syncState = new SyncState();
    private static boolean isSyncCancelled;

    public static IntentFilter getBroadcastIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_SYNC_ACTION);
        return intentFilter;
    }

    public static Intent getSyncRequestIntent(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.putExtra(KEY_SYNC_REQUEST, SYNC_REQUEST_SYNC);
        return intent;
    }

    public static Intent getSignOutRequestIntent(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.putExtra(KEY_SYNC_REQUEST, SYNC_REQUEST_SIGN_OUT);
        return intent;
    }

    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        isSyncCancelled = false;
        if (intent != null) {
            getSyncState().clearLogList();
            if (intent.getIntExtra(KEY_SYNC_REQUEST, -1) == SYNC_REQUEST_SYNC) {
                syncState.setSyncStatus(SyncState.SYNC_STATUS_RUNNING);
                onSyncStateChanged();
                SyncNotifications.dismissSyncFailureNotification(this);
                try {
                    SyncOperations.sync(this, this);
                    syncState.setSyncStatus(SyncState.SYNC_STATUS_FINISHED);
                    SyncNotifications.dismissSyncFailureNotification(this);
                } catch (SignInException e) {
                    e.printStackTrace();
                    syncState.setSyncStatus(SyncState.SYNC_STATUS_SIGN_IN_FAILURE);
                    SyncNotifications.postSyncFailureNotification(this);
                } catch (SyncFailureException e) {
                    e.printStackTrace();
                    syncState.setSyncStatus(SyncState.SYNC_STATUS_FAILURE);
                    SyncNotifications.postSyncFailureNotification(this);
                } catch (SyncCancelledException e) {
                    e.printStackTrace();
                    syncState.setSyncStatus(SyncState.SYNC_STATUS_CANCELLED);
                } finally {
                    onSyncStateChanged();
                }
            } else if (intent.getIntExtra(KEY_SYNC_REQUEST, -1) == SYNC_REQUEST_SIGN_OUT) {
                syncState.setSyncStatus(SyncState.SYNC_STATUS_SIGNING_OUT);
                onSyncStateChanged();
                try {
                    SyncOperators.getCurrentOperator(this).signOut(this, this);
                    syncState.setSyncStatus(SyncState.SYNC_STATUS_SIGNED_OUT);
                } catch (SignInException e) {
                    e.printStackTrace();
                    syncState.setSyncStatus(SyncState.SYNC_STATUS_SIGN_IN_FAILURE);
                } catch (SyncFailureException e) {
                    e.printStackTrace();
                    syncState.setSyncStatus(SyncState.SYNC_STATUS_FAILURE);
                } finally {
                    onSyncStateChanged();
                }
            }
            SyncNotifications.dismissSyncStateNotification(this);
        }
    }

    private void onSyncStateChanged() {
        Intent intent = new Intent();
        intent.setAction(INTENT_SYNC_ACTION);
        intent.putExtra(KEY_BROADCAST_INTENT_TAG, BROADCAST_INTENT_SYNC_STATE);
        sendBroadcast(intent);
        SyncNotifications.postSyncStateNotification(this);
    }

    @Override
    public void setStatus(String status) {
        getSyncState().addLog(status);
        onSyncStateChanged();
    }

    @Override
    public void setStatus(int statusResId) {
        setStatus(getString(statusResId));
    }

    @Override
    public boolean isProgressCancelled() {
        return isSyncCancelled();
    }

    private static synchronized boolean isSyncCancelled() {
        return isSyncCancelled;
    }

    public static synchronized boolean isBroadcastIntentSyncState(Intent intent) {
        return INTENT_SYNC_ACTION.equals(intent.getAction()) &&
                intent.getIntExtra(KEY_BROADCAST_INTENT_TAG, -1) == BROADCAST_INTENT_SYNC_STATE;
    }

    public static synchronized void cancelSync() {
        syncState.setSyncStatus(SyncState.SYNC_STATUS_CANCEL_ATTEMPT);
        isSyncCancelled = true;
    }

    public static synchronized SyncState getSyncState() {
        return syncState;
    }

    public static class SyncState {
        public static final int SYNC_STATUS_INITIAL = 0;
        public static final int SYNC_STATUS_RUNNING = 1;
        public static final int SYNC_STATUS_FINISHED = 2;
        public static final int SYNC_STATUS_CANCEL_ATTEMPT = 3;
        public static final int SYNC_STATUS_CANCELLED = 4;
        public static final int SYNC_STATUS_FAILURE = 5;
        public static final int SYNC_STATUS_SIGN_IN_FAILURE = 6;
        public static final int SYNC_STATUS_SIGNING_OUT = 7;
        public static final int SYNC_STATUS_SIGNED_OUT = 8;

        private static final int LogListThreshold = 200;

        private LinkedList<String> logList = new LinkedList<>();
        private int syncStatus = SYNC_STATUS_INITIAL;

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

        private synchronized void setSyncStatus(int syncStatus) {
            this.syncStatus = syncStatus;
        }

        public synchronized int getSyncStatus() {
            return syncStatus;
        }
    }
}
