package org.ministryofhealth.newimci.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.ministryofhealth.newimci.NotificationActivity;
import org.ministryofhealth.newimci.config.Constants;
import org.ministryofhealth.newimci.database.DatabaseHandler;
import org.ministryofhealth.newimci.model.Notification;
import org.ministryofhealth.newimci.util.NotificationUtils;

import java.util.HashMap;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private NotificationUtils notificationUtils;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Remote message: we have something");
        if(remoteMessage == null)
            return;

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if(remoteMessage.getNotification() != null){
            Log.d(TAG, "Payload number: " + remoteMessage.getData().size());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                Map<String, String> params = remoteMessage.getData();
                JSONObject data = new JSONObject(params);
                handleDataMessage(data.getString("title"), data.getString("body"), data);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }else{
            Log.d(TAG, "The payload was: " + remoteMessage.getData().size());
        }
    }

    private void handleNotification(String message){
        Log.d(TAG, "Handle Notification called");
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            Intent pushNotification = new Intent(Constants.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{

        }
    }

    private void handleDataMessage(String title, String message, JSONObject json){
        Log.d(TAG, "Handle Daa Notification called");

        try {
//            JSONObject payload = new JSONObject(payloadParams);

            Notification notification = new Notification();

            notification.setTitle(title);
            notification.setMessage(message);
            notification.setStatus(0);
            notification.setCreated_at(json.getString("created_at"));
            notification.setId(json.getInt("id"));

            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            db.addNotification(notification);

            Log.e(TAG, "title: " + title);
            Log.e(TAG, "message: " + message);
//            Log.e(TAG, "payload: " + payload.toString());


//            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Constants.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
                if (json.has("image")) {
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, json.getString("created_at"), pushNotification, json.getString("image"));
                }else{
                    showNotificationMessage(getApplicationContext(), title, message,json.getString("created_at"), pushNotification);
                }
//            } else {
                // app is in background, show the notification in notification tray
//                Intent resultIntent = new Intent(getApplicationContext(), NotificationActivity.class);
//                resultIntent.putExtra("message", message);
//                if (json.getString("image") != null) {
//                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, json.getString("created_at"), resultIntent, json.getString("image"));
//                }else{
//                    showNotificationMessage(getApplicationContext(), title, message,json.getString("created_at"), resultIntent);
//                }
//            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
