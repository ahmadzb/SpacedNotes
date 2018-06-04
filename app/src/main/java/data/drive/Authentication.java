package data.drive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import data.sync.SignInException;
import util.Concurrent.TaskResult;

/**
 * Created by Ahmad on 01/29/18.
 * All rights reserved.
 */

public class Authentication {
    private static Lock signInLock = new ReentrantLock();
    private static SignIn signIn;

    public static Intent getSignInIntent(Context context) {
        GoogleSignInClient signInClient = buildGoogleSignInClient(context);
        return signInClient.getSignInIntent();
    }

    public static SignIn getSignIn(final Context context) throws SignInException {
        try {
            signInLock.lock();
            if (signIn == null) {
                GoogleSignInClient signInClient = buildGoogleSignInClient(context);
                Task<GoogleSignInAccount> task = signInClient.silentSignIn();
                Tasks.await(task);
                signIn = new SignIn();
                signIn.signInAccount = task.getResult();
                signIn.client = Drive.getDriveClient(context, signIn.signInAccount);
                signIn.resourceClient = Drive.getDriveResourceClient(context, signIn.signInAccount);
            }
            return signIn;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new SignInException();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new SignInException();
        } finally {
            signInLock.unlock();
        }
    }

    public static void signOut(Context context) throws ExecutionException, InterruptedException {
        Task<Void> task = buildGoogleSignInClient(context).signOut();
        Tasks.await(task);
    }

    private static GoogleSignInClient buildGoogleSignInClient(Context context) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(context, signInOptions);
    }

    public static class SignIn {
        private GoogleSignInAccount signInAccount;
        private DriveClient client;
        private DriveResourceClient resourceClient;

        public GoogleSignInAccount getSignInAccount() {
            return signInAccount;
        }

        public DriveClient getClient() {
            return client;
        }

        public DriveResourceClient getResourceClient() {
            return resourceClient;
        }
    }
}
