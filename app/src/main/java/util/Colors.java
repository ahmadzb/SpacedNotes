package util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

import com.diplinkblaze.spacednote.R;

import java.util.ArrayList;

/**
 * Created by Ahmad on 11/24/17.
 * All rights reserved.
 */

public class Colors {

    private static ArrayList<Integer> colorArray;

    private static ArrayList<Integer> getColorArray(Resources resources) {
        if (colorArray == null) {
            int[] colors = resources.getIntArray(R.array.materialColors);
            colorArray = new ArrayList<>(colors.length);
            for (int i = 2; i < colors.length - 3; i = i + 3) {
                colorArray.add(colors[i]);
            }
            for (int i = 1; i < colors.length - 3; i = i + 3) {
                colorArray.add(colors[i]);
            }
            for (int i = 0; i < colors.length - 3; i = i + 3) {
                colorArray.add(colors[i]);
            }
        }
        return colorArray;
    }

    public static int getConsistentColor(int position, Resources resources) {
        return getColorArray(resources).get(Math.abs(position) % getColorArray(resources).size());
    }

    public static int getRandomColor(Resources resources) {
        return getColorArray(resources).get((int) (Math.random() * getColorArray(resources).size()));
    }

    public static int getPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

    public static int getR(int color) {
        return (color & 0x00ff0000) >> 16;
    }

    public static int getG(int color) {
        return (color & 0x0000ff00) >> 8;
    }

    public static int getB(int color) {
        return color & 0x000000ff;
    }

    public static class ColorCache{
        public final int primaryColor;
        public final int accentColor;

        public ColorCache(Context context) {
            primaryColor = Colors.getPrimaryColor(context);
            accentColor = Colors.getAccentColor(context);
        }
    }

    public static @ColorInt int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f;
        return Color.HSVToColor(hsv);
    }
}
