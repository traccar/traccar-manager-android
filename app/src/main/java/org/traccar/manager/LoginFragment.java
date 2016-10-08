/*
 * Copyright 2016 Anton Tananaev (anton.tananaev@gmail.com)
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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class LoginFragment extends Fragment {

    private TextView emailInput;
    private TextView passwordInput;
    private View loginButton;

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            loginButton.setEnabled(
                    emailInput.getText().length() > 0 && passwordInput.getText().length() > 0);
        }

    };

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailInput = (TextView) view.findViewById(R.id.input_email);
        passwordInput = (TextView) view.findViewById(R.id.input_password);
        loginButton = view.findViewById(R.id.button_login);

        emailInput.addTextChangedListener(textWatcher);
        passwordInput.addTextChangedListener(textWatcher);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        emailInput.setText(preferences.getString(MainApplication.PREFERENCE_EMAIL, null));

        if (preferences.getBoolean(MainApplication.PREFERENCE_AUTHENTICATED, false)) {
            login();
        }

        view.findViewById(R.id.button_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = inflater.inflate(R.layout.view_settings, null);
                final EditText input = (EditText) dialogView.findViewById(R.id.input_url);

                input.setText(preferences.getString(MainApplication.PREFERENCE_URL, null));

                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.settings_title)
                        .setView(dialogView)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String url = input.getText().toString();
                                if (HttpUrl.parse(url) != null) {
                                    preferences.edit().putString(
                                            MainApplication.PREFERENCE_URL, url).apply();
                                } else {
                                    Toast.makeText(getContext(), R.string.error_invalid_url, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                preferences
                        .edit()
                        .putBoolean(MainApplication.PREFERENCE_AUTHENTICATED, true)
                        .putString(MainApplication.PREFERENCE_EMAIL, emailInput.getText().toString())
                        .putString(MainApplication.PREFERENCE_PASSWORD, passwordInput.getText().toString())
                        .apply();

                login();
            }
        });

        return view;
    }

    private void login() {
        final ProgressDialog progress = new ProgressDialog(getContext());
        progress.setMessage(getString(R.string.app_loading));
        progress.setCancelable(false);
        progress.show();
        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service) {
                if (progress.isShowing()) {
                    progress.dismiss();
                }
                getActivity().finish();
                startActivity(new Intent(getContext(), MainActivity.class));
            }

            @Override
            public boolean onFailure() {
                if (progress.isShowing()) {
                    progress.dismiss();
                }
                return false;
            }
        });
    }

}
