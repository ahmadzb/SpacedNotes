package data.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Ahmad on 06/22/18.
 * All rights reserved.
 */
public class NoteToPdf {

    private ArrayList<String> toTextLines(String texts, PDFont font, int fontSize, float lineWidth)
            throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        StringBuilder remainText = new StringBuilder(text);

        float leading = 1.5f * fontSize;

        int lastSpaceIndex = -1;
        while (remainText.length() > 0) {
            int nextSpaceIndex = remainText.indexOf(" ", lastSpaceIndex + 1);
            if (nextSpaceIndex < 0)
                nextSpaceIndex = remainText.length();
            String nextLineText = remainText.substring(0, nextSpaceIndex);
            float size = fontSize * font.getStringWidth(nextLineText) / 1000;
            if (size > lineWidth) {
                if (lastSpaceIndex < 0)
                    lastSpaceIndex = nextSpaceIndex;
                nextLineText = remainText.substring(0, lastSpaceIndex);
                lines.add(nextLineText);
                remainText.replace(0, lastSpaceIndex, "");
                text = text.substring(lastSpaceIndex).trim();
                lastSpaceIndex = -1;
            } else if (nextSpaceIndex == text.length()) {
                lines.add(text);
                text = "";
            } else {
                lastSpaceIndex = nextSpaceIndex;
            }
        }

        return lines;
    }

    private void text() throws IOException {
        PDDocument doc = null;
        try {
            doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);

            PDFont pdfFont = PDType1Font.HELVETICA;
            float fontSize = 25;
            float leading = 1.5f * fontSize;

            PDRectangle mediabox = page.getMediaBox();
            float margin = 72;
            float width = mediabox.getWidth() - 2 * margin;
            float startX = mediabox.getLowerLeftX() + margin;
            float startY = mediabox.getUpperRightY() - margin;

            String text = "I am trying to create a PDF file with a lot of text contents in the document. I am using PDFBox";
            ArrayList<String> lines = new ArrayList<String>();
            int lastSpace = -1;
            while (text.length() > 0) {
                int spaceIndex = text.indexOf(' ', lastSpace + 1);
                if (spaceIndex < 0)
                    spaceIndex = text.length();
                String subString = text.substring(0, spaceIndex);
                float size = fontSize * pdfFont.getStringWidth(subString) / 1000;
                System.out.printf("'%s' - %f of %f\n", subString, size, width);
                if (size > width) {
                    if (lastSpace < 0)
                        lastSpace = spaceIndex;
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    text = text.substring(lastSpace).trim();
                    System.out.printf("'%s' is line\n", subString);
                    lastSpace = -1;
                } else if (spaceIndex == text.length()) {
                    lines.add(text);
                    System.out.printf("'%s' is line\n", text);
                    text = "";
                } else {
                    lastSpace = spaceIndex;
                }
            }

            contentStream.beginText();
            contentStream.setFont(pdfFont, fontSize);
            contentStream.newLineAtOffset(startX, startY);
            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -leading);
            }
            contentStream.endText();
            contentStream.close();

            doc.save(new File(RESULT_FOLDER, "break-long-string.pdf"));
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }
}
