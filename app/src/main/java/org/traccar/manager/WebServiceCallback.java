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

import android.content.Context;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class WebServiceCallback<T> implements Callback<T> {

    private Context context;

    public WebServiceCallback(Context context) {
        this.context = context;
    }

    public abstract void onSuccess(Response<T> response);

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onSuccess(response);
        } else {
            onFailure(call, new ServiceException(response.message()));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String text;
        if (t instanceof ServiceException) {
            text = context.getString(R.string.error_general);
        } else {
            text = context.getString(R.string.error_connection);
        }
        Toast.makeText(context, text + ": " + t.getMessage(), Toast.LENGTH_LONG).show();
    }

}
