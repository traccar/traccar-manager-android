/*
 * Copyright 2016 Gabor Somogyi (gabor.g.somogyi@gmail.com)
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.traccar.manager.model.CommandType;
import org.traccar.manager.model.Device;
import org.traccar.manager.model.User;

import java.util.List;

import retrofit2.Response;

public class EditDeviceFragment extends Fragment {

    public static final String EXTRA_DEVICE_ID = "deviceId";

    private Device device;
    private EditText nameEditText;
    private EditText identifierEditText;
    private View saveButton;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_device, container, false);

        nameEditText = (EditText) view.findViewById(R.id.name);
        identifierEditText = (EditText) view.findViewById(R.id.identifier);
        saveButton = (Button) view.findViewById(R.id.button_save);

        final long deviceId = getActivity().getIntent().getExtras().getLong(EXTRA_DEVICE_ID);
        if (deviceId != 0) {
            final MainApplication application = (MainApplication) getActivity().getApplication();
            final WebService service = application.getService();
            service.getDevices().enqueue(new WebServiceCallback<List<Device>>(getContext()) {
                @Override
                public void onSuccess(Response<List<Device>> response) {
                    for (Device device: response.body()) {
                        if (device.getId() == deviceId) {
                            EditDeviceFragment.this.device = device;
                            nameEditText.setText(device.getName());
                            identifierEditText.setText(device.getUniqueId());
                        }
                    }
                }
            });
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MainApplication application = (MainApplication) getActivity().getApplication();
                final WebService service = application.getService();

                if (device == null) {
                    device = new Device();
                    device.setName(nameEditText.getText().toString());
                    device.setUniqueId(identifierEditText.getText().toString());
                    service.addDevice(device).enqueue(new WebServiceCallback<Device>(getContext()) {
                        @Override
                        public void onSuccess(Response<Device> response) {
                            Toast.makeText(getContext(), R.string.device_created, Toast.LENGTH_LONG).show();
                            getActivity().setResult(0);
                            getActivity().finish();
                        }
                    });
                } else {
                    device.setName(nameEditText.getText().toString());
                    device.setUniqueId(identifierEditText.getText().toString());
                    service.updateDevice(device.getId(), device).enqueue(new WebServiceCallback<Device>(getContext()) {
                        @Override
                        public void onSuccess(Response<Device> response) {
                            Toast.makeText(getContext(), R.string.device_updated, Toast.LENGTH_LONG).show();
                            getActivity().setResult(0);
                            getActivity().finish();
                        }
                    });
                }
            }
        });

        return view;
    }
}
