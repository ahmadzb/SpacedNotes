package com.diplinkblaze.spacednote.universal.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.woxthebox.draglistview.DragListView;
import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BackSupportListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;
import com.diplinkblaze.spacednote.contract.ContentUpdateUtil;
import com.diplinkblaze.spacednote.universal.util.ListData;

import java.util.ArrayList;

import util.Flags;
import util.TypeFaceUtils;

public class ListFragment extends Fragment implements ContentUpdateListener, BackSupportListener,
        ListContentFragment.OnFragmentInteractionListener, ListContentFragment.OnFragmentMarkActionListener {

    private static final String KEY_TAG = "tag";
    private static final String KEY_LIST_DATA_PREFIX = "listData_";
    private static final String KEY_IDENTIFIER = "identifier";

    private static final String KEY_LIST_CONTENT_ROOT_ENTITY = "listContentRootEntity";

    private static final String TAG_LIST_CONTENT_PREFIX = "listContent_";

    boolean isLayoutLoaded = false;
    Bundle savedInstanceState;
    private ListData listData;

    private OnBackStackClickListener backStackClickListener = new OnBackStackClickListener();

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(String tag) {
        return newInstance(tag, null);
    }

    public static ListFragment newInstance(String tag, Bundle identifier) {

        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TAG, tag);
        args.putBundle(KEY_IDENTIFIER, identifier);
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
        getChildFragmentManager().addOnBackStackChangedListener(new OnBackStackChanged());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isLayoutLoaded = false;
        View contentView = inflater.inflate(R.layout.fragment_list, container, false);
        loadLayout(inflater, contentView);
        return contentView;
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
            if (savedInstanceState != null) {
                listData.restoreInstanceState(savedInstanceState, KEY_LIST_DATA_PREFIX);
                updateLists();
            }

            initializeViews(contentView);
            updateViews(inflater, contentView);
            updateLists();
            isLayoutLoaded = true;
        }
    }

    private void initializeViews(View contentView) {
        //Content List
        {
            addListFragmentIfNotExist(listData.getItems());
        }

        if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_TREE)) {
            ViewStub backStackStub = contentView.findViewById(R.id.fragment_universal_list_tree_path_stub);
            backStackStub.inflate();
        }

        if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_MARKABLE) && listData.getMarkMode() == ListData.MARK_ALWAYS_CHOOSER) {
            ViewStub okCancelStub = contentView.findViewById(R.id.fragment_universal_list_ok_cancel_stub);
            View okCancelView = okCancelStub.inflate();
            TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), okCancelView);
            View ok = okCancelView.findViewById(R.id.partial_list_ok_cancel_ok);
            View cancel = okCancelView.findViewById(R.id.partial_list_ok_cancel_cancel);
            OnOkCancelClickListener listener = new OnOkCancelClickListener();
            ok.setOnClickListener(listener);
            cancel.setOnClickListener(listener);
        }
    }

    private void tryUpdateViews() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View contentView = getView();
        if (inflater != null && contentView != null) {
            updateViews(inflater, contentView);
        }
    }

    private void updateViews(LayoutInflater inflater, View contentView) {
        //Back Stack
        {
            if (Flags.hasFlags(listData.getModeFlags(), ListData.MODE_FLAG_TREE)) {

                LinearLayout backStackLayout = contentView.findViewById(R.id.fragment_universal_list_back_stack);
                backStackLayout.removeAllViews();
                int count = getChildFragmentManager().getBackStackEntryCount();
                for (int i = count - 1; i >= 0; i--) {
                    String tag = getChildFragmentManager().getBackStackEntryAt(i).getName();
                    ListData.Entity stackItem = getEntityByTag(tag);
                    if (stackItem != null) {
                        View stackItemView = inflater.inflate(R.layout.partial_back_stack_item, backStackLayout, false);
                        stackItemView.setTag(stackItem);
                        TextView stackItemText = stackItemView.findViewById(R.id.partial_back_stack_item_text);
                        stackItemText.setText(stackItem.getTitle(getResources()));
                        TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), stackItemText);
                        if (stackItem.getBackReference() == null) {
                            ImageView stackItemImage = stackItemView.findViewById(R.id.partial_back_stack_item_picture);
                            stackItemImage.setVisibility(View.GONE);
                        }
                        backStackLayout.addView(stackItemView, 0);
                        stackItemView.setOnClickListener(backStackClickListener);
                    } else {
                        getChildFragmentManager().popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        listData.saveInstanceState(outState, KEY_LIST_DATA_PREFIX);
    }

    private void addListFragmentIfNotExist(ListData.Entity rootEntity) {
        String tag = getTagForEntity(rootEntity);
        if (getChildFragmentManager().findFragmentByTag(tag) == null) {
            ListContentFragment fragment = ListContentFragment.newInstance(
                    getIdentifierByEntity(rootEntity), rootEntity.getEntityId());
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_universal_list_frame_content, fragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        }
    }

    public ListData.Entity getCurrentRootEntity() {
        int count = getChildFragmentManager().getBackStackEntryCount();
        String tag = getChildFragmentManager().getBackStackEntryAt(count - 1).getName();
        return getEntityByTag(tag);
    }

    //======================================== Listeners ===========================================
    private void onBackStackItemPressed(ListData.Entity entity) {
        getChildFragmentManager().popBackStack(getTagForEntity(entity), 0);
    }

    private class OnBackStackClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            ListData.Entity entity = (ListData.Entity) view.getTag();
            if (entity == null)
                throw new RuntimeException("clicked back stack view is missing the tag");
            onBackStackItemPressed(entity);
        }
    }

    private class OnOkCancelClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.partial_list_ok_cancel_ok) {
                onUniversalListItemsSelected();
            } else if (v.getId() == R.id.partial_list_ok_cancel_cancel) {
                universalListDismissRequest();
            }
        }
    }

    private class OnBackStackChanged implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            updateContent();
            tryUpdateViews();
        }
    }

    //================================== Communication Parent ======================================
    @Override
    public boolean onBackPressed() {
        if (listData.hasMarkedItems()) {
            listData.clearMarkedItems();
            updateLists();
            onUniversalListMarkedItemsChanged();
            return true;
        } else if (getChildFragmentManager().getBackStackEntryCount() == 1) {
            return false;
        } else {
            getChildFragmentManager().popBackStack();
            return true;
        }
    }

    @Override
    public void updateContent() {
        boolean previousLoadState = isLayoutLoaded;
        retrieveUniversalListData();
        tryLoadLayout();
        if (previousLoadState) {
            updateLists();
            tryUpdateViews();
        }
    }

    public void updateLists() {
        ContentUpdateUtil.updateContentChildren(this);
    }

    private void retrieveUniversalListData() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        OnFragmentInteractionListener listener = getListener();
        if (listener != null) {
            listData = listener.retrieveUniversalListData(getContext(), tag, identifier);
        }
    }

    private void onUniversalListPositionsChanged(ListData.Entity rootEntity) {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListPositionsChanged(listData, rootEntity, tag, identifier);
    }

    private void onUniversalListItemSelected(ListData.Entity entity) {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListItemSelected(entity, tag, identifier);
    }

    private void onUniversalListActionClicked(ListData.InfoRow infoRow, View v) {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListActionClicked(infoRow, v, tag, identifier);
    }

    private void onUniversalListActionClicked(ListViewInfo viewInfo) {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListDeliverViewInfo(viewInfo, tag, identifier);
    }

    private void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView) {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onUniversalListItemMoreSelected(entity, moreView, tag, identifier);
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            return ((OnFragmentInteractionListener) getParentFragment());
        else if (getActivity() instanceof OnFragmentInteractionListener)
            return ((OnFragmentInteractionListener) getActivity());
        else
            throw new RuntimeException("Either parent fragment or activity should implement" +
                    " OnFragmentInteractionListener");
    }

    private void onUniversalListItemsSelected() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        if (getParentFragment() instanceof OnFragmentMarkActionListener)
            ((OnFragmentMarkActionListener) getParentFragment()).onUniversalListItemsSelected(listData.getMarkedItems(), tag, identifier);
        else if (getActivity() instanceof OnFragmentMarkActionListener)
            ((OnFragmentMarkActionListener) getActivity()).onUniversalListItemsSelected(listData.getMarkedItems(), tag, identifier);
        else
            throw new RuntimeException("Either parent fragment or activity should implement" +
                    " OnFragmentMarkActionListener");
    }

    private void onUniversalListMarkedItemsChanged() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        if (getParentFragment() instanceof OnFragmentMarkActionListener)
            ((OnFragmentMarkActionListener) getParentFragment()).onUniversalListMarkedItemsChanged(listData.getMarkedItems(), tag, identifier);
        else if (getActivity() instanceof OnFragmentMarkActionListener)
            ((OnFragmentMarkActionListener) getActivity()).onUniversalListMarkedItemsChanged(listData.getMarkedItems(), tag, identifier);
        else
            throw new RuntimeException("Either parent fragment or activity should implement" +
                    " OnFragmentMarkActionListener");
    }

    private void universalListDismissRequest() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        if (getParentFragment() instanceof OnFragmentMarkActionListener)
            ((OnFragmentMarkActionListener) getParentFragment()).universalListDismissRequest(tag, identifier);
        else if (getActivity() instanceof OnFragmentMarkActionListener)
            ((OnFragmentMarkActionListener) getActivity()).universalListDismissRequest(tag, identifier);
        else
            throw new RuntimeException("Either parent fragment or activity should implement" +
                    " OnFragmentMarkActionListener");
    }

    public interface OnFragmentInteractionListener {
        ListData retrieveUniversalListData(Context context, String tag, Bundle identifier);

        void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier);

        void onUniversalListItemSelected(ListData.Entity entity, String tag, Bundle identifier);

        void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView, String tag, Bundle identifier);

        void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, String tag, Bundle identifier);

        void onUniversalListDeliverViewInfo(ListViewInfo viewInfo, String tag, Bundle identifier);
    }

    public interface OnFragmentMarkActionListener {
        void onUniversalListItemsSelected(ArrayList<Long> ids, String tag, Bundle identifier);

        void universalListDismissRequest(String tag, Bundle identifier);

        void onUniversalListMarkedItemsChanged(ArrayList<Long> ids, String tag, Bundle identifier);
    }

    //================================= Communication Children =====================================
    @Override
    public ListData retrieveUniversalListData(Context context, Bundle identifier) {
        return listData;
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, Bundle identifier) {
        onUniversalListPositionsChanged(getEntityByIdentifier(identifier));
    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, Bundle identifier) {
        onUniversalListItemSelected(entity);
    }

    @Override
    public void onUniversalListNextSelected(ListData.Entity entity, Bundle identifier) {
        addListFragmentIfNotExist(entity);
    }

    @Override
    public void onUniversalListMarkedItemsChanged(Bundle identifier) {
        onUniversalListMarkedItemsChanged();
    }

    @Override
    public void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, Bundle identifier) {
        onUniversalListActionClicked(infoRow, view);
    }

    @Override
    public void onUniversalListDeliverViewInfo(ListViewInfo viewInfo, Bundle identifier) {
        onUniversalListActionClicked(viewInfo);
    }

    @Override
    public void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView, Bundle identifier) {
        onUniversalListItemMoreSelected(entity, moreView);
    }

    //======================================= Tag Factory ==========================================
    public String getTagForEntity(ListData.Entity entity) {
        return TAG_LIST_CONTENT_PREFIX + entity.getEntityId();
    }

    public ListData.Entity getEntityByTag(String tag) {
        long entityId = Long.parseLong(tag.replace(TAG_LIST_CONTENT_PREFIX, ""));
        return listData.findItemByEntityId(entityId);
    }

    public Bundle getIdentifierByEntity(ListData.Entity entity) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_IDENTIFIER, entity.getEntityId());
        return bundle;
    }

    public ListData.Entity getEntityByIdentifier(Bundle identifier) {
        long entityId = identifier.getLong(KEY_IDENTIFIER);
        return listData.findItemByEntityId(entityId);
    }

    public static class ListViewInfo {
        private RecyclerView recyclerView;
        private DragListView dragListView;

        public RecyclerView getRecyclerView() {
            return recyclerView;
        }

        public void setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        public DragListView getDragListView() {
            return dragListView;
        }

        public void setDragListView(DragListView dragListView) {
            this.dragListView = dragListView;
        }
    }
}
