package data.model.note;

/**
 * Created by Ahmad on 02/06/18.
 * All rights reserved.
 */

public class ElementDivider extends Element {
    long dataId;

    private ElementDivider() {

    }

    public static ElementDivider newInstance() {
        return new ElementDivider();
    }

    public long getDataId() {
        return dataId;
    }

    public ElementDivider setDataId(long dataId) {
        this.dataId = dataId;
        return this;
    }

    @Override
    public boolean areSubFieldsEqual(Element second) {
        if (second instanceof ElementDivider) {
            ElementDivider dividerSecond = (ElementDivider) second;
            return dividerSecond.dataId == dataId;
        }
        return false;
    }

    @Override
    public boolean hasContent() {
        return false;
    }
}
