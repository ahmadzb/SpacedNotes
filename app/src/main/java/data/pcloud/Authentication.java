package data.pcloud;

import android.content.Context;
import android.content.Intent;

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
        //TODO
        throw new RuntimeException("Not implemented");
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
                //TODO
                throw new RuntimeException("Not implemented");
            }

            return signIn;
        } finally {
            signInLock.unlock();
        }
    }

    public static void signOut(Context context) throws SignInException  {
        SignIn signIn = getSignIn(context);
        //TODO
        throw new RuntimeException("Not implemented");

    }


    public static class SignIn {

        //TODO
        private Object client;

        //TODO
        public Object getClient() {
            return client;
        }

    }
}
