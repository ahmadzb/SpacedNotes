package data.xml.profiles;

import android.content.Context;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;

import data.storage.Profiles;
import data.xml.util.BufferedDocumentProvider;
import data.xml.util.DocumentInitializer;

/**
 * Created by Ahmad on 02/08/18.
 * All rights reserved.
 */

public class ProfilesProvider {
    private static BufferedDocumentProvider profilesDocument;
    private static Document toModifyDocument;

    static void reloadDocument() {
        if (profilesDocument != null) {
            profilesDocument.reloadDocument();
        }
        toModifyDocument = null;
    }

    static void waitUntilNoOperations() {
        if (profilesDocument != null) {
            profilesDocument.waitUntilNoWrite();
        }
    }

    private static BufferedDocumentProvider getProfilesDocument(Context context) {
        if (profilesDocument == null) {
            profilesDocument = BufferedDocumentProvider.newInstance(
                    Profiles.getFile(context),
                    new DocumentInitializer() {
                        @Override
                        public Document initializeDocument() {
                            Element profiles = new Element(ProfilesContract.itemName);

                            return new Document(profiles);
                        }
                    }
            );
        }
        return profilesDocument;
    }

    static Element getProfiles(Context context) {
        if (toModifyDocument == null) {
            try {
                toModifyDocument = getProfilesDocument(context).getDocument();
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (toModifyDocument != null) {
            return toModifyDocument.getRootElement();
        }
        return null;
    }

    static void saveDocument(Context context) {
        if (toModifyDocument != null) {
            getProfilesDocument(context).writeDocumentAsync(toModifyDocument);
            toModifyDocument = null;
        }
    }

    static void beginTransaction(Context context) {
        getProfilesDocument(context).beginTransaction();
    }

    static void setTransactionSuccessful(Context context) {
        getProfilesDocument(context).setTransactionSuccessful();
    }

    static void endTransaction(Context context) {
        getProfilesDocument(context).endTransaction();
    }
}
