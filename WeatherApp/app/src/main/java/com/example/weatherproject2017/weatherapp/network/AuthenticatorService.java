package com.example.weatherproject2017.weatherapp.network;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * A bound Service that instantiates the authenticator when started.
 */
public class AuthenticatorService extends Service {
    private static final String TAG = AuthenticatorService.class.getSimpleName();
    private static final String ACCOUNT_TYPE = "com.example.weatherproject2017.weatherapp";
    public static final String ACCOUNT_NAME = "sync";

    private Authenticator mAuthenticator;

    public static Account getAccount() {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Authenticator Service started.");
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
