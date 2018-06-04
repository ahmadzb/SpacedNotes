package com.diplinkblaze.spacednote.universal.fragments;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.diplinkblaze.spacednote.R;

import util.TypeFaceUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class TextInputDialogFragment extends DialogFragment {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_NUMBER = 1;

    private static final String KEY_TAG = "tag";
    private static final String KEY_TYPE = "type";
    private static final String KEY_IDENTIFIER = "identifier";
    private static final String KEY_INITIAL_TEXT = "initialText";
    private static final String KEY_HINT_RES_ID = "hintResId";

    public TextInputDialogFragment() {
        // Required empty public constructor
    }

    public static TextInputDialogFragment getInstance(int type, String initialText, Integer hintResId, String tag, Bundle identifier) {
        TextInputDialogFragment fragment = new TextInputDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TAG, tag);
        args.putInt(KEY_TYPE, type);
        args.putBundle(KEY_IDENTIFIER, identifier);
        args.putString(KEY_INITIAL_TEXT, initialText);
        if (hintResId != null) args.putInt(KEY_HINT_RES_ID, hintResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_text_input_dialog, container, false);
        initializeViews(contentView);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        EditText text = contentView.findViewById(R.id.fragment_text_input_dialog_text);
        EditText number = contentView.findViewById(R.id.fragment_text_input_dialog_number);
        EditText textView;
        if (getArguments().getInt(KEY_TYPE) == TYPE_TEXT) {
            number.setVisibility(View.GONE);
            textView = text;
        } else if (getArguments().getInt(KEY_TYPE) == TYPE_NUMBER) {
            text.setVisibility(View.GONE);
            textView = number;
        } else
            throw new RuntimeException("Given type is not recognized");

        textView.setText(getArguments().getString(KEY_INITIAL_TEXT));
        if (getArguments().containsKey(KEY_HINT_RES_ID))
            textView.setHint(getArguments().getInt(KEY_HINT_RES_ID));
        textView.addTextChangedListener(new EmptyWatcher());

        TextView save = contentView.findViewById(R.id.fragment_text_input_dialog_save);
        TextView dismiss = contentView.findViewById(R.id.fragment_text_input_dialog_dismiss);
        save.setOnClickListener(new OnSaveClickListener());
        dismiss.setOnClickListener(new OnDismissClickListener());
        TypeFaceUtils.setTypefaceDefaultCascade(getResources().getAssets(), contentView);
    }

    private void updateViews(View contentView) {
        TextView textView = getCurrentEditText(contentView);
        if (textView.getText().toString().length() == 0)
            disableSaveButton(contentView);
        else
            enableSaveButton(contentView);
    }

    private EditText getCurrentEditText(View contentView) {
        EditText text = contentView.findViewById(R.id.fragment_text_input_dialog_text);
        EditText number = contentView.findViewById(R.id.fragment_text_input_dialog_number);
        EditText textView;
        if (getArguments().getInt(KEY_TYPE) == TYPE_TEXT) {
            textView = text;
        } else if (getArguments().getInt(KEY_TYPE) == TYPE_NUMBER) {
            textView = number;
        } else
            throw new RuntimeException("Given type is not recognized");
        return textView;
    }
    //===================================== Communication Parent ===================================
    private void onSave(View contentView) {
        TextView textView = getCurrentEditText(contentView);
        String text = textView.getText().toString();
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);

        if (getParentFragment() instanceof OnFragmentInteractionListener)
            ((OnFragmentInteractionListener) getParentFragment()).onTextInputDialogSave(text, tag, identifier);
        else if (getActivity() instanceof OnFragmentInteractionListener)
            ((OnFragmentInteractionListener) getActivity()).onTextInputDialogSave(text, tag, identifier);
        else
            throw new RuntimeException("Neither parent fragment of activity implements " +
                    "OnFragmentInteractionListener");

        Dialog dialog = getDialog();
        if (dialog != null)
            dialog.dismiss();
    }

    private void onDismiss() {
        Dialog dialog = getDialog();
        if (dialog != null)
            dialog.dismiss();
    }

    public interface OnFragmentInteractionListener {
        void onTextInputDialogSave(String text, String tag, Bundle identifier);
    }

    //======================================== User Interaction ====================================
    private void disableSaveButton(View contentView) {
        TextView save = contentView.findViewById(R.id.fragment_text_input_dialog_save);
        save.setEnabled(false);
    }

    private void enableSaveButton(View contentView) {
        TextView save = contentView.findViewById(R.id.fragment_text_input_dialog_save);
        save.setEnabled(true);
    }

    private class OnSaveClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            onSave(getView());
        }
    }

    private class OnDismissClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            onDismiss();
        }
    }

    private class EmptyWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            View contentView = getView();
            if (contentView != null) {
                updateViews(contentView);
            }
        }
    }
}
