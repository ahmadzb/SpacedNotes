package data.xml.log.operations;

import android.content.Context;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import data.model.profiles.Profile;
import data.xml.log.operator.LogContract;
import data.xml.log.operator.LogOperations;
import data.xml.profiles.ProfilesOperations;

/**
 * Created by Ahmad on 02/08/18.
 * All rights reserved.
 */

public class ProfileOperations {
    //============================================ Read ============================================
    public static void performOperation(Element element, long time, Context context) {
        String name = element.getName();
        try {
            if (name.equals(LogContract.Profile.Add.itemName)) {
                performAddProfile(element, context);
            } else if (name.equals(LogContract.Profile.Update.itemName)) {
                performUpdateProfile(element, context);
            } else if (name.equals(LogContract.Profile.Delete.itemName)) {
                performDeleteProfile(element, context);
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
    }

    public static void performAddProfile(Element element, Context context) throws DataConversionException {
        Profile profile = Profile.newInstance();
        profile.setId(element.getAttribute(LogContract.Profile.Add.id).getLongValue());
        profile.setColor(element.getAttribute(LogContract.Profile.Add.color).getIntValue());
        profile.setArchived(element.getAttribute(LogContract.Profile.Add.isArchived).getBooleanValue());
        profile.setName(element.getAttribute(LogContract.Profile.Add.name).getValue());
        profile.setPosition(element.getAttribute(LogContract.Profile.Add.position).getIntValue());
        profile.setOffline(element.getAttribute(LogContract.Profile.Add.offline).getBooleanValue());
        profile.setImageQualityPercentage(
                element.getAttribute(LogContract.Profile.Add.imageQualityPercentage).getIntValue());
        profile.setRealized(true);
        ProfilesOperations.addProfile(profile, context);
    }

    public static void performUpdateProfile(Element element, Context context) throws DataConversionException {
        Profile profile = Profile.newInstance();
        profile.setId(element.getAttribute(LogContract.Profile.Update.id).getLongValue());
        profile.setColor(element.getAttribute(LogContract.Profile.Update.color).getIntValue());
        profile.setArchived(element.getAttribute(LogContract.Profile.Update.isArchived).getBooleanValue());
        profile.setName(element.getAttribute(LogContract.Profile.Update.name).getValue());
        profile.setPosition(element.getAttribute(LogContract.Profile.Update.position).getIntValue());
        profile.setOffline(element.getAttribute(LogContract.Profile.Update.offline).getBooleanValue());
        profile.setImageQualityPercentage(
                element.getAttribute(LogContract.Profile.Update.imageQualityPercentage).getIntValue());
        ProfilesOperations.updateProfile(profile, context);
    }

    public static void performDeleteProfile(Element element, Context context) throws DataConversionException {
        Profile profile = Profile.newInstance();
        profile.setId(element.getAttribute(LogContract.Profile.Delete.id).getLongValue());
        ProfilesOperations.deleteProfile(profile, context);
    }

    //=========================================== Write ============================================
    public static void addProfile(Profile profile, Context context) {
        Element element = new Element(LogContract.Profile.Add.itemName);
        element.setAttribute(LogContract.Profile.Add.id, String.valueOf(profile.getId()));
        if (profile.getName() != null) {
            element.setAttribute(LogContract.Profile.Add.name, profile.getName());
        }
        element.setAttribute(LogContract.Profile.Add.color, String.valueOf(profile.getColor()));
        element.setAttribute(LogContract.Profile.Add.isArchived, String.valueOf(profile.isArchived()));
        element.setAttribute(LogContract.Profile.Add.position, String.valueOf(profile.getPosition()));
        element.setAttribute(LogContract.Profile.Add.offline, String.valueOf(profile.isOffline()));
        element.setAttribute(LogContract.Profile.Add.imageQualityPercentage,
                String.valueOf(profile.getImageQualityPercentage()));
        LogOperations.addProfileOperation(element, context);
    }

    public static void updateProfile(Profile profile, Context context) {
        Element element = new Element(LogContract.Profile.Update.itemName);
        element.setAttribute(LogContract.Profile.Update.id, String.valueOf(profile.getId()));
        if (profile.getName() != null) {
            element.setAttribute(LogContract.Profile.Update.name, profile.getName());
        }
        element.setAttribute(LogContract.Profile.Update.color, String.valueOf(profile.getColor()));
        element.setAttribute(LogContract.Profile.Update.isArchived, String.valueOf(profile.isArchived()));
        element.setAttribute(LogContract.Profile.Update.position, String.valueOf(profile.getPosition()));
        element.setAttribute(LogContract.Profile.Update.offline, String.valueOf(profile.isOffline()));
        element.setAttribute(LogContract.Profile.Update.imageQualityPercentage,
                String.valueOf(profile.getImageQualityPercentage()));
        LogOperations.addProfileOperation(element, context);
    }

    public static void deleteProfile(Profile profile, Context context) {
        Element element = new Element(LogContract.Profile.Delete.itemName);
        element.setAttribute(LogContract.Profile.Delete.id, String.valueOf(profile.getId()));
        LogOperations.addProfileOperation(element, context);
    }
}
