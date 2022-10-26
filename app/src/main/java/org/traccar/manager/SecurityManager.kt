/*
 * Copyright 2022 Anton Tananaev (anton@traccar.org)
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
package org.traccar.manager

import android.app.Activity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager

object SecurityManager {

    fun saveToken(activity: Activity, token: String) {
        PreferenceManager.getDefaultSharedPreferences(activity)
            .edit().putString(KEY_TOKEN, token).apply()
    }

    fun readToken(activity: Activity, onResult: (String?) -> Unit) {
        val token = PreferenceManager.getDefaultSharedPreferences(activity)
            .getString(KEY_TOKEN, null)
        if (token != null) {
            authenticate(activity) {
                onResult(if (it) token else null)
            }
        }
        onResult(null)
    }

    fun deleteToken(activity: Activity) {
        PreferenceManager.getDefaultSharedPreferences(activity)
            .edit().remove(KEY_TOKEN).apply()
    }

    private fun authenticate(activity: Activity, onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity as FragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(false)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(true)
                }

                override fun onAuthenticationFailed() {
                    onResult(false)
                }
            },
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_title))
            .setNegativeButtonText(activity.getString(R.string.biometric_cancel))
            .build()
        activity.runOnUiThread { biometricPrompt.authenticate(promptInfo) }
    }

    private const val KEY_TOKEN = "managerToken"

}
