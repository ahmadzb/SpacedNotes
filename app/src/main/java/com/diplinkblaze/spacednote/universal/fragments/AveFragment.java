package com.diplinkblaze.spacednote.universal.fragments;

import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.BackSupportListener;
import com.diplinkblaze.spacednote.universal.contract.AveContentChangeListener;
import com.diplinkblaze.spacednote.universal.util.AveComponentSet;
import com.diplinkblaze.spacednote.universal.util.ListData;
import com.diplinkblaze.spacednote.universal.util.ListUtil;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.chrono.PersianChronology;

import util.Colors;
import util.datetime.format.DateTimeFormat;
import util.datetime.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import data.database.OpenHelper;
import data.model.label.LabelListCatalog;
import util.BottomSheetUtil;
import util.EnglishTypeFace;
import util.Keyboard;
import util.Numbers;
import util.TypeFaceUtils;
import util.StringTransformer;

public class AveFragment extends BottomSheetDialogFragment implements
        ListFragment.OnFragmentInteractionListener, ListFragment.OnFragmentMarkActionListener,
        AutoCompleteFragment.OnFragmentInteractionListener, CalendarFragment.OnFragmentInteractionListener,
        CalendarTimeFragment.OnFragmentInteractionListener, BackSupportListener,
        DateTimeChooserFragment.OnFragmentInteractionListener, AmountInputFragment.OnFragmentInteractionListener {

    private static final String KEY_IS_INLINE = "keyIsInline";
    private static final String KEY_COMPONENT_SET = "keyComponentSet";
    private static final String KEY_COMPONENT_SET_ROLLBACK = "keyComponentSetRollback";
    private static final String KEY_READ_ONLY_ORIGINAL = "keyReadOnlyOriginal";
    private static final String KEY_SHOW_HIDDEN_COMPONENTS = "keyShowHiddenComponents";
    private static final String KEY_SHOW_TUTORIALS = "keyShowTutorials";

    private AveComponentSet mComponentSet;
    private AveComponentSet mComponentSetRollback;
    private boolean mReadOnlyOriginal;
    @Deprecated
    private boolean showHiddenComponents = false;
    private boolean showTutorials = true;

    private OnViewClickListener viewClickListener = new OnViewClickListener();
    private OnPopupChangeListener popupChangeListener = new OnPopupChangeListener();

    public AveFragment() {
        // Required empty public constructor
    }

    public static AveFragment newInstance(AveComponentSet componentSet) {
        return newInstance(componentSet, false);
    }

    public static AveFragment newInstance(AveComponentSet componentSet, boolean isInline) {
        componentSetCompatibilityCheck(componentSet);

        AveFragment fragment = new AveFragment();
        Bundle args = new Bundle();
        args.putBundle(KEY_COMPONENT_SET, componentSet.toBundle());
        args.putBoolean(KEY_READ_ONLY_ORIGINAL, componentSet.isReadOnly());
        args.putBoolean(KEY_IS_INLINE, isInline);
        fragment.setArguments(args);
        return fragment;
    }

    private static void componentSetCompatibilityCheck(AveComponentSet componentSet) {
        //Non null
        if (componentSet == null)
            throw new RuntimeException("Component set cannot be null");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReadOnlyOriginal = getArguments().getBoolean(KEY_READ_ONLY_ORIGINAL);
        showHiddenComponents = mReadOnlyOriginal;
        if (savedInstanceState == null) {
            mComponentSet = AveComponentSet.fromBundle(getArguments().getBundle(KEY_COMPONENT_SET));
        } else {
            mComponentSet = AveComponentSet.fromBundle(savedInstanceState.getBundle(KEY_COMPONENT_SET));
            Bundle rollbackBundle = savedInstanceState.getBundle(KEY_COMPONENT_SET_ROLLBACK);
            if (rollbackBundle != null)
                mComponentSetRollback = AveComponentSet.fromBundle(rollbackBundle);
            showHiddenComponents = savedInstanceState.getBoolean(KEY_SHOW_HIDDEN_COMPONENTS);
            showTutorials = savedInstanceState.getBoolean(KEY_SHOW_TUTORIALS);
        }

        getChildFragmentManager().addOnBackStackChangedListener(popupChangeListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_universal_ave, container, false);
        initializeViews(inflater, contentView);
        SQLiteDatabase readableDb = OpenHelper.getDatabase(getContext());
        updateViews(inflater, contentView, readableDb);
        return contentView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (mComponentSet != null) {
            BottomSheetUtil.expandOnShow(dialog);
        }
        return dialog;
    }

    //==================================== initializing Views ======================================
    private void initializeViews(LayoutInflater inflater, View contentView) {

        //Header Items
        {
            View headerRoot = contentView.findViewById(R.id.universal_ave_header_root);
            headerRoot.setBackgroundColor(Colors.getPrimaryColor(getContext()));
            if (getArguments().getBoolean(KEY_IS_INLINE)) {
                headerRoot.setVisibility(View.GONE);
            } else {
                ImageView dismissImageView = contentView.findViewById(R.id.universal_ave_dismiss);
                dismissImageView.setOnClickListener(viewClickListener);

                TextView saveTextView = contentView.findViewById(R.id.universal_ave_save);
                TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), saveTextView);

                saveTextView.setOnClickListener(viewClickListener);

                View menu = contentView.findViewById(R.id.universal_ave_menu);
                menu.setOnClickListener(viewClickListener);
            }
        }

        //Components
        {
            addComponentsViews(inflater, contentView);
        }

        //Cancel Popup
        {
            View emptyUp = contentView.findViewById(R.id.universal_ave_popup_empty);
            View emptyUp2 = contentView.findViewById(R.id.universal_ave_popup_empty_2);
            emptyUp.setOnClickListener(viewClickListener);
            if (emptyUp2 != null)
                emptyUp2.setOnClickListener(viewClickListener);
        }

        //Floating Action Bar:
        {
            FloatingActionButton fab = contentView.findViewById(R.id.fab);
            if (mComponentSet.isEditable) {
                fab.setOnClickListener(viewClickListener);
            }
        }
    }

    private void addComponentsViews(LayoutInflater inflater, View contentView) {
        ArrayList<AveComponentSet.Component> components = new ArrayList<>(mComponentSet.components.size() + 1);
        if (mComponentSet.headComponent != null) {
            if (getArguments().getBoolean(KEY_IS_INLINE)) {
                components.add(mComponentSet.headComponent);
            } else {
                LinearLayout headerContainer = contentView.findViewById(R.id.universal_ave_header_layout);
                addComponentViews(inflater, headerContainer, mComponentSet.headComponent, null);
            }
        }

        components.addAll(mComponentSet.components);
        LinearLayout container = contentView.findViewById(R.id.universal_ave_content_layout);
        for (int i = 0; i < components.size(); i++) {
            AveComponentSet.Component component = components.get(i);
            if (i != 0 && component.hasDivider) {
                View divider = inflater.inflate(R.layout.partial_ave_divider, container, false);
                divider.setTag(makeTagComponentDivider(component));
                container.addView(divider);
            }
            addComponentViews(inflater, container, component, i);
        }
        View divider = inflater.inflate(R.layout.partial_ave_divider, container, false);
        container.addView(divider);
    }

    private void addComponentViews(LayoutInflater inflater, ViewGroup container,
                                   AveComponentSet.Component component, @Nullable Integer componentIndex) {
        //Handling Partial View:
        {
            if (component instanceof AveComponentSet.Date) {
                View partialView = getViewForComponent(inflater, container, component, false);
                container.addView(partialView);

                //Time fragment preparation:
                {
                    AveComponentSet.Date dateComponent = (AveComponentSet.Date) component;

                    if (!dateComponent.removable && dateComponent.time) {
                        View timeFrame = partialView.findViewById(R.id.partial_ave_date_time_time_frame);
                        timeFrame.setId(component.componentId);
                        String tag = makeTagComponent(component);
                        Fragment childFragment = getChildFragmentManager().findFragmentByTag(tag);
                        if (childFragment == null) {
                            childFragment = CalendarTimeFragment.newInstance(
                                    new LocalTime(dateComponent.date, PersianChronology.getInstance()),
                                    tag,
                                    mComponentSet.isReadOnly()
                            );

                            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                            transaction.add(component.componentId, childFragment, tag);
                            transaction.commit();
                        }
                    }
                }
            } else {
                View partialView = getViewForComponent(inflater, container, component, componentIndex == null);
                container.addView(partialView);
            }
        }
    }

    private View getViewForComponent(LayoutInflater inflater, ViewGroup container,
                                     AveComponentSet.Component component, boolean isHeader) {
        if (component == null)
            throw new RuntimeException("Component should not be null");
        if (component instanceof AveComponentSet.LabelList) {
            View view = inflater.inflate(
                    isHeader ? R.layout.partial_ave_label_list_dark : R.layout.partial_ave_label_list, container, false);
            view.setTag(makeTagComponent(component));
            TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), view);

            TextView title = view.findViewById(R.id.partial_ave_label_list_text);
            title.setText(((AveComponentSet.LabelList) component).hintResId);
            View removeIcon = view.findViewById(R.id.partial_ave_label_list_remove);
            removeIcon.setTag(makeTagRemoveIcon(component));
            view.setOnClickListener(viewClickListener);
            removeIcon.setOnClickListener(viewClickListener);
            return view;
        } else if (component instanceof AveComponentSet.Amount) {
            View view = inflater.inflate(
                    isHeader ? R.layout.partial_ave_amount_dark : R.layout.partial_ave_amount, container, false);
            view.setTag(makeTagComponent(component));
            TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), view);

            EditText amountEditText = view.findViewById(R.id.partial_ave_amount_edit_text);
            TextView amountViewText = view.findViewById(R.id.partial_ave_amount_view_text);
            View popupAmount = view.findViewById(R.id.partial_ave_amount_popup);
            amountEditText.setHint(R.string.universal_amount_hint);
            amountEditText.addTextChangedListener(new EditableComponentsTextWatcher(component));
            popupAmount.setTag(makeTagComponent(component));
            popupAmount.setOnClickListener(viewClickListener);

            if (!component.hasIcon) {
                View icon = view.findViewById(R.id.partial_ave_amount_image);
                icon.setVisibility(View.INVISIBLE);
            }

            return view;
        } else if (component instanceof AveComponentSet.Date) {
            View view;
            if (((AveComponentSet.Date) component).removable) {
                view = inflater.inflate(
                        isHeader ? R.layout.partial_ave_date_text_dark : R.layout.partial_ave_date_text, container, false);
                view.setOnClickListener(viewClickListener);

                View removeView = view.findViewById(R.id.partial_ave_date_text_remove);
                removeView.setTag(makeTagRemoveIcon(component));
                removeView.setOnClickListener(viewClickListener);

                if (!component.hasIcon) {
                    View icon = view.findViewById(R.id.partial_ave_date_text_image);
                    icon.setVisibility(View.INVISIBLE);
                }
            } else {
                view = inflater.inflate(
                        isHeader ? R.layout.partial_ave_date_time_dark : R.layout.partial_ave_date_time, container, false);

                View selectableView = view.findViewById(R.id.partial_ave_date_time_date_text);
                selectableView.setTag(makeTagComponent(component));
                selectableView.setOnClickListener(viewClickListener);

                if (!component.hasIcon) {
                    View icon = view.findViewById(R.id.partial_ave_date_time_image);
                    icon.setVisibility(View.INVISIBLE);
                }
            }
            view.setTag(makeTagComponent(component));
            return view;
        } else if (component instanceof AveComponentSet.Text) {
            AveComponentSet.Text textComponent = (AveComponentSet.Text) component;

            View view;
            if (textComponent.maxLines == null) {
                view = inflater.inflate(
                        isHeader ? R.layout.partial_ave_text_dark : R.layout.partial_ave_text, container, false);
            } else {
                view = inflater.inflate(
                        isHeader ? R.layout.partial_ave_text_max_line_dark : R.layout.partial_ave_text_max_line, container, false);
            }

            view.setTag(makeTagComponent(component));

            EditText editText = view.findViewById(R.id.partial_ave_text_edit_text);
            TextView viewText = view.findViewById(R.id.partial_ave_text_view_text);
            TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), editText);
            TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), viewText);
            editText.addTextChangedListener(new EditableComponentsTextWatcher(component));
            if (textComponent.hint != null)
                editText.setHint(textComponent.hint);
            else if (textComponent.hintResId != null)
                editText.setHint(textComponent.hintResId);
            if (textComponent.maxLines != null) {
                editText.setMaxLines(textComponent.maxLines);
            }
            if (!component.hasIcon) {
                View icon = view.findViewById(R.id.partial_ave_text_image);
                icon.setVisibility(View.INVISIBLE);
            }
            return view;
        } else if (component instanceof AveComponentSet.Color) {
            View view = inflater.inflate(
                    isHeader ? R.layout.partial_ave_color_dark : R.layout.partial_ave_color, container, false);
            view.setTag(makeTagComponent(component));
            view.setOnClickListener(viewClickListener);

            if (!component.hasIcon) {
                View icon = view.findViewById(R.id.partial_ave_color_image);
                icon.setVisibility(View.INVISIBLE);
            }
            return view;
        } else if (component instanceof AveComponentSet.Number) {
            View view = inflater.inflate(
                    isHeader ? R.layout.partial_ave_number_dark : R.layout.partial_ave_number, container, false);
            view.setTag(makeTagComponent(component));

            AveComponentSet.Number numberComponent = (AveComponentSet.Number) component;
            EditText editText = view.findViewById(R.id.partial_ave_number_edit_text);
            TextView viewText = view.findViewById(R.id.partial_ave_number_view_text);
            TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), editText);
            TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), viewText);
            editText.addTextChangedListener(new EditableComponentsTextWatcher(component));
            if (numberComponent.hint != null)
                editText.setHint(numberComponent.hint);
            else if (numberComponent.hintResId != null)
                editText.setHint(numberComponent.hintResId);

            if (!component.hasIcon) {
                View icon = view.findViewById(R.id.partial_ave_number_image);
                icon.setVisibility(View.INVISIBLE);
            }
            return view;
        } else if (component instanceof AveComponentSet.Property) {
            View view = inflater.inflate(
                    isHeader ? R.layout.partial_ave_property_dark : R.layout.partial_ave_property, container, false);
            view.setTag(makeTagComponent(component));

            AveComponentSet.Property property = (AveComponentSet.Property) component;
            Switch propertySwitch = view.findViewById(R.id.partial_ave_property_switch);
            propertySwitch.setOnCheckedChangeListener(new OnPropertyChanged(property));
            TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), propertySwitch);
            if (property.text != null)
                propertySwitch.setText(property.text);
            else if (property.textResId != 0)
                propertySwitch.setText(property.textResId);
            else
                throw new RuntimeException("Property component should specify a text to be shown to the user");

            ImageView icon = view.findViewById(R.id.partial_ave_property_image);
            if (!component.hasIcon) {
                icon.setVisibility(View.INVISIBLE);
            } else {
                icon.setImageResource(((AveComponentSet.Property) component).iconResId);
                icon.setVisibility(View.VISIBLE);
            }
            return view;
        } else if (component instanceof AveComponentSet.Choice) {
            View view = inflater.inflate(
                    isHeader ? R.layout.partial_ave_choice_dark : R.layout.partial_ave_choice, container, false);
            view.setTag(makeTagComponent(component));

            AveComponentSet.Choice choice = (AveComponentSet.Choice) component;
            Spinner spinner = view.findViewById(R.id.partial_ave_choice_text);
            view.setTag(makeTagComponent(component));
            spinner.setAdapter(new ChoiceAdapter(choice));
            spinner.setOnItemSelectedListener(new OnChoiceItemSelectedListener(choice));

            ImageView icon = view.findViewById(R.id.partial_ave_choice_image);
            if (!component.hasIcon) {
                icon.setVisibility(View.INVISIBLE);
            } else {
                icon.setImageResource(((AveComponentSet.Choice) component).iconResId);
                icon.setVisibility(View.VISIBLE);
            }
            return view;
        } else if (component instanceof AveComponentSet.DatePeriod) {
            View view = inflater.inflate(
                    isHeader ? R.layout.partial_ave_date_text_dark : R.layout.partial_ave_date_text,
                    container, false);
            view.setTag(makeTagComponent(component));
            TextView textView = view.findViewById(R.id.partial_ave_date_text_text);
            TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), textView);
            view.setTag(makeTagComponent(component));
            view.setOnClickListener(viewClickListener);
            if (!component.hasIcon) {
                View icon = view.findViewById(R.id.partial_ave_date_text_image);
                icon.setVisibility(View.INVISIBLE);
            }
            return view;
        } else
            throw new RuntimeException("Given component wasn't recognized for creating the view.");
    }

    //======================================= Update Views =========================================
    private void tryUpdateViews(SQLiteDatabase readableDb) {
        View view = getView();
        Context context = getContext();
        if (context == null) return;
        LayoutInflater inflater = LayoutInflater.from(context);
        if (view != null && inflater != null)
            updateViews(inflater, view, readableDb);
    }

    private void updateViews(LayoutInflater inflater, View contentView, SQLiteDatabase readableDb) {
        updateVisibilities(contentView);
        updateContents(contentView, readableDb);
        updateEditability(contentView);
        updateViewComponents(inflater, contentView);
    }

    //=============== Visibility
    private boolean shouldComponentHide(AveComponentSet.Component component) {
        boolean shouldHide = false;

        //Component Value Check:
        {
            shouldHide = shouldHide || mComponentSet.isReadOnly() && !component.hasValue();
        }

        //Constraint Check
        {
            if (component != mComponentSet.headComponent) {
                Integer componentIndex = mComponentSet.getComponentIndex(component);
                for (AveComponentSet.Constraint constraint : mComponentSet.constraints) {
                    if (constraint instanceof AveComponentSet.HideConstraint) {
                        AveComponentSet.HideConstraint hideConstraint = (AveComponentSet.HideConstraint) constraint;
                        shouldHide = shouldHide || constraint.doesConstraint(componentIndex) &&
                                hideConstraint.shouldHide.shouldHide(mComponentSet);
                    }
                }
            }
        }

        return shouldHide;
    }

    private void tryUpdateVisibilities() {
        View contentView = getView();
        if (contentView != null)
            updateVisibilities(contentView);
    }

    private void updateVisibilities(View contentView) {
        //Controls Visibility
        {
            updateControlsVisibility(contentView);
        }

        //Components
        {
            updateComponentsVisibility(contentView);
        }
    }

    private void updateControlsVisibility(View contentView) {
        //Popup visibility
        {
            View popupRootView = contentView.findViewById(R.id.universal_ave_popup_layout_root);
            if (getChildFragmentManager().getBackStackEntryCount() > 0) {
                popupRootView.setVisibility(View.VISIBLE);
            } else {
                popupRootView.setVisibility(View.GONE);
            }
        }

        //Remove Icon Visibility
        {
            for (AveComponentSet.Component component : mComponentSet.components) {
                View removeIcon = contentView.findViewWithTag(makeTagRemoveIcon(component));
                if (removeIcon != null) {
                    if (!mComponentSet.isReadOnly() && component.hasValue())
                        removeIcon.setVisibility(View.VISIBLE);
                    else
                        removeIcon.setVisibility(View.GONE);
                }
            }
        }

        //App bar
        {
            View menu = contentView.findViewById(R.id.universal_ave_menu);
            View save = contentView.findViewById(R.id.universal_ave_save);
            if (mComponentSet.isReadOnly()) {
                menu.setVisibility(View.VISIBLE);
                save.setVisibility(View.GONE);
            } else {
                menu.setVisibility(View.GONE);
                save.setVisibility(View.VISIBLE);
                save.setEnabled(mComponentSet.isComponentSetComplete());
            }
        }

        //Floating Action Bar
        {
            FloatingActionButton fab = contentView.findViewById(R.id.fab);
            if (!mComponentSet.isEditable) {
                fab.hide();
            } else if (mComponentSet.isReadOnly()) {//for entering edit mode
                fab.show();
                fab.setImageResource(R.drawable.ic_edit_white);
                //} else if (mComponentSet.hasHiddenComponents() && getChildFragmentManager().getBackStackEntryCount() == 0) { //for viewing hidden components
                //    fab.show();
                //    if (showHiddenComponents)
                //        fab.setImageResource(R.drawable.ic_collapse_white);
                //    else
                //        fab.setImageResource(R.drawable.ic_expand_white);
            } else {
                fab.hide();
            }
        }
    }

    private void updateComponentsVisibility(View contentView) {
        ArrayList<AveComponentSet.Component> components = mComponentSet.getAllComponents();
        for (AveComponentSet.Component component : components) {
            boolean shouldHide = shouldComponentHide(component);
            {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                componentView.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
            }
            View dividerView = contentView.findViewWithTag(makeTagComponentDivider(component));
            if (dividerView != null) {
                dividerView.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
            }
        }
    }

    //=============== Content
    private void updateContents(View contentView, SQLiteDatabase readableDb) {
        //Components:
        {
            ArrayList<AveComponentSet.Component> components = mComponentSet.getAllComponents();
            for (AveComponentSet.Component component : components) {
                if (component instanceof AveComponentSet.LabelList) {
                    AveComponentSet.LabelList labelList = (AveComponentSet.LabelList) component;
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    TextView titleTextView = componentView.findViewById(R.id.partial_ave_label_list_text);
                    if (labelList.labelListId == null) {
                        titleTextView.setText(labelList.hintResId);
                    } else {
                        titleTextView.setText(LabelListCatalog.getLabelListById(
                                labelList.labelListId, readableDb).getTitle());
                    }
                } else if (component instanceof AveComponentSet.Color) {
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    TextView titleTextView = componentView.findViewById(R.id.partial_ave_color_text);
                    AveComponentSet.Color colorComponent = (AveComponentSet.Color) component;
                    titleTextView.setText(ListUtil.Color.getTextForColor(
                            getResources(), colorComponent.color));
                    TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), titleTextView);

                    ImageView imageView = componentView.findViewById(R.id.partial_ave_color_image);
                    imageView.setColorFilter(colorComponent.color, PorterDuff.Mode.MULTIPLY);
                } else if (component instanceof AveComponentSet.Date) {
                    AveComponentSet.Date dateComponent = (AveComponentSet.Date) component;
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    if (dateComponent.removable) {
                        TextView dateTextView = componentView.findViewById(R.id.partial_ave_date_text_text);
                        if (dateComponent.hasValue() && dateComponent.time) {
                            dateTextView.setText(DateTimeFormat.fullDateTime(getContext()).
                                    print(dateComponent.date));
                        } else if (dateComponent.hasValue() && !dateComponent.time) {
                            dateTextView.setText(DateTimeFormat.fullDate(getResources()).
                                    print(dateComponent.date));
                        } else if (dateComponent.hint != null) {
                            dateTextView.setText(dateComponent.hint);
                        } else if (dateComponent.hintResId != null) {
                            dateTextView.setText(dateComponent.hintResId);
                        } else {
                            dateTextView.setText(R.string.universal_datetime);
                        }
                    } else {
                        TextView dateTextView = componentView.findViewById(R.id.partial_ave_date_time_date_text);
                        dateTextView.setText(DateTimeFormat.fullDate(getResources()).
                                print(dateComponent.date));
                        //Time is updated by its own fragment
                    }
                    if (!dateComponent.removable) {
                        TextView description = componentView.findViewById(R.id.partial_ave_date_time_description);

                        if (dateComponent.hint != null) {
                            description.setText(dateComponent.hint);
                            description.setVisibility(View.VISIBLE);
                        } else if (dateComponent.hintResId != null) {
                            description.setText(dateComponent.hintResId);
                            description.setVisibility(View.VISIBLE);
                        } else {
                            description.setVisibility(View.GONE);
                        }
                    }

                    TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), componentView);
                } else if (component instanceof AveComponentSet.DatePeriod) {
                    AveComponentSet.DatePeriod datePeriod = (AveComponentSet.DatePeriod) component;
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    TextView dateTextView = componentView.findViewById(R.id.partial_ave_date_text_text);
                    DateTimeFormatter formatter = DateTimeFormat.fullDate(getResources());
                    String text = formatter.print(datePeriod.dateFrom) + " " +
                            getString(R.string.partial_until) + " " + formatter.print(datePeriod.dateTo);
                    dateTextView.setText(text);
                } else if (component instanceof AveComponentSet.Amount) {
                    AveComponentSet.Amount componentAmount = (AveComponentSet.Amount) component;
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    EditText editText = componentView.findViewById(R.id.partial_ave_amount_edit_text);
                    TextView viewText = componentView.findViewById(R.id.partial_ave_amount_view_text);
                    View viewPopup = componentView.findViewById(R.id.partial_ave_amount_popup);
                    if (Numbers.isSmall(componentAmount.amount) && !mComponentSet.isReadOnly()) {
                        editText.setText(null);
                        editText.setHint(componentAmount.hintResId);
                        viewText.setText(componentAmount.hintResId);
                    } else {
                        editText.setText(EnglishTypeFace.withEnglishAmountFormat(componentAmount.amount));
                        viewText.setText(EnglishTypeFace.withEnglishAmountFormat(componentAmount.amount));
                    }
                    if (mComponentSet.isReadOnly()) {
                        viewPopup.setVisibility(View.GONE);
                    } else {
                        viewPopup.setVisibility(View.VISIBLE);
                    }
                } else if (component instanceof AveComponentSet.Number) {
                    AveComponentSet.Number componentNumber = (AveComponentSet.Number) component;
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    EditText editText = componentView.findViewById(R.id.partial_ave_number_edit_text);
                    TextView viewText = componentView.findViewById(R.id.partial_ave_number_view_text);
                    editText.setText(componentNumber.number);
                    viewText.setText(componentNumber.number);
                } else if (component instanceof AveComponentSet.Text) {
                    AveComponentSet.Text componentText = (AveComponentSet.Text) component;
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    EditText editText = componentView.findViewById(R.id.partial_ave_text_edit_text);
                    TextView viewText = componentView.findViewById(R.id.partial_ave_text_view_text);
                    editText.setText(componentText.text);
                    viewText.setText(componentText.text);
                } else if (component instanceof AveComponentSet.Property) {
                    AveComponentSet.Property componentProperty = (AveComponentSet.Property) component;
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    Switch componentSwitch = componentView.findViewById(R.id.partial_ave_property_switch);
                    componentSwitch.setChecked(componentProperty.value);
                } else if (component instanceof AveComponentSet.Choice) {
                    AveComponentSet.Choice componentChoice = (AveComponentSet.Choice) component;
                    View componentView = contentView.findViewWithTag(makeTagComponent(component));
                    Spinner spinner = componentView.findViewById(R.id.partial_ave_choice_text);
                    spinner.setSelection(componentChoice.currentPosition);
                }
            }
        }

        //App bar:
        {
            ImageView dismissImageView = contentView.findViewById(R.id.universal_ave_dismiss);
            if (mComponentSet.isReadOnly()) {
                dismissImageView.setImageResource(R.drawable.ic_back_white);
            } else {
                dismissImageView.setImageResource(R.drawable.ic_dismiss_white);
            }
        }
    }

    //=============== Editability
    private void updateEditability(View contentView) {
        boolean readOnly = mComponentSet.isReadOnly();
        ArrayList<AveComponentSet.Component> components = mComponentSet.getAllComponents();
        for (AveComponentSet.Component component : components) {
            if (component instanceof AveComponentSet.LabelList) {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                if (readOnly) {
                    componentView.setClickable(false);
                } else {
                    componentView.setClickable(true);
                }
            } else if (component instanceof AveComponentSet.Amount) {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                EditText editText = componentView.findViewById(R.id.partial_ave_amount_edit_text);
                TextView viewText = componentView.findViewById(R.id.partial_ave_amount_view_text);
                if (readOnly) {
                    editText.setVisibility(View.GONE);
                    viewText.setVisibility(View.VISIBLE);
                } else {
                    editText.setVisibility(View.VISIBLE);
                    viewText.setVisibility(View.GONE);
                }
            } else if (component instanceof AveComponentSet.Number) {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                EditText editText = componentView.findViewById(R.id.partial_ave_number_edit_text);
                TextView viewText = componentView.findViewById(R.id.partial_ave_number_view_text);
                if (readOnly) {
                    editText.setVisibility(View.GONE);
                    viewText.setVisibility(View.VISIBLE);
                } else {
                    editText.setVisibility(View.VISIBLE);
                    viewText.setVisibility(View.GONE);
                }
            } else if (component instanceof AveComponentSet.Text) {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                EditText editText = componentView.findViewById(R.id.partial_ave_text_edit_text);
                TextView viewText = componentView.findViewById(R.id.partial_ave_text_view_text);
                if (readOnly) {
                    editText.setVisibility(View.GONE);
                    viewText.setVisibility(View.VISIBLE);
                } else {
                    editText.setVisibility(View.VISIBLE);
                    viewText.setVisibility(View.GONE);
                }
            } else if (component instanceof AveComponentSet.Color ||
                    component instanceof AveComponentSet.DatePeriod) {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                if (readOnly) {
                    componentView.setClickable(false);
                } else {
                    componentView.setClickable(true);
                }
            } else if (component instanceof AveComponentSet.Property) {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                Switch propertySwitch = componentView.findViewById(R.id.partial_ave_property_switch);
                if (readOnly) {
                    propertySwitch.setClickable(false);
                } else {
                    propertySwitch.setClickable(true);
                }
            } else if (component instanceof AveComponentSet.Date) {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                if (((AveComponentSet.Date) component).removable) {
                    if (readOnly) {
                        componentView.setClickable(false);
                    } else {
                        componentView.setClickable(true);
                    }
                } else {
                    View dateView = componentView.findViewById(R.id.partial_ave_date_time_date_text);
                    //Time is handled by its fragment
                    if (readOnly) {
                        dateView.setClickable(false);
                    } else {
                        dateView.setClickable(true);
                    }
                }
            } else if (component instanceof AveComponentSet.Choice) {
                View componentView = contentView.findViewWithTag(makeTagComponent(component));
                Spinner spinner = componentView.findViewById(R.id.partial_ave_choice_text);
                if (readOnly) {
                    spinner.setEnabled(false);
                } else {
                    spinner.setEnabled(true);
                }
            }
        }
    }

    //=============== View Components
    private void updateViewComponents(LayoutInflater inflater, View contentView) {
        if (mComponentSet.hasViewComponents && mComponentSet.isReadOnly()) {
            ArrayList<AveComponentSet.ViewComponent> viewComponents = getViewComponents();
            LinearLayout container = contentView.findViewById(R.id.universal_ave_view_components_layout);
            container.removeAllViews();
            for (AveComponentSet.ViewComponent component : viewComponents) {
                View componentView;
                if (component instanceof AveComponentSet.ViewText) {
                    AveComponentSet.ViewText viewText = (AveComponentSet.ViewText) component;
                    componentView = inflater.inflate(R.layout.partial_ave_view_text, container, false);
                    TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), componentView);
                    TextView text = componentView.findViewById(R.id.partial_ave_view_text_text);
                    if (viewText.text != null)
                        text.setText(viewText.text);
                    else if (viewText.textResId != null)
                        text.setText(viewText.textResId);
                    else
                        throw new RuntimeException("Both text and textResId are null");

                    TextView value = componentView.findViewById(R.id.partial_ave_view_text_value);
                    value.setText(viewText.value);
                    if (viewText.hasIcon) {
                        ImageView icon = componentView.findViewById(R.id.partial_ave_view_text_image);
                        icon.setImageResource(viewText.iconResId);
                    }
                } else
                    throw new RuntimeException("Given view component was not recognized");

                container.addView(componentView);
            }
            container.setVisibility(View.VISIBLE);
        } else {
            LinearLayout container = contentView.findViewById(R.id.universal_ave_view_components_layout);
            container.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(KEY_COMPONENT_SET, mComponentSet.toBundle());
        if (mComponentSetRollback != null)
            outState.putBundle(KEY_COMPONENT_SET_ROLLBACK, mComponentSetRollback.toBundle());
        outState.putBoolean(KEY_SHOW_HIDDEN_COMPONENTS, showHiddenComponents);
        outState.putBoolean(KEY_SHOW_TUTORIALS, showTutorials);
    }

    //================================== Communication Children ====================================

    //=========== All Children: AveNewContent

    private void aveNewContent() {
        ArrayList<AveComponentSet.Component> components = mComponentSet.getAllComponents();
        for (AveComponentSet.Component component : components) {
            if (component instanceof AveComponentSet.Date) {
                Fragment fragment = getChildFragmentManager().findFragmentByTag(makeTagComponent(component));
                if ((fragment == null || !(fragment instanceof CalendarTimeFragment)) && ((AveComponentSet.Date) component).time)
                    throw new RuntimeException("Fragment for date component is missing or is manipulated");
                if (fragment != null) {
                    ((CalendarTimeFragment) fragment).aveNewContent(new LocalTime(
                            ((AveComponentSet.Date) component).date,
                            PersianChronology.getInstance())
                    );
                }
            }
        }
    }

    //=========== Universal List : Single Select
    @Override
    public ListData retrieveUniversalListData(Context context, String tag, Bundle identifier) {
        if (tag == null) {
            throw new RuntimeException("tag is null, something most have gone wrong");
        }

        int componentCode = getComponentCodeByTag(tag);
        if (componentCode == AveComponentSet.Component.COMPONENT_COLOR) {
            return ListUtil.Color.create(context.getResources());
        } else if (componentCode == AveComponentSet.Component.COMPONENT_LABEL_LIST) {
            return ListUtil.LabelList.createTree(OpenHelper.getDatabase(getContext()), false);
        } else
            throw new RuntimeException("Still not implemented");
    }

    @Override
    public void onUniversalListPositionsChanged(ListData data, ListData.Entity rootEntity, String tag, Bundle identifier) {
        //Action not supported here
    }

    @Override
    public void onUniversalListItemSelected(ListData.Entity entity, String popupTag, Bundle identifier) {
        AveComponentSet.Component popupReference = getComponentByTagPopup(popupTag);
        if (popupReference instanceof AveComponentSet.Color) {
            if (entity.isValid()) {
                ((AveComponentSet.Color) popupReference).color =
                        ListUtil.Color.getColorByEntity(entity);
                SQLiteDatabase readableDb = OpenHelper.getDatabase(getContext());
                tryUpdateViews(readableDb);
            }
            getChildFragmentManager().popBackStack();
        } else if (popupReference instanceof AveComponentSet.LabelList) {
            if (entity.isValid()) {
                AveComponentSet.LabelList labelList = (AveComponentSet.LabelList) popupReference;
                labelList.labelListId = entity.getId();
                tryUpdateViews(OpenHelper.getDatabase(getContext()));
            }
            getChildFragmentManager().popBackStack();
        }
    }

    @Override
    public void onUniversalListActionClicked(ListData.InfoRow infoRow, View view, String tag, Bundle identifier) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void onUniversalListDeliverViewInfo(ListFragment.ListViewInfo viewInfo, String tag, Bundle identifier) {

    }

    @Override
    public void onUniversalListItemMoreSelected(ListData.Entity entity, View moreView, String tag, Bundle identifier) {

    }

    //=========== Universal List : Multi-Select
    @Override
    public void onUniversalListItemsSelected(ArrayList<Long> ids, String popupTag, Bundle identifier) {
        AveComponentSet.Component popupReference = getComponentByTagPopup(popupTag);
        //Nothing here
    }

    @Override
    public void universalListDismissRequest(String tag, Bundle identifier) {
        getChildFragmentManager().popBackStack();
    }

    @Override
    public void onUniversalListMarkedItemsChanged(ArrayList<Long> ids, String tag, Bundle identifier) {
        //Nothing here
    }

    //============ UniversalAutoComplete
    @Override
    public ListData retrieveAutoCompleteListData(String tag, Bundle component) {
        int componentCode = getComponentCodeByTag(tag);
        //
        return null;
    }

    @Override
    public ListData retrieveAutoCompleteFullListData(String tag, Bundle component) {
        int componentCode = getComponentCodeByTag(tag);
        //
        return null;
    }

    @Override
    public void onAutoCompleteContentChanged(ArrayList<ListData.Entity> entities, String tag, Bundle identifier) {
        ArrayList<ListData.Entity> newEntities = new ArrayList<>(entities);
        AveComponentSet.Component component = getComponentByTag(tag);
        //
        tryUpdateVisibilities();
    }

    @Override
    public void onAutoCompleteNewItemRequest(String input, StringTransformer transformer, String tag, Bundle identifier) {
        AveComponentSet.Component component = getComponentByTag(tag);
        SQLiteDatabase database = OpenHelper.getDatabase(getContext());
        //
        aveNewContent();
        tryUpdateVisibilities();
    }

    @Override
    public void requestExplicitChoose(String tag, Bundle identifier) {
        AveComponentSet.Component component = getComponentByTag(tag);
        //
        tryUpdateVisibilities();
    }

    //========== Amount Input

    @Override
    public void onAmountInputFragmentDone(Double amount, String tag, Bundle identifier) {
        AveComponentSet.Component component = getComponentByTagPopup(tag);
        AveComponentSet.Amount amountComponent = (AveComponentSet.Amount) component;
        amountComponent.amount = amount == null ? 0 : amount;
        tryUpdateViews(OpenHelper.getDatabase(getContext()));
    }

    //========== Calendar Time
    @Override
    public void onTimeSelected(LocalTime time, String tag) {
        AveComponentSet.Component component = getComponentByTag(tag);
        if (component != null) {
            if (component instanceof AveComponentSet.Date) {
                AveComponentSet.Date dateComponent = (AveComponentSet.Date) component;
                long dateMillis = dateComponent.date;
                DateTime dateTime = new DateTime(dateMillis, PersianChronology.getInstance());
                dateTime = dateTime.withTime(time);
                dateComponent.date = dateTime.getMillis();
            } else
                throw new RuntimeException("Component is supposed to be an instance of " +
                        "UniversalAveComponentSet.Date, something must have gone wrong");
        }
        tryUpdateVisibilities();
    }

    //=========== Calendar

    @Override
    public void onDateSelected(CalendarFragment.DateState dateState, String popupTag, Bundle identifier) {
        AveComponentSet.Component popupReference = getComponentByTagPopup(popupTag);
        String tag = makeTagComponent(popupReference);
        if (popupReference instanceof AveComponentSet.Date) {
            AveComponentSet.Date dateComponent = ((AveComponentSet.Date) popupReference);
            DateTime dateTime = new DateTime(dateComponent.date, PersianChronology.getInstance());
            dateComponent.date = dateTime.withDate(dateState.currentDate).getMillis();
            if (dateComponent.removable) {
                dateComponent.hasValue = true;
            }

            SQLiteDatabase readableDb = OpenHelper.getDatabase(getContext());
            tryUpdateViews(readableDb);
        } else if (popupReference instanceof AveComponentSet.DatePeriod) {
            AveComponentSet.DatePeriod datePeriod = (AveComponentSet.DatePeriod) popupReference;
            datePeriod.dateFrom = dateState.from.toDateTimeAtStartOfDay().getMillis();
            datePeriod.dateTo = dateState.to.plusDays(1).toDateTimeAtStartOfDay().getMillis() - 1;
            SQLiteDatabase readableDb = OpenHelper.getDatabase(getContext());
            tryUpdateViews(readableDb);
        } else
            throw new RuntimeException("popupReference doesn't match, something must have gone wrong");

        getChildFragmentManager().popBackStack();
    }

    //========== DateTime Chooser

    @Override
    public void onDateTimeChooserResult(DateTime dateTime, String popupTag, Bundle identifier) {
        AveComponentSet.Component popupReference = getComponentByTagPopup(popupTag);
        String tag = makeTagComponent(popupReference);
        if (popupReference instanceof AveComponentSet.Date) {
            AveComponentSet.Date dateComponent = (AveComponentSet.Date) popupReference;
            dateComponent.hasValue = true;
            dateComponent.date = dateTime.getMillis();
            tryUpdateViews(OpenHelper.getDatabase(getContext()));
        }
        getChildFragmentManager().popBackStack();
    }

    //=================================== Communication Parent =====================================
    public AveComponentSet getCurrentComponentSet() {
        return mComponentSet;
    }

    private boolean onSaveResult() {
        if (mComponentSet.isComponentSetComplete()) {
            getListenerParent().onSaveResult(mComponentSet);
            return true;
        } else {
            return false;
        }
    }

    private void onEditPressed() {
        boolean result = getListenerParent().onEditPressed(mComponentSet);
        if (result) {
            onFinish(false);
        } else {
            BottomSheetUtil.expand(getDialog());
            onStateChanged(AveComponentSet.STATE_EDIT, false);
            mComponentSetRollback = mComponentSet.clone();
        }
    }

    private void onFinish(boolean success) {
        getListenerParent().onFinish(success);
    }

    private void onMenuItemClicked(int itemId) {
        getListenerParent().onMenuClicked(itemId, mComponentSet);
    }

    private ArrayList<AveComponentSet.ViewComponent> getViewComponents() {
        if (!mComponentSet.hasViewComponents)
            throw new RuntimeException("given componentSet does not support viewComponents");
        return getListenerParent().getViewComponents(mComponentSet);
    }

    private OnFragmentInteractionListener getListenerParent() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            return (OnFragmentInteractionListener) getParentFragment();
        else if (getActivity() instanceof OnFragmentInteractionListener)
            return (OnFragmentInteractionListener) getActivity();
        else
            throw new RuntimeException("Either parent fragment or activity must implement " +
                    "OnFragmentInteractionListener");
    }

    public interface OnFragmentInteractionListener {
        void onSaveResult(AveComponentSet componentSet);

        /*
        if true is returned, ave goes to edit mode
         */
        boolean onEditPressed(AveComponentSet componentSet);

        void onFinish(boolean success);

        void onMenuClicked(int itemId, AveComponentSet componentSet);

        ArrayList<AveComponentSet.ViewComponent> getViewComponents(AveComponentSet componentSet);
    }

    //======================================= User Interaction =====================================

    //==================== Controls
    private void onSaveClicked() {
        if (mReadOnlyOriginal) {
            boolean result = onSaveResult();
            if (result) {
                onStateChanged(AveComponentSet.STATE_VIEW, false);
                mComponentSetRollback = null;
                Keyboard.hide(getActivity(), this);
            }
        } else {
            boolean result = onSaveResult();
            if (result) {
                Keyboard.hide(getActivity(), this);
                onFinish(true);
            }
        }
    }

    private void onFabClicked() {
        if (mComponentSet.isReadOnly()) {
            onEditPressed();
        } else {//show/hide hidden components
            showHiddenComponents = !showHiddenComponents;
            updateVisibilities(getView());
        }
    }

    private void onMenuButtonClicked(View contentView) {
        View menuView = contentView.findViewById(R.id.universal_ave_menu);
        PopupMenu popupMenu = new PopupMenu(getContext(), menuView);
        popupMenu.getMenuInflater().inflate(mComponentSet.menuResId, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new OnPopupMenuItemClicked());
        popupMenu.show();
    }

    private class OnPopupMenuItemClicked implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AveFragment.this.onMenuItemClicked(item.getItemId());
            return true;
        }
    }

    private void onNegativeActionClicked() {
        Keyboard.hide(getActivity(), this);
        if (mReadOnlyOriginal && !mComponentSet.isReadOnly()) {
            mComponentSet.copyFrom(mComponentSetRollback);
            mComponentSetRollback = null;
            onStateChanged(AveComponentSet.STATE_VIEW, true);
        } else {
            onFinish(false);
        }
    }

    //==================== State
    private void onStateChanged(int state, boolean rollback) {
        if (state != mComponentSet.state) {
            mComponentSet.state = state;
            SQLiteDatabase readableDb = OpenHelper.getDatabase(getContext());
            tryUpdateViews(readableDb);

            List<Fragment> fragments = getChildFragmentManager().getFragments();
            if (fragments != null)
                for (Fragment fragment : fragments) {
                    if (fragment instanceof AveContentChangeListener)
                        ((AveContentChangeListener) fragment).aveStateChanged(mComponentSet.isReadOnly());
                }

            if (rollback)
                aveNewContent();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        } else if (mReadOnlyOriginal && !mComponentSet.isReadOnly()) {
            onNegativeActionClicked();
            return true;
        } else
            return false;
    }

    //==================== Components
    private void onComponentClicked(AveComponentSet.Component component) {
        if (component instanceof AveComponentSet.Color ||
                component instanceof AveComponentSet.LabelList) {
            ListFragment fragment = ListFragment.newInstance(makeTagPopupComponent(component), component.toBundle());
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.universal_ave_popup_layout_content, fragment, makeTagPopupComponent(component));
            transaction.addToBackStack(makeTagComponent(component));
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        } else if (component instanceof AveComponentSet.Date) {
            AveComponentSet.Date dateComponent = (AveComponentSet.Date) component;
            DateTime initialInstant = new DateTime(
                    dateComponent.hasValue() ? dateComponent.date : System.currentTimeMillis(),
                    PersianChronology.getInstance());
            LocalDate minDate = dateComponent.minDate == null ? null : new LocalDate(dateComponent.minDate, PersianChronology.getInstance());
            LocalDate maxDate = dateComponent.maxDate == null ? null : new LocalDate(dateComponent.maxDate, PersianChronology.getInstance());

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            Fragment fragment;
            if (dateComponent.removable && dateComponent.time) {
                fragment = DateTimeChooserFragment.newInstance(initialInstant, minDate, maxDate,
                        makeTagPopupComponent(component), null);
            } else {
                CalendarFragment.DateState dateState = new CalendarFragment.DateState();
                dateState.currentDate = initialInstant.toLocalDate();
                fragment = CalendarFragment.newInstance(dateState, minDate, maxDate,
                        makeTagPopupComponent(component), null, getContext());
            }
            transaction.add(R.id.universal_ave_popup_layout_content, fragment, makeTagPopupComponent(component));
            transaction.addToBackStack(makeTagComponent(component));
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        } else if (component instanceof AveComponentSet.DatePeriod) {
            AveComponentSet.DatePeriod datePeriod = (AveComponentSet.DatePeriod) component;
            CalendarFragment.DateState dateState = new CalendarFragment.DateState();
            dateState.isPeriod = true;
            dateState.from = new LocalDate(datePeriod.dateFrom, PersianChronology.getInstance());
            dateState.to = new LocalDate(datePeriod.dateTo, PersianChronology.getInstance());
            dateState.type = datePeriod.toDateStateType();
            CalendarFragment fragment = CalendarFragment.newInstance(dateState, makeTagPopupComponent(component), null, getContext());
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.universal_ave_popup_layout_content, fragment, makeTagPopupComponent(component));
            transaction.addToBackStack(makeTagComponent(component));
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        } else if (component instanceof AveComponentSet.Amount) {
            BottomSheetDialogFragment fragment = AmountInputFragment.newInstance(
                    ((AveComponentSet.Amount) component).amount,
                    makeTagPopupComponent(component), component.toBundle());
            fragment.show(getChildFragmentManager(), makeTagComponent(component));
        } else
            throw new RuntimeException("Operation is not implemented");
    }

    private void onRemoveComponentClicked(AveComponentSet.Component component) {
        component.removeValue();
        SQLiteDatabase readableDb = OpenHelper.getDatabase(getContext());
        tryUpdateViews(readableDb);
    }

    private class OnViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null) {
                ArrayList<AveComponentSet.Component> components = mComponentSet.getAllComponents();
                for (AveComponentSet.Component component : components) {
                    if (view.getTag().equals(makeTagComponent(component)) ||
                            view.getTag().equals(makeTagComponentIcon(component)))
                        onComponentClicked(component);
                    else if (view.getTag().equals(makeTagRemoveIcon(component)))
                        onRemoveComponentClicked(component);
                }
            }
            //Cancel Popup
            if (view.getId() == R.id.universal_ave_popup_empty || view.getId() == R.id.universal_ave_popup_empty_2) {
                getChildFragmentManager().popBackStack();
            } else if (view.getId() == R.id.universal_ave_dismiss) {
                onNegativeActionClicked();
            } else if (view.getId() == R.id.universal_ave_save) {
                onSaveClicked();
            } else if (view.getId() == R.id.fab) {
                onFabClicked();
            } else if (view.getId() == R.id.universal_ave_menu) {
                onMenuButtonClicked(getView());
            }
        }
    }

    private class OnPopupChangeListener implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            updateVisibilities(getView());
            Keyboard.hide(getActivity(), AveFragment.this);
        }
    }

    //==== TextWatcher
    private class EditableComponentsTextWatcher implements TextWatcher {
        AveComponentSet.Component component;

        EditableComponentsTextWatcher(AveComponentSet.Component component) {
            this.component = component;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (component instanceof AveComponentSet.Amount) {
                AveComponentSet.Amount amountComponent = (AveComponentSet.Amount) component;
                StringBuilder number = TypeFaceUtils.fromTypefaceNumberFormat(s.toString());
                if (number.length() != 0) {
                    double amount = Double.parseDouble(number.toString());
                    if (!Numbers.isPreciseSmall(amount - amountComponent.amount)) {
                        amount = (amount * 100 - (amount * 100 % 1)) / 100;
                        amountComponent.amount = amount;
                        String newAmount = amount == 0 ? "" : EnglishTypeFace.withEnglishAmountFormat(amount);
                        s.replace(0, s.length(), newAmount);
                    }
                }
            } else if (component instanceof AveComponentSet.Number) {
                ((AveComponentSet.Number) component).number = s.toString();
            } else if (component instanceof AveComponentSet.Text) {
                ((AveComponentSet.Text) component).text = s.toString();
            }
            tryUpdateVisibilities();
        }
    }

    //==== Property
    private class OnPropertyChanged implements CompoundButton.OnCheckedChangeListener {
        AveComponentSet.Property propertyComponent;

        OnPropertyChanged(AveComponentSet.Property propertyComponent) {
            this.propertyComponent = propertyComponent;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            propertyComponent.value = isChecked;
        }
    }

    //==== Choice
    private class OnChoiceItemSelectedListener implements AdapterView.OnItemSelectedListener {
        AveComponentSet.Choice choice;

        OnChoiceItemSelectedListener(AveComponentSet.Choice choice) {
            this.choice = choice;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            choice.currentPosition = position;
            tryUpdateViews(OpenHelper.getDatabase(getContext()));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class ChoiceAdapter extends BaseAdapter {
        AveComponentSet.Choice choice;

        public ChoiceAdapter(AveComponentSet.Choice choice) {
            this.choice = choice;
        }

        @Override
        public int getCount() {
            if (choice.choiceList != null)
                return choice.choiceList.size();
            else if (choice.choiceListResIds != null)
                return choice.choiceListResIds.size();
            else
                throw new RuntimeException("Both choiceList and choiceListResIds are null");
        }

        @Override
        public Object getItem(int position) {
            if (choice.choiceList != null)
                return choice.choiceList.get(position);
            else if (choice.choiceListResIds != null)
                return choice.choiceListResIds.get(position);
            else
                throw new RuntimeException("Both choiceList and choiceListResIds are null");
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_universal, parent, false);
                TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), convertView);
            }
            convertView.findViewById(R.id.list_item_universal_footer).setVisibility(View.GONE);
            convertView.findViewById(R.id.list_item_universal_detail).setVisibility(View.GONE);
            convertView.findViewById(R.id.list_item_universal_next).setVisibility(View.GONE);
            convertView.findViewById(R.id.list_item_universal_value).setVisibility(View.GONE);
            convertView.findViewById(R.id.list_item_universal_icon).setVisibility(View.GONE);
            convertView.findViewById(R.id.list_item_universal_divider).setVisibility(View.GONE);
            TextView title = convertView.findViewById(R.id.list_item_universal_title);
            if (choice.choiceList != null)
                title.setText(choice.choiceList.get(position));
            else if (choice.choiceListResIds != null)
                title.setText(choice.choiceListResIds.get(position));
            else
                throw new RuntimeException("Both choiceList and choiceListResIds are null");
            return convertView;
        }
    }


    //======================================= View Tag Factory =====================================
    private int getComponentCodeByTag(String componentTag) {
        return Integer.parseInt(componentTag.split("#")[1]);
    }

    private AveComponentSet.Component getComponentByTag(String tag) {
        if (tag == null)
            return null;
        ArrayList<AveComponentSet.Component> components = mComponentSet.getAllComponents();
        for (AveComponentSet.Component component : components)
            if (tag.equals(makeTagComponent(component)))
                return component;
        return null;
    }

    private AveComponentSet.Component getComponentByTagPopup(String tag) {
        return getComponentByTag(tag.substring("Popup_".length()));
    }

    private String makeTagComponent(AveComponentSet.Component component) {
        int componentCode;
        if (component instanceof AveComponentSet.LabelList)
            componentCode = AveComponentSet.Component.COMPONENT_LABEL_LIST;
        else if (component instanceof AveComponentSet.Amount)
            componentCode = AveComponentSet.Component.COMPONENT_AMOUNT;
        else if (component instanceof AveComponentSet.Date)
            componentCode = AveComponentSet.Component.COMPONENT_DATE;
        else if (component instanceof AveComponentSet.Text)
            componentCode = AveComponentSet.Component.COMPONENT_TEXT;
        else if (component instanceof AveComponentSet.Color)
            componentCode = AveComponentSet.Component.COMPONENT_COLOR;
        else if (component instanceof AveComponentSet.Number)
            componentCode = AveComponentSet.Component.COMPONENT_NUMBER;
        else if (component instanceof AveComponentSet.Property)
            componentCode = AveComponentSet.Component.COMPONENT_PROPERTY;
        else if (component instanceof AveComponentSet.Choice)
            componentCode = AveComponentSet.Component.COMPONENT_CHOICE;
        else if (component instanceof AveComponentSet.DatePeriod)
            componentCode = AveComponentSet.Component.COMPONENT_DATE_PERIOD;
        else
            throw new RuntimeException("Unknown Component");

        return "Partial_#" + componentCode + "#" + component.tag;
    }

    private String makeTagPopupComponent(AveComponentSet.Component component) {
        return "Popup_" + makeTagComponent(component);
    }

    private String makeTagComponentIcon(AveComponentSet.Component component) {
        return "Icon_" + makeTagComponent(component);
    }

    private String makeTagRemoveIcon(AveComponentSet.Component component) {
        return "Remove_" + makeTagComponent(component);
    }

    private String makeTagComponentDivider(AveComponentSet.Component component) {
        return "Divider_" + makeTagComponent(component);
    }
}