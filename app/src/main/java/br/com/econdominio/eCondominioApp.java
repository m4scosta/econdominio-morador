package br.com.econdominio;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;

import java.util.concurrent.atomic.AtomicReference;

public class eCondominioApp extends Application {

    private static AtomicReference<NotifiableActivity> activity = new AtomicReference<>(null);

    @Override
    public void onCreate() {
        super.onCreate();
        setupParse();
        saveInstallation();
    }

    private void setupParse() {
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        Parse.enableLocalDatastore(this);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getResources().getString(R.string.parse_app_id))
                .clientKey(getResources().getString(R.string.parse_client_key))
                .server("https://parseapi.back4app.com")
                .build());
        ParseACL defaultACL = new ParseACL();
        ParseACL.setDefaultACL(defaultACL, true);
    }

    private void saveInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", getResources().getString(R.string.parse_gcm_sender_id));
        installation.saveInBackground();
    }

    public static NotifiableActivity getNotifiableActivity() {
        return activity.get();
    }

    public static void setNotifiableActivity(NotifiableActivity notifiableActivity) {
        activity.set(notifiableActivity);
    }
}
