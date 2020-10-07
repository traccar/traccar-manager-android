/*
 * Copyright 2016 - 2020 Anton Tananaev (anton@traccar.org)
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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewFragment;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainFragment extends WebViewFragment {

    public final static String EVENT_LOGIN = "eventLogin";
    public final static String EVENT_TOKEN = "eventToken";
    public final static String KEY_TOKEN = "keyToken";

    private final static int REQUEST_FILE_CHOOSER = 1;

    private LocalBroadcastManager broadcastManager;

    public class AppInterface {

        @JavascriptInterface
        public void postMessage(String message) {
            if (message.contains("login")) {
                broadcastManager.sendBroadcast(new Intent(EVENT_LOGIN));
            }
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if ((getActivity().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        getWebView().setWebChromeClient(webChromeClient);
        getWebView().addJavascriptInterface(new AppInterface(), "appInterface");

        WebSettings webSettings = getWebView().getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        String url = PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getString(MainActivity.PREFERENCE_URL, null);

        getWebView().loadUrl(url);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra(KEY_TOKEN);
            String code = "updateNotificationToken && updateNotificationToken('" + token + "')";
            getWebView().evaluateJavascript(code, null);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(EVENT_TOKEN);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    private ValueCallback<Uri> openFileCallback;
    private ValueCallback<Uri[]> openFileCallback2;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE_CHOOSER) {
            Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
            if (openFileCallback != null) {
                openFileCallback.onReceiveValue(result);
                openFileCallback = null;
            }
            if (openFileCallback2 != null) {
                openFileCallback2.onReceiveValue(result != null ? new Uri[] { result } : new Uri[0]);
                openFileCallback2 = null;
            }
        }
    }

    private WebChromeClient webChromeClient = new WebChromeClient() {

        // Android 3.0+
        public void openFileChooser(ValueCallback uploadMessage, String acceptType) {
            openFileChooser(uploadMessage);
        }

        // Android 4.1+
        protected void openFileChooser(ValueCallback<Uri> uploadMessage, String acceptType, String capture) {
            openFileChooser(uploadMessage);
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMessage) {
            MainFragment.this.openFileCallback = uploadMessage;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.file_browser)), REQUEST_FILE_CHOOSER);
        }

        // Android 5.0+
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (openFileCallback2 != null) {
                openFileCallback2.onReceiveValue(null);
                openFileCallback2 = null;
            }

            openFileCallback2 = filePathCallback;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_FILE_CHOOSER);
                } catch (ActivityNotFoundException e) {
                    openFileCallback2 = null;
                    return false;
                }
            }
            return true;
        }

    };

}
