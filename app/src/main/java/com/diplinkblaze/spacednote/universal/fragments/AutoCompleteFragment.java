package com.diplinkblaze.spacednote.universal.fragments;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.universal.contract.AveContentChangeListener;
import com.diplinkblaze.spacednote.universal.util.ListData;

import java.util.ArrayList;

import util.TypeFaceUtils;
import util.StringTransformer;


public class AutoCompleteFragment extends Fragment implements AveContentChangeListener {
    private static final String KEY_TAG = "keyTag";
    private static final String KEY_IDENTIFIER = "keyIdentifier";
    private static final String KEY_HINT_TEXT = "keyHintText";
    private static final String KEY_ICON_RES_ID = "keyIconResId";
    private static final String KEY_SINGLE_MODE = "keySingleMode";
    private static final String KEY_SELECTED_ENTITIES = "keySelectedEntities";
    private static final String KEY_READ_ONLY = "keyReadOnly";
    private static final String KEY_NEW_ITEM_SUPPORT = "keyNewItemSupport";
    private static final String KEY_NEW_ITEM_TRANSFORMERS = "keyNewItemPrefixes";

    private Adapter mAdapter;
    private ListData mData;
    private ListData mFullData;
    private ArrayList<Long> mSelectedEntities;
    private boolean readOnly;
    private boolean singleMode;
    private boolean newItemSupport;
    private ArrayList<StringTransformer> newItemTransformers;

    private OnViewClickListener mViewClickListener = new OnViewClickListener();

    public AutoCompleteFragment() {
        // Required empty public constructor
    }

    public static AutoCompleteFragment newInstance(
            @Nullable String hint, @Nullable Integer iconResId, ArrayList<Long> selectedEntities,
            boolean readOnly, boolean singleMode, boolean newItemSupport,
            ArrayList<StringTransformer> newItemTransformers, String tag, Bundle identifier) {
        AutoCompleteFragment fragment = new AutoCompleteFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TAG, tag);
        args.putBundle(KEY_IDENTIFIER, identifier);
        args.putSerializable(KEY_ICON_RES_ID, iconResId);
        args.putString(KEY_HINT_TEXT, hint);
        args.putSerializable(KEY_SELECTED_ENTITIES, new ArrayList<Long>(selectedEntities));
        args.putBoolean(KEY_READ_ONLY, readOnly);
        args.putBoolean(KEY_SINGLE_MODE, singleMode);
        args.putBoolean(KEY_NEW_ITEM_SUPPORT, newItemSupport);
        args.putSerializable(KEY_NEW_ITEM_TRANSFORMERS, newItemTransformers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataRetrieve();
        if (savedInstanceState == null) {
            mSelectedEntities = (ArrayList<Long>) getArguments().getSerializable(KEY_SELECTED_ENTITIES);
            if (mSelectedEntities == null)
                mSelectedEntities = new ArrayList<Long>();
            readOnly = getArguments().getBoolean(KEY_READ_ONLY);
        } else {
            mSelectedEntities = (ArrayList<Long>) savedInstanceState.getSerializable(KEY_SELECTED_ENTITIES);
            readOnly = savedInstanceState.getBoolean(KEY_READ_ONLY);
        }
        singleMode = getArguments().getBoolean(KEY_SINGLE_MODE);
        newItemSupport = getArguments().getBoolean(KEY_NEW_ITEM_SUPPORT);
        newItemTransformers = (ArrayList<StringTransformer>) getArguments().getSerializable(KEY_NEW_ITEM_TRANSFORMERS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_universal_auto_complete, container, false);
        initializeViews(contentView);
        updateViews(inflater, contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        AutoCompleteTextView aCTextView = contentView.
                findViewById(R.id.fragment_universal_auto_complete_text);
        mAdapter = new Adapter();
        aCTextView.setAdapter(mAdapter);
        TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), aCTextView);

        String hint = getArguments().getString(KEY_HINT_TEXT);
        if (hint != null)
            aCTextView.setHint(hint);
        Integer iconResId = (Integer) getArguments().getSerializable(KEY_ICON_RES_ID);
        if (iconResId != null) {
            ImageView iconImageView = contentView.findViewById(R.id.fragment_universal_auto_complete_image);
            iconImageView.setImageResource(iconResId);
            iconImageView.setOnClickListener(mViewClickListener);
        }

        View openPopup = contentView.findViewById(R.id.fragment_universal_auto_complete_open_popup);
        openPopup.setOnClickListener(mViewClickListener);
    }

    private void updateViews(LayoutInflater inflater, View contentView) {
        //Selected Items:
        {
            LinearLayout container = contentView.findViewById(R.id.fragment_universal_auto_complete_list);

            //Add views if necessary, and update for readOnly
            for (Long entityId : mSelectedEntities) {
                ListData.Entity entity = mFullData.findItemByEntityId(entityId);
                if (entity != null) {
                    LinearLayout entityView = container.findViewWithTag(makeTagForSelectedEntity(entity));

                    //Add views if necessary:
                    {
                        if (entityView == null) {
                            entityView = (LinearLayout) inflater.inflate(R.layout.list_selected_auto_complete,
                                    container, false);
                            TextView entityViewText = entityView.findViewById(R.id.list_selected_auto_complete_text);
                            View entityViewRemove = entityView.findViewById(R.id.list_selected_auto_complete_remove);

                            entityView.setTag(makeTagForSelectedEntity(entity));
                            entityViewRemove.setTag(makeTagForSelectedEntity(entity));
                            entityViewText.setText(entity.getTitle(getResources()));
                            entityViewRemove.setOnClickListener(mViewClickListener);

                            TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), entityViewText);

                            container.addView(entityView);
                        }
                    }

                    //update for readOnly:
                    {
                        View entityViewRemove = entityView.findViewById(R.id.list_selected_auto_complete_remove);
                        if (readOnly) {
                            entityViewRemove.setVisibility(View.GONE);
                        } else {
                            entityViewRemove.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            //Remove deleted entities' views from list
            ArrayList<View> removeList = new ArrayList<View>();
            int count = container.getChildCount();
            for (int i = 0; i < count; i++) {
                View childView = container.getChildAt(i);
                String childTag = (String) childView.getTag();
                boolean shouldRemove = true;
                for (Long entityId : mSelectedEntities) {
                    if (makeTagForSelectedEntity(entityId).equals(childTag))
                        shouldRemove = false;
                }
                if (shouldRemove)
                    removeList.add(childView);
            }
            for (View view : removeList)
                container.removeView(view);

        }

        //AutoComplete component:
        {
            //update for readOnly and single mode:
            View inputLayout = contentView.findViewById(R.id.fragment_universal_auto_complete_input_layout);
            if (readOnly || singleMode && mSelectedEntities.size() == 1) {
                inputLayout.setVisibility(View.GONE);
            } else {
                inputLayout.setVisibility(View.VISIBLE);
            }
        }

        //Popup
        {
            ImageView iconImageView = contentView.findViewById(R.id.fragment_universal_auto_complete_image);
            View openPopup = contentView.findViewById(R.id.fragment_universal_auto_complete_open_popup);
            if (readOnly) {
                openPopup.setVisibility(View.GONE);
                iconImageView.setEnabled(false);
            } else {
                openPopup.setVisibility(View.VISIBLE);
                iconImageView.setEnabled(true);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SELECTED_ENTITIES, mSelectedEntities);
        outState.putBoolean(KEY_READ_ONLY, readOnly);
    }

    //=================================== Tag Factory ======================================
    private long getEntityIdByTag(String tag) {
        return Long.parseLong(tag.split("#")[1]);
    }

    private String makeTagForSelectedEntity(ListData.Entity entity) {
        return "Selected Entity #" + entity.getEntityId();
    }

    private String makeTagForSelectedEntity(Long entityId) {
        return "Selected Entity #" + entityId;
    }

    //==================================== Listeners =======================================

    private void removeSelectedItem(Long entityId) {
        mSelectedEntities.remove(entityId);
        mAdapter.updateEntities(mSelectedEntities);
        onSelectedEntitiesChanged();
        updateViews(LayoutInflater.from(getContext()), getView());
    }

    private void listItemSelected(ListData.Entity entity) {
        if (entity.getEntityId() <= MockEntity.ENTITY_ID_ADD_START) {
            MockEntity mockEntity = (MockEntity) entity;
            String input = mockEntity.getAddInput();
            if (newItemTransformers == null || newItemTransformers.size() == 0) {
                onNewItemRequest(input, null);
            } else {
                onNewItemRequest(input, mockEntity.getAddTransformer());
            }
            AutoCompleteTextView aCTextView = getView().findViewById(R.id.fragment_universal_auto_complete_text);
            aCTextView.setText(null);
        } else {
            mSelectedEntities.add(entity.getEntityId());
            onSelectedEntitiesChanged();
            updateViews(LayoutInflater.from(getContext()), getView());
            AutoCompleteTextView aCTextView = getView().findViewById(R.id.fragment_universal_auto_complete_text);
            aCTextView.setText(null);
        }
    }

    private void openPopupClicked() {
        requestExplicitChoose();
    }

    private class OnViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.list_selected_auto_complete_remove) {
                ListData.Entity removeEntity = null;
                long entityId = getEntityIdByTag((String) view.getTag());
                removeSelectedItem(entityId);
            } else if (view.getId() == R.id.fragment_universal_auto_complete_open_popup ||
                    view.getId() == R.id.fragment_universal_auto_complete_image) {
                openPopupClicked();
            }
        }
    }

    //=============================== Parent Communication ================================
    @Override
    public void aveStateChanged(boolean readOnly) {
        this.readOnly = readOnly;
        updateViews(LayoutInflater.from(getContext()), getView());
    }

    public void aveNewContent(ArrayList<Long> selectedEntities) {
        mSelectedEntities = new ArrayList<>(selectedEntities);
        dataRetrieve();
        updateViews(LayoutInflater.from(getContext()), getView());
    }

    private void dataRetrieve() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        mData = getListener().retrieveAutoCompleteListData(tag, identifier);
        mFullData = getListener().retrieveAutoCompleteFullListData(tag, identifier);
    }

    private void onSelectedEntitiesChanged() {
        ArrayList<ListData.Entity> newList = mData.findItemsByEntityIds(mSelectedEntities);
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onAutoCompleteContentChanged(newList, tag, identifier);
    }

    private void onNewItemRequest(String input, StringTransformer transformer) {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onAutoCompleteNewItemRequest(input, transformer, tag, identifier);
    }

    private void requestExplicitChoose() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().requestExplicitChoose(tag, identifier);
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            return (OnFragmentInteractionListener) getParentFragment();
        else if (getActivity() instanceof OnFragmentInteractionListener)
            return (OnFragmentInteractionListener) getActivity();
        else
            throw new RuntimeException("Neither Parent Fragment nor Activity is an instance of " +
                    "OnFragmentInteractionListener");
    }

    public interface OnFragmentInteractionListener {
        ListData retrieveAutoCompleteListData(String tag, Bundle identifier);

        ListData retrieveAutoCompleteFullListData(String tag, Bundle identifier);

        void onAutoCompleteContentChanged(ArrayList<ListData.Entity> entities, String tag, Bundle identifier);

        void onAutoCompleteNewItemRequest(String input, StringTransformer transformer, String tag, Bundle identifier);

        void requestExplicitChoose(String tag, Bundle identifier);
    }

    //==================================== Classes ========================================
    private class Adapter extends BaseAdapter implements Filterable {
        private OnViewClickListener viewClickListener = new OnViewClickListener();
        private ListData.Entity mFilteredEntities;
        private EntityFilter mFilter = new EntityFilter();

        public Adapter() {
        }


        @Override
        public int getCount() {
            if (mFilteredEntities != null)
                return mFilteredEntities.getChildrenCountImmediate();
            else
                return 0;
        }

        @Override
        public Object getItem(int i) {
            if (mFilteredEntities != null)
                return mFilteredEntities.getChildAt(i);
            else
                return null;
        }

        @Override
        public long getItemId(int i) {
            if (mFilteredEntities != null)
                return mFilteredEntities.getChildAt(i).getEntityId();
            else
                return -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_universal, parent, false);

                holder = new ViewHolder();
                convertView.setOnClickListener(viewClickListener);

                holder.mTitle = convertView.findViewById(R.id.list_item_universal_title);
                holder.mDetails = convertView.findViewById(R.id.list_item_universal_detail);
                holder.mValue = convertView.findViewById(R.id.list_item_universal_value);
                holder.mFooter = convertView.findViewById(R.id.list_item_universal_footer);
                holder.mIcon = convertView.findViewById(R.id.list_item_universal_icon);
                holder.mNext = convertView.findViewById(R.id.list_item_universal_next);
                holder.mView = convertView;
                holder.mDivider = convertView.findViewById(R.id.list_item_universal_divider);

                TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), holder.mDetails);
                TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), holder.mTitle);
                TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), holder.mValue);
                TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), holder.mFooter);
                TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), holder.mIcon);
            } else
                holder = ((TagObject) convertView.getTag()).holder;

            {
                ListData.Entity entity = mFilteredEntities.getChildAt(position);
                convertView.setTag(new TagObject(holder, entity));
                ListData.MetaData metaData;

                if (mData.getItems().overrideMetadataForChildren()) {
                    metaData = mData.getItems().getMetadata();
                } else {
                    metaData = entity.getMetadata();
                }
                prepareViews(holder, metaData);

                Resources resources = getResources();

                setValueOrHide(holder.mTitle, entity.getTitle(resources));
                setValueOrHide(holder.mDetails, entity.getDetails(resources));
                setValueOrHide(holder.mValue, entity.getValue(getContext()));
                setValueOrHide(holder.mFooter, entity.getFooter());

                if (metaData.iconMode == ListData.MetaData.ICON_MODE_VISIBLE) {
                    holder.mIcon.setText(entity.getTitle(resources).substring(0, 1));
                }

                if (entity.hasChildren()) {
                    holder.mNext.setVisibility(View.VISIBLE);
                } else
                    holder.mNext.setVisibility(View.GONE);
            }

            return convertView;
        }

        private void setValueOrHide(TextView textView, String value) {
            if (value != null) {
                textView.setText(value);
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        }

        private void prepareViews(ViewHolder holder, ListData.MetaData metaData) {
            if (metaData.hasColorTitle)
                holder.mTitle.setTextColor(metaData.colorTitle);
            if (metaData.hasColorDetails)
                holder.mDetails.setTextColor(metaData.colorDetails);
            if (metaData.hasColorValue)
                holder.mValue.setTextColor(metaData.colorValue);
            if (metaData.hasColorIcon) {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_bobble);
                drawable.setColorFilter(metaData.colorIcon, PorterDuff.Mode.MULTIPLY);
                holder.mIcon.setBackground(drawable);
            }
            if (metaData.hasDivider)
                holder.mDivider.setVisibility(View.VISIBLE);
            else
                holder.mDivider.setVisibility(View.GONE);

            if (metaData.iconMode == ListData.MetaData.ICON_MODE_VISIBLE)
                holder.mIcon.setVisibility(View.VISIBLE);
            else if (metaData.iconMode == ListData.MetaData.ICON_MODE_INVISIBLE)
                holder.mIcon.setVisibility(View.INVISIBLE);
            else if (metaData.iconMode == ListData.MetaData.ICON_MODE_GONE)
                holder.mIcon.setVisibility(View.GONE);
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        public void updateEntities(ArrayList<Long> selectedItems) {
            mSelectedEntities = selectedItems;
            notifyDataSetChanged();
        }

        public class EntityFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                ListData.Entity filteredEntities = new MockEntity(null, MockEntity.ENTITY_ID_ROOT,
                        mData.getItems().getMetadata(), null, null);

                if (charSequence != null) {
                    String keyWord = charSequence.toString().toLowerCase();
                    if (mData.getItems().getChildrenCountImmediate() != 0) {
                        for (ListData.Entity entity : mData.getItems().getChildren()) {
                            if (entity.getTitle(getResources()).toLowerCase().contains(keyWord) &&
                                    (mSelectedEntities == null || !mSelectedEntities.contains(entity.getEntityId())))
                                filteredEntities.addChild(entity);
                        }
                    }
                    if (newItemSupport) {
                        ListData.MetaData newMetaData = new ListData.MetaData();
                        if (newItemTransformers == null || newItemTransformers.size() == 0) {
                            String add = getResources().getString(R.string.partial_add);
                            String quoteOpen = getResources().getString(R.string.partial_quote_open);
                            String quoteClose = getResources().getString(R.string.partial_quote_close);
                            String title = add + " " + quoteOpen + charSequence + quoteClose;
                            filteredEntities.addChild(new MockEntity(title, MockEntity.ENTITY_ID_ADD_START,
                                    newMetaData, null, charSequence.toString()));
                        } else {
                            int i = 0;
                            for (StringTransformer transformer : newItemTransformers) {
                                filteredEntities.addChild(new MockEntity(
                                        transformer.transform(charSequence.toString()),
                                        MockEntity.ENTITY_ID_ADD_START - i,
                                        newMetaData,
                                        transformer,
                                        charSequence.toString()));
                                i++;
                            }
                        }
                    }
                }

                if (filteredEntities.getChildrenCountImmediate() == 0) {
                    results.values = null;
                    results.count = 0;
                } else {
                    results.values = filteredEntities;
                    results.count = filteredEntities.getChildrenCountImmediate();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.values == null)
                    mFilteredEntities = null;
                else
                    mFilteredEntities = (ListData.Entity) filterResults.values;
                notifyDataSetChanged();
            }
        }

        private class TagObject {
            public ViewHolder holder;
            public ListData.Entity entity;

            public TagObject(ViewHolder holder, ListData.Entity entity) {
                this.holder = holder;
                this.entity = entity;
            }
        }

        private class ViewHolder {
            public TextView mDetails;
            public TextView mTitle;
            public TextView mValue;
            public TextView mIcon;
            public TextView mFooter;
            public View mDivider;
            public View mNext;
            public View mView;
        }

        private class OnViewClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                listItemSelected(((TagObject) v.getTag()).entity);
            }
        }
    }

    private static class MockEntity extends ListData.Entity {
        public static final long ENTITY_ID_ROOT = -1;
        public static final long ENTITY_ID_ADD_START = -2;

        private StringTransformer addTransformer;
        private String addInput;

        private String title;
        private long entityId;
        private ListData.MetaData metaData;

        public MockEntity(String title, long entityId, ListData.MetaData metaData,
                          StringTransformer addTransformer, String addInput) {
            this.addTransformer = addTransformer;
            this.addInput = addInput;
            this.title = title;
            this.entityId = entityId;
            this.metaData = metaData;
        }


        @Override
        public long getEntityId() {
            return entityId;
        }

        @Override
        public long getId() {
            throw new RuntimeException("Invalid Operation");
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public String getTitle(Resources resources) {
            return title;
        }

        @Override
        public ListData.MetaData getMetadata() {
            return metaData;
        }

        private ListData.MetaData createFromSample(ListData.MetaData sample) {
            ListData.MetaData metaData = new ListData.MetaData();
            metaData.iconMode = sample.iconMode;
            metaData.colorTitle = sample.colorTitle;

            return metaData;
        }

        public StringTransformer getAddTransformer() {
            return addTransformer;
        }

        public String getAddInput() {
            return addInput;
        }
    }
}
