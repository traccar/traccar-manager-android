/*
 * Copyright 2018 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.manager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ManagerMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);;
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(remoteMessage.hashCode(), builder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        ((GoogleMainApplication) getApplication()).broadcastToken(token);
    }

}
