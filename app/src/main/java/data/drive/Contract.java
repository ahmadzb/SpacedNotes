package data.drive;

import com.google.android.gms.drive.metadata.CustomPropertyKey;

/**
 * Created by Ahmad on 02/14/18.
 * All rights reserved.
 */

public class Contract {

    public static final String HIERARCHY_SEPARATOR = "/";
    public static final String CATEGORY_PREFIX_FOLDER = "FOLDER:/";
    public static final String CATEGORY_PREFIX_FILE = "FILE:/";
    public static final String CATEGORY_MAIN_FOLDER = "MAIN_FOLDER";


    public static final String directoryName = "SpacedNotes";

    public static final CustomPropertyKey CustomPropertyCategory = new CustomPropertyKey("Category", CustomPropertyKey.PUBLIC);


}
