package com.diplinkblaze.spacednote.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.contract.ContentUpdateListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeMap;

import data.database.OpenHelper;
import data.model.note.Element;
import data.model.note.ElementCatalog;
import data.model.note.ElementDivider;
import data.model.note.ElementList;
import data.model.note.ElementPicture;
import data.model.note.ElementText;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.profiles.ProfileCatalog;
import data.storage.Pictures;
import util.Measures;
import util.TypeFaceUtils;

public class NoteContentViewFragment extends Fragment implements ContentUpdateListener{

    private static final String KEY_NOTE_ID = "noteId";

    public NoteContentViewFragment() {
        // Required empty public constructor
    }

    public static NoteContentViewFragment newInstance() {
        NoteContentViewFragment fragment = new NoteContentViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_note_content_view, container, false);
        updateContentView(contentView);
        return contentView;
    }

    private void tryUpdateContentView() {
        View contentView = getView();
        if (contentView != null) {
            updateContentView(contentView);
        }
    }

    private void updateContentView(View contentView) {
        Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_NOTE_ID)) {
            //Layout
            LinearLayout layout = contentView.findViewById(R.id.fragment_note_content_view_frame);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            //Note elements
            SQLiteDatabase database = OpenHelper.getDatabase(getContext());
            Note note = NoteCatalog.getNoteById(args.getLong(KEY_NOTE_ID), database);
            ArrayList<Element> elements = ElementCatalog.getNoteElements(note, database);
            Collections.sort(elements, new Element.PositionComparator());
            //Type elements
            TreeMap<Long, data.model.type.Element> typeElementMap =
                    data.model.type.ElementCatalog.getElementMap(note.getTypeId(), database);

            layout.removeAllViews();
            for (Element element : elements) {
                data.model.type.Element typeElement = typeElementMap.get(element.getElementId());
                if (element instanceof ElementText) {
                    insertTextElement((ElementText) element, typeElement, layout, inflater);
                } else if (element instanceof ElementPicture) {
                    insertPictureElement((ElementPicture) element, typeElement, layout, inflater);
                } else if (element instanceof ElementList) {
                    insertListElement((ElementList) element, typeElement, layout, inflater);
                } else if (element instanceof ElementDivider) {
                    insertDividerElement((ElementDivider) element, typeElement, layout, inflater);
                } else {
                    throw new RuntimeException("Cannot recognize element type");
                }
            }
        }
    }

    @Override
    public void updateContent() {
        tryUpdateContentView();
    }

    private void insertTextElement(ElementText elementText, data.model.type.Element typeElement,
                                   LinearLayout layout, LayoutInflater inflater) {
        View elementView = inflater.inflate(R.layout.partial_note_view_text, layout, false);
        TextView textView = elementView.findViewById(R.id.partial_note_view_text_text);
        data.model.type.Element.TextInterpreter textInterpreter =
                (data.model.type.Element.TextInterpreter) typeElement.getInterpreter();
        textView.setText(elementText.getText());
        textView.setTextSize(textInterpreter.getTextSize());
        textView.setTypeface(TypeFaceUtils.getFont(getResources().getAssets(), textInterpreter.getFontName()));
        textView.setTextColor(textInterpreter.getColor());
        if (textInterpreter.isBold() && textInterpreter.isItalic()) {
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD_ITALIC);
        } else if (textInterpreter.isBold()) {
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        } else if (textInterpreter.isItalic()) {
            textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
        } else {
            textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
        }
        layout.addView(elementView);
    }

    private void insertPictureElement(ElementPicture elementPicture, data.model.type.Element typeElement,
                                      LinearLayout layout, LayoutInflater inflater) {
        View elementView = inflater.inflate(R.layout.partial_note_view_picture_container, layout, false);
        LinearLayout pictureLayout = elementView.findViewById(R.id.partial_note_view_picture_container_frame);
        long profileId = ProfileCatalog.getCurrentProfile(getContext()).getId();
        for (ElementPicture.PictureItem pictureItem : elementPicture.getSortedList()) {
            long pictureId = pictureItem.getPictureId();
            final File pictureFile = Pictures.getPictureFile(profileId, pictureId);
            if (pictureFile.exists()) {
                final View pictureView = inflater.inflate(R.layout.partial_note_view_picture_item, pictureLayout, false);
                pictureLayout.addView(pictureView);
                AsyncTask<Void, Void, Drawable> picLoadTask = new AsyncTask<Void, Void, Drawable>() {
                    @Override
                    protected Drawable doInBackground(Void... voids) {
                        Drawable drawable = Drawable.createFromPath(pictureFile.getAbsolutePath());
                        return drawable;
                    }

                    @Override
                    protected void onPostExecute(final Drawable drawable) {
                        final ImageView imageView = pictureView.findViewById(R.id.partial_note_view_picture_item_picture);
                        if (imageView != null) {
                            imageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    int width = imageView.getMeasuredWidth();
                                    int intWidth = drawable.getIntrinsicWidth();
                                    int intHeight = drawable.getIntrinsicHeight();
                                    if (width != 0 && intWidth != 0 && intHeight != 0) {
                                        width = width - Measures.dpToPx(0, getContext());
                                        int height = (width * intHeight) / intWidth;

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
                picLoadTask.execute();
            }
        }
        layout.addView(elementView);
    }

    private void insertListElement(ElementList elementList, data.model.type.Element typeElement,
                                   LinearLayout layout, LayoutInflater inflater) {
        View elementView = inflater.inflate(R.layout.partial_note_view_list_container, layout, false);
        LinearLayout listLayout = elementView.findViewById(R.id.partial_note_view_list_container_frame);
        data.model.type.Element.ListInterpreter listInterpreter =
                (data.model.type.Element.ListInterpreter) typeElement.getInterpreter();
        ArrayList<ElementList.ListItem> listItems = elementList.getSortedList();
        for (int i = 0; i < listItems.size(); i++) {
            ElementList.ListItem listItem = listItems.get(i);
            View listItemView = inflater.inflate(R.layout.partial_note_view_list_item, listLayout, false);
            TextView listItemText = listItemView.findViewById(R.id.partial_note_view_list_item_text);
            TextView listItemSymbol = listItemView.findViewById(R.id.partial_note_view_list_item_symbol);
            listItemText.setText(listItem.getText());
            if (listInterpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_BULLETS) {
                listItemSymbol.setText("•");
            } else if (listInterpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_BULLETS_EMPTY) {
                listItemSymbol.setText("○");
            } else if (listInterpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_NUMBERS) {
                listItemSymbol.setText(TypeFaceUtils.withNumberFormat(i + 1));
            }
            for (int j = 0; j < 2; j++) {
                TextView textView;
                if (j == 0)
                    textView = listItemText;
                else
                    textView = listItemSymbol;
                textView.setTextColor(listInterpreter.getColor());
                textView.setTypeface(TypeFaceUtils.getFont(getResources().getAssets(), listInterpreter.getFontName()));
                textView.setTextSize(listInterpreter.getTextSize());
                if (listInterpreter.isBold() && listInterpreter.isItalic()) {
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD_ITALIC);
                } else if (listInterpreter.isBold()) {
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                } else if (listInterpreter.isItalic()) {
                    textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
                } else {
                    textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
                }
            }
            listLayout.addView(listItemView);
        }
        layout.addView(elementView);
    }

    private void insertDividerElement(ElementDivider elementDivider, data.model.type.Element typeElement,
                                      LinearLayout layout, LayoutInflater inflater) {

        View elementView;

        data.model.type.Element.DividerInterpreter dividerInterpreter =
                (data.model.type.Element.DividerInterpreter) typeElement.getInterpreter();

        if (dividerInterpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_LINE) {
            elementView = inflater.inflate(R.layout.partial_note_divider_line, layout, false);
        } else if (dividerInterpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_DASHED_LINE) {
            elementView = inflater.inflate(R.layout.partial_note_divider_dashed_line, layout, false);
        } else if (dividerInterpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_TITLE) {
            elementView = inflater.inflate(R.layout.partial_note_divider_title, layout, false);
        } else if (dividerInterpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_TITLE_BACKGROUND) {
            elementView = inflater.inflate(R.layout.partial_note_divider_title_background, layout, false);
        } else if (dividerInterpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_SPACE) {
            elementView = inflater.inflate(R.layout.partial_note_divider_space, layout, false);
        } else {
            throw new RuntimeException("divider type was not recognized");
        }

        TextView textView = elementView.findViewById(R.id.partial_note_divide_text);
        if (textView != null) {
            textView.setText(typeElement.getTitle());
            textView.setTextSize(dividerInterpreter.getTextSize());
            textView.setTextColor(dividerInterpreter.getColor());
            TypeFaceUtils.setTypefaceCascade(getResources().getAssets(), textView, dividerInterpreter.getFontName());
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
        layout.addView(elementView);
    }

    public void setContent(Note note) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
            setArguments(args);
        }
        args.putLong(KEY_NOTE_ID, note.getId());
        tryUpdateContentView();
    }
}
