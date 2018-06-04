package com.diplinkblaze.spacednote.labels;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.google.android.gms.drive.events.ListenerToken;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import data.database.OpenHelper;
import data.model.label.Label;
import data.model.label.LabelCatalog;
import data.model.label.LabelList;
import data.model.label.LabelListCatalog;
import data.preference.ContentPreferences;
import data.sync.SyncFileContract;
import util.Colors;
import util.TypeFaceUtils;

public class LabelLookupFragment extends DialogFragment {

    private static final String KEY_MODE_PREFIX = "modePrefix";
    private static final String KEY_CURRENT_MODE = "currentMode";


    private ModeAllLabels modeAllLabels = new ModeAllLabels();
    private ModeLabelLists modeLabelLists = new ModeLabelLists();

    private Mode mode;
    private String searchKeyword = "";

    public LabelLookupFragment() {
        // Required empty public constructor
    }

    public static LabelLookupFragment newInstance() {
        LabelLookupFragment fragment = new LabelLookupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        Integer modeInt = ContentPreferences.LabelLookup.getMode(getContext());
        if (modeInt == null || modeInt == ContentPreferences.LabelLookup.MODE_ALL_LABELS) {
            mode = modeAllLabels;
        } else if (modeInt == ContentPreferences.LabelLookup.MODE_LABEL_LISTS) {
            mode = modeLabelLists;
        } else {
            throw new RuntimeException("given mode was not recognized");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_label_lookup, container, false);
        initializeViews(contentView, savedInstanceState);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView, Bundle savedInstanceState) {
        mode.initializeViews((RecyclerView) contentView.findViewById(R.id.fragment_label_lookup_recycler_view),
                savedInstanceState, KEY_MODE_PREFIX);
        EditText searchEditText = contentView.findViewById(R.id.fragment_label_lookup_search_edit_text);
        searchEditText.addTextChangedListener(new SearchTextWatcher());
        View labelMode = contentView.findViewById(R.id.fragment_label_lookup_mode_label);
        View labelListMode = contentView.findViewById(R.id.fragment_label_lookup_mode_label_list);
        labelMode.setOnClickListener(new OnLabelModeViewClicked());
        labelListMode.setOnClickListener(new OnLabelListModeViewClicked());
    }

    private void updateViews(View contentView) {
        mode.updateViews((RecyclerView) contentView.findViewById(R.id.fragment_label_lookup_recycler_view));
        ImageView labelMode = contentView.findViewById(R.id.fragment_label_lookup_mode_label);
        ImageView labelListMode = contentView.findViewById(R.id.fragment_label_lookup_mode_label_list);
        if (mode instanceof ModeLabelLists) {
            labelListMode.setAlpha(1.0f);
            labelMode.setAlpha(0.5f);
        } else if (mode instanceof ModeAllLabels) {
            labelListMode.setAlpha(0.5f);
            labelMode.setAlpha(1.0f);
        } else {
            throw new RuntimeException("Mode was not recognized");
        }
    }

    private void onModeChanged() {
        View contentView = getView();
        if (contentView != null) {
            mode.initializeViews((RecyclerView) contentView.findViewById(R.id.fragment_label_lookup_recycler_view),
                    null, KEY_MODE_PREFIX);
            updateViews(contentView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mode.onSaveState(outState, KEY_MODE_PREFIX);
    }

    private abstract class Mode {
        abstract void initializeViews(RecyclerView contentFrame, Bundle savedState, String prefix);

        abstract void updateViews(RecyclerView contentFrame);

        abstract void onSearchKeywordChanged(String searchKeyword);

        abstract void onSaveState(Bundle outState, String prefix);

        protected void tryUpdateViews() {
            View contentView = getView();
            if (contentView != null) {
                this.updateViews((RecyclerView) contentView.findViewById(R.id.fragment_label_lookup_recycler_view));
            }
        }

        protected void onLabelSelected(long labelId) {
            onLabelLookupItemSelected(labelId);
        }
    }

    private class ModeAllLabels extends Mode {
        private ArrayList<Label> allLabels;
        private ArrayList<Label> selectableLabels;
        private Adapter adapter;

        @Override
        void initializeViews(RecyclerView contentFrame, Bundle savedState, String prefix) {
            allLabels = LabelCatalog.getLabels(OpenHelper.getDatabase(getContext()));
            selectableLabels = new ArrayList<>(allLabels);
            adapter = new Adapter(selectableLabels);
            RecyclerView recyclerView = contentFrame.findViewById(R.id.fragment_label_lookup_recycler_view);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        @Override
        void updateViews(RecyclerView contentFrame) {

        }

        @Override
        void onSearchKeywordChanged(String searchKeyword) {
            if (searchKeyword == null || searchKeyword.length() == 0) {
                selectableLabels = new ArrayList<>(allLabels);
            } else {
                selectableLabels = new ArrayList<>(allLabels.size());
                for (Label label : allLabels) {
                    if (label.getTitle() != null && label.getTitle().toLowerCase()
                            .contains(searchKeyword.toLowerCase())) {
                        selectableLabels.add(label);
                    }
                }
            }
            adapter.updateList(selectableLabels);
        }

        @Override
        void onSaveState(Bundle outState, String prefix) {
            //Do nothing
        }

        private class Adapter extends RecyclerView.Adapter<ViewHolder> {
            ArrayList<Label> labels;

            public Adapter(ArrayList<Label> labels) {
                this.labels = labels;
            }

            public void updateList(ArrayList<Label> labels) {
                this.labels = labels;
                notifyDataSetChanged();
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(getContext())
                        .inflate(R.layout.partial_label_lookup_item, parent, false);
                ViewHolder holder = new ViewHolder(itemView);
                return holder;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                Label label = labels.get(position);
                holder.itemView.setTag(label);
                holder.title.setText(label.getTitle());
            }

            @Override
            public int getItemCount() {
                return labels.size();
            }
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;
            TextView details;

            public ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.partial_label_lookup_item_icon);
                title = itemView.findViewById(R.id.partial_label_lookup_item_title);
                details = itemView.findViewById(R.id.partial_label_lookup_item_details);

                details.setVisibility(View.GONE);
                icon.setAlpha(0.5f);
                itemView.setOnClickListener(new OnLabelViewClicked());
            }
        }

        private class OnLabelViewClicked implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Label label = (Label) v.getTag();
                onLabelSelected(label.getId());
            }
        }
    }

    private class ModeLabelLists extends Mode {
        private final String KEY_EXPANDED_LABEL_LIST_IDS = "expandedLabelListIds";

        TreeMap<Long, LabelList> labelListMap;
        ArrayList<TreeList> treeLists;
        Adapter adapter;

        @Override
        void initializeViews(RecyclerView contentFrame, Bundle savedState, String prefix) {
            SQLiteDatabase database = OpenHelper.getDatabase(getContext());
            labelListMap = LabelListCatalog.getLabelListsMapWithLabels(database);
            treeLists = generateTreeLists(labelListMap, null);
            if (savedState != null) {
                onRestoreState(savedState, prefix);
            }
            adapter = new Adapter(getSelectableListItems(treeLists));
            RecyclerView recyclerView = contentFrame.findViewById(R.id.fragment_label_lookup_recycler_view);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        @Override
        void updateViews(RecyclerView contentFrame) {

        }

        @Override
        void onSearchKeywordChanged(String searchKeyword) {
            TreeSet<Long> expandedLabelListIds = new TreeSet<>();
            for (TreeList treeList : getAllTreeLists()) {
                if (treeList.isExpanded())
                    expandedLabelListIds.add(treeList.labelList.getId());
            }
            treeLists = generateTreeLists(labelListMap, searchKeyword);
            for (TreeList treeList : getAllTreeLists()) {
                if (expandedLabelListIds.contains(treeList.labelList.getId())) {
                    treeList.expand();
                }
            }
            adapter.updateList(getSelectableListItems(treeLists));
        }

        @Override
        void onSaveState(Bundle outState, String prefix) {
            ArrayList<TreeList> allTreeLists = getAllTreeLists();
            ArrayList<Long> expandedLabelListIds = new ArrayList<>();
            for (TreeList treeList : allTreeLists) {
                if (treeList.isExpanded()) {
                    expandedLabelListIds.add(treeList.labelList.getId());
                }
            }
            outState.putSerializable(prefix + KEY_EXPANDED_LABEL_LIST_IDS, expandedLabelListIds);
        }

        void onRestoreState(Bundle savedState, String prefix) {
            ArrayList<TreeList> allTreeLists = getAllTreeLists();
            TreeSet<Long> expandedLabelListIds = new TreeSet<>(
                    (ArrayList<Long>) savedState.getSerializable(prefix + KEY_EXPANDED_LABEL_LIST_IDS));
            for (TreeList treeList : allTreeLists) {
                if (expandedLabelListIds.contains(treeList.labelList.getId())) {
                    treeList.expand();
                }
            }
        }

        private ArrayList<TreeList> getAllTreeLists() {
            ArrayList<TreeList> allTreeLists = new ArrayList<>();
            for (TreeList treeList : treeLists) {
                allTreeLists.addAll(getAllTreeListsCascade(treeList));
            }
            return allTreeLists;
        }

        private ArrayList<TreeList> getAllTreeListsCascade(TreeList treeList) {
            ArrayList<TreeList> treeLists = new ArrayList<>();
            treeLists.add(treeList);
            for (TreeList t : treeList.treeLists) {
                treeLists.addAll(getAllTreeListsCascade(t));
            }
            return treeLists;
        }

        private ArrayList<ListItem> getSelectableListItems(ArrayList<TreeList> treeLists) {
            ArrayList<ListItem> listItems = new ArrayList<>();
            for (TreeList treeList : treeLists) {
                listItems.add(treeList);
                if (treeList.isExpanded()) {
                    listItems.addAll(treeList.flattenExpandedItems());
                }
            }
            return listItems;
        }

        private class Adapter extends RecyclerView.Adapter<ViewHolder> {
            ArrayList<ListItem> listItems;

            public Adapter(ArrayList<ListItem> listItems) {
                this.listItems = listItems;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(getContext())
                        .inflate(R.layout.partial_label_lookup_item, parent, false);
                ViewHolder holder = new ViewHolder(itemView);
                return holder;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                ListItem listItem = listItems.get(position);
                holder.itemView.setTag(listItem);
                if (listItem instanceof TreeList) {
                    TreeList treeList = (TreeList) listItem;
                    LabelList labelList = treeList.labelList;
                    holder.title.setText(labelList.getTitle());
                    holder.title.setTextColor(labelList.getColor());
                    holder.details.setText(TypeFaceUtils.withNumberFormat(treeList.labels.size()));
                    holder.details.setTextColor(labelList.getColor());
                    holder.icon.setVisibility(View.GONE);
                } else if (listItem instanceof LabelItem) {
                    LabelItem labelItem = (LabelItem) listItem;
                    Label label = labelItem.label;
                    LabelList parentList = ((TreeList) labelItem.parent).labelList;
                    holder.title.setText(label.getTitle());
                    holder.title.setTextColor(parentList.getColor());
                    holder.details.setText("");
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.icon.setColorFilter(parentList.getColor());
                } else {
                    throw new RuntimeException("List item type was not recognized");
                }
            }

            @Override
            public int getItemCount() {
                return listItems.size();
            }

            private void updateList(ArrayList<ListItem> listItems) {
                this.listItems = listItems;
                notifyDataSetChanged();
            }
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;
            TextView details;

            public ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.partial_label_lookup_item_icon);
                title = itemView.findViewById(R.id.partial_label_lookup_item_title);
                details = itemView.findViewById(R.id.partial_label_lookup_item_details);

                //icon.setAlpha(0.5f);
                itemView.setOnClickListener(new OnLabelViewClicked());
            }
        }

        private class OnLabelViewClicked implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                ListItem listItem = (ListItem) v.getTag();
                if (listItem instanceof TreeList) {
                    TreeList treeList = (TreeList) listItem;
                    if (treeList.isExpanded())
                        treeList.collapse();
                    else {
                        for (TreeList t : treeLists)
                            t.collapse();
                        treeList.expand();
                        ListItem parent = treeList.parent;
                        while (parent != null) {
                            if (parent instanceof TreeList) {
                                TreeList parentTreeList = (TreeList) parent;
                                parentTreeList.expand();
                                parent = parentTreeList.parent;
                            }
                        }
                    }
                    adapter.updateList(getSelectableListItems(treeLists));
                } else if (listItem instanceof LabelItem) {
                    Label label = ((LabelItem) listItem).label;
                    onLabelSelected(label.getId());
                }
            }
        }

        //==========================================
        private ArrayList<TreeList> generateTreeLists(TreeMap<Long, LabelList> labelListMap,
                                                      String searchKeyword) {
            ArrayList<LabelList> rootLists = new ArrayList<>();
            for (LabelList list : labelListMap.values()) {
                if (list.getParentId() == null) {
                    rootLists.add(list);
                }
            }
            ArrayList<TreeList> treeLists = new ArrayList<>(rootLists.size());
            for (LabelList list : rootLists) {
                TreeList treeList = generateTreeListCascade(list, labelListMap, searchKeyword);
                if (treeList != null) {
                    treeLists.add(treeList);
                }
            }
            return treeLists;
        }

        private TreeList generateTreeListCascade(LabelList labelList, TreeMap<Long, LabelList> map,
                                                 String searchKeyword) {
            TreeList treeList = new TreeList();
            treeList.labelList = labelList;
            treeList.treeLists = new ArrayList<>();
            for (LabelList list : map.values()) {
                if (list.getParentId() != null && list.getParentId() == labelList.getId()) {
                    TreeList child = generateTreeListCascade(list, map, searchKeyword);
                    if (child != null) {
                        treeList.treeLists.add(child);
                        child.parent = treeList;
                    }
                }
            }
            if (labelList.getLabels() == null) {
                treeList.labels = new ArrayList<>();
            } else {
                treeList.labels = new ArrayList<>(labelList.getLabels().size());
                for (Label label : labelList.getLabels()) {
                    if (searchKeyword == null ||
                            label.getTitle() != null && label.getTitle().toLowerCase().contains(searchKeyword)) {
                        LabelItem labelItem = new LabelItem();
                        labelItem.label = label;
                        treeList.labels.add(labelItem);
                        labelItem.parent = treeList;
                    }
                }
            }
            if (treeList.labels.size() != 0 || treeList.treeLists.size() != 0)
                return treeList;
            else
                return null;
        }

        private abstract class ListItem {
            ListItem parent;
        }

        private class TreeList extends ListItem {
            private LabelList labelList;
            private ArrayList<TreeList> treeLists;
            private ArrayList<LabelItem> labels;
            private boolean isExpanded;

            private ArrayList<ListItem> flattenExpandedItems() {
                ArrayList<ListItem> listItems = new ArrayList<>();
                listItems.addAll(labels);
                for (TreeList treeList : treeLists) {
                    listItems.add(treeList);
                    if (treeList.isExpanded()) {
                        listItems.addAll(treeList.flattenExpandedItems());
                    }
                }
                return listItems;
            }

            private void expand() {
                isExpanded = true;
            }

            private void collapse() {
                isExpanded = false;
                for (TreeList treeList : treeLists) {
                    treeList.isExpanded = false;
                }
            }

            public boolean isExpanded() {
                return isExpanded;
            }
        }

        private class LabelItem extends ListItem {
            private Label label;
        }
    }

    private class SearchTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String newSearchKeyword = s.toString();
            if (!newSearchKeyword.equals(searchKeyword)) {
                mode.onSearchKeywordChanged(newSearchKeyword);
                searchKeyword = newSearchKeyword;
            }
        }
    }

    private class OnLabelModeViewClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mode = modeAllLabels;
            ContentPreferences.LabelLookup.setMode(
                    ContentPreferences.LabelLookup.MODE_ALL_LABELS, getContext());
            onModeChanged();
        }
    }

    private class OnLabelListModeViewClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mode = modeLabelLists;
            ContentPreferences.LabelLookup.setMode(
                    ContentPreferences.LabelLookup.MODE_LABEL_LISTS, getContext());
            onModeChanged();
        }
    }

    //================================== Parent Communication ======================================
    private void onLabelLookupItemSelected(long labelId) {
        getListener().onLabelLookupItemSelected(labelId);
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener) {
            return (OnFragmentInteractionListener) getParentFragment();
        } else if (getActivity() instanceof OnFragmentInteractionListener) {
            return (OnFragmentInteractionListener) getActivity();
        } else
            throw new RuntimeException("Parent fragment or activity should implement " +
                    "OnFragmentInteractionListener");
    }

    public interface OnFragmentInteractionListener {
        void onLabelLookupItemSelected(long labelId);
    }
}
