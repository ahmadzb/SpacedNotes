package data.pcloud;

import android.content.Context;
import android.content.Intent;


import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.AuthorizationActivity;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.PCloudSdkV2;

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

    public static boolean saveToken(Context context, Intent data) {
        String accessToken = null;
        if (data.getExtras() != null && data.getExtras().containsKey(AuthorizationActivity.KEY_ACCESS_TOKEN)) {
            accessToken = data.getStringExtra(AuthorizationActivity.KEY_ACCESS_TOKEN);
        }

        if (accessToken != null) {
            SyncPreferences.PCloud.setToken(accessToken, context);
            return true;
        }
        return false;
    }

    public static SignIn getSignIn(final Context context) throws SignInException {
        try {
            signInLock.lock();
            if (signIn == null) {
                String accessToken = SyncPreferences.PCloud.getToken(context);
                if (accessToken == null) {
                    throw new SignInException();
                }
                signIn = new SignIn();
                signIn.client = PCloudSdkV2.newClientBuilder()
                        .authenticator(Authenticators.newOAuthAuthenticator(accessToken)).create();
            }

            return signIn;
        } finally {
            signInLock.unlock();
        }
    }

    public static void signOut(Context context) throws SignInException  {
        SignIn signIn = getSignIn(context);
        signIn.getClient().shutdown();

    }


    public static class SignIn {

        private ApiClient client;

        public ApiClient getClient() {
            return client;
        }

    }
}
