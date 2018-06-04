package data.preference;

import android.content.Context;

import org.joda.time.DateTimeZone;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.PersianChronology;

import java.util.TreeMap;

import data.model.profiles.Profile;

/**
 * Created by Ahmad on 02/01/18.
 * All rights reserved.
 */

public class ContentPreferences {
    private static final String KEY_CURRENT_PROFILE_ID = "currentProfileId";
    private static final String KEY_CURRENT_PORT = "currentPort";
    private static final String KEY_CURRENT_CHRONOLOGY = "currentChronology";
    private static final String KEY_LABEL_LOOKUP = "labelLookup";
    private static final String KEY_NOTE_SIDE_VISIBILITY = "noteSideVisibility";

    public static class Profiles {
        private static Long currentProfileId = null;

        public static void setSavedCurrentProfile(Profile profile, Context context) {
            if (!profile.isRealized()) {
                throw new RuntimeException("Profile must be realized");
            }
            Contract.getContentPreferences(context).edit().putLong(KEY_CURRENT_PROFILE_ID, profile.getId()).apply();
            currentProfileId = profile.getId();
        }

        public static void setSavedCurrentProfile(long id, Context context) {
            Contract.getContentPreferences(context).edit().putLong(KEY_CURRENT_PROFILE_ID, id).apply();
            currentProfileId = id;
        }

        public static long getSavedCurrentProfileId(Context context) {
            if (currentProfileId == null) {
                currentProfileId = Contract.getContentPreferences(context).getLong(KEY_CURRENT_PROFILE_ID, 0);
            }
            return currentProfileId;
        }
    }

    public static class Port {
        private static Integer port = null;

        public static void setCurrentPort(int port, Context context) {
            Contract.getContentPreferences(context).edit().putInt(KEY_CURRENT_PORT, port).apply();
            Port.port = port;
        }

        public static Integer getCurrentPort(Context context) {
            if (port == null) {
                port = Contract.getContentPreferences(context).getInt(KEY_CURRENT_PORT, -1);
                if (port == -1) {
                    port = null;
                }
            }
            return port;
        }
    }

    public static class Chronology {
        private static final int CHRONOLOGY_PERSIAN = 1;
        private static final int CHRONOLOGY_GREGORIAN = 2;

        private static final int DEFAULT_CHRONOLOGY = CHRONOLOGY_GREGORIAN;

        private static Integer currentChronology;

        public static void setCurrentChronologyPersian(Context context) {
            currentChronology = CHRONOLOGY_PERSIAN;
            setCurrentChronology(context);
        }

        public static void setCurrentChronologyGregorian(Context context) {
            currentChronology = CHRONOLOGY_GREGORIAN;
            setCurrentChronology(context);
        }

        private static void setCurrentChronology(Context context) {
            if (currentChronology != null) {
                Contract.getContentPreferences(context).edit().putInt(KEY_CURRENT_CHRONOLOGY, currentChronology).apply();
            }
        }

        public static org.joda.time.Chronology getCurrentChronology(Context context) {
            return getCurrentChronology(context, DateTimeZone.getDefault());
        }

        public static org.joda.time.Chronology getCurrentChronology(Context context, DateTimeZone timeZone) {
            if (currentChronology == null) {
                currentChronology = Contract.getContentPreferences(context)
                        .getInt(KEY_CURRENT_CHRONOLOGY, DEFAULT_CHRONOLOGY);
            }
            if (currentChronology == CHRONOLOGY_PERSIAN) {
                return PersianChronology.getInstance(timeZone);
            } else if (currentChronology == CHRONOLOGY_GREGORIAN) {
                return GregorianChronology.getInstance(timeZone);
            }
            return null;
        }
    }

    public static class LabelLookup {
        public static final int MODE_ALL_LABELS = 0;
        public static final int MODE_LABEL_LISTS = 1;

        private static Integer mode = null;

        public static void setMode(int mode, Context context) {
            Contract.getContentPreferences(context).edit().putInt(KEY_LABEL_LOOKUP, mode).apply();
            LabelLookup.mode = mode;
        }

        public static Integer getMode(Context context) {
            if (mode == null) {
                mode = Contract.getContentPreferences(context).getInt(KEY_LABEL_LOOKUP, -1);
                if (mode == -1) {
                    mode = null;
                }
            }
            return mode;
        }
    }

    public static class NoteSideVisibility {
        public static final int MODE_FRONT_VISIBLE = 0;
        public static final int MODE_BACK_VISIBLE = 1;

        private static Integer mode = null;

        public static void setMode(int mode, Context context) {
            Contract.getContentPreferences(context).edit().putInt(KEY_NOTE_SIDE_VISIBILITY, mode).apply();
            NoteSideVisibility.mode = mode;
        }

        public static void setModeFrontVisible(Context context) {
            setMode(MODE_FRONT_VISIBLE, context);
        }

        public static void setModeBackVisible(Context context) {
            setMode(MODE_BACK_VISIBLE, context);
        }

        public static int getMode(Context context) {
            if (mode == null) {
                mode = Contract.getContentPreferences(context).getInt(KEY_NOTE_SIDE_VISIBILITY, MODE_FRONT_VISIBLE);
            }
            return mode;
        }

        public static boolean isModeFrontVisible(Context context) {
            return getMode(context) == MODE_FRONT_VISIBLE;
        }

        public static boolean isModeBackVisible(Context context) {
            return getMode(context) == MODE_BACK_VISIBLE;
        }
    }
}
