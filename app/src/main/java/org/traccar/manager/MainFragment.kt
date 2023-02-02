/*
 * Copyright 2016 - 2021 Anton Tananaev (anton@traccar.org)
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
@file:Suppress("DEPRECATION")
package org.traccar.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager

class MainFragment : WebViewFragment() {

    private lateinit var broadcastManager: LocalBroadcastManager

    inner class AppInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            if (message.startsWith("login")) {
                if (message.length > 6) {
                    SecurityManager.saveToken(activity, message.substring(6))
                }
                broadcastManager.sendBroadcast(Intent(EVENT_LOGIN))
            } else if (message.startsWith("authentication")) {
                SecurityManager.readToken(activity) { token ->
                    if (token != null) {
                        val code = "handleLoginToken && handleLoginToken('$token')"
                        webView.evaluateJavascript(code, null)
                    }
                }
            } else if (message.startsWith("logout")) {
                SecurityManager.deleteToken(activity)
            } else if (message.startsWith("server")) {
                val url = message.substring(7)
                PreferenceManager.getDefaultSharedPreferences(activity)
                    .edit().putString(MainActivity.PREFERENCE_URL, url).apply()
                activity.runOnUiThread { loadPage() }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        broadcastManager = LocalBroadcastManager.getInstance(activity)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if ((activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.webViewClient = webViewClient
        webView.webChromeClient = webChromeClient
        webView.setDownloadListener(downloadListener)
        webView.addJavascriptInterface(AppInterface(), "appInterface")
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.setSupportMultipleWindows(true)
        loadPage()
    }

    private fun loadPage() {
        val url = PreferenceManager.getDefaultSharedPreferences(activity)
            .getString(MainActivity.PREFERENCE_URL, null)
        if (url != null) {
            val mainActivity = activity as? MainActivity
            val eventId = mainActivity?.pendingEventId
            mainActivity?.pendingEventId = null
            if (eventId != null) {
                webView.loadUrl("$url?eventId=$eventId")
            } else {
                webView.loadUrl(url)
            }
        } else {
            activity.fragmentManager
                .beginTransaction().replace(android.R.id.content, StartFragment())
                .commitAllowingStateLoss()
        }
    }

    private val tokenBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val token = intent.getStringExtra(KEY_TOKEN)
            val code = "updateNotificationToken && updateNotificationToken('$token')"
            webView.evaluateJavascript(code, null)
        }
    }

    private val eventIdBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            loadPage()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_PERMISSIONS_NOTIFICATION
                )
            }
        }
        broadcastManager.registerReceiver(tokenBroadcastReceiver, IntentFilter(EVENT_TOKEN))
        broadcastManager.registerReceiver(eventIdBroadcastReceiver, IntentFilter(EVENT_EVENT))
    }

    override fun onStop() {
        super.onStop()
        broadcastManager.unregisterReceiver(tokenBroadcastReceiver)
        broadcastManager.unregisterReceiver(eventIdBroadcastReceiver)
    }

    private var openFileCallback: ValueCallback<Uri?>? = null
    private var openFileCallback2: ValueCallback<Array<Uri>>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FILE_CHOOSER) {
            val result = if (resultCode != Activity.RESULT_OK) null else data?.data
            if (openFileCallback != null) {
                openFileCallback?.onReceiveValue(result)
                openFileCallback = null
            }
            if (openFileCallback2 != null) {
                openFileCallback2?.onReceiveValue(if (result != null) arrayOf(result) else arrayOf())
                openFileCallback2 = null
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_LOCATION) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (geolocationCallback != null) {
                geolocationCallback?.invoke(geolocationRequestOrigin, granted, false)
                geolocationRequestOrigin = null
                geolocationCallback = null
            }
        }
    }

    private var geolocationRequestOrigin: String? = null
    private var geolocationCallback: GeolocationPermissions.Callback? = null

    private val webViewClient = object : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().flush()
            }
        }
    }

    private val webChromeClient = object : WebChromeClient() {

        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
            val data = view.hitTestResult.extra
            return if (data != null) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
                view.context.startActivity(browserIntent)
                true
            } else {
                false
            }
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            geolocationRequestOrigin = null
            geolocationCallback = null
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(activity)
                        .setMessage(R.string.permission_location_rationale)
                        .setNeutralButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            geolocationRequestOrigin = origin
                            geolocationCallback = callback
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSIONS_LOCATION
                            )
                        }
                        .show()
                } else {
                    geolocationRequestOrigin = origin
                    geolocationCallback = callback
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_LOCATION
                    )
                }
            } else {
                callback.invoke(origin, true, false)
            }
        }

        // Android 4.1+
        @Suppress("UNUSED_PARAMETER")
        fun openFileChooser(uploadMessage: ValueCallback<Uri?>?, acceptType: String?, capture: String?) {
            openFileCallback = uploadMessage
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(
                Intent.createChooser(intent, getString(R.string.file_browser)),
                REQUEST_FILE_CHOOSER
            )
        }

        // Android 5.0+
        override fun onShowFileChooser(
            mWebView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            openFileCallback2?.onReceiveValue(null)
            openFileCallback2 = filePathCallback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = fileChooserParams.createIntent()
                try {
                    startActivityForResult(intent, REQUEST_FILE_CHOOSER)
                } catch (e: ActivityNotFoundException) {
                    openFileCallback2 = null
                    return false
                }
            }
            return true
        }
    }

    private val downloadListener = DownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
        val request = DownloadManager.Request(Uri.parse(url))
        request.setMimeType(mimeType)
        request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            URLUtil.guessFileName(url, contentDisposition, mimeType),
        )
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    companion object {
        const val EVENT_LOGIN = "eventLogin"
        const val EVENT_TOKEN = "eventToken"
        const val EVENT_EVENT = "eventEvent"
        const val KEY_TOKEN = "keyToken"
        private const val REQUEST_PERMISSIONS_LOCATION = 1
        private const val REQUEST_PERMISSIONS_NOTIFICATION = 2
        private const val REQUEST_FILE_CHOOSER = 1
    }
}
