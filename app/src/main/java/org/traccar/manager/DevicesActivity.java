/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

public class DevicesActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener, DevicesFragment.Listener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_layout, new DevicesFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        DevicesFragment devicesFragment = (DevicesFragment)getSupportFragmentManager().findFragmentById(R.id.content_layout);
        devicesFragment.refreshDevices();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case android.R.id.text1:
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        DevicesFragment devicesFragment = (DevicesFragment)getSupportFragmentManager().findFragmentById(R.id.content_layout);
                        devicesFragment.showPopupMenu(view);
                    }
                });
                break;

            case android.R.id.button1:
                onEditDevice(0);
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        switch (id) {
            case DialogInterface.BUTTON_POSITIVE:
                DevicesFragment devicesFragment = (DevicesFragment)getSupportFragmentManager().findFragmentById(R.id.content_layout);
                long deviceId = (Long) ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).getTag();
                devicesFragment.removeDevice(deviceId);
                break;
        }
        dialog.dismiss();
    }

    @Override
    public void onEditDevice(long deviceId) {
        startActivityForResult(new Intent(this, EditDeviceActivity.class).putExtra(DevicesFragment.EXTRA_DEVICE_ID, deviceId), 0);
    }

    @Override
    public void onShowOnMap(long deviceId) {
        setResult(MainFragment.RESULT_SUCCESS, new Intent().putExtra(DevicesFragment.EXTRA_DEVICE_ID, deviceId));
        finish();
    }

    @Override
    public void onSendCommand(long deviceId) {
        startActivity(new Intent(this, SendCommandActivity.class).putExtra(DevicesFragment.EXTRA_DEVICE_ID, deviceId));
    }
}
