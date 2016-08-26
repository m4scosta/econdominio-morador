package br.com.econdominio.notifications;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.econdominio.NotifiableActivity;
import br.com.econdominio.eCondominioApp;

public class NotificationReceiver extends ParsePushBroadcastReceiver {

    private static final String TAG = NotificationReceiver.class.getName();

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        NotifiableActivity activity = eCondominioApp.getNotifiableActivity();
        if (activity == null) {
            super.onPushReceive(context, intent);
        } else {
            JSONObject data = getPushData(intent);
            if (data != null) {
                activity.notify(data);
            } else {
                Log.w(TAG, "Null data received on push notification");
            }
        }
    }

    protected JSONObject getPushData(Intent intent) {
        String pushDataStr = intent.getStringExtra(KEY_PUSH_DATA);
        if (pushDataStr == null) {
            Log.e(TAG, "Can not get push data from intent.");
            return null;
        }
        Log.v(TAG, "Received push data: " + pushDataStr);

        JSONObject pushData = null;
        try {
            pushData = new JSONObject(pushDataStr);
        } catch (JSONException e) {
            Log.e(TAG, "Unexpected JSONException when receiving push data: ", e);
        }
        return pushData;
    }
}
