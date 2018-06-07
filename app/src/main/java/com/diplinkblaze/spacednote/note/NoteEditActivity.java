package com.diplinkblaze.spacednote.note;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diplinkblaze.spacednote.contract.NoActionbarActivity;
import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.diplinkblaze.spacednote.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import data.database.OpenHelper;
import data.database.file.FileOpenHelper;
import data.model.label.Label;
import data.model.label.LabelCatalog;
import data.model.note.ElementDivider;
import data.model.note.ElementList;
import data.model.note.ElementPicture;
import data.model.note.ElementText;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.pictures.PictureCatalog;
import data.model.type.Element;
import data.model.type.ElementCatalog;
import data.model.type.Type;
import data.model.type.TypeCatalog;
import data.storage.PictureOperations;
import util.Concurrent.TaskResult;
import util.Measures;
import util.TypeFaceUtils;

public class NoteEditActivity extends NoActionbarActivity implements NoteDrawerFragment.NoteDrawerFragmentInteractions {

    private static final String TAG_NAV_VIEW = "navView";

    private static final String KEY_TYPE = "type";
    private static final String KEY_NOTE_ID = "noteId";
    private static final String KEY_LABELS = "labels";

    private static final String KEY_LAST_GROUP_ID = "lastGroupId";
    private static final String KEY_ELEMENT_BUNDLES = "elementBundles";

    private static final String KEY_TEXT_PREFIX = "textPrefix";
    private static final String KEY_LIST_PREFIX = "listPrefix";
    private static final String KEY_PICTURE_PREFIX = "picturePrefix";
    private static final String KEY_TOOLBAR_PREFIX = "toolbarPrefix";
    private static final String KEY_ELEMENTS_EDIT_PREFIX = "elementsEditPrefix";
    private static final String KEY_CONTENT_HOLDER_PREFIX = "contentHolderPrefix";

    private final Main main = new Main();
    private final Toolbar toolbar = new Toolbar();
    private final ElementsEdit elementsEdit = new ElementsEdit();
    private final ContentHolder contentHolder = new ContentHolder();

    private final TextElement textElement = new TextElement();
    private final DividerElement dividerElement = new DividerElement();
    private final ListElement listElement = new ListElement();
    private final PictureElement pictureElement = new PictureElement();

    private final ElementModule[] elementModules = {textElement, dividerElement, listElement, pictureElement};
    private final String[] elementModulesKeys = {KEY_TEXT_PREFIX, null, KEY_LIST_PREFIX, KEY_PICTURE_PREFIX};
    private final int[] elementModulesCodes = {0b00, 0b01, 0b10, 0b11};

    private long lastGroupId;

    public static Intent getIntentNew(Context context, long type) {
        Intent intent = new Intent(context, NoteEditActivity.class);
        intent.putExtra(KEY_TYPE, type);
        return intent;
    }

    public static Intent getIntentNew(Context context, long type, ArrayList<Long> labels) {
        Intent intent = new Intent(context, NoteEditActivity.class);
        intent.putExtra(KEY_TYPE, type);
        intent.putExtra(KEY_LABELS, labels);
        return intent;
    }

    public static Intent getIntentEdit(Context context, Note note) {
        Intent intent = new Intent(context, NoteEditActivity.class);
        intent.putExtra(KEY_TYPE, note.getTypeId());
        intent.putExtra(KEY_NOTE_ID, note.getId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("SPACED", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        initializeContent(savedInstanceState);

        if (getSupportFragmentManager().findFragmentByTag(TAG_NAV_VIEW) == null) {
            NoteDrawerFragment fragment = NoteDrawerFragment.newInstance(true);
            fragment.setNoteDrawerInstance(NoteDrawerFragment.NoteDrawerInstance
                    .newInstanceFromNote(contentHolder.note, contentHolder.labels, this));
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.activity_note_nav_view, fragment, TAG_NAV_VIEW);
            transaction.commit();
        }

        for (int i = 0; i < elementModules.length; i++) {
            elementModules[i].prepareViews();
        }

        if (savedInstanceState != null) {
            for (int i = 0; i < elementModules.length; i++) {
                elementModules[i].readBundle(savedInstanceState, elementModulesKeys[i]);
            }
        } else if (contentHolder.note != null) {
            for (int i = 0; i < elementModules.length; i++) {
                elementModules[i].readNoteElements();
            }
        }

        for (int i = 0; i < elementModules.length; i++) {
            elementModules[i].finalizeViews();
        }

        toolbar.initialize(savedInstanceState, KEY_TOOLBAR_PREFIX);
        elementsEdit.initialize(savedInstanceState, KEY_ELEMENTS_EDIT_PREFIX);
        contentHolder.initialize(savedInstanceState, KEY_CONTENT_HOLDER_PREFIX);
        if (savedInstanceState == null) {
            lastGroupId = getMaxGroupId();
        } else {
            lastGroupId = savedInstanceState.getLong(KEY_LAST_GROUP_ID);
        }

        updateViews();
    }

    private void updateViews() {
        toolbar.updateViews();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (main.hasUnsavedContent()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.discard)
                    .setMessage(R.string.sentence_unsaved_changes)
                    .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NoteEditActivity.super.onBackPressed();
                        }
                    }).setNegativeButton(R.string.action_no, null).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (int i = 0; i < elementModules.length; i++) {
            elementModules[i].writeBundle(outState, elementModulesKeys[i]);
        }
        outState.putSerializable(KEY_LABELS, contentHolder.labels);
        outState.putSerializable(KEY_ELEMENT_BUNDLES, contentHolder.elementBundles);
        toolbar.saveToBundle(outState, KEY_TOOLBAR_PREFIX);
        elementsEdit.saveToBundle(outState, KEY_ELEMENTS_EDIT_PREFIX);
        contentHolder.saveToBundle(outState, KEY_CONTENT_HOLDER_PREFIX);
        outState.putLong(KEY_LAST_GROUP_ID, lastGroupId);
    }

    //====================================== Initialize Views ======================================
    private void initializeContent(Bundle savedInstanceState) {
        contentHolder.elementsLayout = (DragLinearLayout) findViewById(R.id.activity_note_edit_element_frame);
        contentHolder.newElementLayout = (LinearLayout) findViewById(R.id.activity_note_edit_new_element_frame);

        long typeId = getIntent().getLongExtra(KEY_TYPE, 0);
        contentHolder.type = TypeCatalog.getTypeById(typeId, OpenHelper.getDatabase(this));
        contentHolder.elementMap = ElementCatalog.getElementMap(contentHolder.type, OpenHelper.getDatabase(this));

        //Note
        {
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(KEY_NOTE_ID)) {
                long noteId = getIntent().getLongExtra(KEY_NOTE_ID, 0);
                contentHolder.note = NoteCatalog.getNoteById(noteId, OpenHelper.getDatabase(getApplicationContext()));
                if (contentHolder.note == null) {
                    throw new RuntimeException("Note was not found");
                }
            } else {
                contentHolder.note = Note.newInstance();
                contentHolder.note.setCreateDate(System.currentTimeMillis());
                contentHolder.note.setModifyDate(contentHolder.note.getCreateDate());
                contentHolder.note.setTypeId(contentHolder.type.getId());
            }

        }

        //Labels
        {
            if (savedInstanceState != null) {
                contentHolder.labels = (ArrayList<Long>) savedInstanceState.getSerializable(KEY_LABELS);
            } else if (contentHolder.note.isRealized()) {
                ArrayList<Label> list = LabelCatalog.getLabelsByNote(contentHolder.note.getId(),
                        OpenHelper.getDatabase(getApplicationContext()));
                contentHolder.labels = new ArrayList<>(list.size());
                for (Label label : list) {
                    contentHolder.labels.add(label.getId());
                }
            } else if (getIntent().getExtras().containsKey(KEY_LABELS)) {
                contentHolder.labels = new ArrayList<>(
                        (ArrayList<Long>) getIntent().getSerializableExtra(KEY_LABELS));
            } else {
                contentHolder.labels = new ArrayList<>();
            }
        }

        //ElementBundles
        {
            LayoutInflater inflater = LayoutInflater.from(this);

            if (savedInstanceState != null) {
                contentHolder.elementBundles = (ArrayList<ElementBundle>) savedInstanceState
                        .getSerializable(KEY_ELEMENT_BUNDLES);
                Collections.sort(contentHolder.elementBundles, new ElementBundlePositionComparator());
                for (ElementBundle elementBundle : contentHolder.elementBundles) {
                    elementBundle.typeElement = contentHolder.elementMap.get(
                            TagFactory.getTypeElementIdByTag(elementBundle.typeTag));
                    elementBundle.moduleReference = getModuleByElement(elementBundle.typeElement);
                    prepareViews(elementBundle, inflater);
                }
            } else if (contentHolder.note.isRealized()) {
                ArrayList<data.model.note.Element> noteElements =
                        data.model.note.ElementCatalog.getNoteElements(contentHolder.note, OpenHelper.getDatabase(this));
                Collections.sort(noteElements, new data.model.note.Element.PositionComparator());
                contentHolder.elementBundles = new ArrayList<>(noteElements.size());
                for (data.model.note.Element element : noteElements) {
                    ElementBundle elementBundle = new ElementBundle();
                    elementBundle.noteElement = element;
                    elementBundle.typeElement = contentHolder.elementMap.get(element.getElementId());
                    elementBundle.moduleReference = getModuleByElement(elementBundle.typeElement);
                    elementBundle.typeTag = TagFactory.getTagByTypeElement(elementBundle.typeElement);
                    elementBundle.position = element.getPosition();
                    elementBundle.groupId = element.getGroupId();
                    prepareViews(elementBundle, inflater);
                    contentHolder.elementBundles.add(elementBundle);
                }
            } else {
                ArrayList<Element> elements = new ArrayList<>(contentHolder.elementMap.values());
                Collections.sort(elements, new Element.PositionComparator());
                contentHolder.elementBundles = new ArrayList<>(elements.size());
                int position = 0;
                for (Element element : elements) {
                    if (element.isInitialCopy()) {
                        ElementBundle elementBundle = new ElementBundle();
                        elementBundle.moduleReference = getModuleByElement(element);
                        elementBundle.typeElement = element;
                        elementBundle.typeTag = TagFactory.getTagByTypeElement(element);
                        elementBundle.position = position++;
                        elementBundle.groupId = getNewGroupId();
                        prepareViews(elementBundle, inflater);
                        contentHolder.elementBundles.add(elementBundle);
                    }
                }

            }
        }
    }

    private void prepareViews(ElementBundle elementBundle, LayoutInflater inflater) {
        elementBundle.elementContainer = (ViewGroup) inflater.inflate(
                R.layout.partial_note_element_container, contentHolder.elementsLayout, false);
        elementBundle.elementView = elementBundle.moduleReference.createFrameView(inflater, elementBundle,
                elementBundle.elementContainer);
        elementBundle.dragHandler = elementBundle.elementContainer.findViewById(
                R.id.partial_note_element_container_handler);
        elementBundle.removeView = elementBundle.elementContainer.findViewById(
                R.id.partial_note_element_container_remove);
        elementBundle.maskView = elementBundle.elementContainer.findViewById(
                R.id.partial_note_element_container_mask);
        elementBundle.elementContainer.addView(elementBundle.elementView);
        contentHolder.elementsLayout.addDragView(elementBundle.elementContainer, elementBundle.dragHandler);
        elementBundle.elementContainer.setTag(elementBundle);
    }

    private void startActivityFromModule(Intent intent, int requestCode, ElementModule module) {
        int shiftPrefix = (int) Math.ceil(Math.log(elementModulesCodes.length) / Math.log(2)) + 1;
        for (int i = 0; i < elementModules.length; i++) {
            if (elementModules[i] == module) {
                int convertRequestCode = (requestCode << shiftPrefix) | elementModulesCodes[i];
                startActivityForResult(intent, convertRequestCode);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int shiftPrefix = (int) Math.ceil(Math.log(elementModulesCodes.length) / Math.log(2)) + 1;
        int mask = 0b0;
        for (int i = 0; i < shiftPrefix; i++) {
            mask = (mask << 1) | 0b1;
        }
        int prefix = requestCode & mask;
        for (int i = 0; i < elementModules.length; i++) {
            if (prefix == elementModulesCodes[i]) {
                elementModules[i].onActivityResult(requestCode >> shiftPrefix, resultCode, data);
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //=========================================== Util =============================================

    private ElementModule getModuleByElement(Element element) {
        if (element.getPattern() == Element.PATTERN_TEXT) {
            return textElement;
        } else if (element.getPattern() == Element.PATTERN_DIVIDER) {
            return dividerElement;
        } else if (element.getPattern() == Element.PATTERN_LIST) {
            return listElement;
        } else if (element.getPattern() == Element.PATTERN_PICTURE) {
            return pictureElement;
        } else
            throw new RuntimeException("Given element was not recognized");
    }

    private static class TagFactory {
        private static String getTagByTypeElement(Element element) {
            return "ElementID" + element.getId();
        }

        private static long getTypeElementIdByTag(String tag) {
            return Long.parseLong(tag.replace("ElementID", ""));
        }
    }

    private long getNewGroupId() {
        lastGroupId++;
        return lastGroupId;
    }

    private long getMaxGroupId() {
        long max = -1;
        if (contentHolder.elementBundles != null) {
            for (ElementBundle elementBundle : contentHolder.elementBundles) {
                if (elementBundle.groupId > max) {
                    max = elementBundle.groupId;
                }
            }
        }
        return max;
    }

    //=========================================== Parts ============================================
    private class Main {

        private void save() {
            SQLiteDatabase database = OpenHelper.getDatabase(getApplicationContext());
            database.beginTransaction();

            //Insert Note if new (phase 1)
            if (!contentHolder.note.isRealized()) {
                contentHolder.note.setInitialized(true);
                contentHolder.note.setId(NoteCatalog.addNote(contentHolder.note, database, getApplicationContext()));
                contentHolder.note.setRealized(true);

                //Insert delayed labels if exist
                {
                    if (contentHolder.labels != null) {
                        for (long labelId : contentHolder.labels) {
                            NoteCatalog.setLabelToNote(contentHolder.note, labelId, database, NoteEditActivity.this);
                        }
                    }
                }
            }

            //Update elements
            {
                ArrayList<data.model.note.Element> noteElements = prepareNoteElements();
                data.model.note.ElementCatalog.updateNoteElements(contentHolder.note, noteElements, database, getApplicationContext());
            }

            //Update note
            {
                ArrayList<data.model.note.Element> noteElements = contentHolder.getNoteElementList();
                Collections.sort(noteElements, new data.model.note.Element.PositionComparator());

                if (contentHolder.shouldResetModifyDate) {
                    contentHolder.note.setModifyDate(System.currentTimeMillis());
                }
                contentHolder.note.setDisplayTitleFront(Note.generateNoteDisplayTitleFront(
                        noteElements, contentHolder.elementMap, getResources()));
                contentHolder.note.setDisplayDetailsFront(Note.generateNoteDisplayDetailsFront(
                        noteElements, contentHolder.elementMap, getResources()));
                contentHolder.note.setDisplayTitleBack(Note.generateNoteDisplayTitleBack(
                        noteElements, contentHolder.elementMap, getResources()));
                contentHolder.note.setDisplayDetailsBack(Note.generateNoteDisplayDetailsBack(
                        noteElements, contentHolder.elementMap, getResources()));
                NoteCatalog.updateNote(contentHolder.note, database, getApplicationContext());
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            setResult(RESULT_OK);
            finish();
        }

        private void dismiss() {
            onBackPressed();
        }

        private boolean hasUnsavedContent() {
            boolean hasUnsaved = contentHolder.hasModifiedContent;
            for (ElementModule elementModule : elementModules) {
                hasUnsaved = hasUnsaved || elementModule.hasUnsavedContent();
            }
            return hasUnsaved;
        }

        private ArrayList<data.model.note.Element> prepareNoteElements() {
            for (int i = 0; i < elementModules.length; i++) {
                elementModules[i].writeNoteElements();
            }

            ArrayList<data.model.note.Element> noteElements = new ArrayList<>(contentHolder.elementBundles.size());
            for (int i = 0; i < contentHolder.elementBundles.size(); i++) {
                ElementBundle elementBundle = contentHolder.elementBundles.get(i);
                if (elementBundle.noteElement != null) {
                    elementBundle.noteElement.setPosition(elementBundle.position);
                    elementBundle.noteElement.setGroupId(elementBundle.groupId);
                    noteElements.add(elementBundle.noteElement);
                }
            }
            return noteElements;
        }
    }

    private class Toolbar {

        private void initialize(Bundle savedInstanceState, String prefix) {
            LinearLayout toolbarLayout = (LinearLayout) findViewById(R.id.activity_note_edit_toolbar);
            TypeFaceUtils.setTypefaceDefaultCascade(getAssets(), toolbarLayout);
            View dismiss = toolbarLayout.findViewById(R.id.activity_note_edit_toolbar_dismiss);
            View labels = toolbarLayout.findViewById(R.id.activity_note_edit_toolbar_labels);
            View info = toolbarLayout.findViewById(R.id.activity_note_edit_toolbar_info);
            View save = toolbarLayout.findViewById(R.id.activity_note_edit_toolbar_save);

            OnViewClickListener viewClickListener = new OnViewClickListener();
            dismiss.setOnClickListener(viewClickListener);
            labels.setOnClickListener(viewClickListener);
            info.setOnClickListener(viewClickListener);
            save.setOnClickListener(viewClickListener);
        }

        private void saveToBundle(Bundle outState, String prefix) {
        }

        private void updateViews() {
            TextView labels = (TextView) findViewById(R.id.activity_note_edit_toolbar_labels);
            labels.setText(TypeFaceUtils.withNumberFormat(contentHolder.labels.size()));
        }

        private void info() {
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            View drawerView = findViewById(R.id.activity_note_nav_view);
            drawerLayout.openDrawer(drawerView);
        }

        private void labels() {
            info();
        }

        private class OnViewClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.activity_note_edit_toolbar_dismiss) {
                    main.dismiss();
                } else if (v.getId() == R.id.activity_note_edit_toolbar_labels) {
                    labels();
                } else if (v.getId() == R.id.activity_note_edit_toolbar_info) {
                    info();
                } else if (v.getId() == R.id.activity_note_edit_toolbar_save) {
                    main.save();
                }
            }
        }
    }

    private class ElementsEdit {

        private void initialize(Bundle savedInstanceState, String prefix) {
            contentHolder.elementsLayout.setOnViewSwapListener(new OnSwapListener());

            ArrayList<Element> typeElements = new ArrayList<>(contentHolder.elementMap.values());
            Collections.sort(typeElements, new Element.PositionComparator());
            LayoutInflater inflater = LayoutInflater.from(NoteEditActivity.this);
            for (Element element : typeElements) {
                View newElementView = inflater.inflate(
                        R.layout.partial_note_new_element, contentHolder.newElementLayout, false);
                newElementView.setOnClickListener(new OnNewElementClicked(element));
                TextView title = newElementView.findViewById(R.id.partial_note_new_element_title);
                if (element.getTitle() != null && element.getTitle().length() != 0) {
                    title.setText(element.getTitle());
                } else if (element.getPattern() == Element.PATTERN_TEXT) {
                    title.setText(R.string.text);
                } else if (element.getPattern() == Element.PATTERN_DIVIDER) {
                    title.setText(R.string.divider);
                } else if (element.getPattern() == Element.PATTERN_LIST) {
                    title.setText(R.string.list);
                } else if (element.getPattern() == Element.PATTERN_PICTURE) {
                    title.setText(R.string.pictures);
                } else {
                    throw new RuntimeException("element pattern was not recognized");
                }
                contentHolder.newElementLayout.addView(newElementView);
            }
            for (ElementBundle elementBundle : contentHolder.elementBundles) {
                elementBundle.removeView.setOnClickListener(new OnRemoveElementClicked(elementBundle));
            }
        }

        private void saveToBundle(Bundle outState, String prefix) {

        }

        private class OnSwapListener implements DragLinearLayout.OnViewSwapListener {
            @Override
            public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                ElementBundle firstElementBundle = (ElementBundle) firstView.getTag();
                ElementBundle secondElementBundle = (ElementBundle) secondView.getTag();
                firstElementBundle.position = secondPosition;
                secondElementBundle.position = firstPosition;
                contentHolder.hasModifiedContent = true;
            }
        }

        private class OnNewElementClicked implements View.OnClickListener {
            Element element;

            public OnNewElementClicked(Element element) {
                this.element = element;
            }

            @Override
            public void onClick(View v) {
                ElementBundle elementBundle = new ElementBundle();
                elementBundle.typeElement = element;
                elementBundle.moduleReference = getModuleByElement(element);
                elementBundle.typeTag = TagFactory.getTagByTypeElement(element);
                elementBundle.position = contentHolder.elementBundles.size();
                elementBundle.groupId = getNewGroupId();
                prepareViews(elementBundle, LayoutInflater.from(NoteEditActivity.this));
                contentHolder.elementBundles.add(elementBundle);
                elementBundle.moduleReference.prepareSingleView(elementBundle);
                elementBundle.moduleReference.finalizeSingleView(elementBundle);
                elementBundle.removeView.setOnClickListener(new OnRemoveElementClicked(elementBundle));
                contentHolder.hasModifiedContent = true;
            }
        }

        private class OnRemoveElementClicked implements View.OnClickListener {
            ElementBundle elementBundle;

            public OnRemoveElementClicked(ElementBundle elementBundle) {
                this.elementBundle = elementBundle;
            }

            @Override
            public void onClick(View v) {
                contentHolder.elementsLayout.removeDragView(elementBundle.elementContainer);
                contentHolder.elementBundles.remove(elementBundle);
                for (ElementBundle e : contentHolder.elementBundles) {
                    if (e.position > elementBundle.position) {
                        e.position = e.position - 1;
                    }
                }
                contentHolder.hasModifiedContent = true;
            }
        }
    }

    private class ContentHolder {
        private static final String KEY_SHOULD_RESET_MODIFY_DATE = "shouldResetModifyDate";
        private static final String KEY_HAS_MODIFIED_CONTENT = "hasModifiedContent";

        private DragLinearLayout elementsLayout;
        private LinearLayout newElementLayout;
        private TreeMap<Long, Element> elementMap;
        private ArrayList<ElementBundle> elementBundles;
        private Type type;

        private Note note;
        private ArrayList<Long> labels;
        private boolean shouldResetModifyDate = true;
        private boolean hasModifiedContent;


        private ArrayList<ElementBundle> getElementBundlesForModule(ElementModule module) {
            ArrayList<ElementBundle> bundles = new ArrayList<>(elementBundles.size());
            for (ElementBundle bundle : elementBundles) {
                if (bundle.moduleReference == module) {
                    bundles.add(bundle);
                }
            }
            return bundles;
        }

        private ArrayList<Element> getTypeElementList() {
            ArrayList<Element> elements = new ArrayList<>(contentHolder.elementBundles.size());
            for (ElementBundle elementBundle : elementBundles) {
                elements.add(elementBundle.typeElement);
            }
            return elements;
        }

        private ArrayList<data.model.note.Element> getNoteElementList() {
            ArrayList<data.model.note.Element> elements = new ArrayList<>(contentHolder.elementBundles.size());
            for (ElementBundle elementBundle : elementBundles) {
                if (elementBundle.noteElement != null) {
                    elements.add(elementBundle.noteElement);
                }
            }
            return elements;
        }

        private ElementBundle getElementBundleByTypeElementId(long id) {
            for (ElementBundle elementBundle : elementBundles) {
                if (elementBundle.typeElement.getId() == id)
                    return elementBundle;
            }
            return null;
        }

        private ElementBundle getElementBundleByGroupId(long id) {
            for (ElementBundle elementBundle : elementBundles) {
                if (elementBundle.groupId == id)
                    return elementBundle;
            }
            return null;
        }

        private void initialize(Bundle bundle, String prefix) {
            if (bundle != null) {
                hasModifiedContent = bundle.getBoolean(prefix + KEY_HAS_MODIFIED_CONTENT);
                shouldResetModifyDate = bundle.getBoolean(prefix + KEY_SHOULD_RESET_MODIFY_DATE);
            }
        }

        private void saveToBundle(Bundle bundle, String prefix) {
            if (bundle != null) {
                bundle.putBoolean(prefix + KEY_HAS_MODIFIED_CONTENT, hasModifiedContent);
                bundle.putBoolean(prefix + KEY_SHOULD_RESET_MODIFY_DATE, shouldResetModifyDate);
            }
        }
    }

    private class TextElement extends ElementModule {
        private final String KEY_TEXT = "text";

        @Override
        public View createFrameView(LayoutInflater inflater, ElementBundle elementBundle, ViewGroup parent) {
            if (elementBundle.typeElement == null || !elementBundle.typeElement.isRealized() ||
                    !elementBundle.typeElement.isInitialized())
                throw new RuntimeException("Element must be initialized and realized");
            View elementView = null;
            Element.Interpreter interpreter = elementBundle.typeElement.getInterpreter();
            if (interpreter instanceof Element.TextInterpreter) {
                Element.TextInterpreter textInterpreter = (Element.TextInterpreter) interpreter;
                if (textInterpreter.isMultiline()) {
                    elementView = inflater.inflate(R.layout.partial_note_text_multiline, parent, false);
                } else {
                    elementView = inflater.inflate(R.layout.partial_note_text_single_line, parent, false);
                }
                EditText editText = elementView.findViewById(R.id.partial_note_text_element);
                editText.setHint(textInterpreter.getHint());
                editText.setTextSize(textInterpreter.getTextSize());
                editText.setTextColor(textInterpreter.getColor());
                TypeFaceUtils.setTypefaceCascade(getAssets(), editText, textInterpreter.getFontName());
                if (textInterpreter.isBold() && textInterpreter.isItalic()) {
                    editText.setTypeface(editText.getTypeface(), Typeface.BOLD_ITALIC);
                } else if (textInterpreter.isBold()) {
                    editText.setTypeface(editText.getTypeface(), Typeface.BOLD);
                } else if (textInterpreter.isItalic()) {
                    editText.setTypeface(editText.getTypeface(), Typeface.ITALIC);
                } else {
                    editText.setTypeface(editText.getTypeface(), Typeface.NORMAL);
                }
            }
            return elementView;
        }

        @Override
        boolean hasUnsavedContent() {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            boolean hasUnsaved = false;
            for (ElementBundle elementBundle : elementBundles) {
                if (elementBundle.moduleReference == this) {
                    EditText editText = elementBundle.elementView.findViewById(R.id.partial_note_text_element);
                    String text = editText.getText().toString();
                    ElementText textElement = (ElementText) elementBundle.noteElement;
                    hasUnsaved = hasUnsaved || !text.equals(textElement == null ? "" : textElement.getText());
                }
            }
            return hasUnsaved;
        }


        @Override
        public void readNoteElements() {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                EditText editText = elementBundle.elementView.findViewById(R.id.partial_note_text_element);
                if (elementBundle.noteElement == null) {
                    editText.setText(null);
                } else {
                    ElementText text = (ElementText) elementBundle.noteElement;
                    editText.setText(text.getText());
                }
            }
        }

        @Override
        public void writeNoteElements() {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                EditText editText = elementBundle.elementView.findViewById(R.id.partial_note_text_element);
                ElementText noteElement = ElementText.newInstance();
                noteElement.setElementId(elementBundle.typeElement.getId());
                noteElement.setNoteId(contentHolder.note.getId());
                noteElement.setText(editText.getText().toString());
                if (noteElement.getText() == null || noteElement.getText().length() == 0) {
                    elementBundle.noteElement = null;
                } else {
                    elementBundle.noteElement = noteElement;
                }
            }
        }

        @Override
        public void readBundle(Bundle bundle, String prefix) {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                EditText editText = elementBundle.elementView.findViewById(R.id.partial_note_text_element);
                editText.setText(bundle.getString(prefix + elementBundle.groupId + KEY_TEXT));
            }
        }

        @Override
        public void writeBundle(Bundle bundle, String prefix) {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                EditText editText = elementBundle.elementView.findViewById(R.id.partial_note_text_element);
                String text = editText.getText().toString();
                bundle.putString(prefix + elementBundle.groupId + KEY_TEXT, text);
            }
        }


    }

    private class DividerElement extends ElementModule {
        @Override
        public View createFrameView(LayoutInflater inflater, ElementBundle elementBundle, ViewGroup parent) {
            if (elementBundle.typeElement == null || !elementBundle.typeElement.isRealized() ||
                    !elementBundle.typeElement.isInitialized())
                throw new RuntimeException("Element must be initialized and realized");
            View elementView = null;
            Element.Interpreter interpreter = elementBundle.typeElement.getInterpreter();
            if (interpreter instanceof Element.DividerInterpreter) {
                Element.DividerInterpreter dividerInterpreter = (Element.DividerInterpreter) interpreter;

                if (dividerInterpreter.getDividerType() == Element.DividerInterpreter.DIVIDER_TYPE_LINE) {
                    elementView = inflater.inflate(R.layout.partial_note_divider_line, parent, false);
                } else if (dividerInterpreter.getDividerType() == Element.DividerInterpreter.DIVIDER_TYPE_DASHED_LINE) {
                    elementView = inflater.inflate(R.layout.partial_note_divider_dashed_line, parent, false);
                } else if (dividerInterpreter.getDividerType() == Element.DividerInterpreter.DIVIDER_TYPE_TITLE) {
                    elementView = inflater.inflate(R.layout.partial_note_divider_title, parent, false);
                } else if (dividerInterpreter.getDividerType() == Element.DividerInterpreter.DIVIDER_TYPE_TITLE_BACKGROUND) {
                    elementView = inflater.inflate(R.layout.partial_note_divider_title_background, parent, false);
                } else if (dividerInterpreter.getDividerType() == Element.DividerInterpreter.DIVIDER_TYPE_SPACE) {
                    elementView = inflater.inflate(R.layout.partial_note_divider_space, parent, false);
                }

                TextView textView = elementView.findViewById(R.id.partial_note_divide_text);
                if (textView != null) {
                    textView.setText(elementBundle.typeElement.getTitle());
                    textView.setTextSize(dividerInterpreter.getTextSize());
                    textView.setTextColor(dividerInterpreter.getColor());
                    TypeFaceUtils.setTypefaceCascade(getAssets(), textView, dividerInterpreter.getFontName());
                    if (dividerInterpreter.isBold() && dividerInterpreter.isItalic()) {
                        textView.setTypeface(textView.getTypeface(), Typeface.BOLD_ITALIC);
                    } else if (dividerInterpreter.isBold()) {
                        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                    } else if (dividerInterpreter.isItalic()) {
                        textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
                    } else {
                        textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
                    }
                }
            }
            return elementView;
        }

        @Override
        boolean hasUnsavedContent() {
            if (contentHolder.note.isRealized()) {
                ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
                boolean hasUnsaved = false;
                for (ElementBundle elementBundle : elementBundles) {
                    hasUnsaved = hasUnsaved || elementBundle.noteElement == null;
                }
                return hasUnsaved;
            }
            return false;
        }


        @Override
        public void readNoteElements() {
            //Nothing to do
        }

        @Override
        public void writeNoteElements() {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                ElementDivider noteElement = ElementDivider.newInstance();
                noteElement.setElementId(elementBundle.typeElement.getId());
                noteElement.setNoteId(contentHolder.note.getId());
                elementBundle.noteElement = noteElement;
            }
        }

        @Override
        public void readBundle(Bundle bundle, String prefix) {
            //Nothing to do
        }

        @Override
        public void writeBundle(Bundle bundle, String prefix) {
            //Nothing to do
        }

    }

    private class ListElement extends ElementModule {
        private final String KEY_LIST_ITEM_COUNT = "listItemCount";
        private final String KEY_LIST_ITEM_TEXT = "listItemText";

        private TreeMap<Long, OnListTextChanged> textChangedTreeMap = new TreeMap<>();

        @Override
        public View createFrameView(LayoutInflater inflater, ElementBundle elementBundle, ViewGroup parent) {
            DragLinearLayout listFrame = (DragLinearLayout) inflater.inflate(
                    R.layout.partial_note_list_container, parent, false);
            Element.ListInterpreter interpreter = (Element.ListInterpreter) elementBundle.typeElement.getInterpreter();
            if (interpreter.getListType() == Element.ListInterpreter.LIST_TYPE_NUMBERS) {
                listFrame.setOnViewSwapListener(new OnSwapNumbersUpdate(elementBundle.typeElement));
            }
            return listFrame;
        }

        @Override
        boolean hasUnsavedContent() {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            boolean hasUnsaved = false;
            for (ElementBundle elementBundle : elementBundles) {
                ElementList noteElement = generateNoteElementFromViews(elementBundle);
                if (noteElement == null) {
                    hasUnsaved = hasUnsaved || elementBundle.noteElement != null;
                } else {
                    hasUnsaved = hasUnsaved || !noteElement.equals(elementBundle.noteElement);
                }
            }
            return hasUnsaved;
        }

        @Override
        public void readNoteElements() {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                ElementList elementList = (ElementList) elementBundle.noteElement;
                DragLinearLayout frame = elementBundle.elementView.findViewById(R.id.partial_note_list_container_frame);
                if (frame.getChildCount() != 0) {
                    throw new RuntimeException("readNoteElements is supposed to populate the lists, " +
                            "whereas there already exists " + frame.getChildCount() + " items");
                }
                if (elementList != null) {
                    ArrayList<ElementList.ListItem> items = elementList.getSortedList();
                    getListTextChanged(elementBundle).isLayoutLoaded = false;
                    for (ElementList.ListItem item : items) {
                        View itemView = addNewItem(elementBundle, inflater, frame);
                        EditText text = itemView.findViewById(R.id.partial_note_list_item_text);
                        text.setText(item.getText());
                    }
                }
                getListTextChanged(elementBundle).isLayoutLoaded = true;
            }
        }

        @Override
        public void writeNoteElements() {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                elementBundle.noteElement = generateNoteElementFromViews(elementBundle);
            }
        }

        private ElementList generateNoteElementFromViews(ElementBundle elementBundle) {
            ElementList noteElement = ElementList.newInstance();
            noteElement.setElementId(elementBundle.typeElement.getId());
            noteElement.setNoteId(contentHolder.note.getId());

            ViewGroup elementView = elementBundle.elementView.findViewById(R.id.partial_note_list_container_frame);
            for (int i = 0; i < elementView.getChildCount(); i++) {
                View itemView = elementView.getChildAt(i);
                EditText editText = itemView.findViewById(R.id.partial_note_list_item_text);
                String text = editText.getText().toString();

                if (text != null && text.length() != 0) {
                    ElementList.ListItem listItem = ElementList.ListItem.newInstance();
                    listItem.setText(text);
                    listItem.setPosition(i);
                    noteElement.addItem(listItem);
                }
            }

            if (noteElement.getItemCount() == 0) {
                return null;
            } else {
                return noteElement;
            }
        }

        @Override
        public void readBundle(Bundle bundle, String prefix) {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                DragLinearLayout elementView = elementBundle.elementView.findViewById(R.id.partial_note_list_container_frame);
                if (elementView.getChildCount() != 0) {
                    throw new RuntimeException("readBundle is supposed to populate the lists, " +
                            "whereas there already exists " + elementView.getChildCount() + " items");
                }
                int count = bundle.getInt(prefix + elementBundle.groupId + KEY_LIST_ITEM_COUNT);
                getListTextChanged(elementBundle).isLayoutLoaded = false;
                for (int i = 0; i < count; i++) {
                    View itemView = addNewItem(elementBundle);
                    EditText editText = itemView.findViewById(R.id.partial_note_list_item_text);
                    String text = bundle.getString(prefix + elementBundle.groupId + KEY_LIST_ITEM_TEXT + i);
                    editText.setText(text);
                }
                getListTextChanged(elementBundle).isLayoutLoaded = true;
            }
        }

        @Override
        public void writeBundle(Bundle bundle, String prefix) {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                ViewGroup elementView = elementBundle.elementView.findViewById(R.id.partial_note_list_container_frame);
                for (int i = 0; i < elementView.getChildCount(); i++) {
                    View itemView = elementView.getChildAt(i);
                    EditText editText = itemView.findViewById(R.id.partial_note_list_item_text);
                    String text = editText.getText().toString();
                    bundle.putString(prefix + elementBundle.groupId + KEY_LIST_ITEM_TEXT + i, text);
                }
                bundle.putInt(prefix + elementBundle.groupId + KEY_LIST_ITEM_COUNT, elementView.getChildCount());
            }
        }

        @Override
        public void finalizeViews() {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                finalizeSingleView(elementBundle);
            }
        }

        @Override
        void finalizeSingleView(ElementBundle bundle) {
            getListTextChanged(bundle).addNewItemIfNecessary();
            updateNumbers(bundle);
        }

        //===================== Item Views ======================

        private View createItemView(LayoutInflater inflater, ElementBundle elementBundle, ViewGroup parent) {
            View item = inflater.inflate(R.layout.partial_note_list_item, parent, false);
            Element.ListInterpreter listInterpreter = (Element.ListInterpreter) elementBundle.typeElement.getInterpreter();
            EditText text = item.findViewById(R.id.partial_note_list_item_text);
            TextView symbol = item.findViewById(R.id.partial_note_list_item_symbol);
            //Symbol
            {
                if (listInterpreter.getListType() == Element.ListInterpreter.LIST_TYPE_BULLETS) {
                    symbol.setText("•");
                } else if (listInterpreter.getListType() == Element.ListInterpreter.LIST_TYPE_BULLETS_EMPTY) {
                    symbol.setText("○");
                }
                symbol.setTextSize(listInterpreter.getTextSize());
                symbol.setTextColor(listInterpreter.getColor());
                TypeFaceUtils.setTypefaceCascade(getAssets(), symbol, listInterpreter.getFontName());
                if (listInterpreter.isBold() && listInterpreter.isItalic()) {
                    symbol.setTypeface(symbol.getTypeface(), Typeface.BOLD_ITALIC);
                } else if (listInterpreter.isBold()) {
                    symbol.setTypeface(symbol.getTypeface(), Typeface.BOLD);
                } else if (listInterpreter.isItalic()) {
                    symbol.setTypeface(symbol.getTypeface(), Typeface.ITALIC);
                } else {
                    symbol.setTypeface(symbol.getTypeface(), Typeface.NORMAL);
                }
            }
            //Text
            {
                text.setTextSize(listInterpreter.getTextSize());
                text.setTextColor(listInterpreter.getColor());
                TypeFaceUtils.setTypefaceCascade(getAssets(), text, listInterpreter.getFontName());
                if (listInterpreter.isBold() && listInterpreter.isItalic()) {
                    text.setTypeface(text.getTypeface(), Typeface.BOLD_ITALIC);
                } else if (listInterpreter.isBold()) {
                    text.setTypeface(text.getTypeface(), Typeface.BOLD);
                } else if (listInterpreter.isItalic()) {
                    text.setTypeface(text.getTypeface(), Typeface.ITALIC);
                } else {
                    text.setTypeface(text.getTypeface(), Typeface.NORMAL);
                }
            }
            return item;
        }

        //Content is stored in views, at last, content will be copied back to data.model
        private View addNewItem(ElementBundle elementBundle) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            DragLinearLayout frame = elementBundle.elementView.findViewById(R.id.partial_note_list_container_frame);
            return addNewItem(elementBundle, inflater, frame);
        }

        private View addNewItem(ElementBundle elementBundle, LayoutInflater inflater, DragLinearLayout frame) {
            View itemView = createItemView(inflater, elementBundle, frame);

            View removeIcon = itemView.findViewById(R.id.partial_note_list_item_remove);
            removeIcon.setOnClickListener(new OnRemoveClickListener(itemView, frame, elementBundle));

            View handler = itemView.findViewById(R.id.partial_note_list_item_symbol);
            frame.addDragView(itemView, handler);

            EditText editText = itemView.findViewById(R.id.partial_note_list_item_text);
            OnListTextChanged listTextChanged = getListTextChanged(elementBundle);
            editText.addTextChangedListener(listTextChanged);
            listTextChanged.updateWatchingList();

            return itemView;
        }

        private OnListTextChanged getListTextChanged(ElementBundle elementBundle) {
            OnListTextChanged listTextChanged = textChangedTreeMap.get(elementBundle.groupId);
            if (listTextChanged == null) {
                listTextChanged = new OnListTextChanged(elementBundle);
                textChangedTreeMap.put(elementBundle.groupId, listTextChanged);
            }
            return listTextChanged;
        }

        private void updateNumbers(ElementBundle elementBundle) {
            Element.ListInterpreter interpreter = (Element.ListInterpreter) elementBundle.typeElement.getInterpreter();
            if (interpreter.getListType() == Element.ListInterpreter.LIST_TYPE_NUMBERS) {
                DragLinearLayout linearLayout = elementBundle.elementView.findViewById(R.id.partial_note_list_container_frame);
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    View itemView = linearLayout.getChildAt(i);
                    TextView symbol = itemView.findViewById(R.id.partial_note_list_item_symbol);
                    symbol.setText(TypeFaceUtils.withNumberFormat(i + 1));
                }
            }
        }

        //=================== UI Interaction ====================
        private class OnRemoveClickListener implements View.OnClickListener {

            private ElementBundle elementBundle;
            private View itemView;
            private DragLinearLayout containerView;

            public OnRemoveClickListener(View itemView, DragLinearLayout containerView, ElementBundle elementBundle) {
                this.itemView = itemView;
                this.containerView = containerView;
                this.elementBundle = elementBundle;
            }

            @Override
            public void onClick(View v) {
                containerView.removeDragView(itemView);
                OnListTextChanged listTextChanged = getListTextChanged(elementBundle);
                listTextChanged.updateWatchingList();
                View view = listTextChanged.addNewItemIfNecessary();
                if (view == null) {
                    updateNumbers(elementBundle);
                }
            }
        }

        //===================== Listeners =======================
        private class OnListTextChanged implements TextWatcher {
            ElementBundle elementBundle;
            ArrayList<EditText> watching;
            boolean isLayoutLoaded = true;

            public OnListTextChanged(ElementBundle elementBundle) {
                this.elementBundle = elementBundle;
            }

            public void updateWatchingList() {
                DragLinearLayout linearLayout = elementBundle.elementView.findViewById(R.id.partial_note_list_container_frame);
                watching = new ArrayList<>(linearLayout.getChildCount());
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    View view = linearLayout.getChildAt(i);
                    EditText editText = view.findViewById(R.id.partial_note_list_item_text);
                    watching.add(editText);
                }
            }

            private View addNewItemIfNecessary() {
                if (!isLayoutLoaded)
                    return null;
                if (watching != null) {
                    for (EditText editText : watching) {
                        if (editText.getText() == null || editText.getText().length() == 0) {
                            return null;
                        }
                    }
                }
                View view = addNewItem(elementBundle);
                updateNumbers(elementBundle);
                return view;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                addNewItemIfNecessary();
            }
        }

        private class OnSwapNumbersUpdate implements DragLinearLayout.OnViewSwapListener {
            Element element;

            public OnSwapNumbersUpdate(Element element) {
                this.element = element;
            }

            @Override
            public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                TextView symbol = firstView.findViewById(R.id.partial_note_list_item_symbol);
                symbol.setText(TypeFaceUtils.withNumberFormat(secondPosition + 1));
                TextView symbolSecond = secondView.findViewById(R.id.partial_note_list_item_symbol);
                symbolSecond.setText(TypeFaceUtils.withNumberFormat(firstPosition + 1));
            }
        }
    }

    private class PictureElement extends ElementModule {

        private final int REQUEST_IMAGE_GET = 0;

        private final String KEY_IMAGE_REQUEST_ELEMENT_ID = "imageRequestElementId";
        private final String KEY_PREVIOUS_FILES_MAP = "previousFilesMap";
        private final String KEY_PICTURE_ITEM_COUNT = "pictureItemCount";
        private final String KEY_PICTURE_ITEM_PATH = "pictureItemName";

        private TreeMap<String, Long> previousFiles;
        private long imageRequestGroupId;

        @Override
        public View createFrameView(LayoutInflater inflater, ElementBundle elementBundle, ViewGroup parent) {
            View pictureFrame = inflater.inflate(R.layout.partial_note_picture_container, parent, false);
            View addView = pictureFrame.findViewById(R.id.partial_note_picture_container_add);
            addView.setOnClickListener(new OnAddNewClickListener(elementBundle));
            return pictureFrame;
        }

        @Override
        boolean hasUnsavedContent() {
            TreeSet<Long> removeSet = new TreeSet<>();
            if (previousFiles != null) {
                removeSet.addAll(previousFiles.values());
            }

            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                DragLinearLayout linearLayout = elementBundle.elementView.findViewById(R.id.partial_note_picture_container_frame);
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    View itemView = linearLayout.getChildAt(i);
                    File picture = (File) itemView.getTag(R.id.picture_view_file_name);
                    Long pictureId = null;
                    if (previousFiles != null) {
                        pictureId = previousFiles.get(picture.getName());
                    }
                    if (pictureId == null) {
                        return true;
                    } else {
                        removeSet.remove(pictureId);
                    }
                }
                return removeSet.size() != 0;
            }
            return false;
        }

        @Override
        public void readNoteElements() {
            PictureLoadTask pictureLoadTask = new PictureLoadTask();
            PictureCatalog.cachePicturesToSelectDirectory(contentHolder.note, getApplicationContext(),
                    OpenHelper.getDatabase(getApplicationContext()), pictureLoadTask);
        }

        @Override
        public void writeNoteElements() {
            if (!contentHolder.note.isRealized()) {
                throw new RuntimeException("This element requires note to be realized before writing elements");
            }

            TreeSet<Long> removeSet = new TreeSet<>();
            if (previousFiles != null) {
                removeSet.addAll(previousFiles.values());
            }

            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {

                ElementPicture noteElement = ElementPicture.newInstance();
                noteElement.setElementId(elementBundle.typeElement.getId());
                noteElement.setNoteId(contentHolder.note.getId());

                DragLinearLayout linearLayout = elementBundle.elementView.findViewById(R.id.partial_note_picture_container_frame);
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    View itemView = linearLayout.getChildAt(i);
                    File picture = (File) itemView.getTag(R.id.picture_view_file_name);
                    Long pictureId = null;
                    if (previousFiles != null) {
                        pictureId = previousFiles.get(picture.getName());
                    }
                    if (pictureId == null) {
                        pictureId = PictureCatalog.submitPicture(picture, contentHolder.note,
                                OpenHelper.getDatabase(getApplicationContext()),
                                FileOpenHelper.getDatabase(getApplicationContext()),
                                getApplicationContext());
                    } else {
                        removeSet.remove(pictureId);
                    }

                    ElementPicture.PictureItem pictureItem = ElementPicture.PictureItem.newInstance();
                    pictureItem.setPictureId(pictureId);
                    pictureItem.setPosition(i);
                    noteElement.addItem(pictureItem);
                }

                if (noteElement.getItemCount() == 0) {
                    elementBundle.noteElement = null;
                } else {
                    elementBundle.noteElement = noteElement;
                }
            }
            for (Long id : removeSet) {
                PictureCatalog.deletePicture(id, getApplicationContext(),
                        OpenHelper.getDatabase(getApplicationContext()),
                        FileOpenHelper.getDatabase(getApplicationContext()));
            }
        }

        @Override
        public void readBundle(Bundle bundle, String prefix) {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            for (ElementBundle elementBundle : elementBundles) {
                DragLinearLayout linearLayout = elementBundle.elementView.findViewById(R.id.partial_note_picture_container_frame);
                int count = bundle.getInt(prefix + elementBundle.groupId + KEY_PICTURE_ITEM_COUNT);
                for (int i = 0; i < count; i++) {
                    String filePath = bundle.getString(prefix + elementBundle.groupId + KEY_PICTURE_ITEM_PATH + i);
                    File file = new File(filePath);
                    View itemView = addNewItem(elementBundle, inflater, linearLayout);
                    updatePicture(itemView, file);
                }
            }
            previousFiles = (TreeMap<String, Long>) bundle.getSerializable(prefix + KEY_PREVIOUS_FILES_MAP);
            imageRequestGroupId = bundle.getLong(prefix + KEY_IMAGE_REQUEST_ELEMENT_ID);
        }

        @Override
        public void writeBundle(Bundle bundle, String prefix) {
            ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(this);
            for (ElementBundle elementBundle : elementBundles) {
                DragLinearLayout linearLayout = elementBundle.elementView.findViewById(R.id.partial_note_picture_container_frame);
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    View itemView = linearLayout.getChildAt(i);
                    File picture = (File) itemView.getTag(R.id.picture_view_file_name);
                    bundle.putString(prefix + elementBundle.groupId + KEY_PICTURE_ITEM_PATH + i, picture.getAbsolutePath());
                }
                bundle.putInt(prefix + elementBundle.groupId + KEY_PICTURE_ITEM_COUNT, linearLayout.getChildCount());
            }
            bundle.putSerializable(prefix + KEY_PREVIOUS_FILES_MAP, previousFiles);
            bundle.putLong(prefix + KEY_IMAGE_REQUEST_ELEMENT_ID, imageRequestGroupId);
        }


        //Content is stored in views, at last, content will be copied back to data.model
        private View addNewItem(ElementBundle elementBundle) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            DragLinearLayout frame = elementBundle.elementView.findViewById(R.id.partial_note_picture_container_frame);
            return addNewItem(elementBundle, inflater, frame);
        }

        private View addNewItem(ElementBundle elementBundle, LayoutInflater inflater, DragLinearLayout frame) {
            View itemView = inflater.inflate(R.layout.partial_note_picture_item, frame, false);

            View removeIcon = itemView.findViewById(R.id.partial_note_picture_item_remove);
            removeIcon.setOnClickListener(new OnRemoveClickListener(itemView, frame));

            View handler = itemView.findViewById(R.id.partial_note_picture_item_handle);
            frame.addDragView(itemView, handler);

            return itemView;
        }

        private void updatePicture(final View itemView, final File file) {
            itemView.setTag(R.id.picture_view_file_name, file);
            AsyncTask<Void, Void, Drawable> taskState = new AsyncTask<Void, Void, Drawable>() {
                @Override
                protected Drawable doInBackground(Void... params) {
                    Drawable drawable = Drawable.createFromPath(file.getAbsolutePath());
                    return drawable;
                }

                @Override
                protected void onPostExecute(final Drawable drawable) {
                    final ImageView imageView = itemView.findViewById(R.id.partial_note_picture_item_content);
                    final View contentScroll = findViewById(R.id.activity_note_edit_content_scroll);
                    if (imageView != null) {
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                int width = imageView.getMeasuredWidth();
                                int intWidth = drawable.getIntrinsicWidth();
                                int intHeight = drawable.getIntrinsicHeight();
                                if (width != 0 && intWidth != 0 && intHeight != 0) {
                                    width = width - Measures.dpToPx(50, getApplicationContext());
                                    int height = (width * intHeight) / intWidth;
                                    int maxHeight = (int) (contentScroll.getMeasuredHeight() * 0.8);
                                    if (maxHeight != 0 && height >= maxHeight) {
                                        height = maxHeight;
                                        width = (height * intWidth) / intHeight;
                                    }

                                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                                    params.width = width;
                                    params.height = height;
                                    imageView.setLayoutParams(params);
                                }
                                imageView.setImageDrawable(drawable);
                            }
                        });
                    }
                }
            };
            taskState.execute();
        }

        private class PictureLoadTask implements TaskResult<TreeMap<Long, File>> {
            @Override
            public void onResultSuccess(final TreeMap<Long, File> result) {
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

                ArrayList<ElementBundle> elementBundles = contentHolder.getElementBundlesForModule(PictureElement.this);
                for (ElementBundle elementBundle : elementBundles) {
                    if (elementBundle.noteElement != null) {
                        ElementPicture elementPicture = (ElementPicture) elementBundle.noteElement;
                        DragLinearLayout frame = elementBundle.elementView.findViewById(R.id.partial_note_picture_container_frame);
                        for (int i = 0; i < elementPicture.getItemCount(); i++) {
                            File file = result.get(elementPicture.getItemAt(i).getPictureId());
                            if (file != null) {
                                View itemView = addNewItem(elementBundle, inflater, frame);
                                updatePicture(itemView, file);
                            }
                        }
                    }
                }

                previousFiles = new TreeMap<>();
                for (Map.Entry<Long, File> entry : result.entrySet()) {
                    previousFiles.put(entry.getValue().getName(), entry.getKey());
                }
            }

            @Override
            public void onResultFailure() {

            }
        }

        //=================== UI Interaction ====================
        @Override
        void onActivityResult(int requestCode, int resultCode, Intent intent) {
            if (requestCode == REQUEST_IMAGE_GET) {
                if (resultCode == RESULT_OK) {
                    Log.i("SPACED", "onActivityResult");
                    AsyncTask<Intent, Void, ArrayList<File>> pictureSaveTask =
                            new AsyncTask<Intent, Void, ArrayList<File>>() {
                                @Override
                                protected ArrayList<File> doInBackground(Intent... intents) {
                                    return PictureOperations.saveToSelectDir(
                                            getApplicationContext(), intents[0]);
                                }

                                @Override
                                protected void onPostExecute(ArrayList<File> files) {
                                    ElementBundle elementBundle = contentHolder.getElementBundleByGroupId(
                                            imageRequestGroupId);
                                    for (File file : files) {
                                        View itemView = addNewItem(elementBundle);
                                        updatePicture(itemView, file);
                                    }
                                }
                            };
                    pictureSaveTask.execute(intent);
                }
            }
        }

        private class OnRemoveClickListener implements View.OnClickListener {

            private View itemView;
            private DragLinearLayout containerView;

            public OnRemoveClickListener(View itemView, DragLinearLayout containerView) {
                this.itemView = itemView;
                this.containerView = containerView;
            }

            @Override
            public void onClick(View v) {
                containerView.removeDragView(itemView);
            }
        }

        private class OnAddNewClickListener implements View.OnClickListener {
            ElementBundle elementBundle;

            public OnAddNewClickListener(ElementBundle elementBundle) {
                this.elementBundle = elementBundle;
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    imageRequestGroupId = elementBundle.groupId;
                    startActivityFromModule(intent, REQUEST_IMAGE_GET, PictureElement.this);
                }
            }
        }
    }

    //================================= Communication Children =====================================
    @Override
    public void onNoteDrawerInstanceChanged(NoteDrawerFragment.NoteDrawerInstance noteDrawerInstance) {
        if (noteDrawerInstance.getLabels() != null) {
            contentHolder.labels = new ArrayList<>(noteDrawerInstance.getLabels().size());
            for (Label label : noteDrawerInstance.getLabels()) {
                contentHolder.labels.add(label.getId());
            }
        }
        if (contentHolder.note.getModifyDate() != noteDrawerInstance.getModifyDate().getMillis()) {
            contentHolder.shouldResetModifyDate = false;
            contentHolder.note.setModifyDate(noteDrawerInstance.getModifyDate().getMillis());
        }
        contentHolder.note.setCreateDate(noteDrawerInstance.getCreateDate().getMillis());
        updateViews();
    }

    //======================================== Contract ============================================
    private abstract class ElementModule {
        abstract View createFrameView(LayoutInflater inflater, ElementBundle elementBundle, ViewGroup parent);

        abstract boolean hasUnsavedContent();

        void prepareViews() {
        }

        void prepareSingleView(ElementBundle bundle) {
        }

        abstract void readNoteElements();

        abstract void writeNoteElements();

        abstract void readBundle(Bundle bundle, String prefix);

        abstract void writeBundle(Bundle bundle, String prefix);

        void finalizeViews() {
        }

        void finalizeSingleView(ElementBundle bundle) {
        }

        void onActivityResult(int requestCode, int resultCode, Intent data) {
        }
    }

    private static class ElementBundle implements Serializable {
        private transient ElementModule moduleReference;
        private transient data.model.note.Element noteElement;
        private transient Element typeElement;
        private String typeTag;
        private int position;
        private long groupId;
        private transient ViewGroup elementContainer;
        private transient View elementView;
        private transient View dragHandler;
        private transient View removeView;
        private transient View maskView;
    }

    private static class ElementBundlePositionComparator implements Comparator<ElementBundle> {
        @Override
        public int compare(ElementBundle o1, ElementBundle o2) {
            return Integer.compare(o1.position, o2.position);
        }
    }
}
