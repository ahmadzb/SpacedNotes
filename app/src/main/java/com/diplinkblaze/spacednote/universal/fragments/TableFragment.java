package com.diplinkblaze.spacednote.universal.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;
import com.diplinkblaze.spacednote.universal.util.TableData;

import util.TypeFaceUtils;

public class TableFragment extends Fragment{
    private static final String KEY_TABLE_DATA = "tableData";

    private TableData tableData;

    public TableFragment() {
        // Required empty public constructor
    }

    public static TableFragment newInstance(TableData tableData) {
        TableFragment fragment = new TableFragment();
        Bundle args = new Bundle();
        args.putBundle(KEY_TABLE_DATA, tableData.toBundle());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tableData = TableData.fromBundle(getArguments().getBundle(KEY_TABLE_DATA));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_table, container, false);
        updateViews(inflater, contentView);
        return contentView;
    }

    private void updateViews(LayoutInflater inflater, View contentView) {
        //Grid
        {
            int backgroundColorEven = getResources().getColor(R.color.colorBackgroundLight);
            int backgroundColorOdd = getResources().getColor(R.color.colorBackgroundSecond);
            int textDefaultColor = getResources().getColor(R.color.colorText);

            GridLayout grid = contentView.findViewById(R.id.fragment_table_grid_layout);

            for (int i = -1; i < tableData.getRowCount(); i++) {
                TableData.Row row;
                if (i == -1)
                    row = tableData.getTitles();
                else
                    row = tableData.getRowAt(i);

                for (int j = -1; j < row.getValueCount(); j++) {
                    String value;
                    if (j == -1)
                        value = row.getName();
                    else
                        value = row.getValueAt(j);
                    int color;

                    if (j == -1)
                        color = tableData.getNameColor();
                    else if (i == -1)
                        color = tableData.getTitleColorFor(j);
                    else
                        color = tableData.getValueColorFor(j);
                    if (color == 0)
                        color = textDefaultColor;
                    int backgroundColor;
                    if (i % 2 == 0)
                        backgroundColor = backgroundColorEven;
                    else
                        backgroundColor = backgroundColorOdd;

                    TextView tv = (TextView) inflater.inflate(R.layout.partial_table_cell, grid, false);
                    TypeFaceUtils.setTypefaceDefault(getResources().getAssets(), tv);
                    tv.setBackgroundColor(backgroundColor);
                    tv.setText(value);
                    tv.setTextColor(color);
                    if (j == -1) {
                        tv.setPaddingRelative(
                                getResources().getDimensionPixelSize(R.dimen.padding_list_sideways)
                                , 0, 0, 0);
                    }

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.rowSpec = GridLayout.spec(i + 1, 1, 1);
                    params.columnSpec = GridLayout.spec(j + 1, 1, 1);
                    grid.addView(tv, params);
                }
            }
        }
    }
}
