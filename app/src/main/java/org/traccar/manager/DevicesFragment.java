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

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.traccar.manager.model.Device;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class DevicesFragment extends ListFragment {

    public static final String EXTRA_DEVICE_ID = "deviceId";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(WebService service) {
                service.getDevices().enqueue(new Callback<List<Device>>() {
                    @Override
                    public void onResponse(Response<List<Device>> response, Retrofit retrofit) {
                        setListAdapter(new ArrayAdapter<>(application, R.layout.list_item, android.R.id.text1, response.body()));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(application, R.string.error_general, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Activity activity = getActivity();
        if (activity != null) {
            Device device = (Device) getListAdapter().getItem(position);
            activity.setResult(MainActivity.RESULT_SUCCESS, new Intent().putExtra(EXTRA_DEVICE_ID, device.getId()));
            activity.finish();
        }
    }

}
