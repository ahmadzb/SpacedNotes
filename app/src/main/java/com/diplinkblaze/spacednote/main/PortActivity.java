package com.diplinkblaze.spacednote.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;

import java.util.ArrayList;

import data.xml.port.Port;
import data.xml.port.PortOperations;

public class PortActivity extends AppCompatActivity {
    private PortAdapter adapter;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, PortActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_port);
        initializeViews();

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (PortOperations.getActivePortsCount() == 0) {
            newDevice();
        }
    }

    private void initializeViews() {
        adapter = new PortAdapter();
        RecyclerView recyclerView = findViewById(R.id.activity_port_device_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        View newDevice = findViewById(R.id.activity_port_continue);
        newDevice.setOnClickListener(new OnNewDeviceClicked());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deactivatePort(final Port port) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_active_device)
                .setMessage(R.string.sentence_remove_selected_active_device)
                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Port portFull = PortOperations.connectionFor(port.getPort()).getPort();
                        portFull.setActive(false);
                        PortOperations.connectionFor(portFull.getPort()).writePort(portFull);
                        adapter.reload();
                    }
                }).setNegativeButton(R.string.action_no, null).show();
    }

    private void newDevice() {
        Port port = PortOperations.getFirstInactivePort();
        if (port != null) {
            PortOperations.setCurrentPortAsync(port.getPort(), this);
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.sentence_max_active_device_reached)
                    .setNegativeButton(R.string.action_back, null).show();
        }
    }

    private class OnNewDeviceClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            newDevice();
        }
    }

    private class PortAdapter extends RecyclerView.Adapter<PortAdapter.ViewHolder> {

        private ArrayList<Port> activePorts;

        public PortAdapter() {
            initialize();
        }

        private void initialize() {
            activePorts = PortOperations.getActivePorts();
        }

        public void reload() {
            initialize();
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(PortActivity.this);
            return new ViewHolder(
                    inflater.inflate(R.layout.partial_portlist_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Port port = activePorts.get(position);
            holder.remove.setTag(port);
            holder.deviceName.setText(port.getLastAgentName());
            holder.port.setText("Port " + port.getPort());
        }

        @Override
        public int getItemCount() {
            return activePorts.size();
        }

        private class OnRemoveClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Port port = (Port) v.getTag();
                deactivatePort(port);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView deviceName;
            private TextView port;
            private View remove;
            private View divider;

            public ViewHolder(View itemView) {
                super(itemView);
                deviceName = itemView.findViewById(R.id.partial_portlist_item_device_name);
                port = itemView.findViewById(R.id.partial_portlist_item_port);
                remove = itemView.findViewById(R.id.partial_portlist_item_remove);
                divider = itemView.findViewById(R.id.partial_portlist_item_divider);

                remove.setOnClickListener(new OnRemoveClickListener());
            }
        }
    }
}
