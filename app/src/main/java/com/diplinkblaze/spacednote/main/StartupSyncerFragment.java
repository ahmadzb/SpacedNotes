package com.diplinkblaze.spacednote.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diplinkblaze.spacednote.R;

public class StartupSyncerFragment extends DialogFragment {

    public StartupSyncerFragment() {
        // Required empty public constructor
    }

    public static StartupSyncerFragment newInstance() {
        StartupSyncerFragment fragment = new StartupSyncerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_startup_syncer, container, false);
        initializeViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        contentView.findViewById(R.id.fragment_startup_syncer_sync_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncDrive();
            }
        });
        contentView.findViewById(R.id.fragment_startup_syncer_sync_dropbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncDropbox();
            }
        });
        contentView.findViewById(R.id.fragment_startup_syncer_sync_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.sync_skip)
                        .setMessage(R.string.sentence_skip_sync_question)
                        .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                syncSkip();
                            }
                        }).setNegativeButton(R.string.action_back, null).show();
            }
        });
    }

    private void syncDrive() {
        getListener().syncDrive();
    }

    private void syncDropbox() {
        getListener().syncDropbox();
    }

    private void syncSkip() {
        getListener().syncSkip();
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener) {
            return (OnFragmentInteractionListener) getParentFragment();
        } else if (getActivity() instanceof OnFragmentInteractionListener) {
            return (OnFragmentInteractionListener) getActivity();
        } else
            throw new RuntimeException("Either parent fragment or activity should implement OnFragmentInteractionListener");
    }

    public interface OnFragmentInteractionListener {
        void syncDrive();
        void syncDropbox();
        void syncSkip();
    }
}
