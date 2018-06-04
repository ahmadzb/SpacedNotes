package com.diplinkblaze.spacednote.main;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.dropbox.core.android.Auth;

import data.database.file.FileOpenHelper;
import data.drive.Authentication;
import data.drive.DriveOperator;
import data.dropbox.DropboxOperator;
import data.model.existence.ExistenceCatalog;
import data.sync.SyncOperators;

public class SyncActivity extends AppCompatActivity {

    private static final int REQUEST_SYNC = 1;
    private static final int REQUEST_SYNC_AUTO_CLOSE = 2;
    private static final int REQUEST_VISIT = 3;
    private static final int REQUEST_VISIT_AUTO_CLOSE = 4;

    private static final String KEY_REQUEST = "request";
    private static final String KEY_IS_SIGN_IN_FLOW = "isSignInFlow";
    private static final String KEY_CAN_SIGN_IN = "canSignIn";

    private static final int SIGN_IN_REQUEST = 0;

    private boolean isSignInFlow;
    private boolean canSignIn;
    private final SyncBroadcastReceiver syncBroadcastReceiver = new SyncBroadcastReceiver();

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SyncActivity.class);
        intent.putExtra(KEY_REQUEST, REQUEST_SYNC);
        return intent;
    }

    public static Intent getAutoCloseIntent(Context context) {
        Intent intent = new Intent(context, SyncActivity.class);
        intent.putExtra(KEY_REQUEST, REQUEST_SYNC_AUTO_CLOSE);
        return intent;
    }

    public static Intent getVisitIntent(Context context) {
        Intent intent = new Intent(context, SyncActivity.class);
        intent.putExtra(KEY_REQUEST, REQUEST_VISIT);
        return intent;
    }

    public static Intent getAutoCloseVisitIntent(Context context) {
        Intent intent = new Intent(context, SyncActivity.class);
        intent.putExtra(KEY_REQUEST, REQUEST_VISIT_AUTO_CLOSE);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        if (savedInstanceState != null) {
            isSignInFlow = savedInstanceState.getBoolean(KEY_IS_SIGN_IN_FLOW);
            canSignIn = savedInstanceState.getBoolean(KEY_CAN_SIGN_IN);
        }

        initializeViews();

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null && SyncService.getSyncState().getSyncStatus() !=
                SyncService.SyncState.SYNC_STATUS_RUNNING) {
            canSignIn = true;
            startService(SyncService.getSyncRequestIntent(this));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isSignInFlow) {
            if (SyncOperators.getCurrentOperator(this) instanceof DropboxOperator) {
                if (Auth.getOAuth2Token() != null) {
                    startService(SyncService.getSyncRequestIntent(this));
                }
                isSignInFlow = false;
            }
        }
        registerReceiver(syncBroadcastReceiver, SyncService.getBroadcastIntentFilter());
        updateViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                startService(SyncService.getSyncRequestIntent(this));
            }
            isSignInFlow = false;
            updateViews();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initializeViews() {

    }

    private void updateViews() {
        invalidateOptionsMenu();
        //LogList
        {
            StringBuilder log = new StringBuilder();
            for (String status : SyncService.getSyncState().getLogListCapture()) {
                log.append(status).append('\n');
            }
            TextView logTextView = findViewById(R.id.activity_sync_log);
            logTextView.setText(log.toString());
        }
        //Status
        {
            TextView syncStatusTextView = findViewById(R.id.activity_sync_status);
            View progressView = findViewById(R.id.activity_sync_progress_bar);
            int syncStatus = SyncService.getSyncState().getSyncStatus();
            if (syncStatus == SyncService.SyncState.SYNC_STATUS_INITIAL) {
                syncStatusTextView.setText(R.string.initializing);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedGray2));
                progressView.setVisibility(View.VISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_RUNNING) {
                syncStatusTextView.setText(R.string.syncing);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedGray2));
                progressView.setVisibility(View.VISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_CANCELLED) {
                syncStatusTextView.setText(R.string.sync_cancelled);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedGray2));
                progressView.setVisibility(View.INVISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_CANCEL_ATTEMPT) {
                syncStatusTextView.setText(R.string.sync_cancel_attempt);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedGray2));
                progressView.setVisibility(View.INVISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_FINISHED) {
                syncStatusTextView.setText(R.string.sync_finished);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedGreen));
                progressView.setVisibility(View.INVISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_FAILURE) {
                syncStatusTextView.setText(R.string.sync_failed);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedRed));
                progressView.setVisibility(View.INVISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_SIGN_IN_FAILURE && !isSignInFlow) {
                syncStatusTextView.setText(R.string.sync_sign_in_failed);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedRed));
                progressView.setVisibility(View.INVISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_SIGN_IN_FAILURE && isSignInFlow) {
                syncStatusTextView.setText(R.string.sync_sign_in_flow);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedBlue));
                progressView.setVisibility(View.VISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_SIGNING_OUT) {
                syncStatusTextView.setText(R.string.sync_signing_out);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedBlue));
                progressView.setVisibility(View.VISIBLE);
            } else if (syncStatus == SyncService.SyncState.SYNC_STATUS_SIGNED_OUT) {
                syncStatusTextView.setText(R.string.sync_signed_out);
                syncStatusTextView.setTextColor(getResources().getColor(R.color.colorNamedGray2));
                progressView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sync_menu, menu);
        int syncStatus = SyncService.getSyncState().getSyncStatus();
        if (syncStatus == SyncService.SyncState.SYNC_STATUS_FINISHED ||
                syncStatus == SyncService.SyncState.SYNC_STATUS_RUNNING) {
            menu.removeItem(R.id.sync_menu_retry);
        }
        if (syncStatus != SyncService.SyncState.SYNC_STATUS_RUNNING) {
            menu.removeItem(R.id.sync_menu_stop_sync);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.sync_menu_retry) {
            retrySync();
        } else if (item.getItemId() == R.id.sync_menu_stop_sync) {
            stopSync();
        } else if (item.getItemId() == R.id.sync_menu_sign_out) {
            signOut();
        } else if (item.getItemId() == R.id.sync_menu_forget_synced_content) {

            forgetSyncedContent();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_SIGN_IN_FLOW, isSignInFlow);
        outState.putBoolean(KEY_CAN_SIGN_IN, canSignIn);
    }

    //============================================ Sync ============================================
    private void explicitSignIn() {
        isSignInFlow = true;
        canSignIn = false;
        if (SyncOperators.getCurrentOperator(this) instanceof DriveOperator) {
            Intent intent = Authentication.getSignInIntent(this);
            startActivityForResult(intent, SIGN_IN_REQUEST);
        } else if (SyncOperators.getCurrentOperator(this) instanceof DropboxOperator) {
            Auth.startOAuth2Authentication(this, "4y76xyq70frs0yf");
        }
        updateViews();
    }

    private void retrySync() {
        canSignIn = true;
        startService(SyncService.getSyncRequestIntent(this));
        updateViews();
    }

    private void stopSync() {
        SyncService.cancelSync();
        onSyncStateChanged();
    }

    private void forgetSyncedContent() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sentence_forget_synced_content)
                .setMessage(R.string.sentence_forget_synced_content_use_cases)
                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopSync();
                        int flag = SyncOperators.getCurrentOperatorExistenceFlag(SyncActivity.this);
                        SQLiteDatabase fileWritableDb = FileOpenHelper.getDatabase(SyncActivity.this);
                        ExistenceCatalog.clearExistenceFlag(flag, fileWritableDb, SyncActivity.this);
                        Toast.makeText(SyncActivity.this,
                                R.string.sentence_synced_content_forgotten_successfully, Toast.LENGTH_SHORT).show();
                        signOut();
                    }
                }).setNegativeButton(R.string.action_back, null).show();
    }

    private void signOut() {
        startService(SyncService.getSignOutRequestIntent(this));
        onSyncStateChanged();
    }

    private class SyncBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SyncService.isBroadcastIntentSyncState(intent)) {
                onSyncStateChanged();
            }
        }
    }

    private void onSyncStateChanged() {
        if (SyncService.getSyncState().getSyncStatus() == SyncService.SyncState.SYNC_STATUS_SIGN_IN_FAILURE
                && canSignIn) {
            explicitSignIn();
        } else if (SyncService.getSyncState().getSyncStatus() == SyncService.SyncState.SYNC_STATUS_FINISHED) {
            int request = getIntent().getIntExtra(KEY_REQUEST, -1);
            if (request == REQUEST_SYNC_AUTO_CLOSE || request == REQUEST_VISIT_AUTO_CLOSE) {
                setResult(RESULT_OK);
                finish();
            }
        }
        updateViews();
    }
}
