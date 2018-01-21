package com.recyclegrid.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.recyclegrid.app.ApplicationActivity;
import com.recyclegrid.app.R;
import com.recyclegrid.core.RecycleMaterial;
import com.recyclegrid.database.SQLiteDataContext;

import java.util.HashMap;
import java.util.Map;

import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;

public class AppFirebaseMessagingService extends FirebaseMessagingService {
    private NotificationCompat.Builder _notificationBuilder;
    private NotificationManager _notificationManager;
    private SQLiteDataContext _db;
    private Context _context;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        _context = this;
        _db = new SQLiteDataContext(_context);
        _notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (_notificationManager.getNotificationChannel("recycle-grid") == null) {
                NotificationChannel channel = new NotificationChannel("recycle-grid", "RecycleGrid", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Recycle Grid Notifications");

                _notificationManager.createNotificationChannel(channel);
            }
        }

        Map<String, String> notificationData = remoteMessage.getData();

        Intent intent = new Intent(this, ApplicationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("event", notificationData.get("Event"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100010, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        _notificationBuilder = new NotificationCompat.Builder(this, "recycle-grid")
                .setSound(defaultSoundUri)
                .setSmallIcon(R.mipmap.ic_app_logo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        switch (notificationData.get("Event")){
            case "friend-request-received":
                _notificationBuilder.setContentTitle(getString(R.string.friend_request_notification_title_template, notificationData.get("Username")));
                _notificationBuilder.setContentText(getString(R.string.friend_request_notification_content));
                break;
            case "friend-request-accepted":
                _notificationBuilder.setContentTitle(getString(R.string.friend_request_accepted_notification_title_template, notificationData.get("Username")));
                _notificationBuilder.setContentText(getString(R.string.friend_request_accepted_notification_content));
                break;
            case "friend-checkin":
                String material = "";

                switch (Integer.parseInt(notificationData.get("VenueAcceptedMaterial"))) {
                    case RecycleMaterial.BATTERY:
                        material = getString(R.string.recycle_material_batteries);
                        break;
                    case RecycleMaterial.PLASTICS:
                        material = getString(R.string.recycle_material_plastic);
                        break;
                }

                _notificationBuilder.setContentTitle(getString(R.string.check_in_notification_title, notificationData.get("Username")));
                _notificationBuilder.setContentText(getString(R.string.check_in_notification_content, notificationData.get("Username"), material, notificationData.get("VenueName")));
                LocalBroadcastManager.getInstance(_context).sendBroadcast(new Intent("refresh-friends-event-stream"));
                break;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            _notificationBuilder.setPriority(NotificationManager.IMPORTANCE_DEFAULT);
        }
        else {
            _notificationBuilder.setPriority(PRIORITY_DEFAULT);
        }

        if (notificationData.containsKey("ProfilePictureUrl")) {
            ImageRequest getUserProfilePicture = new ImageRequest(notificationData.get("ProfilePictureUrl"), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    _notificationBuilder.setLargeIcon(response);

                    _notificationManager.notify(18218321, _notificationBuilder.build());
                }
            }, 64, 64, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.ALPHA_8, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            Volley.newRequestQueue(this).add(getUserProfilePicture);
        }
        else {
            _notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_group_add_black_36dp));
            _notificationManager.notify(18218321, _notificationBuilder.build());
        }
    }
}
