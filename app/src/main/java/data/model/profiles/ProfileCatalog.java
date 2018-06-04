package data.model.profiles;

import android.content.Context;

import java.util.ArrayList;

import data.xml.log.operations.ProfileOperations;
import data.xml.profiles.ProfilesOperations;

/**
 * Created by Ahmad on 01/31/18.
 * All rights reserved.
 */

public class ProfileCatalog {
    public static data.model.profiles.Profile getCurrentProfileIfExist(Context context) {
        return ProfilesOperations.getCurrentProfileIfExist(context);
    }

    public static data.model.profiles.Profile getCurrentProfile(Context context) {
        return ProfilesOperations.getCurrentProfile(context);
    }

    public static void setCurrentProfile(data.model.profiles.Profile profile, Context context) {
        ProfilesOperations.setCurrentProfile(profile, context);
    }

    public static data.model.profiles.Profile getProfileById(long id, Context context) {
        return ProfilesOperations.getProfileById(id, context);
    }

    public static ArrayList<Profile> getProfilesNotOffline(Context context) {
        return ProfilesOperations.getProfilesNotOffline(context);
    }

    public static ArrayList<data.model.profiles.Profile> getProfilesNotArchivedSorted(Context context) {
        return ProfilesOperations.getProfilesNotArchivedSorted(context);
    }

    public static ArrayList<data.model.profiles.Profile> getProfilesSorted(Context context) {
        return ProfilesOperations.getProfilesSorted(context);
    }

    public static ArrayList<data.model.profiles.Profile> getProfiles(Context context) {
        return ProfilesOperations.getProfiles(context);
    }

    public static long addProfile(data.model.profiles.Profile profile, Context context) {
        profile.setId(ProfilesOperations.addProfile(profile, context));
        ProfileOperations.addProfile(profile, context);//log
        return profile.getId();
    }

    public static void updateProfile(data.model.profiles.Profile profile, Context context) {
        ProfilesOperations.updateProfile(profile,context);
        ProfileOperations.updateProfile(profile, context);//log
    }

    public static void deleteProfile(data.model.profiles.Profile profile, Context context) {
        ProfilesOperations.deleteProfile(profile, context);
        ProfileOperations.deleteProfile(profile, context);//log
    }


    //======================================= Transaction ==========================================
    public static void beginTransaction(Context context) {
        ProfilesOperations.beginTransaction(context);
    }

    public static void setTransactionSuccessful(Context context) {
        ProfilesOperations.setTransactionSuccessful(context);
    }

    public static void endTransaction(Context context) {
        ProfilesOperations.endTransaction(context);
    }
}
