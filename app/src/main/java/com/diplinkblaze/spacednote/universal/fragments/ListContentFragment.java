package com.diplinkblaze.spacednote.universal.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.draglistview.DragListView;
import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.universal.util.ListData;

import java.util.ArrayList;

import util.Colors;
import util.Flags;
import util.TypeFaceUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListContentFragment extends Fragment implements ContentUpdateListener {
    private static final String KEY_LIST_DATA_PREFIX = "listData_";
    private static final String KEY_IDENTIFIER = "identifier";
    private static final String KEY_ROOT_ENTITY = "rootEntity";

    boolean isLayoutLoaded = false;
    Bundle savedInstanceState;
    private ListData listData;

    private ListData.Entity rootEntity;
    private Adapters.UniversalAdapter adapter;

    public ListContentFragment() {
        // Required empty public constructor
    }

    public static ListContentFragment newInstance(Bundle identifier, long rootEntity) {

        ListContentFragment fragment = new ListContentFragment();
        Bundle args = new Bundle();
        args.putBundle(KEY_IDENTIFIER, identifier);
        args.putLong(KEY_ROOT_ENTITY, rootEntity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLayoutLoaded = false;
        this.savedInstanceState = savedInstanceState;
        if (listData == null) {
            retrieveUniversalListData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isLayoutLoaded = false;
        View contentView = inflater.inflate(R.layout.fragment_list_content, container, false);
        loadLayout(inflater, contentView);
        return contentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isLayoutLoaded = false;
    }

    private void tryLoadLayout() {
        Context context = getContext();
        if (context == null)
            return;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = getView();
        if (layoutInflater != null && view != null)
            loadLayout(layoutInflater, view);
    }

    private void loadLayout(LayoutInflater inflater, View contentView) {
        if (listData != null && !isLayoutLoaded) {
            rootEntity = listData.findItemByEntityId(getArguments().getLong(KEY_ROOT_ENTITY));
            initializeViews(contentView);
            updateViews(inflater, contentView);
            isLayoutLoaded = true;
        }
    }

    private void initializeViews(View contentView) {
        RecyclerView recyclerView = null;
        DragListView dragListView = null;
        if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_SWAPPABLE)) {
            if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_MARKABLE) &&
                    listData.getMarkMode() == ListData.MARK_ON_FIRST)
                throw new RuntimeException("cannot have on hold marks on a swappable list");

            boolean showNext = Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_TREE);
            ListData.Entity items = rootEntity;
            Adapters.DragListAdapter adapter = new Adapters.DragListAdapter(this, items, showNext);
            this.adapter = adapter;

            ViewStub dragListStub = contentView.findViewById(R.id.fragment_universal_list_content_drag_list_stub);
            dragListStub.inflate();
            dragListView = contentView.findViewById(R.id.partial_list_drag_list);
            dragListView.setLayoutManager(new LinearLayoutManager(getContext()));
            dragListView.setAdapter(adapter, true);
            dragListView.setCanDragHorizontally(false);
            dragListView.setDragListListener(new Adapters.DragListAdapter.DragListener(this, items));
        } else if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_HEADERS)) {
            if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_TREE))
                throw new RuntimeException("Tree with headers is not supported yet");

            Adapters.HeadersRecyclerAdapter adapter = new Adapters.HeadersRecyclerAdapter(this, rootEntity, false);
            this.adapter = adapter;

            ViewStub headerListStub = contentView.findViewById(R.id.fragment_universal_list_content_header_list_stub);
            headerListStub.inflate();
            recyclerView = contentView.findViewById(R.id.partial_list_header_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        } else {
            boolean showNext = Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_TREE);
            Adapters.MyRecyclerAdapter adapter = new Adapters.MyRecyclerAdapter(this, rootEntity, showNext);
            this.adapter = adapter;

            ViewStub simpleListStub = contentView.findViewById(R.id.fragment_universal_list_content_simple_list_stub);
            simpleListStub.inflate();
            recyclerView = contentView.findViewById(R.id.partial_list_simple_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }

        ListFragment.ListViewInfo viewInfo = new ListFragment.ListViewInfo();
        viewInfo.setDragListView(dragListView);
        viewInfo.setRecyclerView(recyclerView);

        onUniversalListDeliverViewInfo(viewInfo);
    }

    private void tryUpdateViews() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View contentView = getView();
        if (inflater != null && contentView != null) {
            updateViews(inflater, contentView);
        }
    }

    private void updateViews(LayoutInflater inflater, View contentView) {

        //Empty Image
        {
            ImageView emptyLogo = contentView.findViewById(R.id.fragment_list_content_empty_logo);
            if (listData == null || listData.getCount() == 0) {
                emptyLogo.setVisibility(View.VISIBLE);
            } else {
                emptyLogo.setVisibility(View.GONE);
            }
        }

    }

    private void updateList() {
        adapter.updateItems(rootEntity);
    }

    //================================== Communication Parent =======================================

    @Override
    public void updateContent() {
        boolean previousLoadState = isLayoutLoaded;
        retrieveUniversalListData();
        tryLoadLayout();
        if (previousLoadState) {
            rootEntity = listData.findItemByEntityId(rootEntity.getEntityId());
            adapter.updateItems(rootEntity);
            tryUpdateViews();
        }
    }

    private void retrieveUniversalListData() {
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        OnFragmentInteractionListener listener = getListener();
        if (listener != null) {
            listData = listener.retrieveUniversalListData(getContext(), identifier);
        }
    }

    private void onUniversalListPositionsChanged() {
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListPositionsChanged(listData, identifier);
    }

    private void onUniversalListItemSelected(ListData.Entity entity) {
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListItemSelected(entity, identifier);
    }

    private void onUniversalListNextSelected(ListData.Entity entity) {
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListNextSelected(entity, identifier);
    }

    private void onUniversalListActionClicked(ListData.InfoRow infoRow, View v) {
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListActionClicked(infoRow, v, identifier);
    }

    private void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo) {
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListDeliverViewInfo(viewInfo, identifier);
    }

    private void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView) {
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListItemMoreSelected(entity, moreView, identifier);
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            return ((OnFragmentInteractionListener) getParentFragment());
        else if (getActivity() instanceof OnFragmentInteractionListener)
            return ((OnFragmentInteractionListener) getActivity());
        else
            return null;
    }

    private void onUniversalListMarkedItemsChanged() {
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        if (getParentFragment() instanceof OnFragmentMarkActionListener)
            ((OnFragmentMarkActionListener) getParentFragment()).onUniversalListMarkedItemsChanged(identifier);
        else if (getActivity() instanceof OnFragmentMarkActionListener)
            ((OnFragmentMarkActionListener) getActivity()).onUniversalListMarkedItemsChanged(identifier);
        else
            throw new RuntimeException("Either parent fragment or activity should implement" +
                    " OnFragmentMarkActionListener");
    }

    public interface OnFragmentInteractionListener {
        ListData retrieveUniversalListData(Context context, Bundle identifier);

        void onUniversalListPositionsChanged(ListData data, Bundle identifier);

        void onUniversalListItemSelected(ListData.Entity entity, Bundle identifier);

        void onUniversalListNextSelected(ListData.Entity entity, Bundle identifier);

        void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, Bundle identifier);

        void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo, Bundle identifier);

        void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView, Bundle identifier);
    }

    public interface OnFragmentMarkActionListener {
        void onUniversalListMarkedItemsChanged(Bundle identifier);
    }

    //================================== Communication Adapters =====================================
    private void onItemClicked(@NonNull ListData.Entity entity) {
        if (entity.isValid()) {
            if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_MARKABLE) &&
                    (listData.getMarkMode() == ListData.MARK_ALWAYS ||
                            listData.getMarkMode() == ListData.MARK_ALWAYS_CHOOSER)) {
                onMarkClicked(entity);
            } else {
                onUniversalListItemSelected(entity);
            }
        } else
            onNextClicked(entity);
    }

    private void onNextClicked(@NonNull ListData.Entity entity) {
        onUniversalListNextSelected(entity);
    }

    private void onMarkClicked(@NonNull ListData.Entity entity) {
        if (listData.isItemMarked(entity))
            listData.removeMarkedItem(entity);
        else
            listData.markItem(entity);
        tryUpdateViews();
        updateList();
        onUniversalListMarkedItemsChanged();
    }

    private void onIconClicked(@NonNull ListData.Entity entity) {
        if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_MARKABLE)) {
            onMarkClicked(entity);
        } else {
            onItemClicked(entity);
        }
    }

    private void onMoreClicked(@NonNull ListData.Entity entity, View moreView) {
        onUniversalListItemMoreSelected(entity, moreView);
    }

    private boolean onItemLongClicked(@NonNull ListData.Entity entity) {
        if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_MARKABLE)) {
            onMarkClicked(entity);
            return true;
        }
        return false;
    }

    private void onPositionsChanged() {
        onUniversalListPositionsChanged();
    }

    //===================================== Classes: Adapters =======================================
    private static class Adapters {

        private static final int FLAG_SHOW_NEXT = 0b1;
        private static final int FLAG_SHOW_MORE = 0b10;
        private static final int FLAG_HAS_HEADER = 0b100;
        private static final int FLAG_ASSIGN_CLICK_LISTENER = 0b1000;
        private static final int FLAG_ASSIGN_HOLD_LISTENER = 0b10000;
        private static final int FLAG_MARKABLE = 0b100000;
        private static final int FLAG_MARKED_MODE = 0b1000000;

        //Adapters:
        public static class DragListAdapter extends DragItemAdapter<ListData.Entity, DragListAdapter.ViewHolder> implements UniversalAdapter {
            ListContentFragment fragment;
            ListData.Entity items;
            boolean showNext;

            public DragListAdapter(ListContentFragment fragment, ListData.Entity items, boolean showNext) {
                this.fragment = fragment;
                this.items = items;
                this.showNext = showNext;

                setHasStableIds(true);
                setItemList(items == null? new ArrayList<ListData.Entity>() : items.getChildren());
            }

            @Override
            public long getItemId(int position) {
                return items.getChildAt(position).getEntityId();
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.list_item_universal, parent, false);
                ViewHolder holder = new ViewHolder(view, R.id.list_item_universal_root, true);
                Adapters.prepareViewHolder(fragment, holder, view, items, 0);
                return holder;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                int flags = 0;
                if (showNext) {
                    flags = flags | FLAG_SHOW_NEXT;
                }
                if (fragment.listData.isMarkMode()) {
                    flags = flags | FLAG_MARKABLE;
                }
                if (fragment.listData.isItemMarked(items.getChildAt(position))) {
                    flags = flags | FLAG_MARKED_MODE;
                }
                if (Flags.hasFlags(fragment.listData.getModeFlags(), ListData.MODE_FLAG_MORE_BUTTON)) {
                    flags = flags | FLAG_SHOW_MORE;
                }
                Adapters.bindViewHolder(fragment.getContext(), holder, items, position, flags);
            }

            @Override
            public void updateItems(ListData.Entity items) {
                this.items = items;
                setItemList(items == null? new ArrayList<ListData.Entity>() : items.getChildren());
            }

            public static class ViewHolder extends DragItemAdapter.ViewHolder implements UniversalListViewHolder {
                public View mView;
                public View mViewItem;
                public TextView mDetails;
                public TextView mTitle;
                public TextView mValue;
                public TextView mFooter;
                public TextView mFooter2;
                public TextView mExtra1;
                public TextView mExtra2;
                public TextView mIcon;
                public View mDivider;
                public View mNext;
                public View mMore;
                public OnViewClickListener listener;

                public ViewHolder(View itemView, int handleResId, boolean dragOnLongPress) {
                    super(itemView, handleResId, dragOnLongPress);
                }

                @Override
                public void setDetails(TextView details) {
                    this.mDetails = details;
                }

                @Override
                public void setTitle(TextView title) {
                    this.mTitle = title;
                }

                @Override
                public void setValue(TextView value) {
                    this.mValue = value;
                }

                @Override
                public void setIcon(TextView icon) {
                    this.mIcon = icon;
                }

                @Override
                public void setFooter(TextView footer) {
                    this.mFooter = footer;
                }

                @Override
                public void setFooter2(TextView footer2) {
                    this.mFooter2 = footer2;
                }

                @Override
                public void setExtra1(TextView extra1) {
                    this.mExtra1 = extra1;
                }

                @Override
                public void setExtra2(TextView extra2) {
                    this.mExtra2 = extra2;
                }

                @Override
                public void setDivider(View divider) {
                    this.mDivider = divider;
                }

                @Override
                public void setNext(View next) {
                    this.mNext = next;
                }

                @Override
                public void setMore(View more) {
                    this.mMore = more;
                }

                @Override
                public void setView(View view) {
                    this.mView = view;
                }

                @Override
                public void setViewItem(View view) {
                    this.mViewItem = view;
                }

                @Override
                public void setListener(OnViewClickListener listener) {
                    this.listener = listener;
                }

                @Override
                public TextView getDetails() {
                    return mDetails;
                }

                @Override
                public TextView getTitle() {
                    return mTitle;
                }

                @Override
                public TextView getValue() {
                    return mValue;
                }

                @Override
                public TextView getIcon() {
                    return mIcon;
                }

                @Override
                public TextView getFooter() {
                    return mFooter;
                }

                @Override
                public TextView getFooter2() {
                    return mFooter2;
                }

                @Override
                public TextView getExtra1() {
                    return mExtra1;
                }

                @Override
                public TextView getExtra2() {
                    return mExtra2;
                }

                @Override
                public View getDivider() {
                    return mDivider;
                }

                @Override
                public View getMore() {
                    return mMore;
                }

                @Override
                public View getNext() {
                    return mNext;
                }

                @Override
                public View getView() {
                    return mView;
                }

                @Override
                public View getViewItem() {
                    return mViewItem;
                }

                @Override
                public OnViewClickListener getListener() {
                    return listener;
                }

                @Override
                public void onItemClicked(View view) {
                    super.onItemClicked(view);
                    if (listener == null)
                        throw new RuntimeException("Listener is not set by prepareViewHolder()");
                    listener.onClick(view);
                }

                @Override
                public boolean getSupportHeader() {
                    return false;
                }

                //====================================== Not Supported =====================================

                @Override
                public void setHeader(View header) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public void setHeaderTitle(TextView title) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public void setHeaderValue(TextView value) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public void setHeaderValue2(TextView value) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public View getHeader() {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public TextView getHeaderTitle() {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public TextView getHeaderValue() {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public TextView getHeaderValue2() {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public void setLongClickListener(OnViewLongClickListener listener) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public OnViewLongClickListener getLongClickListener() {
                    throw new RuntimeException("Not supported");
                }
            }

            public static class DragListener implements DragListView.DragListListener {
                ListContentFragment fragment;

                public DragListener(ListContentFragment fragment, ListData.Entity items) {
                    this.fragment = fragment;
                }

                @Override
                public void onItemDragStarted(int position) {

                }

                @Override
                public void onItemDragging(int itemPosition, float x, float y) {

                }

                @Override
                public void onItemDragEnded(int fromPosition, int toPosition) {
                    if (fromPosition != toPosition)
                        fragment.onPositionsChanged();
                }
            }
        }

        public static class HeadersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements UniversalAdapter {
            private static final int VIEW_TYPE_ITEM = 0;
            private static final int VIEW_TYPE_ACTION = 1;

            ListContentFragment fragment;
            ListData.Entity items;
            boolean showNext;

            public HeadersRecyclerAdapter(ListContentFragment fragment, ListData.Entity items, boolean showNext) {
                this.fragment = fragment;
                this.items = items;
                this.showNext = showNext;
            }

            @Override
            public int getItemViewType(int position) {
                if (position == 0)
                    return VIEW_TYPE_ACTION;
                else
                    return VIEW_TYPE_ITEM;
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if (viewType == VIEW_TYPE_ACTION) {
                    return makeInfoViewHolder(fragment, parent);
                } else {
                    View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.list_item_universal, parent, false);
                    ViewHolder holder = new ViewHolder(view);
                    int flags = FLAG_HAS_HEADER | FLAG_ASSIGN_CLICK_LISTENER;
                    if (Flags.hasFlags(fragment.listData.getModeFlags(), ListData.MODE_FLAG_MARKABLE) &&
                            fragment.listData.getMarkMode() == ListData.MARK_ON_FIRST) {
                        flags = flags | FLAG_ASSIGN_HOLD_LISTENER;
                    }
                    Adapters.prepareViewHolder(fragment, holder, view, items, flags);
                    return holder;
                }
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (position == 0) {
                    updateInfoViewHolder(fragment, (InfoViewHolder) holder);
                } else {
                    position = getItemPosition(position);
                    int flags = 0;
                    if (showNext) {
                        flags = flags | FLAG_SHOW_NEXT;
                    }
                    if (hasHeader(position)) {
                        flags = flags | FLAG_HAS_HEADER;
                    }
                    if (fragment.listData.isMarkMode()) {
                        flags = flags | FLAG_MARKABLE;
                    }
                    if (fragment.listData.isItemMarked(items.getChildAt(position))) {
                        flags = flags | FLAG_MARKED_MODE;
                    }
                    if (Flags.hasFlags(fragment.listData.getModeFlags(), ListData.MODE_FLAG_MORE_BUTTON)) {
                        flags = flags | FLAG_SHOW_MORE;
                    }
                    Adapters.bindViewHolder(fragment.getContext(), (ViewHolder) holder, items, position, flags);
                }
            }

            @Override
            public int getItemCount() {
                return items == null? 0 : items.getChildrenCountImmediate() + 1;
            }

            @Override
            public void updateItems(ListData.Entity items) {
                this.items = items;
                notifyDataSetChanged();
            }

            public boolean hasHeader(int itemPosition) {
                if (itemPosition == 0)
                    return true;
                else
                    return items.getChildAt(itemPosition).getHeaderId() != items.getChildAt(itemPosition - 1).getHeaderId();
            }

            private static int getItemPosition(int position) {
                return position - 1;
            }

            public static class ViewHolder extends RecyclerView.ViewHolder implements UniversalListViewHolder {
                public View mView;
                public View mViewItem;
                public TextView mDetails;
                public TextView mTitle;
                public TextView mValue;
                public TextView mFooter;
                public TextView mFooter2;
                public TextView mExtra1;
                public TextView mExtra2;
                public TextView mIcon;
                public View mDivider;
                public View mMore;
                public View mNext;

                public View mHeader;
                public TextView mHeaderTitle;
                public TextView mHeaderValue;
                public TextView mHeaderValue2;

                public OnViewClickListener listener;
                public OnViewLongClickListener longListener;

                public ViewHolder(View itemView) {
                    super(itemView);
                }

                @Override
                public void setDetails(TextView details) {
                    this.mDetails = details;
                }

                @Override
                public void setTitle(TextView title) {
                    this.mTitle = title;
                }

                @Override
                public void setValue(TextView value) {
                    this.mValue = value;
                }

                @Override
                public void setIcon(TextView icon) {
                    this.mIcon = icon;
                }

                @Override
                public void setFooter(TextView footer) {
                    this.mFooter = footer;
                }

                @Override
                public void setFooter2(TextView footer2) {
                    this.mFooter2 = footer2;
                }

                @Override
                public void setExtra1(TextView extra1) {
                    this.mExtra1 = extra1;
                }

                @Override
                public void setExtra2(TextView extra2) {
                    this.mExtra2 = extra2;
                }

                @Override
                public void setDivider(View divider) {
                    this.mDivider = divider;
                }

                @Override
                public void setMore(View more) {
                    this.mMore = more;
                }

                @Override
                public void setNext(View next) {
                    this.mNext = next;
                }

                @Override
                public void setView(View view) {
                    this.mView = view;
                }

                @Override
                public void setViewItem(View view) {
                    this.mViewItem = view;
                }

                @Override
                public void setListener(OnViewClickListener listener) {
                    this.listener = listener;
                }

                @Override
                public void setLongClickListener(OnViewLongClickListener listener) {
                    this.longListener = listener;
                }

                @Override
                public TextView getDetails() {
                    return mDetails;
                }

                @Override
                public TextView getTitle() {
                    return mTitle;
                }

                @Override
                public TextView getValue() {
                    return mValue;
                }

                @Override
                public TextView getIcon() {
                    return mIcon;
                }

                @Override
                public TextView getFooter() {
                    return mFooter;
                }

                @Override
                public TextView getFooter2() {
                    return mFooter2;
                }

                @Override
                public TextView getExtra1() {
                    return mExtra1;
                }

                @Override
                public TextView getExtra2() {
                    return mExtra2;
                }

                @Override
                public View getDivider() {
                    return mDivider;
                }

                @Override
                public View getMore() {
                    return mMore;
                }

                @Override
                public View getNext() {
                    return mNext;
                }

                @Override
                public View getView() {
                    return mView;
                }

                @Override
                public View getViewItem() {
                    return mViewItem;
                }

                @Override
                public OnViewClickListener getListener() {
                    return listener;
                }

                @Override
                public OnViewLongClickListener getLongClickListener() {
                    return longListener;
                }

                //================================== Header ===================================

                @Override
                public boolean getSupportHeader() {
                    return true;
                }

                @Override
                public void setHeader(View header) {
                    mHeader = header;
                }

                @Override
                public void setHeaderTitle(TextView title) {
                    mHeaderTitle = title;
                }

                @Override
                public void setHeaderValue(TextView value) {
                    mHeaderValue = value;
                }

                @Override
                public void setHeaderValue2(TextView value) {
                    mHeaderValue2 = value;
                }

                @Override
                public View getHeader() {
                    return mHeader;
                }

                @Override
                public TextView getHeaderTitle() {
                    return mHeaderTitle;
                }

                @Override
                public TextView getHeaderValue() {
                    return mHeaderValue;
                }

                @Override
                public TextView getHeaderValue2() {
                    return mHeaderValue2;
                }
            }
        }

        public static class MyRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements UniversalAdapter {
            private static final int VIEW_TYPE_ITEM = 0;
            private static final int VIEW_TYPE_ACTION = 1;

            ListContentFragment fragment;
            ListData.Entity items;
            boolean showNext;

            public MyRecyclerAdapter(ListContentFragment fragment, ListData.Entity items, boolean showNext) {
                this.fragment = fragment;
                this.items = items;
                this.showNext = showNext;
            }


            @Override
            public int getItemViewType(int position) {
                if (position == 0)
                    return VIEW_TYPE_ACTION;
                else
                    return VIEW_TYPE_ITEM;
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if (viewType == VIEW_TYPE_ACTION) {
                    return makeInfoViewHolder(fragment, parent);
                } else {
                    View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.list_item_universal, parent, false);
                    ViewHolder holder = new ViewHolder(view);
                    int flags = FLAG_ASSIGN_CLICK_LISTENER;
                    if (Flags.hasFlags(fragment.listData.getModeFlags(), ListData.MODE_FLAG_MARKABLE) &&
                            fragment.listData.getMarkMode() == ListData.MARK_ON_FIRST) {
                        flags = flags | FLAG_ASSIGN_HOLD_LISTENER;
                    }
                    Adapters.prepareViewHolder(fragment, holder, view, items, flags);
                    return holder;
                }
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (position == 0) {
                    updateInfoViewHolder(fragment, (InfoViewHolder) holder);
                } else {
                    position = getItemPosition(position);
                    int flags = 0;
                    if (showNext) {
                        flags = flags | FLAG_SHOW_NEXT;
                    }
                    if (fragment.listData.isMarkMode()) {
                        flags = flags | FLAG_MARKABLE;
                    }
                    if (fragment.listData.isItemMarked(items.getChildAt(position))) {
                        flags = flags | FLAG_MARKED_MODE;
                    }
                    if (Flags.hasFlags(fragment.listData.getModeFlags(), ListData.MODE_FLAG_MORE_BUTTON)) {
                        flags = flags | FLAG_SHOW_MORE;
                    }
                    Adapters.bindViewHolder(fragment.getContext(), (ViewHolder) holder, items, position, flags);
                }
            }

            @Override
            public int getItemCount() {
                return items == null? 0 : items.getChildrenCountImmediate() + 1;
            }

            @Override
            public void updateItems(ListData.Entity items) {
                this.items = items;
                notifyDataSetChanged();
            }

            private static int getItemPosition(int position) {
                return position - 1;
            }

            public static class ViewHolder extends RecyclerView.ViewHolder implements UniversalListViewHolder {
                public View mView;
                public View mViewItem;
                public TextView mDetails;
                public TextView mTitle;
                public TextView mValue;
                public TextView mFooter;
                public TextView mFooter2;
                public TextView mExtra1;
                public TextView mExtra2;
                public TextView mIcon;
                public View mDivider;
                public View mMore;
                public View mNext;
                public OnViewClickListener listener;
                public OnViewLongClickListener longListener;

                public ViewHolder(View itemView) {
                    super(itemView);
                }

                @Override
                public void setDetails(TextView details) {
                    this.mDetails = details;
                }

                @Override
                public void setTitle(TextView title) {
                    this.mTitle = title;
                }

                @Override
                public void setValue(TextView value) {
                    this.mValue = value;
                }

                @Override
                public void setIcon(TextView icon) {
                    this.mIcon = icon;
                }

                @Override
                public void setFooter(TextView footer) {
                    this.mFooter = footer;
                }

                @Override
                public void setFooter2(TextView footer2) {
                    this.mFooter2 = footer2;
                }

                @Override
                public void setExtra1(TextView extra1) {
                    this.mExtra1 = extra1;
                }

                @Override
                public void setExtra2(TextView extra2) {
                    this.mExtra2 = extra2;
                }

                @Override
                public void setDivider(View divider) {
                    this.mDivider = divider;
                }

                @Override
                public void setMore(View more) {
                    this.mMore = more;
                }

                @Override
                public void setNext(View next) {
                    this.mNext = next;
                }

                @Override
                public void setView(View view) {
                    this.mView = view;
                }

                @Override
                public void setViewItem(View view) {
                    this.mViewItem = view;
                }

                @Override
                public void setListener(OnViewClickListener listener) {
                    this.listener = listener;
                }

                @Override
                public void setLongClickListener(OnViewLongClickListener listener) {
                    this.longListener = listener;
                }

                @Override
                public TextView getDetails() {
                    return mDetails;
                }

                @Override
                public TextView getTitle() {
                    return mTitle;
                }

                @Override
                public TextView getValue() {
                    return mValue;
                }

                @Override
                public TextView getIcon() {
                    return mIcon;
                }

                @Override
                public TextView getFooter() {
                    return mFooter;
                }

                @Override
                public TextView getFooter2() {
                    return mFooter2;
                }

                @Override
                public TextView getExtra1() {
                    return mExtra1;
                }

                @Override
                public TextView getExtra2() {
                    return mExtra2;
                }

                @Override
                public View getDivider() {
                    return mDivider;
                }

                @Override
                public View getNext() {
                    return mNext;
                }

                @Override
                public View getMore() {
                    return mMore;
                }

                @Override
                public View getView() {
                    return mView;
                }

                @Override
                public View getViewItem() {
                    return mViewItem;
                }

                @Override
                public OnViewClickListener getListener() {
                    return listener;
                }

                @Override
                public OnViewLongClickListener getLongClickListener() {
                    return this.longListener;
                }

                @Override
                public boolean getSupportHeader() {
                    return false;
                }

                //====================================== Not Supported =====================================

                @Override
                public void setHeader(View header) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public void setHeaderTitle(TextView title) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public void setHeaderValue(TextView value) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public void setHeaderValue2(TextView value) {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public View getHeader() {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public TextView getHeaderTitle() {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public TextView getHeaderValue() {
                    throw new RuntimeException("Not supported");
                }

                @Override
                public TextView getHeaderValue2() {
                    throw new RuntimeException("Not supported");
                }
            }
        }

        //Utility Methods:
        private static void prepareViewHolder(ListContentFragment fragment, UniversalListViewHolder holder, View view, ListData.Entity items, int flags) {
            holder.setTitle((TextView) view.findViewById(R.id.list_item_universal_title));
            holder.setDetails((TextView) view.findViewById(R.id.list_item_universal_detail));
            holder.setValue((TextView) view.findViewById(R.id.list_item_universal_value));
            holder.setFooter((TextView) view.findViewById(R.id.list_item_universal_footer));
            holder.setFooter2((TextView) view.findViewById(R.id.list_item_universal_footer2));
            holder.setExtra1((TextView) view.findViewById(R.id.list_item_universal_extra1));
            holder.setExtra2((TextView) view.findViewById(R.id.list_item_universal_extra2));
            holder.setIcon((TextView) view.findViewById(R.id.list_item_universal_icon));
            holder.setNext(view.findViewById(R.id.list_item_universal_next));
            holder.setMore(view.findViewById(R.id.list_item_universal_more));
            holder.setViewItem(view.findViewById(R.id.list_item_universal_item));
            holder.setView(view);
            holder.setDivider(view.findViewById(R.id.list_item_universal_divider));
            holder.setListener(new OnViewClickListener(fragment));
            holder.getNext().setOnClickListener(holder.getListener());
            holder.getMore().setOnClickListener(holder.getListener());
            holder.getIcon().setOnClickListener(holder.getListener());
            if (Flags.hasFlags(flags, FLAG_ASSIGN_CLICK_LISTENER)) {
                holder.getViewItem().setOnClickListener(holder.getListener());
            }
            if (Flags.hasFlags(flags, FLAG_ASSIGN_HOLD_LISTENER)) {
                holder.setLongClickListener(new OnViewLongClickListener(fragment));
                holder.getViewItem().setOnLongClickListener(holder.getLongClickListener());
            }
            TypeFaceUtils.setTypefaceDefaultCascade(fragment.getResources().getAssets(), holder.getViewItem());

            if (Flags.hasFlags(flags, FLAG_HAS_HEADER)) {
                holder.setHeader(view.findViewById(R.id.list_item_universal_header));
                holder.setHeaderTitle((TextView) view.findViewById(R.id.list_header_universal_title));
                holder.setHeaderValue((TextView) view.findViewById(R.id.list_header_universal_value));
                holder.setHeaderValue2((TextView) view.findViewById(R.id.list_header_universal_value2));
                TypeFaceUtils.setTypefaceDefault(fragment.getResources().getAssets(), holder.getHeaderTitle());
                TypeFaceUtils.setTypefaceDefault(fragment.getResources().getAssets(), holder.getHeaderValue());
                TypeFaceUtils.setTypefaceDefault(fragment.getResources().getAssets(), holder.getHeaderValue2());
            }

            int padding = fragment.getResources().getDimensionPixelSize(R.dimen.padding_list_sideways);
            holder.getExtra1().setCompoundDrawablePadding(padding);
            holder.getExtra2().setCompoundDrawablePadding(padding);
            holder.getTitle().setCompoundDrawablePadding(padding);
        }

        private static void bindViewHolder(Context context, UniversalListViewHolder holder, ListData.Entity items, int position, int flags) {
            ListData.Entity entity = items.getChildAt(position);
            ListData.MetaData metaData;
            Resources resources = context.getResources();

            //Item
            {
                if (items.overrideMetadataForChildren()) {
                    metaData = items.getMetadata();
                } else {
                    metaData = entity.getMetadata();
                }
                prepareViews(context, holder, metaData, entity, flags);

                String title = entity.getTitle(resources);
                setValueOrHide(holder.getTitle(), title);
                if (title != null) {
                    Integer iconResId = entity.getTitleIconResId();
                    if (iconResId != null) {
                        holder.getTitle().setCompoundDrawablesRelativeWithIntrinsicBounds(iconResId, 0, 0, 0);
                    } else {
                        holder.getTitle().setCompoundDrawables(null, null, null, null);
                    }
                }

                setValueOrHide(holder.getDetails(), entity.getDetails(resources));
                setValueOrHide(holder.getValue(), entity.getValue(context));
                setValueOrHide(holder.getFooter(), entity.getFooter());
                if (metaData.iconMode == ListData.MetaData.ICON_MODE_VISIBLE &&
                        !Flags.hasFlags(flags, FLAG_MARKED_MODE)) {
                    holder.getIcon().setText(entity.getIconText(resources));
                } else if (Flags.hasFlags(flags, FLAG_MARKABLE)) {
                    holder.getIcon().setText(null);
                } else if (metaData.iconMode == ListData.MetaData.ICON_MODE_VISIBLE_RES_ID) {
                    holder.getIcon().setText("");
                }

                if (!entity.hideNextButton() && entity.hasChildren() && Flags.hasFlags(flags, FLAG_SHOW_NEXT)) {
                    holder.getNext().setVisibility(View.VISIBLE);
                } else
                    holder.getNext().setVisibility(View.GONE);

                if (entity.hasMoreButton() && Flags.hasFlags(flags, FLAG_SHOW_MORE)) {
                    holder.getMore().setVisibility(View.VISIBLE);
                } else
                    holder.getMore().setVisibility(View.GONE);

                String footer2 = entity.getFooter2();
                if (footer2 != null) {
                    holder.getFooter2().setVisibility(View.VISIBLE);
                    holder.getFooter2().setText(footer2);
                } else {
                    holder.getFooter2().setVisibility(View.GONE);
                }

                String extra1 = entity.getExtra1();
                if (extra1 != null) {
                    holder.getExtra1().setVisibility(View.VISIBLE);
                    holder.getExtra1().setText(extra1);
                    Integer resId = entity.getExtra1Icon();
                    if (resId != null) {
                        holder.getExtra1().setCompoundDrawablesRelativeWithIntrinsicBounds(resId, 0, 0, 0);
                    }
                } else {
                    holder.getExtra1().setVisibility(View.GONE);
                }

                String extra2 = entity.getExtra2();
                if (extra2 != null) {
                    holder.getExtra2().setVisibility(View.VISIBLE);
                    holder.getExtra2().setText(extra2);
                    Integer resId = entity.getExtra2Icon();
                    if (resId != null) {
                        holder.getExtra2().setCompoundDrawablesRelativeWithIntrinsicBounds(resId, 0, 0, 0);
                    }
                } else {
                    holder.getExtra2().setVisibility(View.GONE);
                }

                holder.getView().setTag(entity);
                holder.getViewItem().setTag(entity);
                holder.getNext().setTag(entity);
                holder.getMore().setTag(entity);
                holder.getIcon().setTag(entity);
            }

            //header
            if (holder.getSupportHeader()) {
                if (Flags.hasFlags(flags, FLAG_HAS_HEADER)) {
                    holder.getHeader().setVisibility(View.VISIBLE);
                    String headerValue = entity.getHeaderValue(context);
                    String headerValue2 = entity.getHeaderValue2(context);
                    if (headerValue == null)
                        holder.getHeaderValue().setVisibility(View.GONE);
                    else {
                        holder.getHeaderValue().setVisibility(View.VISIBLE);
                        holder.getHeaderValue().setText(headerValue);
                        if (metaData.hasHeaderValueColor) {
                            holder.getHeaderValue().setTextColor(metaData.colorHeaderValue);
                        }
                    }
                    if (headerValue2 == null)
                        holder.getHeaderValue2().setVisibility(View.GONE);
                    else {
                        holder.getHeaderValue2().setVisibility(View.VISIBLE);
                        holder.getHeaderValue2().setText(headerValue2);
                        if (metaData.hasHeaderValue2Color) {
                            holder.getHeaderValue2().setTextColor(metaData.colorHeaderValue2);
                        }
                    }

                    holder.getHeaderTitle().setText(entity.getHeader(resources));
                } else {
                    holder.getHeader().setVisibility(View.GONE);
                }
            }
        }

        private static void setValueOrHide(TextView textView, String value) {
            if (value != null) {
                textView.setText(value);
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        }

        private static void prepareViews(
                Context context, UniversalListViewHolder holder, ListData.MetaData metaData,
                ListData.Entity entity, int flags) {
            Colors.ColorCache colorCache = new Colors.ColorCache(context);
            if (Flags.hasFlags(flags, FLAG_MARKED_MODE)) {
                Drawable drawable = context.getDrawable(R.drawable.ic_bobble_mark_white);
                drawable.setColorFilter(colorCache.accentColor, PorterDuff.Mode.MULTIPLY);
                holder.getIcon().setBackground(drawable);
            } else if (metaData.iconMode == ListData.MetaData.ICON_MODE_VISIBLE && metaData.hasColorIcon) {
                Drawable drawable = context.getDrawable(R.drawable.ic_bobble);
                drawable.setColorFilter(metaData.colorIcon, PorterDuff.Mode.MULTIPLY);
                holder.getIcon().setBackground(drawable);
            } else if (metaData.iconMode == ListData.MetaData.ICON_MODE_VISIBLE_RES_ID) {
                Drawable drawable = context.getDrawable(entity.getIconResId());
                if (metaData.hasColorIcon) {
                    drawable.setColorFilter(metaData.colorIcon, PorterDuff.Mode.MULTIPLY);
                } else {
                    drawable.setColorFilter(colorCache.accentColor, PorterDuff.Mode.MULTIPLY);
                }
                holder.getIcon().setBackground(drawable);
            } else if (holder.getIcon().getVisibility() == View.VISIBLE) {
                Drawable drawable = context.getDrawable(R.drawable.ic_bobble);
                drawable.setColorFilter(context.getResources().getColor(R.color.colorUnavailable), PorterDuff.Mode.MULTIPLY);
                holder.getIcon().setBackground(drawable);
            }

            if (metaData.hasColorTitle)
                holder.getTitle().setTextColor(metaData.colorTitle);
            if (metaData.hasColorDetails)
                holder.getDetails().setTextColor(metaData.colorDetails);
            if (metaData.hasColorValue)
                holder.getValue().setTextColor(metaData.colorValue);
            if (metaData.hasDivider)
                holder.getDivider().setVisibility(View.VISIBLE);
            else
                holder.getDivider().setVisibility(View.GONE);

            if (metaData.iconMode == ListData.MetaData.ICON_MODE_VISIBLE || Flags.hasFlags(flags, FLAG_MARKABLE))
                holder.getIcon().setVisibility(View.VISIBLE);
            else if (metaData.iconMode == ListData.MetaData.ICON_MODE_INVISIBLE)
                holder.getIcon().setVisibility(View.INVISIBLE);
            else if (metaData.iconMode == ListData.MetaData.ICON_MODE_GONE)
                holder.getIcon().setVisibility(View.GONE);
        }

        private static InfoViewHolder makeInfoViewHolder(ListContentFragment fragment, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(fragment.getContext());
            InfoViewHolder holder = InfoViewHolder.newInstance(inflater, parent);
            holder.layout = holder.itemView.findViewById(R.id.partial_action_layout_frame);
            return holder;
        }

        private static void updateInfoViewHolder(ListContentFragment fragment, InfoViewHolder holder) {
            LayoutInflater inflater = LayoutInflater.from(fragment.getContext());
            ArrayList<ListData.InfoRow> infoRows = fragment.listData.getInfoRows();
            if (infoRows == null || infoRows.size() == 0)
                holder.layout.setVisibility(View.GONE);
            else {
                holder.layout.setVisibility(View.VISIBLE);
                holder.layout.removeAllViews();
                OnActionClickListener actionClickListener = new OnActionClickListener(fragment);
                for (ListData.InfoRow infoRow : infoRows) {
                    View infoItemView = inflater.inflate(R.layout.partial_info_item, holder.layout, false);
                    TypeFaceUtils.setTypefaceDefaultCascade(fragment.getResources().getAssets(), infoItemView);
                    ImageView icon = infoItemView.findViewById(R.id.partial_info_item_icon);
                    TextView text = infoItemView.findViewById(R.id.partial_info_item_text);
                    TextView value = infoItemView.findViewById(R.id.partial_info_item_amount);

                    if (infoRow.getIconResId() != null) {
                        icon.setImageResource(infoRow.getIconResId());
                        icon.setColorFilter(infoRow.getIconColor(), PorterDuff.Mode.MULTIPLY);
                    }
                    if (infoRow.getText() != null) {
                        text.setText(infoRow.getText());
                    } else if (infoRow.getTextResId() != null) {
                        text.setText(infoRow.getTextResId());
                    } else {
                        text.setText(null);
                    }
                    value.setText(TypeFaceUtils.withTypefaceAmountFormat(infoRow.getAmount()));
                    text.setTextColor(infoRow.getTextColor());
                    value.setTextColor(infoRow.getValueColor());

                    infoItemView.setTag(infoRow);
                    infoItemView.setOnClickListener(actionClickListener);
                    holder.layout.addView(infoItemView);
                }
            }
        }

        private interface UniversalListViewHolder {
            void setDetails(TextView details);

            void setTitle(TextView title);

            void setValue(TextView value);

            void setIcon(TextView icon);

            void setFooter(TextView footer);

            void setFooter2(TextView footer2);

            void setExtra1(TextView extra1);

            void setExtra2(TextView extra2);

            void setDivider(View divider);

            void setNext(View next);

            void setMore(View more);

            void setView(View view);

            void setViewItem(View view);

            void setHeader(View header);

            void setHeaderTitle(TextView title);

            void setHeaderValue(TextView value);

            void setHeaderValue2(TextView value);

            void setListener(OnViewClickListener listener);

            void setLongClickListener(OnViewLongClickListener listener);

            TextView getDetails();

            TextView getTitle();

            TextView getValue();

            TextView getIcon();

            TextView getFooter();

            TextView getFooter2();

            TextView getExtra1();

            TextView getExtra2();

            View getDivider();

            View getNext();

            View getMore();

            View getView();

            View getViewItem();

            View getHeader();

            TextView getHeaderTitle();

            TextView getHeaderValue();

            TextView getHeaderValue2();

            OnViewClickListener getListener();

            OnViewLongClickListener getLongClickListener();

            boolean getSupportHeader();
        }

        private interface UniversalAdapter {
            void updateItems(ListData.Entity items);
        }

        private static class InfoViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout;

            public static InfoViewHolder newInstance(LayoutInflater inflater, ViewGroup parent) {
                View itemView = inflater.inflate(R.layout.partial_info_layout, parent, false);
                return new InfoViewHolder(itemView);
            }

            public InfoViewHolder(View itemView) {
                super(itemView);
            }
        }

        private static class OnViewClickListener implements View.OnClickListener {
            ListContentFragment fragment;

            public OnViewClickListener(ListContentFragment fragment) {
                this.fragment = fragment;
            }

            @Override
            public void onClick(View v) {
                ListData.Entity entity = (ListData.Entity) v.getTag();
                if (entity == null)
                    throw new RuntimeException("Entity is null, tag is not set to the view");
                if (v.getId() == R.id.list_item_universal_next) {
                    fragment.onNextClicked(entity);
                } else if (v.getId() == R.id.list_item_universal_item || v.getId() == R.id.list_item_universal_root) {
                    fragment.onItemClicked(entity);
                } else if (v.getId() == R.id.list_item_universal_icon) {
                    fragment.onIconClicked(entity);
                } else if (v.getId() == R.id.list_item_universal_more) {
                    fragment.onMoreClicked(entity, v);
                }
            }
        }

        private static class OnActionClickListener implements View.OnClickListener {
            ListContentFragment fragment;

            public OnActionClickListener(ListContentFragment fragment) {
                this.fragment = fragment;
            }

            @Override
            public void onClick(View v) {
                fragment.onUniversalListActionClicked((ListData.InfoRow) v.getTag(), v);
            }
        }

        private static class OnViewLongClickListener implements View.OnLongClickListener {
            ListContentFragment fragment;

            public OnViewLongClickListener(ListContentFragment fragment) {
                this.fragment = fragment;
            }

            @Override
            public boolean onLongClick(View v) {
                ListData.Entity entity = (ListData.Entity) v.getTag();
                if (v.getId() == R.id.list_item_universal_item || v.getId() == R.id.list_item_universal_root) {
                    return fragment.onItemLongClicked(entity);
                }
                return false;
            }
        }
    }
}
