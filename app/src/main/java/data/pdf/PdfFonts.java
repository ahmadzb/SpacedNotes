package data.pdf;

import android.content.res.AssetManager;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.font.PDType3Font;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import util.TypeFaceUtils;

/**
 * Created by Ahmad on 06/22/18.
 * All rights reserved.
 */
public class PdfFonts {
    private PDDocument document;
    private AssetManager assetManager;
    private TreeMap<String, PDFont> cache = new TreeMap<>();

    public PdfFonts(PDDocument document, AssetManager assetManager) {
        this.document = document;
        this.assetManager = assetManager;
    }

    public PDFont getFontOrDefault(String fontName) {
        PDFont font = getFont(fontName);
        if (font == null) {
            font = getDefaultFont();
        }
        return font;
    }

    public PDFont getFont(String fontName) {
        PDFont font = cache.get(fontName);
        if (font == null) {
            try {
                InputStream in = TypeFaceUtils.getFontInputStream(assetManager, fontName);
                font = PDType0Font.load(document, in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return font;
    }

    public PDFont getDefaultFont() {
        return getFont(TypeFaceUtils.FONT_ROBOTO);
    }
}
