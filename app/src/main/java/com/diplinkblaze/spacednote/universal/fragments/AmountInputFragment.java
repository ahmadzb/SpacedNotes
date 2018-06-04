package com.diplinkblaze.spacednote.universal.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

import com.diplinkblaze.spacednote.R;

import util.BottomSheetUtil;
import util.EnglishTypeFace;
import util.Numbers;

public class AmountInputFragment extends BottomSheetDialogFragment {

    private static final String KEY_TAG = "tag";
    private static final String KEY_IDENTIFIER = "identifier";
    private static final String KEY_SENTENCE = "currentText";

    private Sentence sentence;

    public AmountInputFragment() {
        // Required empty public constructor
    }

    public static AmountInputFragment newInstance(String tag, Bundle identifier) {
        return newInstance(null, tag, identifier);
    }

    public static AmountInputFragment newInstance(Double initialAmount, String tag, Bundle identifier) {
        AmountInputFragment fragment = new AmountInputFragment();
        Bundle args = new Bundle();
        Sentence sentence = Sentence.newInstance(initialAmount);
        args.putSerializable(KEY_SENTENCE, sentence);
        args.putString(KEY_TAG, tag);
        args.putBundle(KEY_IDENTIFIER, identifier);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            sentence = (Sentence) savedInstanceState.getSerializable(KEY_SENTENCE);
        } else if (getArguments() != null) {
            sentence = (Sentence) getArguments().getSerializable(KEY_SENTENCE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        BottomSheetUtil.expandOnShow(dialog);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contentView = inflater.inflate(R.layout.fragment_amount_input, container, false);
        initializeViews(contentView);
        updateViews(contentView);
        return contentView;
    }

    private void initializeViews(View contentView) {
        initializeCascade(contentView);
        View doneView = contentView.findViewById(R.id.fragment_amount_input_done);
        doneView.setOnClickListener(new OnDoneClicked());
    }

    private void initializeCascade(View view) {
        OnInputCellClicked inputCellClicked = new OnInputCellClicked();
        OnInputCellLongClicked inputCellLongClicked = new OnInputCellLongClicked();
        OnInputTouchListener inputTouchListener = new OnInputTouchListener();
        if (view == null)
            return;
        if (getString(R.string.tag_amount_input_cell).equals(view.getTag())) {
            view.setOnClickListener(inputCellClicked);
            view.setOnTouchListener(inputTouchListener);
        } else if (getString(R.string.tag_amount_input_cell_second).equals(view.getTag())) {
            view.setOnClickListener(inputCellClicked);
            view.setOnLongClickListener(inputCellLongClicked);
            view.setOnTouchListener(inputTouchListener);
        } else if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                initializeCascade(parent.getChildAt(i));
            }
        }
    }

    private void tryUpdateViews() {
        View contentView = getView();
        if (contentView != null)
            updateViews(contentView);
    }

    private void updateViews(View contentView) {
        TextView inputText = contentView.findViewById(R.id.fragment_amount_input_amount);
        TextView inputDetails = contentView.findViewById(R.id.fragment_amount_input_amount_detail);
        inputText.setText(sentence.getText(getContext()));
        inputDetails.setText(sentence.expressionCount() > 2 ? sentence.getEvaluateText() : "");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SENTENCE, sentence);
    }

    private class OnInputCellClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String input = ((TextView) v).getText().toString();
            sentence.onNewInput(input);
            tryUpdateViews();
        }
    }

    private class OnInputCellLongClicked implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            String input = ((TextView) v).getText().toString();
            if (input.equals("DEL")) {
                sentence.onClearPressed();
                tryUpdateViews();
                return true;
            }
            return false;
        }
    }

    private class OnDoneClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Dialog dialog = getDialog();
            if (dialog != null)
                dialog.dismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onAmountInputFragmentDone();
    }

    private class OnInputTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        }
    }

    //===================================== Communication Parent ===================================

    private void onAmountInputFragmentDone() {
        String tag = getArguments().getString(KEY_TAG);
        Bundle identifier = getArguments().getBundle(KEY_IDENTIFIER);
        getListener().onAmountInputFragmentDone(sentence.evaluate(), tag, identifier);
    }

    private OnFragmentInteractionListener getListener() {
        if (getParentFragment() instanceof OnFragmentInteractionListener)
            return (OnFragmentInteractionListener) getParentFragment();
        else if (getActivity() instanceof OnFragmentInteractionListener)
            return (OnFragmentInteractionListener) getActivity();
        else
            throw new RuntimeException("Either parent fragment or activity must implement" +
                    " OnFragmentInteractionListener");
    }

    public interface OnFragmentInteractionListener {
        void onAmountInputFragmentDone(Double amount, String tag, Bundle identifier);
    }

    //========================================== Data Model ========================================
    private static abstract class Expression implements Serializable {
        public abstract boolean onDeletePressed();

        public abstract String getText(Context context);
    }

    private interface Operand {

    }

    private static class ValueExpression extends Expression {
        private static ArrayList<Character> numbers;
        public String value;

        static {
            char[] chars = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
            numbers = new ArrayList<>(chars.length);
            for (char c : chars)
                numbers.add(c);
        }

        public ValueExpression(String input) {
            if (input != null) {
                if (input.equals(".")) {
                    this.value = "0.";
                } else {
                    this.value = EnglishTypeFace.withEnglishAmountFormat(
                            EnglishTypeFace.fromEnglishAmountFormat(input)
                    );
                }
            }

        }

        public ValueExpression(Double input) {
            if (input != null) {
                this.value = EnglishTypeFace.withEnglishAmountFormat(input);
            }
        }


        @Override
        public boolean onDeletePressed() {
            if (value == null || value.length() <= 1)
                return false;
            else {
                value = value.substring(0, value.length() - 1);
                if (!value.equals("")) {
                    boolean hasLastDecimal = value.substring(value.length() - 1, value.length()).equals(".");
                    if (!hasLastDecimal) {
                        value = EnglishTypeFace.withEnglishAmountFormat(
                                EnglishTypeFace.fromEnglishAmountFormat(value));
                    }

                }
                return true;
            }
        }

        public void addToValue(char c) {
            if (numbers.contains(c)) {
                if (!value.contains(".") || value.indexOf(".") > value.length() - 3) {
                    value = value + c;
                }
            } else if (c == '.') {
                if (!value.contains("."))
                    value = value + c;
            }
            if (value != null && value.length() != 0) {
                String whole = "";
                String decimal = null;
                for (char w : value.toCharArray()) {
                    if (w == '.') {
                        decimal = "";
                    } else if (decimal == null) {
                        whole = whole + w;
                    } else {
                        decimal = decimal + w;
                    }
                }
                if (!whole.equals("")) {
                    value = EnglishTypeFace.withEnglishAmountFormat(
                            EnglishTypeFace.fromEnglishAmountFormat(whole));
                }
                if (decimal != null) {
                    value = (value.equals("")? "0" : value) + "." + decimal;
                }
            }

        }

        @Override
        public String getText(Context context) {
            return value;
        }

        public double getValue() {
            if (value == null || value.length() == 0)
                return 0;
            else
                return EnglishTypeFace.fromEnglishAmountFormat(value);
        }

        public static boolean canContain(char c) {
            if (numbers.contains(c) || c == '.') {
                return true;
            }
            return false;
        }
    }

    private static class AddExpression extends Expression implements Operand {
        @Override
        public boolean onDeletePressed() {
            return false;
        }

        @Override
        public String getText(Context context) {
            return "+";
        }
    }

    private static class MinusExpression extends Expression implements Operand {

        @Override
        public boolean onDeletePressed() {
            return false;
        }

        @Override
        public String getText(Context context) {
            return "-";
        }
    }

    private static class DivideExpression extends Expression implements Operand {

        @Override
        public boolean onDeletePressed() {
            return false;
        }

        @Override
        public String getText(Context context) {
            return "÷";
        }
    }

    private static class MultiplyExpression extends Expression implements Operand {

        @Override
        public boolean onDeletePressed() {
            return false;
        }

        @Override
        public String getText(Context context) {
            return "×";
        }
    }

    private static class OpenExpression extends Expression {

        @Override
        public boolean onDeletePressed() {
            return false;
        }

        @Override
        public String getText(Context context) {
            return "(";
        }
    }

    private static class CloseExpression extends Expression {

        @Override
        public boolean onDeletePressed() {
            return false;
        }

        @Override
        public String getText(Context context) {
            return ")";
        }
    }

    private static class Sentence implements Serializable {

        ArrayList<Expression> expressions;

        private Sentence() {
            this.expressions = new ArrayList<>();
        }

        public static Sentence newInstance(Double initialAmount) {
            Sentence sentence = new Sentence();
            if (initialAmount != null && !Numbers.isSmall(initialAmount)) {
                sentence.expressions.add(new ValueExpression(initialAmount));
            }
            return sentence;
        }

        private Double evaluate() {
            Stack<Expression> stack = new Stack<>();
            for (Expression expression : expressions) {
                if (expression instanceof CloseExpression) {
                    ArrayList<Expression> window = new ArrayList<>();
                    while (stack.size() != 0 && !(stack.peek() instanceof OpenExpression)) {
                        window.add(0, stack.pop());
                    }
                    if (stack.size() == 0) {
                        return null;
                    } else {
                        stack.pop();
                    }
                    stack.push(new ValueExpression(evaluateNoParenthesis(window)));
                } else {
                    stack.push(expression);
                }
            }
            for (Expression expression : stack) {
                if (expression instanceof OpenExpression)
                    return null;
            }
            return evaluateNoParenthesis(new ArrayList<>(stack));
        }

        private int expressionCount() {
            return expressions.size();
        }

        private double evaluateNoParenthesis(ArrayList<Expression> expressions) {
            if (expressions.size() == 0)
                return 0;

            double arg1 = 0;
            ArrayList<Expression> window = new ArrayList<>(expressions.size());
            Expression lastOperand = null;
            for (Expression expression : expressions) {
                if (expression instanceof AddExpression || expression instanceof MinusExpression) {
                    if (lastOperand == null) {
                        if (window.size() != 0) {
                            arg1 = evaluateNoAddMinus(window);
                            window.clear();
                        }
                    } else if (lastOperand instanceof AddExpression) {
                        if (window.size() != 0) {
                            arg1 = arg1 + evaluateNoAddMinus(window);
                            window.clear();
                        }
                    } else if (lastOperand instanceof MinusExpression) {
                        if (window.size() != 0) {
                            arg1 = arg1 - evaluateNoAddMinus(window);
                            window.clear();
                        }
                    }
                    lastOperand = expression;
                } else {
                    window.add(expression);
                }
            }
            if (window.size() != 0) {
                if (lastOperand == null) {
                    arg1 = evaluateNoAddMinus(window);
                } else if (lastOperand instanceof AddExpression) {
                    arg1 = arg1 + evaluateNoAddMinus(window);
                } else if (lastOperand instanceof MinusExpression) {
                    arg1 = arg1 - evaluateNoAddMinus(window);
                }
            }
            return arg1;
        }

        private double evaluateNoAddMinus(ArrayList<Expression> expressions) {
            if (expressions.size() == 0)
                return 0;

            double arg1 = 1;
            Expression lastExpression = null;
            for (Expression expression : expressions) {
                if (lastExpression instanceof MultiplyExpression || lastExpression instanceof ValueExpression) {
                    if (expression instanceof ValueExpression) {
                        arg1 = arg1 * ((ValueExpression) expression).getValue();
                    }
                } else if (lastExpression instanceof DivideExpression) {
                    if (expression instanceof ValueExpression) {
                        arg1 = arg1 / ((ValueExpression) expression).getValue();
                    }
                } else if (lastExpression == null) {
                    if (expression instanceof ValueExpression) {
                        arg1 = ((ValueExpression) expression).getValue();
                    }
                }
                lastExpression = expression;
            }
            return arg1;
        }

        private String getEvaluateText() {
            Double evaluation = evaluate();
            return evaluation == null ? "" : EnglishTypeFace.withEnglishAmountFormat(evaluation);
        }

        private void onDeletePressed() {
            int lastIndex = expressions.size() - 1;
            if (lastIndex >= 0) {
                boolean result = expressions.get(lastIndex).onDeletePressed();
                if (!result) {
                    expressions.remove(lastIndex);
                }
            }
        }

        private void onClearPressed() {
            expressions.clear();
        }

        private String getText(Context context) {
            String result = null;
            for (Expression expression : expressions) {
                result = (result == null ? "" : result + " ") + expression.getText(context);
            }
            return result;
        }

        private void onNewInput(String input) {
            if (input.equals("DEL")) {
                onDeletePressed();
            } else if (input.equals("+")) {
                if (expressions.size() != 0) {
                    Expression lastExpression = expressions.get(expressions.size() - 1);
                    if (lastExpression instanceof Operand) {
                        expressions.remove(lastExpression);
                    }
                    expressions.add(new AddExpression());
                }
            } else if (input.equals("-")) {
                if (expressions.size() != 0) {
                    Expression lastExpression = expressions.get(expressions.size() - 1);
                    if (lastExpression instanceof Operand) {
                        expressions.remove(lastExpression);
                    }
                    expressions.add(new MinusExpression());
                }
            } else if (input.equals("×")) {
                if (expressions.size() != 0) {
                    Expression lastExpression = expressions.get(expressions.size() - 1);
                    if (lastExpression instanceof Operand) {
                        expressions.remove(lastExpression);
                    }
                    expressions.add(new MultiplyExpression());
                }
            } else if (input.equals("÷")) {
                if (expressions.size() != 0) {
                    Expression lastExpression = expressions.get(expressions.size() - 1);
                    if (lastExpression instanceof Operand) {
                        expressions.remove(lastExpression);
                    }
                    expressions.add(new DivideExpression());
                }
            } else if (input.equals("=")) {
                Double value = evaluate();
                if (value != null) {
                    expressions.clear();
                    expressions.add(new ValueExpression(value));
                }
            } else if (input.equals("(")) {
                expressions.add(new OpenExpression());
            } else if (input.equals(")")) {
                expressions.add(new CloseExpression());
            } else if (input.length() == 1 && ValueExpression.canContain(input.charAt(0))) {
                if (expressions.size() != 0 && expressions.get(expressions.size() - 1) instanceof ValueExpression) {
                    ((ValueExpression) expressions.get(expressions.size() - 1)).addToValue(input.charAt(0));
                } else {
                    expressions.add(new ValueExpression(input));
                }
            } else if (input.equals("000")) {
                onNewInput("0");
                onNewInput("0");
                onNewInput("0");
            }
        }
    }
}
