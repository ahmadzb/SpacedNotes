package com.diplinkblaze.spacednote.contract;

import com.diplinkblaze.spacednote.R;

/**
 * Created by Ahmad on 05/25/18.
 * All rights reserved.
 */
public class NoActionbarActivity extends BaseActivity {
    @Override
    protected void setStatusBarColor() {
        setStatusBarColor(getResources().getColor(R.color.colorNamedGray));
    }
}
