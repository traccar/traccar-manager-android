/*
 * Copyright 2017 Anton Tananaev (anton.tananaev@gmail.com)
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

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class GoogleMainApplication extends Application {

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(MainFragment.EVENT_LOGIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.notification_channel_id), getString(R.string.notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    broadcastToken(instanceIdResult.getToken());
                }
            });
        }
    };

    public void broadcastToken(String token) {
        Intent intent = new Intent(MainFragment.EVENT_TOKEN);
        intent.putExtra(MainFragment.KEY_TOKEN, token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
