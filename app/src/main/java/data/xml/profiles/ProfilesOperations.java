package data.xml.profiles;

import android.content.Context;

import com.diplinkblaze.spacednote.R;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.database.Contract;
import data.database.file.ExistenceOperations;
import data.database.file.FileOpenHelper;
import data.model.existence.Existence;
import data.model.existence.ExistenceCatalog;
import data.model.profiles.Profile;
import data.model.profiles.ProfileCatalog;
import data.preference.ContentPreferences;
import data.xml.log.operator.LogOperations;
import data.xml.port.IdProvider;

/**
 * Created by Ahmad on 02/08/18.
 * All rights reserved.
 */
public class ProfilesOperations {

    public static void reloadProfiles() {
        ProfilesProvider.reloadDocument();
    }

    public static void waitUntilNoOperations() {
        ProfilesProvider.waitUntilNoOperations();
    }

    public static Profile getCurrentProfileIfExist(Context context) {
        long id = ContentPreferences.Profiles.getSavedCurrentProfileId(context);
        Profile profile = getProfileById(id, context);
        if (profile == null) {
            LogOperations.performNewOperationsFromLog(context);
            ArrayList<Profile> profiles = getProfiles(context);
            if (profiles.size() != 0) {
                profile = profiles.get(0);
                setCurrentProfile(profile, context);
            }
        }
        return profile;
    }

    public static Profile getCurrentProfile(Context context) {
        Profile profile = getCurrentProfileIfExist(context);
        if (profile == null) {
            profile = addDefaultProfile(context);
        }
        return profile;
    }

    public static void setCurrentProfile(Profile profile, Context context) {
        ContentPreferences.Profiles.setSavedCurrentProfile(profile, context);
    }

    public static Profile getProfileById(long id, Context context) {
        ArrayList<Profile> profiles = getProfiles(context);
        for (Profile profile : profiles) {
            if (profile.getId() == id) {
                return profile;
            }
        }
        return null;
    }

    public static ArrayList<Profile> getProfilesNotOffline(Context context) {
        ArrayList<Profile> profiles = getProfiles(context);
        ArrayList<Profile> result = new ArrayList<>(profiles.size());
        for (Profile profile : profiles) {
            if (!profile.isOffline())
                result.add(profile);
        }
        return result;
    }

    public static ArrayList<Profile> getProfilesNotArchivedSorted(Context context) {
        ArrayList<Profile> profiles = getProfiles(context);
        ArrayList<Profile> result = new ArrayList<>(profiles.size());
        for (Profile profile : profiles) {
            if (!profile.isArchived())
                result.add(profile);
        }
        Collections.sort(result, new Profile.PositionComparator());
        return result;
    }

    public static ArrayList<Profile> getProfilesSorted(Context context) {
        ArrayList<Profile> profiles = getProfiles(context);
        Collections.sort(profiles, new Profile.PositionComparator());
        return profiles;
    }

    public static ArrayList<Profile> getProfiles(Context context) {
        ArrayList<Profile> profiles = null;
        try {
            Element root = ProfilesProvider.getProfiles(context);
            profiles = new ArrayList<>();
            List<Element> childElements = root.getChildren();
            for (Element element : childElements) {
                if (ProfilesContract.Profile.itemName.equals(element.getName())) {
                    Profile profile = Profile.newInstance();
                    profile.setId(element.getAttribute(ProfilesContract.Profile.id).getLongValue());
                    profile.setName(element.getAttribute(ProfilesContract.Profile.name).getValue());
                    profile.setPosition(element.getAttribute(ProfilesContract.Profile.position).getIntValue());
                    profile.setColor(element.getAttribute(ProfilesContract.Profile.color).getIntValue());
                    profile.setArchived(element.getAttribute(ProfilesContract.Profile.isArchived).getBooleanValue());
                    profile.setOffline(element.getAttribute(ProfilesContract.Profile.offline).getBooleanValue());
                    profile.setImageQualityPercentage(
                            element.getAttribute(ProfilesContract.Profile.imageQualityPercentage).getIntValue());
                    profile.setInitialized(true).setRealized(true);
                    profiles.add(profile);
                }
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    private static Profile addDefaultProfile(Context context) {
        Profile profile = Profile.newInstance();
        profile.setName(context.getString(R.string.default_profile));
        profile.setColor(context.getResources().getColor(R.color.primaryColor));
        profile.setInitialized(true);
        profile.setOffline(true);
        profile.setImageQualityPercentage(50);
        profile.setId(ProfileCatalog.addProfile(profile, context));//Should use ProfileCatalog to create log as well
        return profile;
    }

    public static long addProfile(Profile profile, Context context) {

        Element root = ProfilesProvider.getProfiles(context);

        //Profile
        {
            Element profileElement = new Element(ProfilesContract.Profile.itemName);
            Profile oldProfile = null;
            if (profile.isRealized()) {
                oldProfile = getProfileById(profile.getId(), context);
            }
            if (oldProfile == null) {
                if (!profile.isRealized()) {
                    profile.setId(IdProvider.nextProfileId(context));
                    profile.setRealized(true);
                }

                profileElement.setAttribute(ProfilesContract.Profile.id, String.valueOf(profile.getId()));
                profileElement.setAttribute(ProfilesContract.Profile.name, profile.getName());
                profileElement.setAttribute(ProfilesContract.Profile.color, String.valueOf(profile.getColor()));
                profileElement.setAttribute(ProfilesContract.Profile.isArchived, String.valueOf(profile.isArchived()));
                profileElement.setAttribute(ProfilesContract.Profile.offline, String.valueOf(profile.isOffline()));
                profileElement.setAttribute(ProfilesContract.Profile.imageQualityPercentage,
                        String.valueOf(profile.getImageQualityPercentage()));
                profileElement.setAttribute(ProfilesContract.Profile.position, String.valueOf(profile.getPosition()));
                root.addContent(profileElement);
                ProfilesProvider.saveDocument(context);
            } else {
                updateProfile(profile, context);
            }
            return profile.getId();
        }
    }

    public static void updateProfile(Profile profile, Context context) {
        try {
            Element root = ProfilesProvider.getProfiles(context);

            Element profileElement = null;
            List<Element> elements = root.getChildren();
            for (Element element : elements) {
                if (ProfilesContract.Profile.itemName.equals(element.getName())) {
                    if (element.getAttribute(ProfilesContract.Profile.id).getLongValue() == profile.getId()) {
                        profileElement = element;
                        break;
                    }
                }
            }
            if (profileElement != null) {
                profileElement.setAttribute(ProfilesContract.Profile.name, profile.getName());
                profileElement.setAttribute(ProfilesContract.Profile.color, String.valueOf(profile.getColor()));
                profileElement.setAttribute(ProfilesContract.Profile.isArchived, String.valueOf(profile.isArchived()));
                profileElement.setAttribute(ProfilesContract.Profile.position, String.valueOf(profile.getPosition()));
                profileElement.setAttribute(ProfilesContract.Profile.offline, String.valueOf(profile.isOffline()));
                profileElement.setAttribute(ProfilesContract.Profile.imageQualityPercentage,
                        String.valueOf(profile.getImageQualityPercentage()));


                ProfilesProvider.saveDocument(context);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    public static void deleteProfile(Profile profile, Context context) {
        try {
            Element root = ProfilesProvider.getProfiles(context);

            //Profile
            {
                Element profileElement = null;
                List<Element> elements = root.getChildren();
                for (Element element : elements) {
                    if (ProfilesContract.Profile.itemName.equals(element.getName())) {
                        if (element.getAttribute(ProfilesContract.Profile.id).getLongValue() == profile.getId()) {
                            profileElement = element;
                            break;
                        }
                    }
                }
                if (profileElement == null) {
                    throw new RuntimeException("Profile not found");
                }
                root.removeContent(profileElement);
            }

            //Existence
            {
                ExistenceOperations.setAllExistencesStateByProfile(profile,
                        Existence.STATE_DELETE, FileOpenHelper.getDatabase(context));
            }

            ProfilesProvider.saveDocument(context);
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    public static void beginTransaction(Context context) {
        ProfilesProvider.beginTransaction(context);
    }

    public static void setTransactionSuccessful(Context context) {
        ProfilesProvider.setTransactionSuccessful(context);
    }

    public static void endTransaction(Context context) {
        ProfilesProvider.endTransaction(context);
    }
}
