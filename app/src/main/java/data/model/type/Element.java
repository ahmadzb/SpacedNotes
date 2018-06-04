package data.model.type;

import java.util.Comparator;

/**
 * Created by Ahmad on 01/08/18.
 * All rights reserved.
 */

public class Element {

    private static final int PATTERN_SIDES = 0b11;

    private static final int SIDE_FRONT = 0b01;
    private static final int SIDE_BACK = 0b10;

    public static int PATTERN_TEXT = 1;
    public static int PATTERN_LIST = 2;
    public static int PATTERN_PICTURE = 3;
    public static int PATTERN_DIVIDER = 4;

    private long id;
    private long typeId;
    private int position;
    private String title;
    private boolean isArchived;
    private int sides;
    private int pattern;
    private boolean initialCopy;
    private Long data1;
    private Long data2;
    private String data3;
    private String data4;
    private boolean isRealized;
    private boolean isInitialized;
    
    private Element() {
        
    }
    
    public static Element newInstance() {
        return new Element();
    }

    public long getId() {
        return id;
    }

    public Element setId(long id) {
        this.id = id;
        return this;
    }

    public long getTypeId() {
        return typeId;
    }

    public Element setTypeId(long typeId) {
        this.typeId = typeId;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public Element setPosition(int position) {
        this.position = position;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Element setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public Element setArchived(boolean archived) {
        isArchived = archived;
        return this;
    }

    public int getSides() {
        return sides;
    }

    public boolean hasSideFront() {
        return (getSides() & SIDE_FRONT) == SIDE_FRONT;
    }

    public boolean hasSideBack() {
        return (getSides() & SIDE_BACK) == SIDE_BACK;
    }

    public Element setSides(int sides) {
        this.sides = sides;
        return this;
    }

    public Element setSideFront(boolean sideFront) {
        this.sides = (sideFront? SIDE_FRONT : 0) | (hasSideBack()? SIDE_BACK : 0);
        return this;
    }

    public Element setSideBack(boolean sideBack) {
        this.sides = (hasSideFront()? SIDE_FRONT : 0) | (sideBack? SIDE_BACK : 0);
        return this;
    }

    public int getPattern() {
        return pattern;
    }

    public Element setPattern(int pattern) {
        this.pattern = pattern;
        return this;
    }

    public boolean isInitialCopy() {
        return initialCopy;
    }

    public void setInitialCopy(boolean initialCopy) {
        this.initialCopy = initialCopy;
    }

    public Long getData1() {
        return data1;
    }

    public Element setData1(Long data1) {
        this.data1 = data1;
        return this;
    }

    public Long getData2() {
        return data2;
    }

    public Element setData2(Long data2) {
        this.data2 = data2;
        return this;
    }

    public String getData3() {
        return data3;
    }

    public Element setData3(String data3) {
        this.data3 = data3;
        return this;
    }

    public String getData4() {
        return data4;
    }

    public Element setData4(String data4) {
        this.data4 = data4;
        return this;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public Element setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Element setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }

    public Interpreter getInterpreter() {
        return Interpreter.getInterpreter(this);
    }
    
    @Override
    public Element clone() {
        Element clone = new Element();
        clone.id = this.id;
        clone.typeId = this.typeId;
        clone.position = this.position;
        clone.title = this.title;
        clone.isArchived = this.isArchived;
        clone.pattern = this.pattern;
        clone.data1 = this.data1;
        clone.data2 = this.data2;
        clone.data3 = this.data3;
        clone.data4 = this.data4;
        clone.isRealized = this.isRealized;
        clone.isInitialized = this.isInitialized;
        return clone;
    }

    //======================================= Interpreters =========================================
    public static abstract class Interpreter {
        public static Interpreter getInterpreter(Element element) {
            if (element.getPattern() == PATTERN_TEXT) {
                return new TextInterpreter(element);
            } else if (element.getPattern() == PATTERN_LIST) {
                return new ListInterpreter(element);
            } else if (element.getPattern() == PATTERN_PICTURE) {
                return new PictureInterpreter(element);
            } else if (element.getPattern() == PATTERN_DIVIDER) {
                return new DividerInterpreter(element);
            }
            throw new RuntimeException("No interpreter was found for element: " + element);
        }
    }

    public static abstract class TextBaseInterpreter extends Interpreter{
        protected Element element;

        private static final long PATTERN_COLOR = 0xffffffff;
        
        private static final long PATTERN_TEXT_SIZE = 0b1111111;
        
        private static final long PATTERN_BOLD = 0b1;
        
        private static final long PATTERN_ITALIC = 0b1;
        
        private static final long PATTERN_MULTILINE = 0b1;
        
        private static final long PATTERN_SUMMERY = 0b1;

        private static final long LENGTH_TEXT_SIZE = 7;
        private static final long LENGTH_BOLD = 1;
        private static final long LENGTH_ITALIC = 1;
        private static final long LENGTH_MULTILINE = 1;
        private static final long LENGTH_SUMMERY = 1;

        private static final long SHIFT_TEXT_SIZE = 0;
        private static final long SHIFT_BOLD = SHIFT_TEXT_SIZE + LENGTH_TEXT_SIZE;
        private static final long SHIFT_ITALIC = SHIFT_BOLD + LENGTH_BOLD;
        private static final long SHIFT_MULTILINE = SHIFT_ITALIC + LENGTH_ITALIC;
        private static final long SHIFT_SUMMERY = SHIFT_MULTILINE + LENGTH_MULTILINE;

        protected static final long SHIFT_DATA1 = SHIFT_SUMMERY + LENGTH_SUMMERY;


        public int getColor() {
            if (element.getData2() == null)
                return 0;
            return (int) (long) element.getData2();
        }

        public int getTextSize() {
            if (element.getData1() == null)
                return 0;
            return (int) ((element.getData1() >> SHIFT_TEXT_SIZE) & PATTERN_TEXT_SIZE);
        }

        public boolean isBold() {
            if (element.getData1() == null)
                return false;
            return ((element.getData1() >> SHIFT_BOLD) & PATTERN_BOLD) == 1;
        }

        public boolean isItalic() {
            if (element.getData1() == null)
                return false;
            return ((element.getData1() >> SHIFT_ITALIC) & PATTERN_ITALIC) == 1;
        }

        public boolean isMultiline() {
            if (element.getData1() == null)
                return false;
            return ((element.getData1() >> SHIFT_MULTILINE) & PATTERN_MULTILINE) == 1;
        }

        public boolean isSummery() {
            if (element.getData1() == null)
                return false;
            return ((element.getData1() >> SHIFT_SUMMERY) & PATTERN_SUMMERY) == 1;
        }

        public String getFontName() {
            return element.getData3();
        }

        public void setColor(int color) {
            element.setData2((long) color);
        }

        public void setTextSize(int textSize) {
            element.setData1(patternData1(textSize, isBold(), isItalic(), isMultiline(),
                    isSummery()));
        }

        public void setBold(boolean bold) {
            element.setData1(patternData1( getTextSize(), bold, isItalic(), isMultiline(),
                    isSummery()));
        }

        public void setItalic(boolean italic) {
            element.setData1(patternData1( getTextSize(), isBold(), italic, isMultiline(),
                    isSummery()));
        }

        public void setMultiline(boolean multiline) {
            element.setData1(patternData1( getTextSize(), isBold(), isItalic(), multiline,
                    isSummery()));
        }

        public void setSummery(boolean summery) {
            element.setData1(patternData1(getTextSize(), isBold(), isItalic(), isMultiline(),
                    summery));
        }

        public void setFontName(String name) {
            element.setData3(name);
        }

        private long patternData1(int textSize, boolean bold, boolean italic,
                                         boolean multiline, boolean summery) {
            int t = textSize >> LENGTH_TEXT_SIZE;
            int b = (bold ? 1 : 0) >> LENGTH_BOLD;
            int i = (italic ? 1 : 0) >> LENGTH_ITALIC;
            int m = (multiline ? 1 : 0) >> LENGTH_MULTILINE;
            int s = (summery ? 1 : 0) >> LENGTH_SUMMERY;
            if ((t | b | i | m | s) != 0)
                throw new RuntimeException("one or more arguments are bigger than expected");

            long data1 = element.getData1() == null? 0 : element.getData1();
            data1 = (data1 >> SHIFT_DATA1 ) << SHIFT_DATA1;

            return data1 | (textSize << SHIFT_TEXT_SIZE) | ((bold ? 1 : 0) << SHIFT_BOLD) |
                    ((italic ? 1 : 0) << SHIFT_ITALIC) | ((multiline ? 1 : 0) << SHIFT_MULTILINE) |
                    ((summery ? 1 : 0) << SHIFT_SUMMERY);
        }
    }

    public static class TextInterpreter extends TextBaseInterpreter{
        public TextInterpreter(Element element) {
            if (element.getPattern() != PATTERN_TEXT)
                throw new RuntimeException("Invalid element for this interpreter");
            this.element = element;
        }

        public void setHint(String hint) {
            element.setData4(hint);
        }

        public String getHint() {
            return element.getData4();
        }
    }

    public static class ListInterpreter extends TextBaseInterpreter{
        private static final int PATTERN_LIST_TYPE = 0b11111;

        public static final int LIST_TYPE_NUMBERS = 0b00000;
        public static final int LIST_TYPE_BULLETS = 0b00001;
        public static final int LIST_TYPE_BULLETS_EMPTY = 0b00010;

        public static final int LENGTH_LIST_TYPE = 5;

        public static final long SHIFT_LIST_TYPE = SHIFT_DATA1;
        public ListInterpreter(Element element) {
            if (element.getPattern() != PATTERN_LIST)
                throw new RuntimeException("Invalid element for this interpreter");
            this.element = element;
        }

        public int getListType() {
            if (element.getData1() == null)
                return 0;
            return (int) ((element.getData1() >> SHIFT_LIST_TYPE) & PATTERN_LIST_TYPE);
        }

        public void setListType(int listType) {
            element.setData1(patternData1(listType));
        }

        private long patternData1(int listType) {
            if (((listType >> LENGTH_LIST_TYPE)) != 0)
                throw new RuntimeException("one or more arguments are bigger than expected");
            long mask = 0;
            for (int i = 0; i < SHIFT_DATA1; i++)
                mask = (mask << 1) | 1;
            long data1 = element.getData1() == null? 0 : element.getData1();
            return (data1 & mask) |  (listType << SHIFT_LIST_TYPE);
        }
    }
    
    public static class PictureInterpreter extends Interpreter{
        private Element element;

        private static final int PATTERN_SINGLE_MODE = 0b1;

        public static final int LENGTH_SINGLE_MODE = 1;

        public static final int SHIFT_SINGLE_MODE = 0;

        public PictureInterpreter(Element element) {
            if (element.getPattern() != PATTERN_PICTURE)
                throw new RuntimeException("Invalid element for this interpreter");
            this.element = element;
        }

        public boolean isSingleMode() {
            if (element.getData1() == null)
                return false;
            return ((element.getData1() >> SHIFT_SINGLE_MODE) & PATTERN_SINGLE_MODE) == 1;
        }

        public void setSingleMode(boolean singleMode) {
            element.setData1(patternData1(singleMode));
        }

        private static long patternData1(boolean singleMode) {
            if ((((singleMode ? 1 : 0) >> LENGTH_SINGLE_MODE)) != 0)
                throw new RuntimeException("one or more arguments are bigger than expected");
            return ((singleMode ? 1 : 0) << SHIFT_SINGLE_MODE);
        }
    }
    
    public static class DividerInterpreter extends TextBaseInterpreter{

        private static final int PATTERN_DIVIDER_TYPE = 0b11111;

        public static final int DIVIDER_TYPE_LINE = 0b00000;
        public static final int DIVIDER_TYPE_DASHED_LINE = 0b00001;
        public static final int DIVIDER_TYPE_TITLE = 0b00010;
        public static final int DIVIDER_TYPE_TITLE_BACKGROUND = 0b00011;
        public static final int DIVIDER_TYPE_SPACE = 0b00100;

        public static final int LENGTH_DIVIDER_TYPE = 5;

        public static final long SHIFT_DIVIDER_TYPE = SHIFT_DATA1;

        public DividerInterpreter(Element element) {
            if (element.getPattern() != PATTERN_DIVIDER)
                throw new RuntimeException("Invalid element for this interpreter");
            this.element = element;
        }

        public int getDividerType() {
            if (element.getData1() == null)
                return 0;
            return (int) ((element.getData1() >> SHIFT_DIVIDER_TYPE) & PATTERN_DIVIDER_TYPE);
        }

        public void setDividerType(int dividerType) {
            element.setData1(patternData1(dividerType));
        }

        private long patternData1(int dividerType) {
            if (((dividerType >> LENGTH_DIVIDER_TYPE)) != 0)
                throw new RuntimeException("one or more arguments are bigger than expected");
            long mask = 0;
            for (int i = 0; i < SHIFT_DATA1; i++)
                mask = (mask << 1) | 1;
            long data1 = element.getData1() == null? 0 : element.getData1();
            return (data1 & mask) | (dividerType << SHIFT_DIVIDER_TYPE);
        }
    }

    //========================================= Utils ==============================================

    public static class PositionComparator implements Comparator<Element> {
        @Override
        public int compare(Element o1, Element o2) {
            return Integer.compare(o1.getPosition(), o2.getPosition());
        }
    }
}
