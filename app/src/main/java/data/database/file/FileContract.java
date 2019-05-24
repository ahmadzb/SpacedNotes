package data.database.file;

/**
 * Created by Ahmad on 02/15/18.
 * All rights reserved.
 */

public class FileContract {
    public static class Existence {
        public static final int STATE_PRESENT = 1;
        public static final int STATE_DELETE = 2;

        public static final int EXISTENCE_FLAG_DRIVE = 0b01;
        public static final int EXISTENCE_FLAG_DROP_BOX = 0b10;
        public static final int EXISTENCE_FLAG_PCLOUD= 0b100;

        public static final String table = "EXISTENCE";
        public static final String pattern = "pattern";
        public static final String type = "type";
        public static final String profile = "profile";
        public static final String existenceFlags = "existenceFlags";
        public static final String state = "state";
        public static final String data1 = "data1";
    }
}
