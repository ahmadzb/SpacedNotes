package com.diplinkblaze.spacednote.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ActivityRequestHost;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.NewItemSupportListener;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import data.database.OpenHelper;
import data.model.note.Note;
import data.model.schedule.Occurrence;
import data.model.schedule.Schedule;
import data.model.schedule.ScheduleCatalog;
import data.model.scheduler.Scheduler;
import data.model.type.Type;
import data.preference.ChronologyCatalog;
import data.preference.ContentPreferences;
import util.Colors;
import util.TypeFaceUtils;
import util.datetime.primitive.Representation;

public class NoteListFragment extends Fragment implements NewItemSupportListener, ContentUpdateListener,
        NoteTypeSelectFragment.OnFragmentInteractionListener {

    private static final String TAG_TYPE_SELECT_FRAGMENT = "typeSelectFragment";
    private static final int ACTIVITY_REQUEST_NOTE_EDIT = 0;
    private static final int ACTIVITY_REQUEST_NOTE_VIEW = 1;

    private static final String KEY_NOTE_SELECTOR = "noteSelector";

    private NoteSelector noteSelector;

    private ArrayList<Note> notes;
    private Adapter adapter = new Adapter();

    private OnItemNextClickListener itemNextClickListener = new OnItemNextClickListener();
    private OnItemViewClickListener itemClickListener = new OnItemViewClickListener();

    public NoteListFragment() {
        // Required empty public constructor
    }

    public static NoteListFragment newInstance(NoteSelector selector) {
        NoteListFragment fragment = new NoteListFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_NOTE_SELECTOR, selector);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteDatabase database = OpenHelper.getDatabase(getContext());
        noteSelector = (NoteSelector) getArguments().getSerializable(KEY_NOTE_SELECTOR);
        notes = noteSelector.getNotes(getContext(), database);
        if (savedInstanceState != null) {
            if (adapter != null) {
                adapter.restoreStates(savedInstanceState);
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_note_list, container, false);
        initializeViews(contentView);
        return contentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_list_menu, menu);
        if (ContentPreferences.NoteSideVisibility.isModeFrontVisible(getContext())) {
            menu.removeItem(R.id.note_list_menu_visible_fronts);
        } else if (ContentPreferences.NoteSideVisibility.isModeBackVisible(getContext())) {
            menu.removeItem(R.id.note_list_menu_visible_backs);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initializeViews(View contentView) {
        final RecyclerView recyclerView = contentView.findViewById(R.id.fragment_note_list_recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            adapter.saveStates(outState);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.note_list_menu_visible_fronts) {
            ContentPreferences.NoteSideVisibility.setModeFrontVisible(getContext());
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
            updateContent();
            return true;
        } else if (item.getItemId() == R.id.note_list_menu_visible_backs) {
            ContentPreferences.NoteSideVisibility.setModeBackVisible(getContext());
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
            updateContent();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //================================= Parent Communication =======================================
    @Override
    public void updateContent() {
        if (noteSelector != null) {
            notes = noteSelector.getNotes(getContext(), OpenHelper.getDatabase(getContext()));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void newItem() {
        NoteTypeSelectFragment fragment = NoteTypeSelectFragment.newInstance();
        fragment.show(getChildFragmentManager(), TAG_TYPE_SELECT_FRAGMENT);
    }

    //================================ Children Communication ======================================

    @Override
    public void onTypeSelected(Type type) {
        int request = ACTIVITY_REQUEST_NOTE_EDIT;
        if (getActivity() instanceof ActivityRequestHost) {
            ActivityRequestHost host = (ActivityRequestHost) getActivity();
            request = (request << host.getRequestShift()) | host.getRequestPrefix(this);
        }
        startActivityForResult(NoteEditActivity.getIntentNew(getContext(), type.getId()), request);
    }

    //======================================= Adapter ==============================================
    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Integer normalColor;
        private TreeMap<Long, Schedule> scheduleMap;

        private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

        public Adapter() {
            viewBinderHelper.setOpenOnlyOne(true);
        }

        private int getNormalColor(Context context) {
            if (normalColor == null) {
                normalColor = context.getResources().getColor(R.color.colorText);
            }
            return normalColor;
        }

        private TreeMap<Long, Schedule> getScheduleMap() {
            if (scheduleMap == null) {
                scheduleMap = ScheduleCatalog.getSchedulesMapWithOccurrences(OpenHelper.getDatabase(getContext()));
            }
            return scheduleMap;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View itemView = inflater.inflate(R.layout.partial_notelist_item, parent, false);
            TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), itemView);
            ViewHolder holder = new ViewHolder(itemView);
            holder.backLayout.setOnClickListener(itemClickListener);
            holder.nextRevision.setOnClickListener(itemNextClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Note note = notes.get(position);
            holder.nextRevision.setTag(note);
            holder.backLayout.setTag(note);
            viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(note.getId()));
            TextView frontTitle;
            TextView frontDetails;
            TextView backTitle;
            TextView backDetails;
            if (ContentPreferences.NoteSideVisibility.isModeFrontVisible(getContext())) {
                frontTitle = holder.titleVisible;
                frontDetails = holder.detailsVisible;
                backTitle = holder.titleHidden;
                backDetails = holder.detailsHidden;
            } else {
                backTitle = holder.titleVisible;
                backDetails = holder.detailsVisible;
                frontTitle = holder.titleHidden;
                frontDetails = holder.detailsHidden;
            }

            if (note.getDisplayDetailsFront() == null) {
                frontDetails.setVisibility(View.GONE);
            } else {
                frontDetails.setVisibility(View.VISIBLE);
                frontDetails.setText(note.getDisplayDetailsFront());
            }

            if (note.getDisplayTitleFront() == null) {
                frontTitle.setVisibility(View.GONE);
            } else {
                frontTitle.setVisibility(View.VISIBLE);
                frontTitle.setText(note.getDisplayTitleFront());
            }

            if (note.getDisplayTitleBack() == null) {
                backTitle.setVisibility(View.GONE);
            } else {
                backTitle.setVisibility(View.VISIBLE);
                backTitle.setText(note.getDisplayTitleBack());
            }

            if (note.getDisplayDetailsBack() == null) {
                backDetails.setVisibility(View.GONE);
            } else {
                backDetails.setVisibility(View.VISIBLE);
                backDetails.setText(note.getDisplayDetailsBack());
            }

            if (noteSelector.shouldHighlightNote(note)) {
                holder.titleVisible.setTextColor(Colors.getAccentColor(getContext()));
            } else {
                holder.titleVisible.setTextColor(getNormalColor(getContext()));
            }
            int now = Representation.fromLocalDate(LocalDate.now(ChronologyCatalog.getCurrentChronology(getContext())));
            if (note.getRevisionFuture() == null || note.getRevisionFuture().getDueDate() > now) {
                holder.nextRevision.setVisibility(View.GONE);
            } else {
                Schedule schedule = getScheduleMap().get(note.getRevisionFuture().getScheduleId());
                Occurrence occurrence = schedule.getOccurrenceByNumber(note.getRevisionFuture().getOccurrenceNumber());
                Occurrence occurrenceNext = schedule.getOccurrenceByNumber(note.getRevisionFuture().getOccurrenceNumber() + 1);
                if (occurrence == null) {
                    holder.nextRevision.setVisibility(View.GONE);
                } else {
                    holder.nextRevision.setVisibility(View.VISIBLE);
                    if (occurrenceNext == null) {
                        holder.nextRevisionText.setText(R.string.last_revision);
                    } else {
                        holder.nextRevisionText.setText("+" + occurrenceNext.getPlusDays());
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        public void saveStates(Bundle outState) {
            viewBinderHelper.saveStates(outState);
        }

        public void restoreStates(Bundle inState) {
            viewBinderHelper.restoreStates(inState);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        SwipeRevealLayout swipeRevealLayout;
        TextView titleHidden;
        TextView titleVisible;
        TextView detailsHidden;
        TextView detailsVisible;
        View nextRevision;
        TextView nextRevisionText;
        ViewGroup frontLayout;
        ViewGroup backLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            swipeRevealLayout = itemView.findViewById(R.id.partial_notelist_item_swipe_reveal_layout);
            titleHidden = itemView.findViewById(R.id.partial_notelist_item_title_hidden);
            titleVisible = itemView.findViewById(R.id.partial_notelist_item_title_visible);
            detailsHidden = itemView.findViewById(R.id.partial_notelist_item_details_hidden);
            detailsVisible = itemView.findViewById(R.id.partial_notelist_item_details_visible);
            nextRevision = itemView.findViewById(R.id.partial_notelist_item_details_next_revision);
            nextRevisionText = itemView.findViewById(R.id.partial_notelist_item_details_next_revision_text);
            frontLayout = itemView.findViewById(R.id.partial_notelist_item_front_layout);
            backLayout = itemView.findViewById(R.id.partial_notelist_item_back_layout);
        }
    }

    private class OnItemNextClickListener implements View.OnLongClickListener, View.OnClickListener {
        @Override
        public boolean onLongClick(View v) {
            onClick(v);
            return true;
        }

        @Override
        public void onClick(View v) {
            Note note = (Note) v.getTag();
            SQLiteDatabase database = OpenHelper.getDatabase(getContext());
            Scheduler.submitCurrentOccurrence(note, database, getContext());
            noteSelector.onNoteNextRevisionClicked(note, database);
            adapter.notifyDataSetChanged();
        }
    }

    private class OnItemViewClickListener implements View.OnLongClickListener, View.OnClickListener {
        @Override
        public boolean onLongClick(View v) {
            onClick(v);
            return true;
        }

        @Override
        public void onClick(View v) {
            Note note = (Note) v.getTag();
            Intent intent = NoteViewActivity.getIntent(note.getId(), getContext());
            int request = ACTIVITY_REQUEST_NOTE_VIEW;
            if (getActivity() instanceof ActivityRequestHost) {
                ActivityRequestHost host = (ActivityRequestHost) getActivity();
                request = (request << host.getRequestShift()) | host.getRequestPrefix(NoteListFragment.this);
            }
            startActivityForResult(intent, request);
        }
    }
}


