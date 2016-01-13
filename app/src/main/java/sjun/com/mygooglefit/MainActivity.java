package sjun.com.mygooglefit;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.common.base.Strings;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    @Bind(R.id.webview)
    WebView webView;

    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Timber.d("Main.onCreate called");

        if (isLoggedin()) {
            // start loading...
            Timber.d("start loading...");

            Toast.makeText(this, "Start loading for ", Toast.LENGTH_LONG).show();
        } else {
            pickUserAccount();
        }

        setWebView();
    }

    private boolean isLoggedin() {
        return !Strings.isNullOrEmpty(PrefUtil.readString(this, PrefUtil.TOKEN));
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onAcitivytResult(%d, %d)", requestCode, resultCode);

        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

                mAccount = new Account(name, type);

                Timber.d("mEmail: " + mEmail);

                PrefUtil.writeString(this, PrefUtil.ACCOUNT_NAME, mAccount.name);
                PrefUtil.writeString(this, PrefUtil.ACCOUNT_TYPE, mAccount.type);

                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, "pick account is canceled", Toast.LENGTH_SHORT).show();
            }
        } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            // Receiving a result that follows a GoogleAuthException, try auth again
            getUsername();
        }

    }

    private final static String ACTIVITY_READ
            = "https://www.googleapis.com/auth/fitness.activity.read";
    private final static String ACTIVITY_WRITE
            = "https://www.googleapis.com/auth/fitness.activity.write";
    private final static String BODY_READ
            = " https://www.googleapis.com/auth/fitness.body.read";
    private final static String BODY_WRITE
            = "https://www.googleapis.com/auth/fitness.body.write";
    private final static String LOCATION_READ
            = "https://www.googleapis.com/auth/fitness.location.read";
    private final static String LOCATION_WRITE
            = "https://www.googleapis.com/auth/fitness.location.write";

    private final static String USERINFO_PROFILE
            = "https://www.googleapis.com/auth/userinfo.profile";
    private final static String mScopes
            = "oauth2:" + USERINFO_PROFILE + " " + ACTIVITY_READ + " "
            + ACTIVITY_WRITE + " " + BODY_READ + " " + BODY_WRITE + " "
            + LOCATION_READ + " " + LOCATION_WRITE;

    /**
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */
    private void getUsername() {
        if (mAccount == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {
                Timber.d("device is online, start task!");

                new GetUsernameTask(MainActivity.this, mAccount, mScopes).execute();
            } else {
                Toast.makeText(this, "device is not in online", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    private void setWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // web setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyGoogleFit2", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        // load html
        webView.loadUrl("file:///android_asset/index.html");
    }

    private class GetUsernameTask extends AsyncTask<Void, Void, Void> {
        private final MainActivity mActivity;
        private final Account mAccount;
        private final String mScope;

        public GetUsernameTask(MainActivity activity, Account account, String scopes) {
            this.mActivity = activity;
            this.mAccount = account;
            this.mScope = scopes;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Timber.d("start doInBackground, mEmail: " + mEmail);

                String token = fetchToken();
                if (token != null) {
                    // save to preference
                    PrefUtil.writeString(mActivity, PrefUtil.TOKEN, token);

                    Timber.d("token: %s", token);
                }
            } catch (IOException e) {
                // The fetchToken() method handles Google-specific exceptions,
                // so this indicates something went wrong at a higher level.
                // TIP: Check for network connectivity before starting the AsyncTask.
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Gets an authentication token from Google and handles any
         * GoogleAuthException that may occur.
         */
        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mActivity, mAccount, mScope);
            } catch (UserRecoverableAuthException userRecoverableException) {
                // GooglePlayServices.apk is either old, disabled, or not present
                // so we need to show the user some UI in the activity to recover.
                mActivity.handleException(userRecoverableException);
            } catch (GoogleAuthException fatalException) {
                // Some other type of unrecoverable exception has occurred.
                // Report and log the error as appropriate for your app.
                fatalException.printStackTrace();
            }

            return null;
        }

    }

    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1002;

    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(() -> {
            if (e instanceof GooglePlayServicesAvailabilityException) {
                // The Google Play services APK is old, disabled, or not present.
                // Show a dialog created by Google Play services that allows
                // the user to update the APK
                int statusCode = ((GooglePlayServicesAvailabilityException) e)
                        .getConnectionStatusCode();
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                        MainActivity.this,
                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                dialog.show();
            } else if (e instanceof UserRecoverableAuthException) {
                // Unable to authenticate, such as when the user has not yet granted
                // the app access to the account, but the user can fix this.
                // Forward the user to an activity in Google Play services.
                Intent intent = ((UserRecoverableAuthException) e).getIntent();
                startActivityForResult(intent,
                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
            }
        });
    }
}
