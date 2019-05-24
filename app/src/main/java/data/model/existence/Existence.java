package data.model.existence;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import data.database.file.FileContract;
import data.model.profiles.Profile;
import data.storage.Captures;
import data.storage.Pictures;

/**
 * Created by Ahmad on 02/15/18.
 * All rights reserved.
 */

public class Existence {
    public static final int STATE_PRESENT = FileContract.Existence.STATE_PRESENT;
    public static final int STATE_DELETE = FileContract.Existence.STATE_DELETE;

    public static int TYPE_PICTURE = 0b001;
    public static int TYPE_CAPTURE = 0b010;

    public static final int EXISTENCE_FLAG_DRIVE = FileContract.Existence.EXISTENCE_FLAG_DRIVE;
    public static final int EXISTENCE_FLAG_DROP_BOX = FileContract.Existence.EXISTENCE_FLAG_DROP_BOX;
    public static final int EXISTENCE_FLAG_PCLOUD = FileContract.Existence.EXISTENCE_FLAG_PCLOUD;

    private long pattern;
    private int type;
    private Long profile;
    private int state;
    private int existenceFlags;
    private Long data1;

    private boolean isRealized;
    private boolean isInitialized;

    private Existence() {

    }

    public static Existence newInstance() {
        return new Existence();
    }

    public long getPattern() {
        return pattern;
    }

    public Existence setPattern(long pattern) {
        this.pattern = pattern;
        return this;
    }

    public int getType() {
        return type;
    }

    public Existence setType(int type) {
        this.type = type;
        return this;
    }

    public Long getProfile() {
        return profile;
    }

    public Existence setProfile(Long profile) {
        this.profile = profile;
        return this;
    }

    public Long getData1() {
        return data1;
    }

    public Existence setData1(Long data1) {
        this.data1 = data1;
        return this;
    }

    public int getState() {
        return state;
    }

    public Existence setState(int state) {
        this.state = state;
        return this;
    }

    public int getExistenceFlags() {
        return existenceFlags;
    }

    public Existence setExistenceFlags(int existenceFlags) {
        this.existenceFlags = existenceFlags;
        return this;
    }

    public boolean isRealized() {
        return isRealized;
    }

    public Existence setRealized(boolean realized) {
        isRealized = realized;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public Existence setInitialized(boolean initialized) {
        isInitialized = initialized;
        return this;
    }

    public Existence clone() {
        return newInstance()
                .setPattern(pattern)
                .setType(type)
                .setExistenceFlags(existenceFlags)
                .setState(state)
                .setData1(data1)
                .setRealized(isRealized)
                .setInitialized(isInitialized);
    }

    public File getDirectory() {
        if (Pattern.isPatternTypeCapture(pattern)) {
            return Captures.getDirectory();
        } else if (Pattern.isPatternTypePicture(pattern)) {
            long profileId = Pattern.Picture.getProfileId(pattern);
            return Pictures.getProfileDir(Profile.newInstance().setId(profileId));
        } else {
            throw new RuntimeException("Given pattern is not recognized");
        }
    }

    public File getFile() {
        if (Pattern.isPatternTypeCapture(pattern)) {
            return Captures.getCaptureFile(Pattern.Capture.getCaptureId(pattern), data1);
        } else if (Pattern.isPatternTypePicture(pattern)) {
            Profile profile = Profile.newInstance().setId(Pattern.Picture.getProfileId(pattern));
            long id = Pattern.Picture.getPictureId(pattern);
            return Pictures.getPictureFile(profile, id);
        } else {
            throw new RuntimeException("Given pattern is not recognized");
        }
    }

    public static ArrayList<Existence> filterForPictures(Collection<Existence> existences) {
        ArrayList<Existence> result = new ArrayList<>(existences.size());
        for (Existence existence : existences) {
            if (Pattern.isPatternTypePicture(existence.getPattern())) result.add(existence);
        }
        return result;
    }

    public static ArrayList<Existence> filterForCaptures(Collection<Existence> existences) {
        ArrayList<Existence> result = new ArrayList<>(existences.size());
        for (Existence existence : existences) {
            if (Pattern.isPatternTypeCapture(existence.getPattern())) result.add(existence);
        }
        return result;
    }

    public static class Pattern {
        public static long TYPE_PICTURE = Existence.TYPE_PICTURE;
        public static long TYPE_CAPTURE = Existence.TYPE_CAPTURE;
        public static long SHIFT_TYPE = 3;

        public static class Picture {
            public static long SHIFT_PROFILE_ID = 18;

            public static long getPattern(Profile profile, long id) {
                return getPattern(profile.getId(), id);
            }

            public static long getPattern(Long profileId, long id) {
                if (profileId > Math.pow(2, SHIFT_PROFILE_ID))
                    throw new RuntimeException("");
                return TYPE_PICTURE | (profileId << SHIFT_TYPE) | (id << (SHIFT_TYPE + SHIFT_PROFILE_ID));
            }

            public static long getProfileId(long pattern) {
                long mask = 0;
                for (int i = 0; i < SHIFT_PROFILE_ID; i++) {
                    mask = (mask << 1) | 1;
                }
                return (pattern >> SHIFT_TYPE) & mask;
            }

            public static long getPictureId(long pattern) {
                return pattern >> (SHIFT_TYPE + SHIFT_PROFILE_ID);
            }
        }

        public static class Capture {

            public static long getPattern(long captureId) {
                return TYPE_CAPTURE | (captureId << SHIFT_TYPE);
            }

            public static long getCaptureId(long pattern) {
                return (pattern >> SHIFT_TYPE);
            }
        }

        public static long getPatternType(long pattern) {
            long mask = 0;
            for (int i = 0; i < SHIFT_TYPE; i++) {
                mask = (mask << 1) | 1;
            }

            return (pattern >> 0) & mask;
        }

        public static boolean isPatternTypePicture(long pattern) {
            return getPatternType(pattern) == TYPE_PICTURE;
        }

        public static boolean isPatternTypeCapture(long pattern) {
            return getPatternType(pattern) == TYPE_CAPTURE;
        }
    }
}
