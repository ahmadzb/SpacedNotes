package data.dropbox;

import android.content.Context;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.auth.DbxUserAuthRequests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data.preference.SyncPreferences;
import data.sync.SignInException;

/**
 * Created by Ahmad on 05/21/18.
 * All rights reserved.
 */
public class Authentication {

    private static Lock signInLock = new ReentrantLock();
    private static SignIn signIn;

    public static SignIn getSignIn(final Context context) throws SignInException {
        try {
            signInLock.lock();
            if (signIn == null) {
                String accessToken = SyncPreferences.Dropbox.getToken(context);
                if (accessToken == null) {
                    accessToken = Auth.getOAuth2Token();
                    if (accessToken != null) {
                        SyncPreferences.Dropbox.setToken(accessToken, context);
                    } else {
                        throw new SignInException();
                    }
                }
                signIn = new SignIn();
                signIn.requestConfig = DbxRequestConfig.newBuilder("SpacedNotes")
                        .build();
                signIn.client = new DbxClientV2(signIn.requestConfig, accessToken);
            }
            return signIn;
        } finally {
            signInLock.unlock();
        }
    }

    public static void signOut(Context context) throws SignInException, DbxException {
        SignIn signIn = getSignIn(context);
        signIn.getClient().auth().tokenRevoke();
    }


    public static class SignIn {
        private DbxRequestConfig requestConfig;
        private DbxClientV2 client;

        public DbxRequestConfig getRequestConfig() {
            return requestConfig;
        }

        public DbxClientV2 getClient() {
            return client;
        }
    }
}
