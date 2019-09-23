/*
 * Copyright 2016 Anton Tananaev (anton@traccar.org)
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

import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String PREFERENCE_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            initContent();
        }
    }

    private void initContent() {
        if (PreferenceManager.getDefaultSharedPreferences(this).contains(PREFERENCE_URL)) {
            getFragmentManager().beginTransaction().add(android.R.id.content, new MainFragment()).commit();
        } else {
            getFragmentManager().beginTransaction().add(android.R.id.content, new StartFragment()).commit();
        }
    }

}
