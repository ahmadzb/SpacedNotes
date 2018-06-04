package com.diplinkblaze.spacednote.universal.util;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Ahmad on 11/25/17.
 * All rights reserved.
 */

public class TableData {
    private static final String KEY_TITLES = "titles";
    private static final String KEY_ROWS_COUNT = "rowsCount";
    private static final String KEY_ROWS_PREFIX = "rowsPrefix";
    private static final String KEY_NAME_COLOR = "nameColor";
    private static final String KEY_TITLE_COLORS = "titleColors";
    private static final String KEY_VALUE_COLORS = "valueColors";

    private Row titles;
    private ArrayList<Row> rows;
    private int nameColor;
    private ArrayList<Integer> titleColors;
    private ArrayList<Integer> valueColors;

    private TableData() {
        rows = new ArrayList<>();
    }

    public static TableData newInstance(Row titles, ArrayList<Row> rows) {
        TableData tableData = new TableData();
        tableData.titles = titles;
        tableData.rows = rows;
        return tableData;
    }

    public void addRow(Row row) {
        rows.add(row);
    }

    public int getRowCount() {
        return rows.size();
    }

    public Row getRowAt(int index) {
        return rows.get(index);
    }

    public Row getTitles() {
        return titles;
    }

    public void setTitles(Row titles) {
        this.titles = titles;
    }

    public int getNameColor() {
        return nameColor;
    }

    public void setNameColor(int nameColor) {
        this.nameColor = nameColor;
    }

    public void setTitleColors(ArrayList<Integer> titleColors) {
        this.titleColors = titleColors;
    }

    public void setValueColors(ArrayList<Integer> valueColors) {
        this.valueColors = valueColors;
    }

    public Integer getTitleColorFor(int position) {
        if (titleColors == null || titleColors.size() == 0)
            return 0;
        else
            return titleColors.get(position % titleColors.size());
    }

    public Integer getValueColorFor(int position) {
        if (valueColors == null || valueColors.size() == 0)
            return 0;
        else
            return valueColors.get(position % valueColors.size());
    }

    public boolean isTableValid() {
        if (rows.size() != 0) {
            int columns = rows.get(0).values.size();
            for (Row row : rows) {
                if (row.values.size() != columns)
                    return false;
            }
            if (titles.values.size() != columns)
                return false;
            return true;
        }
        return false;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putBundle(KEY_TITLES, titles.toBundle());
        bundle.putInt(KEY_ROWS_COUNT, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            bundle.putBundle(KEY_ROWS_PREFIX + i, rows.get(i).toBundle());
        }
        bundle.putIntegerArrayList(KEY_TITLE_COLORS, titleColors);
        bundle.putInt(KEY_NAME_COLOR, nameColor);
        bundle.putIntegerArrayList(KEY_VALUE_COLORS, valueColors);
        return bundle;
    }

    public static TableData fromBundle(Bundle bundle) {
        TableData tableData = new TableData();
        tableData.titles = Row.fromBundle(bundle.getBundle(KEY_TITLES));
        int count = bundle.getInt(KEY_ROWS_COUNT);
        for (int i = 0; i < count; i++) {
            tableData.rows.add(Row.fromBundle(bundle.getBundle(KEY_ROWS_PREFIX + i)));
        }
        tableData.titleColors = bundle.getIntegerArrayList(KEY_TITLE_COLORS);
        tableData.nameColor = bundle.getInt(KEY_NAME_COLOR);
        tableData.valueColors = bundle.getIntegerArrayList(KEY_VALUE_COLORS);
        return tableData;
    }

    public static class Row {
        private static final String KEY_NAME = "name";
        private static final String KEY_VALUES = "values";

        private String name;
        private ArrayList<String> values;

        private Row() {

        }

        public static Row newInstance(String name, String... values) {
            Row row = new Row();
            row.name = name;
            row.values = new ArrayList<>(values.length);
            for (String value : values) {
                row.values.add(value);
            }
            return row;
        }

        public String getName() {
            return name;
        }

        public String getValueAt(int index) {
            return values.get(index);
        }

        public int getValueCount() {
            return values.size();
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(KEY_VALUES, values);
            bundle.putString(KEY_NAME, name);
            return bundle;
        }

        public static Row fromBundle(Bundle bundle) {
            Row row = new Row();
            row.name = bundle.getString(KEY_NAME);
            row.values = bundle.getStringArrayList(KEY_VALUES);
            return row;
        }
    }
}
