package util;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Created by Ahmad on 09/30/17.
 * All rights reserved.
 */

public class TypeFaceUtils {
    public static final String FONT_ALEGREYA = "Alegreya.ttf";
    public static final String FONT_ALEGREYA_SANS = "AlegreyaSans.ttf";
    public static final String FONT_MERRIWEATHER = "Merriweather.ttf";
    public static final String FONT_MERRIWEATHER_SANS = "MerriweatherSans.ttf";
    public static final String FONT_NUNITO = "Nunito.ttf";
    public static final String FONT_NUNITO_SANS = "NunitoSans.ttf";
    public static final String FONT_QUATTROCENTO = "Quattrocento.ttf";
    public static final String FONT_QUATTROCENTO_SANS = "QuattrocentoSans.ttf";
    public static final String FONT_ROBOTO = "Roboto.ttf";
    public static final String FONT_ROBOTO_MONO = "RobotoMono.ttf";
    public static final String FONT_ROBOTO_SLAB = "RobotoSlab.ttf";

    private static TreeMap<String, Typeface> fontCache = new TreeMap<String, Typeface>();
    private static NumberFormat persianNumberFormat;
    private static DecimalFormat persianDecimalFormat;
    private static DecimalFormat persianDecimalFormatEnglishSymbols;

    public static InputStream getFontInputStream(AssetManager assets, String fontName) throws IOException {
        return assets.open("fonts/" + fontName);
    }

    public static Typeface getFont(AssetManager assets, String fontName) {
        Typeface tf = fontCache.get(fontName);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(assets, "fonts/" + fontName);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(fontName, tf);
        }
        return tf;
    }

    public static Typeface getDefaultFont(AssetManager assets) {
        return getFont(assets, FONT_ROBOTO);
    }

    public static void setTypefaceCascade(AssetManager assets, View view, String fontName) {
        Typeface font = getFont(assets, fontName);
        if (font == null)
            return;
        setTypefaceCascade(view, font);
    }
    private static void setTypefaceCascade(View view, Typeface font) {
        if (view instanceof ViewGroup)
        {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
                setTypefaceCascade(viewGroup.getChildAt(i), font);
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            Typeface typeface = tv.getTypeface();
            if (typeface != null)
                tv.setTypeface(font, typeface.getStyle());
            else
                tv.setTypeface(font);
        }
    }

    public static void setTypefaceDefault(AssetManager assets, TextView tv)
    {
        Typeface typeface = tv.getTypeface();
        if (typeface != null)
            tv.setTypeface(getDefaultFont(assets), typeface.getStyle());
        else
            tv.setTypeface(getDefaultFont(assets));

    }

    public static void setTypefaceDefaultCascade(AssetManager assets, View view)
    {
        if (view instanceof ViewGroup)
        {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
                setTypefaceDefaultCascade(assets, viewGroup.getChildAt(i));
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            Typeface typeface = tv.getTypeface();
            if (typeface != null)
                tv.setTypeface(getDefaultFont(assets), typeface.getStyle());
            else
                tv.setTypeface(getDefaultFont(assets));
        }
    }

    public static String withNumberFormat(long number)
    {
        if (persianNumberFormat == null)
            persianNumberFormat = NumberFormat.getInstance();

        String result = persianNumberFormat.format(number);
        return result;
    }

    public static StringBuilder fromTypefaceNumberFormat(String text) {
        StringBuilder number = new StringBuilder();
        boolean hasDecimal = false;
        for (int i = 0; i < text.length(); i++) {
            for (int n = 0; n < 10; n++)
                if (text.substring(i, i + 1).equals(TypeFaceUtils.withTypefaceAmountFormat(n))
                        || text.substring(i, i + 1).equals(String.valueOf(n)))
                    number.append(String.valueOf(n));
            if (!hasDecimal && text.substring(i, i + 1).equals(".")) {
                number.append(".");
                hasDecimal = true;
            }
        }
        return number;
    }

    public static String withTypefaceAmountFormat(long number) {
        return withTypefaceAmountFormat((double) number);
    }
    public static String withTypefaceAmountFormat(double number)
    {
        if (persianDecimalFormat == null) {
            String pattern = "###,###,###.##";
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            persianDecimalFormat = new DecimalFormat(pattern, symbols);
        }
        String result = persianDecimalFormat.format(number);
        return result;
    }

    public static Collection<String> getFontList() {
        ArrayList<String> list = new ArrayList<>(11);
        list.add(FONT_ROBOTO);
        list.add(FONT_ROBOTO_MONO);
        list.add(FONT_ROBOTO_SLAB);
        list.add(FONT_ALEGREYA);
        list.add(FONT_ALEGREYA_SANS);
        list.add(FONT_MERRIWEATHER);
        list.add(FONT_MERRIWEATHER_SANS);
        list.add(FONT_NUNITO);
        list.add(FONT_NUNITO_SANS);
        list.add(FONT_QUATTROCENTO);
        list.add(FONT_QUATTROCENTO_SANS);
        return list;
    }

    public static String toUserFriendlyName(String fontName) {
        if (fontName.length() < 5)
            throw new RuntimeException("given name is incorrect: " + fontName);
        return fontName.substring(0, fontName.length() - 4);
    }

    public static String fromUserFriendlyName(String fontName) {
        return fontName + ".ttf";
    }
}
