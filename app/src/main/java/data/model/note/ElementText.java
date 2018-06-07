package data.model.note;

import data.database.Contract;

/**
 * Created by Ahmad on 01/08/18.
 * All rights reserved.
 */

public class ElementText extends Element {
    long dataId;
    String text;

    private ElementText() {

    }

    public static ElementText newInstance() {
        return new ElementText();
    }

    public long getDataId() {
        return dataId;
    }

    public ElementText setDataId(long dataId) {
        this.dataId = dataId;
        return this;
    }

    public String getText() {
        return text;
    }

    public ElementText setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public boolean areElementsEqual(Element second) {
        if (second instanceof ElementText) {
            ElementText textSecond = (ElementText) second;
            return textSecond.dataId == dataId && (textSecond.text == null? "" : textSecond.text).equals(text);
        }
        return false;
    }

    @Override
    public boolean hasContent() {
        return text != null && !text.isEmpty();
    }
}
