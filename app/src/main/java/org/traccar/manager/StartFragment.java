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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StartFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = StartFragment.class.getSimpleName();

    private EditText serverField;
    private Button startButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        serverField = (EditText) view.findViewById(R.id.field_server);
        startButton = (Button) view.findViewById(R.id.button_start);
        startButton.setOnClickListener(this);
        return view;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onClick(View view) {
        startButton.setEnabled(false);

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... urls) {
                try {

                    Uri uri = Uri.parse(urls[0]).buildUpon().appendEncodedPath("api/server").build();
                    URL url = new URL(uri.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;
                    StringBuilder responseBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }

                    new JSONObject(responseBuilder.toString());

                    return true;

                } catch (IOException | JSONException e) {
                    Log.w(TAG, e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (getActivity() != null) {
                    if (result) {
                        onSuccess();
                    } else {
                        onError();
                    }
                }
            }

        }.execute(serverField.getText().toString());
    }

    private void onSuccess() {
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit().putString(MainActivity.PREFERENCE_URL, serverField.getText().toString()).apply();
        getActivity().getFragmentManager()
                .beginTransaction().replace(android.R.id.content, new MainFragment()).commitAllowingStateLoss();
    }

    private void onError() {
        startButton.setEnabled(true);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(getString(R.string.error_connection));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
