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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_DEVICE = 1;
    public static final int RESULT_SUCCESS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_layout, new MainFragment())
                    .commit();

            if (!((MainApplication) getApplication()).isConfigured()) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_devices:
                startActivityForResult(new Intent(this, DevicesActivity.class), REQUEST_DEVICE);
                return true;
            case R.id.action_logout:
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit().putBoolean(MainApplication.PREFERENCE_AUTHENTICATED, false).apply();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DEVICE && resultCode == RESULT_SUCCESS) {
            long deviceId = data.getLongExtra(DevicesFragment.EXTRA_DEVICE_ID, 0);
            Toast.makeText(this, "device selected: " + deviceId, Toast.LENGTH_LONG).show();
            // TODO select device
        }
    }

}
