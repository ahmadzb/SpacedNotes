package com.diplinkblaze.spacednote.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import com.diplinkblaze.spacednote.R;

import data.preference.SyncPreferences;
import data.xml.port.PortOperations;

public class LauncherActivity extends AppCompatActivity implements StartupSyncerFragment.OnFragmentInteractionListener{

    private static final int ACTIVITY_REQUEST_SYNC = 0;
    private static final int ACTIVITY_REQUEST_PORT = 1;

    private static final String FRAGMENT_STARTUP_SYNCER = "fragmentStartupSyncer";

    private static final int PERMISSION_REQUEST_ON_CREATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_ON_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        if (requestCode == PERMISSION_REQUEST_ON_CREATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (PortOperations.getCurrentPort(this) != null) {
                    startActivity(MainActivity.getIntent(this));
                    finish();
                } else {
                    StartupSyncerFragment syncerFragment = StartupSyncerFragment.newInstance();
                    syncerFragment.show(getSupportFragmentManager(), FRAGMENT_STARTUP_SYNCER);
                }
            } else {
                new AlertDialog.Builder(this).setTitle(R.string.sentence_permission_denied)
                        .setMessage(R.string.sentence_storage_permission_explanation)
                        .setNegativeButton(R.string.action_exit, null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        }).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST_SYNC) {
            if (resultCode == RESULT_OK) {
                startPortActivity();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.sync_failed)
                        .setMessage(R.string.sentence_skip_sync_question)
                        .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startPortActivity();
                            }
                        }).setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StartupSyncerFragment syncerFragment = (StartupSyncerFragment) getSupportFragmentManager()
                                .findFragmentByTag(FRAGMENT_STARTUP_SYNCER);
                        if (syncerFragment == null) {
                            syncerFragment = StartupSyncerFragment.newInstance();
                            syncerFragment.show(getSupportFragmentManager(), FRAGMENT_STARTUP_SYNCER);
                        }
                    }
                }).show();
            }
        } else if (requestCode == ACTIVITY_REQUEST_PORT) {
            if (PortOperations.getCurrentPort(this) != null) {
                startActivity(MainActivity.getIntent(this));
            }
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPortActivity() {
        Intent intent = PortActivity.getIntent(this);
        startActivityForResult(intent, ACTIVITY_REQUEST_PORT);
    }

    @Override
    public void syncDrive() {
        SyncPreferences.setCurrentSyncOperatorDrive(this);
        startActivityForResult(SyncActivity.getAutoCloseIntent(this), ACTIVITY_REQUEST_SYNC);
    }

    @Override
    public void syncDropbox() {
        SyncPreferences.setCurrentSyncOperatorDropbox(this);
        startActivityForResult(SyncActivity.getAutoCloseIntent(this), ACTIVITY_REQUEST_SYNC);
    }

    @Override
    public void syncSkip() {
        startPortActivity();
    }
}
