package com.diplinkblaze.spacednote.note;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BaseActivity;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.NoActionbarActivity;
import com.diplinkblaze.spacednote.revision.RevisionPastListActivity;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import data.database.OpenHelper;
import data.model.label.Label;
import data.model.label.LabelCatalog;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.schedule.Occurrence;
import data.model.schedule.Schedule;
import data.model.schedule.ScheduleCatalog;
import data.model.scheduler.RevisionCatalog;
import data.model.scheduler.RevisionFuture;
import data.model.scheduler.Scheduler;
import data.pdf.NoteToPdf;
import util.Colors;
import util.TypeFaceUtils;
import util.datetime.primitive.Representation;

public class NoteViewActivity extends NoActionbarActivity implements ContentUpdateListener,
        NoteDrawerFragment.NoteDrawerFragmentInteractions {

    private static final String KEY_NOTE_ID = "noteId";
    private static final String KEY_READ_ONLY = "readOnly";

    private static final String KEY_SCHEDULE_CONTROLS_STATE = "scheduleControlsExpanded";

    private static final String TAG_NOTE_CONTENT_FRAGMENT = "noteContentFragment";
    private static final String TAG_NAV_VIEW = "navView";
    private static final int ACTIVITY_REQUEST_NOTE_EDIT = 0;
    private static final int ACTIVITY_REQUEST_REVISION_PASTS_LIST = 1;

    private ActionBar actionBar = new ActionBar();
    private ScheduleControls scheduleControls = new ScheduleControls();

    private Note note;
    private ArrayList<Label> labels;

    public static Intent getIntent(long noteId, Context context) {
        Intent intent = new Intent(context, NoteViewActivity.class);
        intent.putExtra(KEY_NOTE_ID, noteId);
        intent.putExtra(KEY_READ_ONLY, false);
        return intent;
    }

    public static Intent getIntentReadOnly(long noteId, Context context) {
        Intent intent = new Intent(context, NoteViewActivity.class);
        intent.putExtra(KEY_NOTE_ID, noteId);
        intent.putExtra(KEY_READ_ONLY, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        SQLiteDatabase database = OpenHelper.getDatabase(this);
        note = NoteCatalog.getNoteById(getIntent().getLongExtra(KEY_NOTE_ID, -1), database);
        labels = LabelCatalog.getLabelsByNote(note.getId(), database);
        initializeViews(savedInstanceState);
        updateViews();
    }

    private void initializeViews(Bundle savedInstanceState) {

        if (getSupportFragmentManager().findFragmentByTag(TAG_NOTE_CONTENT_FRAGMENT) == null) {
            NoteContentViewFragment fragment = NoteContentViewFragment.newInstance();
            fragment.setContent(note);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_note_view_content_frame, fragment, TAG_NOTE_CONTENT_FRAGMENT);
            transaction.commit();
        }
        if (getSupportFragmentManager().findFragmentByTag(TAG_NAV_VIEW) == null) {
            NoteDrawerFragment fragment = NoteDrawerFragment.newInstance(false);
            fragment.setNoteDrawerInstance(NoteDrawerFragment.NoteDrawerInstance.newInstanceFromNote(note,this));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.activity_note_nav_view, fragment, TAG_NAV_VIEW);
            transaction.commit();
        }

        actionBar.initializeViews();
        scheduleControls.initializeViews(savedInstanceState);
    }

    private void updateViews() {
        actionBar.updateViews();
        scheduleControls.updateViews();
    }

    @Override
    public void updateContent() {
        note = NoteCatalog.getNoteById(getIntent().getLongExtra(KEY_NOTE_ID, -1),
                OpenHelper.getDatabase(this));

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment fragment : fragmentList) {
                if (fragment instanceof ContentUpdateListener)
                    ((ContentUpdateListener) fragment).updateContent();
                if (fragment instanceof NoteDrawerFragment) {
                    ((NoteDrawerFragment) fragment).setNoteDrawerInstance(
                            NoteDrawerFragment.NoteDrawerInstance.newInstanceFromNote(note, this));
                }
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        scheduleControls.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST_NOTE_EDIT) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                updateContent();
            }
        } else if (requestCode == ACTIVITY_REQUEST_REVISION_PASTS_LIST) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                updateViews();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ActionBar {

        public void initializeViews() {
            View backButton = findViewById(R.id.content_note_view_toolbar_dismiss);
            View editButton = findViewById(R.id.content_note_view_toolbar_edit);
            View pdfButton = findViewById(R.id.content_note_view_toolbar_to_pdf);
            View deleteButton = findViewById(R.id.content_note_view_toolbar_delete);
            View labelsButton = findViewById(R.id.content_note_view_toolbar_labels);
            View infoButton = findViewById(R.id.content_note_view_toolbar_info);


            OnActionBarItemClicked actionBarItemClicked = new OnActionBarItemClicked();
            backButton.setOnClickListener(actionBarItemClicked);
            pdfButton.setOnClickListener(actionBarItemClicked);
            labelsButton.setOnClickListener(actionBarItemClicked);
            infoButton.setOnClickListener(actionBarItemClicked);
            boolean isReadOnly = getIntent().getBooleanExtra(KEY_READ_ONLY, false);
            if (isReadOnly) {
                deleteButton.setVisibility(View.GONE);
                editButton.setVisibility(View.GONE);
            } else {
                editButton.setOnClickListener(actionBarItemClicked);
                deleteButton.setOnClickListener(actionBarItemClicked);
            }
        }

        public void updateViews() {
            TextView labelsTextView = (TextView) findViewById(R.id.content_note_view_toolbar_labels);
            labelsTextView.setText(TypeFaceUtils.withNumberFormat(labels.size()));
        }

        private void dismiss() {
            finish();
        }

        private void edit() {
            Intent intent = NoteEditActivity.getIntentEdit(getApplicationContext(), note);
            startActivityForResult(intent, ACTIVITY_REQUEST_NOTE_EDIT);
        }

        private void toPdf() {
            Intent intent = NoteToPdfService.getIntent(note.getId(), getApplicationContext());
            startService(intent);
            //NoteToPdf.noteToPdf(note.getId(), OpenHelper.getDatabase(getApplicationContext()), getApplicationContext());
            //Toast.makeText(NoteViewActivity.this, "PDF Successful", Toast.LENGTH_SHORT).show();
        }

        private void labels() {
            info();
        }

        private void info() {
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            View drawerView = findViewById(R.id.activity_note_nav_view);
            drawerLayout.openDrawer(drawerView);
        }

        private void delete() {
            new AlertDialog.Builder(NoteViewActivity.this)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.sentence_delete_item_question)
                    .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NoteCatalog.markAsDeleted(note, OpenHelper.getDatabase(NoteViewActivity.this),
                                    NoteViewActivity.this);
                            setResult(RESULT_OK);
                            finish();
                        }
                    }).setNegativeButton(R.string.action_no, null).show();

        }

        private class OnActionBarItemClicked implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.content_note_view_toolbar_dismiss) {
                    dismiss();
                } else if (v.getId() == R.id.content_note_view_toolbar_edit) {
                    edit();
                } else if (v.getId() == R.id.content_note_view_toolbar_to_pdf) {
                    toPdf();
                } else if (v.getId() == R.id.content_note_view_toolbar_labels) {
                    labels();
                } else if (v.getId() == R.id.content_note_view_toolbar_info) {
                    info();
                } else if (v.getId() == R.id.content_note_view_toolbar_delete) {
                    delete();
                }
            }
        }
    }

    private class ScheduleControls {
        private final int SCHEDULE_CONTROLS_STATE_COLLAPSED = 0;
        private final int SCHEDULE_CONTROLS_STATE_EXPANDED = 1;
        private final int SCHEDULE_CONTROLS_STATE_NEW_SCHEDULE = 2;
        private final int SCHEDULE_CONTROLS_STATE_RESTART_SCHEDULE = 3;

        private int scheduleControlsState = SCHEDULE_CONTROLS_STATE_COLLAPSED;
        private ScheduleAdapter scheduleAdapter;
        private OccurrenceAdapter occurrenceAdapter;

        public void initializeViews(Bundle savedInstanceState) {
            SQLiteDatabase database = OpenHelper.getDatabase(getApplicationContext());
            //General
            {
                if (savedInstanceState != null) {
                    scheduleControlsState = savedInstanceState.getInt(KEY_SCHEDULE_CONTROLS_STATE);
                } else {
                    if (shouldShowNewScheduleState(database)) {
                        scheduleControlsState = SCHEDULE_CONTROLS_STATE_NEW_SCHEDULE;
                    } else {
                        scheduleControlsState = SCHEDULE_CONTROLS_STATE_COLLAPSED;
                    }
                }

                View scheduleControlsShadow = findViewById(R.id.content_note_view_schedule_shadow);
                View scheduleControlsExpandButton = findViewById(R.id.content_note_view_schedule_expand);
                scheduleControlsShadow.setOnClickListener(new OnScheduleControlsShadowClicked());
                scheduleControlsExpandButton.setOnClickListener(new OnScheduleControlsExpandClicked());
            }

            //Schedule Controls
            {
                View nextOccurrence = findViewById(R.id.content_note_view_schedule_next_occurrence_layout);
                nextOccurrence.setOnClickListener(new OnNextOccurrenceClicked());

                Spinner scheduleSpinner = findViewById(R.id.partial_note_schedule_controls_schedule);
                Spinner occurrenceSpinner = findViewById(R.id.partial_note_schedule_controls_occurrence);
                scheduleAdapter = new ScheduleAdapter(ScheduleCatalog.getSchedulesWithOccurrences(database));
                scheduleSpinner.setAdapter(scheduleAdapter);
                scheduleSpinner.setOnItemSelectedListener(new OnScheduleSpinnerItemSelected());
                occurrenceSpinner.setOnItemSelectedListener(new OnOccurrenceSpinnerItemSelected());

                View restartView = findViewById(R.id.partial_note_schedule_controls_restart);
                restartView.setOnClickListener(new OnScheduleControlsRestartClicked());

                View restartDismiss = findViewById(R.id.content_note_view_schedule_new_schedule_dismiss);
                restartDismiss.setOnClickListener(new OnScheduleControlsRestartDismissClicked());

                View revisionPastsView = findViewById(R.id.partial_note_schedule_controls_view_past);
                revisionPastsView.setOnClickListener(new OnViewRevisionPastsClicked());
            }
        }

        public void updateViews() {
            SQLiteDatabase database = OpenHelper.getDatabase(getApplicationContext());
            //General
            {
                View scheduleControlsShadow = findViewById(R.id.content_note_view_schedule_shadow);
                View scheduleControls = findViewById(R.id.content_note_view_schedule_controls);
                ImageView scheduleControlsExpandButton = findViewById(R.id.content_note_view_schedule_expand);
                if (scheduleControlsState == SCHEDULE_CONTROLS_STATE_EXPANDED) {
                    scheduleControlsShadow.setVisibility(View.VISIBLE);
                    scheduleControls.setVisibility(View.VISIBLE);
                    scheduleControlsExpandButton.setImageResource(R.drawable.ic_expand);
                } else {
                    scheduleControlsShadow.setVisibility(View.GONE);
                    scheduleControls.setVisibility(View.GONE);
                    scheduleControlsExpandButton.setImageResource(R.drawable.ic_collapse);
                }
            }
            //New Schedule
            {
                View newScheduleScroll = findViewById(R.id.content_note_view_schedule_new_schedule_scroll);
                if (scheduleControlsState == SCHEDULE_CONTROLS_STATE_NEW_SCHEDULE ||
                        scheduleControlsState == SCHEDULE_CONTROLS_STATE_RESTART_SCHEDULE) {
                    newScheduleScroll.setVisibility(View.VISIBLE);
                    LinearLayout scheduleListLayout = findViewById(R.id.content_note_view_schedule_new_schedule_list_layout);
                    scheduleListLayout.removeAllViews();
                    ArrayList<Schedule> schedules = ScheduleCatalog.getSchedulesWithOccurrences(database);
                    Collections.sort(schedules, Schedule.getPositionComparator());
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    for (Schedule schedule : schedules) {
                        View scheduleItemView = inflater.inflate(R.layout.partial_schedule_view_item_horizontal,
                                scheduleListLayout, false);
                        View scheduleItemLayout = scheduleItemView.findViewById(R.id.partial_schedule_view_item_horizontal_layout);
                        TextView scheduleName = scheduleItemView.findViewById(R.id.partial_schedule_view_item_horizontal_schedule);
                        TextView schedulePlusDays = scheduleItemView.findViewById(R.id.partial_schedule_view_item_horizontal_plus_days);
                        ImageView scheduleColorBar = scheduleItemView.findViewById(R.id.partial_schedule_view_item_horizontal_color_bar);
                        scheduleItemLayout.setOnClickListener(new OnNewScheduleItemClicked(schedule));
                        scheduleName.setText(schedule.getTitle());
                        Occurrence firstOccurrence = schedule.getFirstOccurrence();
                        if (firstOccurrence != null) {
                            schedulePlusDays.setText("+" + firstOccurrence.getPlusDays());
                        } else {
                            schedulePlusDays.setText(R.string.no_occurrence);
                        }
                        scheduleColorBar.setBackgroundColor(schedule.getColor());
                        scheduleName.setTextColor(schedule.getColor());
                        schedulePlusDays.setTextColor(schedule.getColor());
                        scheduleListLayout.addView(scheduleItemView);
                    }
                    View newScheduleDismiss = findViewById(R.id.content_note_view_schedule_new_schedule_dismiss);
                    if (scheduleControlsState == SCHEDULE_CONTROLS_STATE_RESTART_SCHEDULE) {
                        newScheduleDismiss.setVisibility(View.VISIBLE);
                    } else {
                        newScheduleDismiss.setVisibility(View.GONE);
                    }
                } else {
                    newScheduleScroll.setVisibility(View.GONE);
                }
            }

            RevisionFuture revisionFuture = RevisionCatalog.getRevisionFutureForNote(note, database);
            //Next Occurrence
            {
                View nextOccurrenceLayout = findViewById(R.id.content_note_view_schedule_next_occurrence_layout);
                TextView nextOccurrenceText = findViewById(R.id.content_note_view_schedule_next_occurrence_text);
                ImageView nextOccurrenceImage = findViewById(R.id.content_note_view_schedule_next_occurrence_image);
                boolean nextEnabled;
                if (revisionFuture == null) {
                    nextOccurrenceText.setText(R.string.no_occurrence);
                    nextEnabled = false;
                } else {
                    LocalDate now = LocalDate.now();
                    LocalDate dueDate = Representation.toLocalDate(revisionFuture.getDueDate());
                    Schedule schedule = ScheduleCatalog.getScheduleById(revisionFuture.getScheduleId(), database);
                    Occurrence occurrence = schedule.getOccurrenceByNumber(revisionFuture.getOccurrenceNumber());
                    String text;
                    if (occurrence == null) {
                        text = getString(R.string.non_existent_occurrence);
                        nextEnabled = false;
                    } else {
                        text = "#" + occurrence.getEndUserNumber() + " +" + occurrence.getPlusDays();
                        if (now.isAfter(dueDate)) {
                            text = text + " (+" + Days.daysBetween(dueDate, now).getDays() + ")";
                            nextEnabled = true;
                        } else if (now.isBefore(dueDate)) {
                            text = text + " (-" + Days.daysBetween(now, dueDate).getDays() + ")";
                            nextEnabled = false;
                        } else {
                            nextEnabled = true;
                        }
                    }
                    nextOccurrenceText.setText(text);
                }
                if (nextEnabled) {
                    nextOccurrenceText.setTextColor(Colors.getPrimaryColor(NoteViewActivity.this));
                    nextOccurrenceLayout.setEnabled(true);
                    nextOccurrenceImage.setVisibility(View.VISIBLE);
                } else {
                    nextOccurrenceText.setTextColor(getResources().getColor(R.color.colorTextDetail));
                    nextOccurrenceLayout.setEnabled(false);
                    nextOccurrenceImage.setVisibility(View.GONE);
                }
            }

            //Schedule Controls
            {
                Spinner scheduleSpinner = findViewById(R.id.partial_note_schedule_controls_schedule);
                Spinner occurrenceSpinner = findViewById(R.id.partial_note_schedule_controls_occurrence);
                if (revisionFuture != null) {
                    int position = scheduleAdapter.getPositionByScheduleId(revisionFuture.getScheduleId());
                    Schedule currentSchedule = scheduleAdapter.getScheduleByPosition(position);
                    scheduleSpinner.setSelection(position);

                    Occurrence occurrence = currentSchedule.getOccurrenceByNumber(revisionFuture.getOccurrenceNumber());
                    if (occurrence == null) {
                        occurrenceSpinner.setAdapter(null);
                    } else {
                        occurrenceAdapter = new OccurrenceAdapter(currentSchedule);
                        occurrenceSpinner.setAdapter(occurrenceAdapter);
                        occurrenceSpinner.setSelection(
                                occurrenceAdapter.getPositionByOccurrenceNumber(occurrence.getNumber()));
                    }
                } else {
                    scheduleSpinner.setSelection(scheduleAdapter.getPositionNoSchedule());
                    occurrenceAdapter = null;
                    occurrenceSpinner.setAdapter(occurrenceAdapter);
                }

            }

        }


        private void onSaveInstanceState(Bundle outState) {
            outState.putInt(KEY_SCHEDULE_CONTROLS_STATE, scheduleControlsState);
        }

        private void resetState(SQLiteDatabase readableDb) {
            if (shouldShowNewScheduleState(readableDb)) {
                scheduleControlsState = SCHEDULE_CONTROLS_STATE_NEW_SCHEDULE;
            } else {
                scheduleControlsState = SCHEDULE_CONTROLS_STATE_COLLAPSED;
            }
            updateViews();
        }

        private boolean shouldShowNewScheduleState(SQLiteDatabase readableDb) {
            return RevisionCatalog.getLastRevisionPastForNote(note, readableDb) == null;
        }

        private class OnNextOccurrenceClicked implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                Scheduler.submitCurrentOccurrence(note, OpenHelper.getDatabase(context), context);
                updateViews();
                setResult(RESULT_OK);
            }
        }

        private class OnNewScheduleItemClicked implements View.OnClickListener {
            Schedule schedule;

            public OnNewScheduleItemClicked(Schedule schedule) {
                this.schedule = schedule;
            }

            @Override
            public void onClick(View v) {
                Context context = NoteViewActivity.this;
                SQLiteDatabase database = OpenHelper.getDatabase(context);
                Scheduler.startSchedule(note, schedule, database, context);
                resetState(database);
                setResult(RESULT_OK);
            }
        }

        private class OnViewRevisionPastsClicked implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                Context context = NoteViewActivity.this;
                Intent intent = RevisionPastListActivity.getIntentForNote(note, context);
                startActivityForResult(intent, ACTIVITY_REQUEST_REVISION_PASTS_LIST);
            }
        }

        private class OnScheduleControlsRestartClicked implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                scheduleControlsState = SCHEDULE_CONTROLS_STATE_RESTART_SCHEDULE;
                updateViews();
            }
        }

        private class OnScheduleControlsRestartDismissClicked implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                scheduleControlsState = SCHEDULE_CONTROLS_STATE_EXPANDED;
                updateViews();
            }
        }

        private class OnScheduleControlsShadowClicked implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                if (scheduleControlsState == SCHEDULE_CONTROLS_STATE_EXPANDED) {
                    scheduleControlsState = SCHEDULE_CONTROLS_STATE_COLLAPSED;
                }
                updateViews();
            }
        }

        private class OnScheduleControlsExpandClicked implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                if (scheduleControlsState == SCHEDULE_CONTROLS_STATE_COLLAPSED) {
                    scheduleControlsState = SCHEDULE_CONTROLS_STATE_EXPANDED;
                } else if (scheduleControlsState == SCHEDULE_CONTROLS_STATE_EXPANDED) {
                    scheduleControlsState = SCHEDULE_CONTROLS_STATE_COLLAPSED;
                }
                updateViews();
            }
        }

        private class OnScheduleSpinnerItemSelected implements AdapterView.OnItemSelectedListener {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Schedule schedule = scheduleAdapter.getScheduleByPosition(position);
                SQLiteDatabase database = OpenHelper.getDatabase(getApplicationContext());
                RevisionFuture revisionFuture = RevisionCatalog.getRevisionFutureForNote(note, database);
                if (schedule == null && revisionFuture == null) {
                    //DO NOTHING
                } else if (schedule == null && revisionFuture != null) {
                    RevisionCatalog.deleteRevisionFuture(note, database, getApplicationContext());
                    updateViews();
                    setResult(RESULT_OK);
                } else if (schedule != null && revisionFuture == null) {
                    Scheduler.carryOnWithSchedule(note, schedule, database, getApplicationContext());
                    updateViews();
                    setResult(RESULT_OK);
                } else if (schedule.getId() != revisionFuture.getScheduleId()) {
                    Scheduler.changeSchedule(note, schedule, database, getApplicationContext());
                    updateViews();
                    setResult(RESULT_OK);
                } else if (schedule.getId() == revisionFuture.getScheduleId()) {
                    //DO NOTHING
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        }

        private class OnOccurrenceSpinnerItemSelected implements AdapterView.OnItemSelectedListener {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SQLiteDatabase database = OpenHelper.getDatabase(getApplicationContext());
                RevisionFuture revisionFuture = RevisionCatalog.getRevisionFutureForNote(note, database);
                Occurrence occurrence = occurrenceAdapter.getOccurrenceByPosition(position);
                if (revisionFuture.getOccurrenceNumber() != occurrence.getNumber()) {
                    Scheduler.changeOccurrence(note, occurrence.getNumber(), database, getApplicationContext());
                    updateViews();
                    setResult(RESULT_OK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        }

        private class ScheduleAdapter extends BaseAdapter {

            private ArrayList<Schedule> schedules;

            public ScheduleAdapter(ArrayList<Schedule> schedulesWithOccurrences) {
                this.schedules = schedulesWithOccurrences;
            }

            @Override
            public int getCount() {
                return schedules.size() + 1;
            }

            @Override
            public Object getItem(int position) {
                return getScheduleByPosition(position);
            }

            @Override
            public long getItemId(int position) {
                Schedule schedule = getScheduleByPosition(position);
                if (schedule != null) {
                    return schedule.getId();
                } else {
                    return -1;
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(NoteViewActivity.this).inflate(
                            R.layout.partial_spinner_view_item, parent, false);
                    holder = new ViewHolder(convertView);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Schedule schedule = getScheduleByPosition(position);
                if (schedule != null) {
                    holder.title.setText(schedule.getTitle());
                    holder.title.setTextColor(schedule.getColor());
                } else {
                    holder.title.setText(R.string.no_schedule);
                    holder.title.setTextColor(getResources().getColor(R.color.colorTextDetail));
                }
                return convertView;
            }

            public int getPositionByScheduleId(long scheduleId) {
                int position = -1;
                for (int i = 0; i < schedules.size(); i++) {
                    if (schedules.get(i).getId() == scheduleId) {
                        position = i;
                    }
                }
                if (position == -1)
                    throw new RuntimeException("schedule was not found, something most have gone wrong");
                return position;
            }

            public int getPositionNoSchedule() {
                return schedules.size();
            }

            @Nullable
            public Schedule getScheduleByPosition(int position) {
                if (position == schedules.size()) {
                    return null;
                } else {
                    return schedules.get(position);
                }
            }

            private class ViewHolder {
                public ViewHolder(View convertView) {
                    convertView.setTag(this);
                    title = convertView.findViewById(R.id.partial_spinner_view_item_title);
                }

                TextView title;
            }
        }

        private class OccurrenceAdapter extends BaseAdapter {

            private Schedule schedule;

            public OccurrenceAdapter(Schedule scheduleWithOccurrences) {
                this.schedule = scheduleWithOccurrences;
            }

            @Override
            public int getCount() {
                return schedule.getOccurrencesCount();
            }

            @Override
            public Object getItem(int position) {
                return getOccurrenceByPosition(position);
            }

            @Override
            public long getItemId(int position) {
                Occurrence occurrence = getOccurrenceByPosition(position);
                if (occurrence == null) {
                    return -1;
                } else {
                    return occurrence.getId();
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(NoteViewActivity.this).inflate(
                            R.layout.partial_spinner_view_item, parent, false);
                    holder = new ViewHolder(convertView);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                Occurrence occurrence = getOccurrenceByPosition(position);
                if (occurrence != null) {
                    holder.title.setText("#" + occurrence.getEndUserNumber() + " +" + occurrence.getPlusDays());
                } else {
                    holder.title.setText(R.string.no_occurrence);
                }
                return convertView;
            }


            public int getPositionByOccurrenceNumber(int occurrenceNumber) {
                if (occurrenceNumber >= schedule.getOccurrencesCount())
                    throw new RuntimeException("occurrence was not found, something most have gone wrong");
                return occurrenceNumber;
            }

            public int getPositionByOccurrenceId(long occurrenceId) {
                int position = -1;
                for (int i = 0; i < schedule.getOccurrencesCount(); i++) {
                    if (schedule.getOccurrenceByNumber(i).getId() == occurrenceId) {
                        position = i;
                    }
                }
                if (position == -1)
                    throw new RuntimeException("occurrence was not found, something most have gone wrong");
                return position;
            }

            @Nullable
            public Occurrence getOccurrenceByPosition(int position) {
                return schedule.getOccurrenceByNumber(position);
            }

            private class ViewHolder {
                public ViewHolder(View convertView) {
                    convertView.setTag(this);
                    title = convertView.findViewById(R.id.partial_spinner_view_item_title);
                }

                TextView title;
            }
        }
    }

    //================================== Communication Children ======================================
    @Override
    public void onNoteDrawerInstanceChanged(NoteDrawerFragment.NoteDrawerInstance noteDrawerInstance) {
        labels = noteDrawerInstance.getLabels();
        updateViews();
    }
}
