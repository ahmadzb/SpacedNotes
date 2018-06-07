package com.diplinkblaze.spacednote.note;

import android.app.TimePickerDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.labels.LabelLookupFragment;
import com.diplinkblaze.spacednote.universal.fragments.CalendarFragment;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.annotation.Nullable;

import data.database.OpenHelper;
import data.model.label.Label;
import data.model.label.LabelCatalog;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.type.Type;
import data.model.type.TypeCatalog;
import data.preference.ChronologyCatalog;
import util.datetime.format.DateTimeFormat;
import util.datetime.format.DateTimeFormatter;


public class NoteDrawerFragment extends Fragment implements LabelLookupFragment.OnFragmentInteractionListener,
        CalendarFragment.OnFragmentInteractionListener {

    private static final String KEY_NOTE_DRAWER_INSTANCE = "noteDrawerInstance";
    private static final String KEY_EDITABLE = "editable";
    private static final String TAG_LABEL_LOOKUP = "labelLookup";
    private static final String TAG_CALENDAR_CREATE_DATE = "calendarCreateDate";
    private static final String TAG_CALENDAR_MODIFY_DATE = "calendarModifyDate";
    private static final String TAG_CALENDAR = "calendar";
    private NoteDrawerInstance noteDrawerInstance;

    public NoteDrawerFragment() {
        // Required empty public constructor
    }

    public static NoteDrawerFragment newInstance(boolean editable) {
        NoteDrawerFragment fragment = new NoteDrawerFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_EDITABLE, editable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (this.noteDrawerInstance == null) {
                this.noteDrawerInstance = NoteDrawerInstance.newInstanceFromBundle(
                        getArguments().getBundle(KEY_NOTE_DRAWER_INSTANCE), getContext());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_note_drawer, container, false);
        initializeViews(contentView);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        View addLabelView = contentView.findViewById(R.id.fragment_note_drawer_labels_add);
        addLabelView.setOnClickListener(new OnAddLabelClickListener());

        TextView createDateText = contentView.findViewById(R.id.fragment_note_drawer_create_date_text);
        TextView modifyDateText = contentView.findViewById(R.id.fragment_note_drawer_modify_date_text);
        createDateText.setOnClickListener(new OnCreateDateClicked());
        modifyDateText.setOnClickListener(new OnModifyDateClicked());
    }

    private void tryUpdateViews() {
        View contentView = getView();
        if (contentView != null) {
            updateViews(contentView);
        }
    }

    private void updateViews(View contentView) {
        if (noteDrawerInstance != null) {
            //Dates
            {
                TextView createDateText = contentView.findViewById(R.id.fragment_note_drawer_create_date_text);
                TextView modifyDateText = contentView.findViewById(R.id.fragment_note_drawer_modify_date_text);
                DateTimeFormatter formatter = DateTimeFormat.fullDateTime(getContext());
                if (noteDrawerInstance.createDate != null) {
                    createDateText.setText(formatter.print(noteDrawerInstance.createDate));
                } else {
                    createDateText.setText("");
                }
                if (noteDrawerInstance.modifyDate != null) {
                    modifyDateText.setText(formatter.print(noteDrawerInstance.modifyDate));
                } else {
                    modifyDateText.setText("");
                }
            }
            //Labels:
            {
                LinearLayout labelLayout = contentView.findViewById(R.id.fragment_note_drawer_tags_frame);
                labelLayout.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                for (Label label : noteDrawerInstance.labels) {
                    View itemView = inflater.inflate(R.layout.partial_label_list_item, labelLayout, false);
                    labelLayout.addView(itemView);
                    TextView title = itemView.findViewById(R.id.partial_label_list_item_title);
                    View remove = itemView.findViewById(R.id.partial_label_list_item_remove);
                    title.setText(label.getTitle());
                    remove.setOnClickListener(new OnRemoveClickListener(label));
                }
            }
            //Note Type
            {
                TextView noteType = contentView.findViewById(R.id.fragment_note_drawer_note_type_text);
                if (noteDrawerInstance.getNoteType() != null) {
                    noteType.setText(noteDrawerInstance.getNoteType().getTitle());
                } else {
                    noteType.setText("");
                }
            }
        }
    }

    public NoteDrawerFragment setNoteDrawerInstance(NoteDrawerInstance noteDrawerInstance) {
        this.noteDrawerInstance = noteDrawerInstance;
        getArguments().putBundle(KEY_NOTE_DRAWER_INSTANCE, noteDrawerInstance.toBundle());
        tryUpdateViews();
        return this;
    }

    private class OnRemoveClickListener implements View.OnClickListener {
        private Label label;

        public OnRemoveClickListener(Label label) {
            this.label = label;
        }

        @Override
        public void onClick(View v) {
            removeLabel(label.getId());
        }
    }

    private class OnAddLabelClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            LabelLookupFragment fragment = new LabelLookupFragment();
            fragment.show(getChildFragmentManager(), TAG_LABEL_LOOKUP);
        }
    }

    private class OnCreateDateClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!getArguments().getBoolean(KEY_EDITABLE))
                return;
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.inflate(R.menu.date_time_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.date_time_menu_change_date) {
                        CalendarFragment.DateState dateState = new CalendarFragment.DateState();
                        dateState.currentDate = noteDrawerInstance.getCreateDate().toLocalDate();
                        dateState.type = CalendarFragment.DateState.TYPE_ALL;
                        CalendarFragment fragment = CalendarFragment.newInstance(
                                dateState, TAG_CALENDAR_CREATE_DATE, null, getContext());
                        fragment.show(getChildFragmentManager(), TAG_CALENDAR);
                        return true;
                    } else if (item.getItemId() == R.id.date_time_menu_change_time) {
                        TimePickerDialog dialog = new TimePickerDialog(
                                getContext(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        DateTime createDate = noteDrawerInstance.getCreateDate()
                                                .withHourOfDay(hourOfDay).withMinuteOfHour(minute);
                                        noteDrawerInstance.setCreateDate(createDate);
                                        onNoteDrawerInstanceChanged(noteDrawerInstance);
                                        tryUpdateViews();
                                    }
                                },
                                noteDrawerInstance.getCreateDate().getHourOfDay(),
                                noteDrawerInstance.getCreateDate().getMinuteOfHour(),
                                DateFormat.is24HourFormat(getContext()));
                        dialog.show();
                        return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    private class OnModifyDateClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!getArguments().getBoolean(KEY_EDITABLE))
                return;
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.inflate(R.menu.date_time_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.date_time_menu_change_date) {
                        CalendarFragment.DateState dateState = new CalendarFragment.DateState();
                        dateState.currentDate = noteDrawerInstance.getModifyDate().toLocalDate();
                        dateState.type = CalendarFragment.DateState.TYPE_ALL;
                        CalendarFragment fragment = CalendarFragment.newInstance(
                                dateState, TAG_CALENDAR_MODIFY_DATE, null, getContext());
                        fragment.show(getChildFragmentManager(), TAG_CALENDAR);
                        return true;
                    } else if (item.getItemId() == R.id.date_time_menu_change_time) {
                        TimePickerDialog dialog = new TimePickerDialog(
                                getContext(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        DateTime createDate = noteDrawerInstance.getModifyDate()
                                                .withHourOfDay(hourOfDay).withMinuteOfHour(minute);
                                        noteDrawerInstance.setModifyDate(createDate);
                                        onNoteDrawerInstanceChanged(noteDrawerInstance);
                                        tryUpdateViews();
                                    }
                                },
                                noteDrawerInstance.getModifyDate().getHourOfDay(),
                                noteDrawerInstance.getModifyDate().getMinuteOfHour(),
                                DateFormat.is24HourFormat(getContext()));
                        dialog.show();
                        return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }


    //========================================= Labels =============================================
    private void addLabel(long labelId) {
        LabelLookupFragment labelLookupFragment = (LabelLookupFragment) getChildFragmentManager()
                .findFragmentByTag(TAG_LABEL_LOOKUP);
        if (labelLookupFragment != null) {
            labelLookupFragment.dismiss();
        }

        SQLiteDatabase database = OpenHelper.getDatabase(getContext());
        if (noteDrawerInstance.note.isRealized()) {
            for (Label label : LabelCatalog.getLabelsByNote(noteDrawerInstance.note.getId(), database))
                if (label.getId() == labelId) return;
            NoteCatalog.setLabelToNote(noteDrawerInstance.note, labelId, database, getContext());
        }
        noteDrawerInstance.addLabelById(labelId, database);
        tryUpdateViews();

        onNoteDrawerInstanceChanged(noteDrawerInstance);
    }

    private void removeLabel(long labelId) {
        SQLiteDatabase database = OpenHelper.getDatabase(getContext());
        if (noteDrawerInstance.note.isRealized()) {
            NoteCatalog.unsetLabelFromNote(noteDrawerInstance.note, labelId, database, getContext());
        }
        noteDrawerInstance.removeLabelById(labelId);
        tryUpdateViews();
        onNoteDrawerInstanceChanged(noteDrawerInstance);
    }

    //===================================== Parent Communication ===================================
    private void onNoteDrawerInstanceChanged(NoteDrawerInstance noteDrawerInstance) {
        getArguments().putBundle(KEY_NOTE_DRAWER_INSTANCE, noteDrawerInstance.toBundle());
        getListener().onNoteDrawerInstanceChanged(noteDrawerInstance);
    }

    private NoteDrawerFragmentInteractions getListener() {
        if (getParentFragment() instanceof NoteDrawerFragmentInteractions) {
            return (NoteDrawerFragmentInteractions) getParentFragment();
        } else if (getActivity() instanceof NoteDrawerFragmentInteractions) {
            return (NoteDrawerFragmentInteractions) getActivity();
        } else
            throw new RuntimeException("No parent implemented the interaction interface");
    }

    public interface NoteDrawerFragmentInteractions {
        void onNoteDrawerInstanceChanged(NoteDrawerInstance noteDrawerInstance);
    }

    public static class NoteDrawerInstance {
        private static final String KEY_CREATE_DATE = "createDate";
        private static final String KEY_MODIFY_DATE = "modifyDate";
        private static final String KEY_LABEL_LIST = "labelList";
        private static final String KEY_NOTE_TYPE_ID = "noteTypeId";
        private static final String KEY_NOTE = "noteNote";

        private DateTime createDate;
        private DateTime modifyDate;
        private ArrayList<Label> labels;
        private Type noteType;
        private Note note;

        private NoteDrawerInstance() {

        }

        public static NoteDrawerInstance newInstanceFromNote(Note note, Context context) {
            ArrayList<Label> labels = LabelCatalog.getLabelsByNote(note.getId(), OpenHelper.getDatabase(context));
            return newInstanceFromNoteAndLabel(note, labels, context);
        }

        public static NoteDrawerInstance newInstanceFromNote(Note note, ArrayList<Long> labelIds, Context context) {
            TreeMap<Long, Label> labelMap = LabelCatalog.getLabelsMap(OpenHelper.getDatabase(context));
            ArrayList<Label> labels = new ArrayList<>(labelIds.size());
            for (long labelId : labelIds) {
                labels.add(labelMap.get(labelId));
            }
            return newInstanceFromNoteAndLabel(note, labels, context);
        }

        private static NoteDrawerInstance newInstanceFromNoteAndLabel(Note note, ArrayList<Label> labels, Context context) {
            NoteDrawerInstance instance = new NoteDrawerInstance();
            Chronology chronology = ChronologyCatalog.getCurrentChronology(context);
            instance.createDate = new DateTime(note.getCreateDate(), chronology);
            instance.modifyDate = new DateTime(note.getModifyDate(), chronology);
            instance.labels = new ArrayList<>(labels);
            instance.noteType = TypeCatalog.getTypeById(note.getTypeId(), OpenHelper.getDatabase(context));
            instance.note = note;
            return instance;
        }

        public DateTime getCreateDate() {
            return createDate;
        }

        public NoteDrawerInstance setCreateDate(DateTime createDate) {
            this.createDate = createDate;
            return this;
        }

        public DateTime getModifyDate() {
            return modifyDate;
        }

        public NoteDrawerInstance setModifyDate(DateTime modifyDate) {
            this.modifyDate = modifyDate;
            return this;
        }

        public ArrayList<Label> getLabels() {
            return labels;
        }

        public NoteDrawerInstance setLabels(ArrayList<Label> labels) {
            this.labels = labels;
            return this;
        }

        public void addLabelById(long labelId, SQLiteDatabase readableDb) {
            for (Label label : labels) {
                if (label.getId() == labelId)
                    return;
            }
            Label label = LabelCatalog.getLabelById(labelId, readableDb);
            labels.add(label);
        }

        public void removeLabelById(long labelId) {
            ArrayList<Label> removeList = new ArrayList<>();
            for (Label label : labels) {
                if (label.getId() == labelId) {
                    removeList.add(label);
                }
            }
            labels.removeAll(removeList);
        }

        public Type getNoteType() {
            return noteType;
        }

        public NoteDrawerInstance setNoteType(Type noteType) {
            this.noteType = noteType;
            return this;
        }

        public Note getNote() {
            return note;
        }

        public NoteDrawerInstance setNote(Note note) {
            this.note = note;
            return this;
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            ArrayList<Long> labelIds = new ArrayList<>(labels.size());
            for (Label label : labels) {
                labelIds.add(label.getId());
            }

            bundle.putLong(KEY_CREATE_DATE, createDate.getMillis());
            bundle.putLong(KEY_MODIFY_DATE, modifyDate.getMillis());
            bundle.putSerializable(KEY_LABEL_LIST, labelIds);
            bundle.putLong(KEY_NOTE_TYPE_ID, noteType.getId());
            bundle.putSerializable(KEY_NOTE, note);
            return bundle;
        }

        public static NoteDrawerInstance newInstanceFromBundle(Bundle bundle, Context context) {
            SQLiteDatabase database = OpenHelper.getDatabase(context);
            NoteDrawerInstance instance = new NoteDrawerInstance();
            Chronology chronology = ChronologyCatalog.getCurrentChronology(context);
            instance.createDate = new DateTime(bundle.getLong(KEY_CREATE_DATE), chronology);
            instance.modifyDate = new DateTime(bundle.getLong(KEY_MODIFY_DATE), chronology);
            ArrayList<Long> labelIds = (ArrayList<Long>) bundle.getSerializable(KEY_LABEL_LIST);
            instance.noteType = TypeCatalog.getTypeById(
                    bundle.getLong(KEY_NOTE_TYPE_ID), database);
            instance.note = (Note) bundle.getSerializable(KEY_NOTE);
            TreeMap<Long, Label> labelMap = LabelCatalog.getLabelsMap(database);
            instance.labels = new ArrayList<>(labelIds.size());
            for (long labelId : labelIds) {
                instance.labels.add(labelMap.get(labelId));
            }
            return instance;
        }
    }

    //=================================== Children Communication ===================================

    @Override
    public void onLabelLookupItemSelected(long labelId) {
        addLabel(labelId);
    }

    //Calendar

    @Override
    public void onDateSelected(CalendarFragment.DateState dateState, String tag, Bundle identifier) {
        if (tag == null) {
        } else if (tag.equals(TAG_CALENDAR_CREATE_DATE)) {
            LocalTime localTime = new LocalTime(noteDrawerInstance.getCreateDate());
            noteDrawerInstance.setCreateDate(dateState.currentDate.toDateTime(localTime));
            onNoteDrawerInstanceChanged(noteDrawerInstance);
            tryUpdateViews();
            CalendarFragment calendarFragment = (CalendarFragment) getChildFragmentManager()
                    .findFragmentByTag(TAG_CALENDAR);
            calendarFragment.dismiss();
        } else if (tag.equals(TAG_CALENDAR_MODIFY_DATE)) {
            LocalTime localTime = new LocalTime(noteDrawerInstance.getModifyDate());
            noteDrawerInstance.setModifyDate(dateState.currentDate.toDateTime(localTime));
            onNoteDrawerInstanceChanged(noteDrawerInstance);
            tryUpdateViews();
            CalendarFragment calendarFragment = (CalendarFragment) getChildFragmentManager()
                    .findFragmentByTag(TAG_CALENDAR);
            calendarFragment.dismiss();
        }
    }
}
