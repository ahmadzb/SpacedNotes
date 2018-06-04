package util;

import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.diplinkblaze.spacednote.R;

import java.util.ArrayList;

/**
 * Created by Ahmad on 11/01/17.
 * All rights reserved.
 */

public class Texts {
    public static String prefixedHint(int prefixResId, int hintResId, Resources resources) {
        return resources.getString(hintResId) + " " +
                resources.getString(R.string.partial_open_parenthesis) + resources.getString(prefixResId) +
                resources.getString(R.string.partial_close_parenthesis);
    }

    public static String prefixedValue( int prefixResId, String value, Resources resources) {
        return resources.getString(prefixResId) + resources.getString(R.string.partial_colon) + " " + value;
    }

    public static String nameItems(@Nullable String prefix, ArrayList<String> items, Resources resources) {
        return nameItems(items, new NameItemsCache(prefix, resources));
    }

    public static String nameItems(ArrayList<String> items, NameItemsCache cache) {
        String text = "";
        for (int i = 0; i < items.size(); i++) {
            if (i == 0) {
                if (cache.prefix == null)
                    text = items.get(i);
                else
                    text = cache.prefix + " " + items.get(i);
            } else if (i == items.size() - 1) {
                text = text + " " + cache.and + " " + items.get(i);
            } else {
                text = text + cache.comma + " " + items.get(i);
            }
        }
        return text;
    }

    public static class NameItemsCache {
        String and;
        String comma;
        String prefix;

        public NameItemsCache(@Nullable String prefix, Resources resources) {
            and = resources.getString(R.string.partial_and);
            comma = resources.getString(R.string.partial_comma);
            this.prefix = prefix;
        }
    }
}
