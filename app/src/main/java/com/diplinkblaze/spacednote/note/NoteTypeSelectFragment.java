package com.diplinkblaze.spacednote.note;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;

import java.util.ArrayList;
import java.util.Collections;

import data.database.OpenHelper;
import data.model.type.Type;
import data.model.type.TypeCatalog;

public class NoteTypeSelectFragment extends BottomSheetDialogFragment {

    private ArrayList<Type> types;
    private Adapter adapter = new Adapter();

    public NoteTypeSelectFragment() {
        // Required empty public constructor
    }

    public static NoteTypeSelectFragment newInstance() {
        NoteTypeSelectFragment fragment = new NoteTypeSelectFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        types = TypeCatalog.getTypesAvailable(OpenHelper.getDatabase(getContext()));
        Collections.sort(types, Type.getTypeComparator());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_note_type_select, container, false);
        initializeViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        RecyclerView recyclerView = contentView.findViewById(R.id.fragment_note_type_select_recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View itemView = inflater.inflate(R.layout.partial_notetype_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Type type = types.get(position);
            holder.text.setText(type.getTitle());
            holder.text.setTextColor(type.getColor());
            holder.itemView.setOnClickListener(new OnTypeSelected(type));
        }

        @Override
        public int getItemCount() {
            return types.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.text = itemView.findViewById(R.id.partial_notetype_item_text);
        }
    }

    private class OnTypeSelected  implements View.OnClickListener{
        Type type;

        public OnTypeSelected(Type type) {
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            getListener().onTypeSelected(type);
        }
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            return (OnFragmentInteractionListener) getParentFragment();
        else if (getActivity() instanceof OnFragmentInteractionListener)
            return (OnFragmentInteractionListener) getActivity();
        return null;
    }

    public interface OnFragmentInteractionListener {
        void onTypeSelected(Type type);
    }
}
