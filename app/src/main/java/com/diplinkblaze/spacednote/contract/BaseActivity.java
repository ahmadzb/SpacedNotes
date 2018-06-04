package com.diplinkblaze.spacednote.contract;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.diplinkblaze.spacednote.theme.Theme;

import util.Colors;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class BaseActivity extends AppCompatActivity {
    private Colors.ColorCache colorCache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.applyTheme(this);
        setStatusBarColor();
    }

    protected void setStatusBarColor() {
        setStatusBarColor(Colors.darkenColor(getPrimaryColor()));
    }

    protected void setStatusBarColor(int color) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    protected int getPrimaryColor() {
        if (colorCache == null) {
            colorCache = new Colors.ColorCache(this);
        }
        return colorCache.primaryColor;
    }

    protected int getAccentColor() {
        if (colorCache == null) {
            colorCache = new Colors.ColorCache(this);
        }
        return colorCache.accentColor;
    }
}
