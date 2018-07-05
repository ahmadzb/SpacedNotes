package data.pdf;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import data.model.note.ElementCatalog;
import data.model.note.ElementDivider;
import data.model.note.ElementList;
import data.model.note.ElementPicture;
import data.model.note.ElementText;
import data.model.note.Note;
import data.model.note.NoteCatalog;
import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;
import data.storage.Captures;
import data.storage.Export;
import data.storage.Pictures;
import util.Colors;
import util.Concurrent.TaskProgress;
import util.Numbers;
import util.TypeFaceUtils;

/**
 * Created by Ahmad on 06/22/18.
 * All rights reserved.
 */
public class NoteToPdf {

    public static File noteToPdf(long noteId, SQLiteDatabase readableDb, Context context, TaskProgress progress) {
        Cache cache = new Cache();
        cache.assetManager = context.getAssets();
        cache.contentWidth = ElementsToPdf.width;
        cache.currentProfile = ProfileCatalog.getCurrentProfile(context);
        File destination = Export.getPDFFile(context, noteId, cache.currentProfile.getId());
        destination.delete();
        try {
            PdfDocument document = new PdfDocument();
            ArrayList<ElementsModel.Element> elements = NoteToElements.convert(noteId, cache, readableDb, progress);
            ElementsToPdf.convert(document, elements, progress);
            progress.setStatus("Saving PDF file");
            FileOutputStream outputStream = new FileOutputStream(destination);
            document.writeTo(outputStream);
            document.close();
            outputStream.close();
            progress.setStatus("PDF created successfully");
        } catch (IOException e) {
            e.printStackTrace();
            progress.setStatus("Failure; PDF not created");
            destination = null;
        }
        return destination;
    }

    private static class ElementsToPdf {
        private static final float absoluteWidth = 595.27563f;
        private static final float margin = 30f;
        private static final float absoluteMaxHeight = 841.8898f;
        private static final float width = absoluteWidth - margin * 2;
        private static final float maxHeight = absoluteMaxHeight - margin * 2;

        private static void convert(PdfDocument document, ArrayList<ElementsModel.Element> elements, TaskProgress progress) throws IOException {
            ArrayList<PageElements> pageElementsList = generatePageElements(elements);
            progress.setStatus("Generating PDF");
            int pageNumber = 1;
            for (PageElements pageElements : pageElementsList) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                        (int) (absoluteWidth + 1),
                        (int) (pageElements.height + 1 + margin * 2),
                        pageNumber).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                pageElements.canvas = page.getCanvas();
                writeElements(pageElements);
                document.finishPage(page);
                pageNumber++;
            }
        }

        private static ArrayList<PageElements> generatePageElements(
                ArrayList<ElementsModel.Element> elements){
            ArrayList<PageElements> pageElements = new ArrayList<>();
            PageElements currentPageElements = new PageElements();
            for (ElementsModel.Element element : elements) {
                element.setHeight(measureElementHeight(element));
                float delta = currentPageElements.height + element.getHeight() - maxHeight;
                if (!Numbers.isSmall(delta) && delta > 0) {
                    pageElements.add(currentPageElements);
                    currentPageElements = new PageElements();
                }
                currentPageElements.elements.add(element);
                currentPageElements.height += element.getHeight();
            }
            if (!Numbers.isPreciseSmall(currentPageElements.height)) {
                pageElements.add(currentPageElements);
            }
            return pageElements;
        }

        private static float measureElementHeight(ElementsModel.Element element) {
            if (element instanceof ElementsModel.TextElement) {
                ElementsModel.TextElement textElement = (ElementsModel.TextElement) element;
                return textElement.paint.getTextSize() * 1.5f + textElement.indentVertical;
            } else if (element instanceof ElementsModel.PictureElement) {
                ElementsModel.PictureElement pictureElement = (ElementsModel.PictureElement) element;
                Bitmap bitmap = pictureElement.getBitmap();
                if (bitmap == null || bitmap.getWidth() == 0) {
                    return 0;
                } else {
                    float height = bitmap.getHeight() * width / bitmap.getWidth();
                    return Math.min(height, maxHeight);
                }
            } else if (element instanceof ElementsModel.DividerElement) {
                ElementsModel.DividerElement dividerElement = (ElementsModel.DividerElement) element;
                if (dividerElement.text == null || dividerElement.text.isEmpty()) {
                    return 40f;
                } else {
                    return 20f + dividerElement.paint.getTextSize() * 1.5f;
                }
            } else {
                throw new RuntimeException("pdf element was not recognized");
            }
        }

        private static class PageElements {
            public PageElements() {
                this.elements = new ArrayList<>();
                this.startY = margin;
            }

            float height;
            ArrayList<ElementsModel.Element> elements;

            Canvas canvas;
            float startY;
        }

        private static void writeElements(PageElements pageElements) {
            for (ElementsModel.Element element : pageElements.elements) {
                if (element instanceof ElementsModel.TextElement) {
                    ElementsModel.TextElement textElement = (ElementsModel.TextElement) element;
                    writeTextElement(textElement, pageElements);
                } else if (element instanceof ElementsModel.PictureElement) {
                    ElementsModel.PictureElement pictureElement = (ElementsModel.PictureElement) element;
                    writePictureElement(pictureElement, pageElements);
                } else if (element instanceof ElementsModel.DividerElement) {
                    ElementsModel.DividerElement dividerElement = (ElementsModel.DividerElement) element;
                    writeDividerElement(dividerElement, pageElements);
                }
            }
        }

        private static void writeTextElement(
                ElementsModel.TextElement textElement, PageElements pageElements) {
            pageElements.canvas.drawText(
                    textElement.text,
                    margin + textElement.indentHorizontal,
                    pageElements.startY + textElement.getHeight() - 15f,
                    textElement.paint);
            pageElements.startY += textElement.getHeight();
        }

        private static void writePictureElement(
                ElementsModel.PictureElement pictureElement, PageElements pageElements) {
            Bitmap bitmap = pictureElement.getBitmap();
            if (bitmap != null) {
                float scale = pictureElement.getHeight() / pictureElement.getBitmap().getHeight();
                float rectWidth = pictureElement.getBitmap().getWidth() * scale;
                float rectHeight = pictureElement.getHeight();
                Rect rect = new Rect(
                        (int) (margin + (width - rectWidth) / 2),
                        (int) (pageElements.startY),
                        (int) (margin + width - (width - rectWidth) / 2),
                        (int) (pageElements.startY + rectHeight)
                );
                pageElements.canvas.drawBitmap(bitmap, null, rect, pictureElement.paint);
            }
            pageElements.startY += pictureElement.getHeight();
        }

        private static void writeDividerElement(
                ElementsModel.DividerElement dividerElement, PageElements pageElements) {
            if (dividerElement.divider == ElementsModel.DividerElement.DIVIDER_LINE) {
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(0x88000000);
                paint.setStrokeWidth(1f);
                paint.setStyle(Paint.Style.STROKE);
                pageElements.canvas.drawLine(margin, pageElements.startY + 10f,
                        margin + width, pageElements.startY + 10f, paint);
            } else if (dividerElement.divider == ElementsModel.DividerElement.DIVIDER_DASH_LINE) {
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(0x88000000);
                paint.setStrokeWidth(1f);
                paint.setStyle(Paint.Style.STROKE);
                paint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));
                pageElements.canvas.drawLine(margin, pageElements.startY + 10f,
                        margin + width, pageElements.startY + 10f, paint);
            } else if (dividerElement.divider == ElementsModel.DividerElement.DIVIDER_SPACE) {
                //Nothing to do
            } else if (dividerElement.divider == ElementsModel.DividerElement.DIVIDER_TITLE) {
                pageElements.canvas.drawText(
                        dividerElement.text,
                        margin + 15f,
                        pageElements.startY + dividerElement.getHeight() - 15f,
                        dividerElement.paint);
            } else if (dividerElement.divider == ElementsModel.DividerElement.DIVIDER_TITLE_BACKGROUND) {
                Paint backgroundPaint = new Paint();
                backgroundPaint.setColor(0x22000000);
                backgroundPaint.setStyle(Paint.Style.FILL);
                pageElements.canvas.drawRect(
                        new Rect((int) margin, (int) pageElements.startY, (int) (margin + width),
                                (int) (pageElements.startY + dividerElement.getHeight())),
                        backgroundPaint);
                pageElements.canvas.drawText(
                        dividerElement.text,
                        margin + 15f,
                        pageElements.startY  + dividerElement.getHeight() - 15f,
                        dividerElement.paint);
            }
            pageElements.startY += dividerElement.getHeight();
        }

    }

    private static class NoteToElements {
        private static ArrayList<ElementsModel.Element> convert(long noteId, Cache cache, SQLiteDatabase readableDb, TaskProgress progress)
                throws IOException {
            progress.setStatus("preparing the note for PDF export");
            Note note = NoteCatalog.getNoteById(noteId, readableDb);
            TreeMap<Long, data.model.type.Element> typeElementMap = data.model.type.ElementCatalog
                    .getElementMap(note.getTypeId(), readableDb);
            ArrayList<data.model.note.Element> noteElements = ElementCatalog.getNoteElements(note, readableDb);
            Collections.sort(noteElements, data.model.note.Element.getPositionComparator());

            ArrayList<ElementsModel.Element> pdfElements = new ArrayList<>(noteElements.size() * 4);
            for (data.model.note.Element noteElement : noteElements) {
                data.model.type.Element typeElement = typeElementMap.get(noteElement.getElementId());
                if (typeElement == null) {
                    throw new RuntimeException("Type for note element was not found");
                }
                if (noteElement instanceof ElementText) {
                    ElementText text = (ElementText) noteElement;
                    data.model.type.Element.TextInterpreter interpreter =
                            (data.model.type.Element.TextInterpreter) typeElement.getInterpreter();
                    pdfElements.addAll(getTextElements(text, interpreter, cache.assetManager, cache.contentWidth));
                } else if (noteElement instanceof ElementList) {
                    ElementList list = (ElementList) noteElement;
                    data.model.type.Element.ListInterpreter interpreter =
                            (data.model.type.Element.ListInterpreter) typeElement.getInterpreter();
                    pdfElements.addAll(getListElements(list, interpreter, cache.assetManager, cache.contentWidth));
                } else if (noteElement instanceof ElementPicture) {
                    ElementPicture picture = (ElementPicture) noteElement;
                    data.model.type.Element.PictureInterpreter interpreter =
                            (data.model.type.Element.PictureInterpreter) typeElement.getInterpreter();
                    pdfElements.addAll(getPictureElements(picture, cache.currentProfile));
                } else if (noteElement instanceof ElementDivider) {
                    ElementDivider divider = (ElementDivider) noteElement;
                    data.model.type.Element.DividerInterpreter interpreter =
                            (data.model.type.Element.DividerInterpreter) typeElement.getInterpreter();
                    pdfElements.add(getDividerElement(typeElement, interpreter, cache.assetManager));
                }
            }
            return pdfElements;
        }

        private static ArrayList<ElementsModel.Element> getTextElements(
                ElementText text, data.model.type.Element.TextInterpreter interpreter, AssetManager assets,
                float lineWidth) throws IOException {
            ArrayList<ElementsModel.Element> elements = new ArrayList<>();
            float indent = interpreter.getTextSize() * 2;
            Paint paint;
            {
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                Typeface typeface = TypeFaceUtils.getFont(assets, interpreter.getFontName());
                int style = 0;
                if (interpreter.isBold()) style |= Typeface.BOLD;
                if (interpreter.isItalic()) style |= Typeface.ITALIC;
                typeface = Typeface.create(typeface, style);
                paint.setTypeface(typeface);
                paint.setColor(interpreter.getColor());
                paint.setTextSize(interpreter.getTextSize());
            }
            ArrayList<TextLine> lines = toTextLines(text.getText(), paint, interpreter.getTextSize(), lineWidth, indent);
            for (TextLine line : lines) {
                ElementsModel.TextElement textElement = new ElementsModel.TextElement();
                textElement.text = line.text;
                if (line.isFirstLine() && !line.isLastLine()) {
                    textElement.indentHorizontal = indent;
                }
                if (line.isFirstLine()) {
                    textElement.indentVertical = interpreter.getTextSize() * 0.8f;
                }
                textElement.paint = paint;
                elements.add(textElement);
            }
            return elements;
        }

        private static ArrayList<ElementsModel.Element> getListElements(
                ElementList list, data.model.type.Element.ListInterpreter interpreter, AssetManager assets,
                float lineWidth) throws IOException {
            ArrayList<ElementsModel.Element> elements = new ArrayList<>();
            float indent = interpreter.getTextSize() * 2;
            Paint paint;
            {
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                Typeface typeface = TypeFaceUtils.getFont(assets, interpreter.getFontName());
                int style = 0;
                if (interpreter.isBold()) style |= Typeface.BOLD;
                if (interpreter.isItalic()) style |= Typeface.ITALIC;
                typeface = Typeface.create(typeface, style);
                paint.setTypeface(typeface);
                paint.setColor(interpreter.getColor());
                paint.setTextSize(interpreter.getTextSize());
            }
            for (int i = 0; i < list.getItemCount(); i++) {
                ElementList.ListItem listItem = list.getItemAt(i);
                ArrayList<TextLine> lines = toTextLines(listItem.getText(), paint, interpreter.getTextSize(), lineWidth, indent);
                for (TextLine line : lines) {
                    ElementsModel.TextElement textElement = new ElementsModel.TextElement();
                    textElement.text = line.text;
                    if (line.isFirstLine()) {
                        textElement.indentHorizontal = indent;
                        textElement.indentVertical = interpreter.getTextSize() * 0.8f;
                        if (interpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_BULLETS) {
                            textElement.text = "• " + textElement.text;
                        } else if (interpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_BULLETS_EMPTY) {
                            textElement.text = "○ " + textElement.text;
                        } else if (interpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_NUMBERS) {
                            textElement.text = TypeFaceUtils.withNumberFormat(i + 1) + ". " + textElement.text;
                        }
                    }
                    textElement.paint = paint;
                    elements.add(textElement);
                }
            }
            return elements;
        }

        private static ElementsModel.Element getDividerElement(
                data.model.type.Element element, data.model.type.Element.DividerInterpreter interpreter, AssetManager assets) {
            ElementsModel.DividerElement dividerElement = new ElementsModel.DividerElement();
            boolean hasText = false;
            if (interpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_LINE) {
                dividerElement.divider = ElementsModel.DividerElement.DIVIDER_LINE;
            } else if (interpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_DASHED_LINE) {
                dividerElement.divider = ElementsModel.DividerElement.DIVIDER_DASH_LINE;
            } else if (interpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_SPACE) {
                dividerElement.divider = ElementsModel.DividerElement.DIVIDER_SPACE;
            } else if (interpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_TITLE) {
                dividerElement.divider = ElementsModel.DividerElement.DIVIDER_TITLE;
                hasText = true;
            } else if (interpreter.getDividerType() == data.model.type.Element.DividerInterpreter.DIVIDER_TYPE_TITLE_BACKGROUND) {
                dividerElement.divider = ElementsModel.DividerElement.DIVIDER_TITLE_BACKGROUND;
                hasText = true;
            } else {
                dividerElement.divider = ElementsModel.DividerElement.DIVIDER_SPACE;
            }
            if (hasText) {
                dividerElement.text = element.getTitle();
                dividerElement.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                Typeface typeface = TypeFaceUtils.getFont(assets, interpreter.getFontName());
                int style = 0;
                if (interpreter.isBold()) style |= Typeface.BOLD;
                if (interpreter.isItalic()) style |= Typeface.ITALIC;
                typeface = Typeface.create(typeface, style);
                dividerElement.paint.setTypeface(typeface);
                dividerElement.paint.setColor(interpreter.getColor());
                dividerElement.paint.setTextSize(interpreter.getTextSize());
            }
            return dividerElement;
        }

        private static ArrayList<ElementsModel.Element> getPictureElements(ElementPicture picture, Profile currentProfile) {
            ArrayList<ElementsModel.Element> pictureElements = new ArrayList<>();
            for (int i = 0; i < picture.getItemCount(); i++) {
                ElementPicture.PictureItem item = picture.getItemAt(i);
                ElementsModel.PictureElement pictureElement = new ElementsModel.PictureElement();
                pictureElement.pictureFile = Pictures.getPictureFile(currentProfile, item.getPictureId());
                pictureElement.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                pictureElements.add(pictureElement);
            }
            return pictureElements;
        }

        private static ArrayList<TextLine> toTextLines(String text, Paint paint, int fontSize, float lineWidth, float indent)
                throws IOException {
            ArrayList<TextLine> lines = new ArrayList<>();
            String[] paragraphs = text.split("\n");
            for (String paragraph : paragraphs) {
                StringBuilder remainText = new StringBuilder(paragraph);
                StringBuilder nextLineText = new StringBuilder();

                boolean firstLine = true;
                while (remainText.length() > 0) {
                    int spaceIndex = remainText.indexOf(" ");
                    String nextWord;
                    if (spaceIndex < 0) {
                        nextWord = remainText.toString();
                        remainText.setLength(0);
                    } else {
                        nextWord = remainText.substring(0, spaceIndex + 1);
                        remainText.replace(0, spaceIndex + 1, "");
                    }
                    String testLine = nextLineText.toString() + nextWord;
                    float size = paint.measureText(testLine);
                    if (firstLine) {
                        size += indent;
                    }
                    if (size > lineWidth) {
                        lines.add(new TextLine(nextLineText.toString(), firstLine, remainText.length() <= 0));
                        nextLineText.setLength(0);
                        firstLine = false;
                    }
                    nextLineText.append(nextWord);
                }
                if (nextLineText.length() > 0) {
                    lines.add(new TextLine(nextLineText.toString(), firstLine, true));
                }
            }

            return lines;
        }

        private static class TextLine {
            String text;
            boolean firstLine;
            boolean lastLine;

            public TextLine(String text, boolean firstLine, boolean lastLine) {
                this.text = text;
                this.firstLine = firstLine;
                this.lastLine = lastLine;
            }

            public boolean isFirstLine() {
                return firstLine;
            }

            public void setFirstLine(boolean firstLine) {
                this.firstLine = firstLine;
            }

            public boolean isLastLine() {
                return lastLine;
            }

            public void setLastLine(boolean lastLine) {
                this.lastLine = lastLine;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }
        }
    }

    private static class Cache {
        AssetManager assetManager;
        float contentWidth;
        Profile currentProfile;
    }

    private static class ElementsModel {

        private interface Element {
            float getHeight();

            void setHeight(float height);
        }

        private static class TextElement implements Element {
            private String text;
            private float indentHorizontal;
            private float indentVertical;
            private Paint paint;
            private float height;

            @Override
            public float getHeight() {
                return height;
            }

            @Override
            public void setHeight(float height) {
                this.height = height;
            }
        }

        private static class PictureElement implements Element {
            private File pictureFile;
            private Bitmap bitmap;
            private Paint paint;
            private float height;

            private Bitmap getBitmap() {
                if (bitmap == null) {
                    if (pictureFile != null && pictureFile.exists()) {
                        bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                    }
                }
                return bitmap;
            }

            @Override
            public float getHeight() {
                return height;
            }

            @Override
            public void setHeight(float height) {
                this.height = height;
            }
        }

        private static class DividerElement implements Element {
            public static final int DIVIDER_LINE = 0;
            public static final int DIVIDER_DASH_LINE = 1;
            public static final int DIVIDER_SPACE = 2;
            public static final int DIVIDER_TITLE = 3;
            public static final int DIVIDER_TITLE_BACKGROUND = 4;

            private int divider;
            private String text;
            private Paint paint;
            private float height;

            @Override
            public float getHeight() {
                return height;
            }

            @Override
            public void setHeight(float height) {
                this.height = height;
            }
        }
    }
}
