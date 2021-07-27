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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Fragment
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.preference.PreferenceManager
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class StartFragment : Fragment(), View.OnClickListener {

    private lateinit var serverField: EditText
    private lateinit var startButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)
        serverField = view.findViewById(R.id.field_server)
        startButton = view.findViewById(R.id.button_start)
        startButton.setOnClickListener(this)
        return view
    }

    @SuppressLint("StaticFieldLeak")
    override fun onClick(view: View) {
        startButton.isEnabled = false
        object : AsyncTask<String, Unit, Boolean>() {
            override fun doInBackground(vararg urls: String): Boolean {
                try {
                    val uri = Uri.parse(urls[0]).buildUpon().appendEncodedPath("api/server").build()
                    var url = uri.toString()
                    var urlConnection: HttpURLConnection? = null
                    for (i in 0 until MAX_REDIRECTS) {
                        val resourceUrl = URL(url)
                        urlConnection = resourceUrl.openConnection() as HttpURLConnection
                        urlConnection.instanceFollowRedirects = false
                        when (urlConnection.responseCode) {
                            HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP -> {
                                url = urlConnection.getHeaderField("Location")
                                continue
                            }
                        }
                        break
                    }
                    val reader = BufferedReader(InputStreamReader(urlConnection?.inputStream))
                    var line: String?
                    val responseBuilder = StringBuilder()
                    while (reader.readLine().also { line = it } != null) {
                        responseBuilder.append(line)
                    }
                    JSONObject(responseBuilder.toString())
                    return true
                } catch (e: IOException) {
                    Log.w(TAG, e)
                } catch (e: JSONException) {
                    Log.w(TAG, e)
                }
                return false
            }

            override fun onPostExecute(result: Boolean) {
                if (activity != null) {
                    if (result) {
                        onSuccess()
                    } else {
                        onError()
                    }
                }
            }
        }.execute(serverField.text.toString())
    }

    private fun onSuccess() {
        PreferenceManager.getDefaultSharedPreferences(activity)
            .edit().putString(MainActivity.PREFERENCE_URL, serverField.text.toString()).apply()
        activity.fragmentManager
            .beginTransaction().replace(android.R.id.content, MainFragment())
            .commitAllowingStateLoss()
    }

    private fun onError() {
        startButton.isEnabled = true
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setMessage(getString(R.string.error_connection))
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(android.R.string.ok)) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    companion object {
        private val TAG = StartFragment::class.java.simpleName
        private const val MAX_REDIRECTS = 5
    }
}
