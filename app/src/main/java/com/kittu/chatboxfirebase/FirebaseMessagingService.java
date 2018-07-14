package com.kittu.chatboxfirebase;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title=remoteMessage.getNotification().getTitle();
        String body=remoteMessage.getNotification().getBody();
String click_action=remoteMessage.getNotification().getClickAction();
String from_user_id=remoteMessage.getData().get("from_user_id");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resIntent=new Intent(click_action);
        resIntent.putExtra("user_key",from_user_id);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(getApplicationContext(),
                        0,
                        resIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(replyPendingIntent);

        int notificationId=(int) System.currentTimeMillis();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());


    }
}
