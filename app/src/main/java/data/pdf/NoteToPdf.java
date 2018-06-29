package data.pdf;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDTrueTypeFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
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
        PDDocument document = new PDDocument();
        cache.pdfFonts = new PdfFonts(document, context.getAssets());
        cache.contentWidth = ElementsToPdf.width;
        cache.currentProfile = ProfileCatalog.getCurrentProfile(context);
        File destination = Export.getPDFFile(context, noteId);
        try {
            ArrayList<ElementsModel.Element> elements = NoteToElements.convert(noteId, cache, readableDb, progress);
            ElementsToPdf.convert(document, elements, progress);
            progress.setStatus("Saving PDF file");
            document.save(destination);
            document.close();
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

        private static void convert(PDDocument document, ArrayList<ElementsModel.Element> elements, TaskProgress progress) throws IOException {
            ArrayList<PageElements> pageElementsList = generatePageElements(document, elements);
            progress.setStatus("Generating PDF");
            for (PageElements pageElements : pageElementsList) {
                writeElements(document, pageElements);
            }
            document.addPage(new PDPage(new PDRectangle(1, 1)));
        }

        private static ArrayList<PageElements> generatePageElements(PDDocument document, ArrayList<ElementsModel.Element> elements) throws IOException {
            ArrayList<PageElements> pageElements = new ArrayList<>();
            PageElements currentPageElements = new PageElements();
            for (ElementsModel.Element element : elements) {
                float elementHeight = calculateElementHeight(document, element);
                float delta = currentPageElements.height + elementHeight - maxHeight;
                if (!Numbers.isSmall(delta) && delta > 0) {
                    pageElements.add(currentPageElements);
                    currentPageElements = new PageElements();
                }
                currentPageElements.elements.add(element);
                currentPageElements.height += elementHeight;
            }
            if (!Numbers.isPreciseSmall(currentPageElements.height)) {
                pageElements.add(currentPageElements);
            }
            return pageElements;
        }

        private static float calculateElementHeight(PDDocument document, ElementsModel.Element element) throws IOException {
            if (element instanceof ElementsModel.TextElement) {
                ElementsModel.TextElement textElement = (ElementsModel.TextElement) element;
                return textElement.size * 1.5f + textElement.indentVertical;
            } else if (element instanceof ElementsModel.PictureElement) {
                ElementsModel.PictureElement pictureElement = (ElementsModel.PictureElement) element;
                PDImageXObject imageXObject = pictureElement.getPdImage(document);
                if (imageXObject == null || imageXObject.getWidth() == 0) {
                    return 0;
                } else {
                    float height = imageXObject.getHeight() * width / imageXObject.getWidth();
                    return Math.min(height, maxHeight);
                }
            } else if (element instanceof ElementsModel.DividerElement) {
                ElementsModel.DividerElement dividerElement = (ElementsModel.DividerElement) element;
                if (dividerElement.text == null || dividerElement.text.isEmpty()) {
                    return 40f;
                } else {
                    return 20f + dividerElement.size * 1.5f;
                }
            } else {
                throw new RuntimeException("pdf element was not recognized");
            }
        }

        private static class PageElements {
            public PageElements() {
                elements = new ArrayList<>();
            }

            PDPage page;

            float startY;

            float currentTextX;
            float currentTextY;

            boolean isInTextBlock;

            float height;
            ArrayList<ElementsModel.Element> elements;

            float moveTextToX(float positionX) {
                float delta = positionX - currentTextX;
                currentTextX = positionX;
                return delta;
            }

            float moveTextToY(float positionY) {
                float delta = positionY - currentTextY;
                currentTextY = positionY;
                return delta;
            }

            float moveTextByX(float delta) {
                currentTextX += delta;
                return currentTextX;
            }

            float moveTextByY(float delta) {
                currentTextY += delta;
                return currentTextY;
            }
        }

        private static void writeElements(PDDocument document, PageElements pageElements) throws IOException {
            pageElements.page = new PDPage(new PDRectangle(absoluteWidth, pageElements.height + margin * 2));
            document.addPage(pageElements.page);
            PDPageContentStream contentStream = new PDPageContentStream(document, pageElements.page);
            pageElements.startY = pageElements.page.getMediaBox().getHeight() - margin;
            for (ElementsModel.Element element : pageElements.elements) {
                if (element instanceof ElementsModel.TextElement) {
                    ElementsModel.TextElement textElement = (ElementsModel.TextElement) element;
                    writeTextElement(textElement, document, pageElements, contentStream);
                } else if (element instanceof ElementsModel.PictureElement) {
                    ElementsModel.PictureElement pictureElement = (ElementsModel.PictureElement) element;
                    writePictureElement(pictureElement, document, pageElements, contentStream);
                }
            }
            if (pageElements.isInTextBlock) {
                contentStream.endText();
            }
            contentStream.close();
        }

        private static void writeTextElement(ElementsModel.TextElement textElement, PDDocument document,
                                             PageElements pageElements,
                                             PDPageContentStream contentStream) throws IOException {
            float deltaY = calculateElementHeight(document, textElement);
            pageElements.startY -= deltaY - 10;
            if (!pageElements.isInTextBlock) {
                contentStream.beginText();
                pageElements.isInTextBlock = true;
                pageElements.currentTextY = 0;
                pageElements.currentTextX = 0;
            }
            contentStream.newLineAtOffset(
                    pageElements.moveTextToX(margin + textElement.indentHorizontal),
                    pageElements.moveTextToY(pageElements.startY));
            pageElements.startY -= 10;

            contentStream.setNonStrokingColor(Colors.getR(textElement.color),
                    Colors.getG(textElement.color), Colors.getB(textElement.color));
            try {
                contentStream.setFont(textElement.font, textElement.size);
                contentStream.showText(textElement.text);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void writePictureElement(ElementsModel.PictureElement pictureElement, PDDocument document,
                                                PageElements pageElements,
                                                PDPageContentStream contentStream) throws IOException {
            if (pageElements.isInTextBlock) {
                contentStream.endText();
                pageElements.isInTextBlock = false;
            }
            int pHeight = (int) calculateElementHeight(document, pictureElement);
            PDImageXObject image = pictureElement.getPdImage(document);
            pageElements.startY -= pHeight;

            if (image != null) {
                int pWidth = image.getWidth() * pHeight / image.getHeight();
                float x = (width - pWidth) / 2 + margin;
                contentStream.drawImage(image, x, pageElements.startY, pWidth, pHeight);
            }
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
                    pdfElements.addAll(getTextElements(text, interpreter, cache.pdfFonts, cache.contentWidth));
                } else if (noteElement instanceof ElementList) {
                    ElementList list = (ElementList) noteElement;
                    data.model.type.Element.ListInterpreter interpreter =
                            (data.model.type.Element.ListInterpreter) typeElement.getInterpreter();
                    pdfElements.addAll(getListElements(list, interpreter, cache.pdfFonts, cache.contentWidth));
                } else if (noteElement instanceof ElementPicture) {
                    ElementPicture picture = (ElementPicture) noteElement;
                    data.model.type.Element.PictureInterpreter interpreter =
                            (data.model.type.Element.PictureInterpreter) typeElement.getInterpreter();
                    pdfElements.addAll(getPictureElements(picture, cache.currentProfile));
                } else if (noteElement instanceof ElementDivider) {
                    ElementDivider divider = (ElementDivider) noteElement;
                    data.model.type.Element.DividerInterpreter interpreter =
                            (data.model.type.Element.DividerInterpreter) typeElement.getInterpreter();
                    pdfElements.add(getDividerElement(typeElement, interpreter, cache.pdfFonts));
                }
            }
            return pdfElements;
        }

        private static ArrayList<ElementsModel.Element> getTextElements(
                ElementText text, data.model.type.Element.TextInterpreter interpreter, PdfFonts fonts,
                float lineWidth) throws IOException {
            ArrayList<ElementsModel.Element> elements = new ArrayList<>();
            float indent = interpreter.getTextSize() * 2;
            PDFont font = fonts.getFontOrDefault(interpreter.getFontName());
            ArrayList<TextLine> lines = toTextLines(text.getText(), font, interpreter.getTextSize(), lineWidth, indent);
            for (TextLine line : lines) {
                ElementsModel.TextElement textElement = new ElementsModel.TextElement();
                textElement.text = line.text;
                if (line.isFirstLine() && !line.isLastLine()) {
                    textElement.indentHorizontal = indent;
                }
                if (line.isFirstLine()) {
                    textElement.indentVertical = interpreter.getTextSize() * 0.8f;
                }
                textElement.font = font;
                textElement.bold = interpreter.isBold();
                textElement.italic = interpreter.isItalic();
                textElement.color = interpreter.getColor();
                textElement.size = interpreter.getTextSize();
                elements.add(textElement);
            }
            return elements;
        }

        private static ArrayList<ElementsModel.Element> getListElements(
                ElementList list, data.model.type.Element.ListInterpreter interpreter, PdfFonts fonts,
                float lineWidth) throws IOException {
            ArrayList<ElementsModel.Element> elements = new ArrayList<>();
            float indent = interpreter.getTextSize() * 2;
            PDFont font = fonts.getFontOrDefault(interpreter.getFontName());
            for (int i = 0; i < list.getItemCount(); i++) {
                ElementList.ListItem listItem = list.getItemAt(i);
                ArrayList<TextLine> lines = toTextLines(listItem.getText(), font, interpreter.getTextSize(), lineWidth, indent);
                for (TextLine line : lines) {
                    ElementsModel.TextElement textElement = new ElementsModel.TextElement();
                    textElement.text = line.text;
                    if (line.isFirstLine()) {
                        textElement.indentHorizontal = indent;
                        textElement.indentVertical = interpreter.getTextSize() * 0.8f;
                        if (interpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_BULLETS) {
                            textElement.text = "- " + textElement.text;
                        } else if (interpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_BULLETS_EMPTY) {
                            textElement.text = "- " + textElement.text;
                        } else if (interpreter.getListType() == data.model.type.Element.ListInterpreter.LIST_TYPE_NUMBERS) {
                            textElement.text = TypeFaceUtils.withNumberFormat(i + 1) + ". " + textElement.text;
                        }
                    }
                    textElement.font = font;
                    textElement.bold = interpreter.isBold();
                    textElement.italic = interpreter.isItalic();
                    textElement.color = interpreter.getColor();
                    textElement.size = interpreter.getTextSize();
                    elements.add(textElement);
                }
            }
            return elements;
        }

        private static ElementsModel.Element getDividerElement(
                data.model.type.Element element, data.model.type.Element.DividerInterpreter interpreter, PdfFonts fonts) {
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
                dividerElement.font = fonts.getFontOrDefault(interpreter.getFontName());
                dividerElement.bold = interpreter.isBold();
                dividerElement.italic = interpreter.isItalic();
                dividerElement.color = interpreter.getColor();
                dividerElement.size = interpreter.getTextSize();
            }
            return dividerElement;
        }

        private static ArrayList<ElementsModel.Element> getPictureElements(ElementPicture picture, Profile currentProfile) {
            ArrayList<ElementsModel.Element> pictureElements = new ArrayList<>();
            for (int i = 0; i < picture.getItemCount(); i++){
                ElementPicture.PictureItem item = picture.getItemAt(i);
                ElementsModel.PictureElement pictureElement = new ElementsModel.PictureElement();
                pictureElement.pictureFile = Pictures.getPictureFile(currentProfile, item.getPictureId());
                pictureElements.add(pictureElement);
            }
            return pictureElements;
        }

        private static ArrayList<TextLine> toTextLines(String text, PDFont font, int fontSize, float lineWidth, float indent)
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
                    float size = fontSize * font.getStringWidth(testLine) / 1000;
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
        PdfFonts pdfFonts;
        float contentWidth;
        Profile currentProfile;
    }

    private static class ElementsModel {

        private interface Element {

        }

        private static class TextElement implements Element {
            private String text;
            private float indentHorizontal;
            private float indentVertical;
            private int color;
            private int size;
            private boolean bold;
            private boolean italic;
            private PDFont font;
        }

        private static class PictureElement implements Element {
            private File pictureFile;
            private PDImageXObject pdImage;
            PDImageXObject getPdImage(PDDocument document) throws IOException {
                if (pdImage == null) {
                    pdImage = PDImageXObject.createFromFile(pictureFile.getAbsolutePath(), document);
                }
                return pdImage;
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
            private int color;
            private int size;
            private boolean bold;
            private boolean italic;
            private PDFont font;
        }
    }
}
