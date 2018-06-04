package com.diplinkblaze.spacednote.theme;


import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

import com.diplinkblaze.spacednote.R;

import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class Theme {

    private static Colors colors;

    public static void applyTheme(AppCompatActivity activity) {
        Profile profile = ProfileCatalog.getCurrentProfileIfExist(activity);
        if (profile != null) {
            Colors colors = getColors(activity);
            int color = profile.getColor();
            int theme = R.style.AppTheme;
            if (color == colors.red) {
                theme = R.style.AppTheme_Red;
            } else if (color == colors.pink) {
                theme = R.style.AppTheme_Pink;
            } else if (color == colors.purple) {
                theme = R.style.AppTheme_Purple;
            } else if (color == colors.deepPurple) {
                theme = R.style.AppTheme_DeepPurple;
            } else if (color == colors.indigo) {
                theme = R.style.AppTheme_Indigo;
            } else if (color == colors.blue) {
                theme = R.style.AppTheme_Blue;
            } else if (color == colors.lightBlue) {
                theme = R.style.AppTheme_LightBlue;
            } else if (color == colors.cyan) {
                theme = R.style.AppTheme_Cyan;
            } else if (color == colors.teal) {
                theme = R.style.AppTheme_Teal;
            } else if (color == colors.green) {
                theme = R.style.AppTheme_Green;
            } else if (color == colors.lightGreen) {
                theme = R.style.AppTheme_LightGreen;
            } else if (color == colors.lime) {
                theme = R.style.AppTheme_Lime;
            } else if (color == colors.yellow) {
                theme = R.style.AppTheme_Yellow;
            } else if (color == colors.amber) {
                theme = R.style.AppTheme_Amber;
            } else if (color == colors.orange) {
                theme = R.style.AppTheme_Orange;
            } else if (color == colors.deepOrange) {
                theme = R.style.AppTheme_DeepOrange;
            } else if (color == colors.brown) {
                theme = R.style.AppTheme_Brown;
            } else if (color == colors.blueGray) {
                theme = R.style.AppTheme_BlueGray;
            }
            activity.setTheme(theme);
        }
    }

    private static Colors getColors(Context context) {
        if (colors == null) {
            colors = new Colors(context.getResources());
        }
        return colors;
    }

    private static class Colors {
        final int red;
        final int pink;
        final int purple;
        final int deepPurple;
        final int indigo;
        final int blue;
        final int lightBlue;
        final int cyan;
        final int teal;
        final int green;
        final int lightGreen;
        final int lime;
        final int yellow;
        final int amber;
        final int orange;
        final int deepOrange;
        final int brown;
        final int blueGray;

        Colors(Resources resources) {
            red = resources.getColor(R.color.colorNamedRed);
            pink = resources.getColor(R.color.colorNamedPink);
            purple = resources.getColor(R.color.colorNamedPurple);
            deepPurple = resources.getColor(R.color.colorNamedDeepPurple);
            indigo = resources.getColor(R.color.colorNamedIndigo);
            blue = resources.getColor(R.color.colorNamedBlue);
            lightBlue = resources.getColor(R.color.colorNamedLightBlue);
            cyan = resources.getColor(R.color.colorNamedCyan);
            teal = resources.getColor(R.color.colorNamedTeal);
            green = resources.getColor(R.color.colorNamedGreen);
            lightGreen = resources.getColor(R.color.colorNamedLightGreen);
            lime = resources.getColor(R.color.colorNamedLime);
            yellow = resources.getColor(R.color.colorNamedYellow);
            amber = resources.getColor(R.color.colorNamedAmber);
            orange = resources.getColor(R.color.colorNamedOrange);
            deepOrange = resources.getColor(R.color.colorNamedDeepOrange);
            brown = resources.getColor(R.color.colorNamedBrown);
            blueGray = resources.getColor(R.color.colorNamedBlueGray);
        }
    }
}
