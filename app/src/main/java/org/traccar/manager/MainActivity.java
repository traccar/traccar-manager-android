/*
 * Copyright 2016 Anton Tananaev (anton@traccar.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.traccar.manager;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {

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
        String url = PreferenceManager.getDefaultSharedPreferences(this).getString(PREFERENCE_URL, null);
        if (url != null) {
            getFragmentManager().beginTransaction().add(android.R.id.content, new MainFragment()).commit();
        } else {
            getFragmentManager().beginTransaction().add(android.R.id.content, new StartFragment()).commit();
        }
    }

}
