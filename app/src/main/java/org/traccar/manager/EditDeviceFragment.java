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

import android.app.Activity;
import android.content.Context;
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

    public static final String NAME_PARAM = "NameParam";
    public static final String IDENTIFIER_PARAM = "IdentifierParam";

    public interface Listener {
        void onDeviceStored(int resultCode);
    };

    public static final String EXTRA_DEVICE_ID = "deviceId";

    private Context context;
    private MainApplication application;

    private Device device;
    private EditText nameEditText;
    private EditText identifierEditText;
    private View saveButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.application = (MainApplication) ((Activity)context).getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_device, container, false);

        nameEditText = (EditText) view.findViewById(R.id.name);
        identifierEditText = (EditText) view.findViewById(R.id.identifier);
        saveButton = (Button) view.findViewById(R.id.button_save);

        final String name;
        final String identifier;
        if(savedInstanceState == null) {
            name = null;
            identifier = null;
        } else {
            name = savedInstanceState.getString(NAME_PARAM);
            identifier = savedInstanceState.getString(IDENTIFIER_PARAM);
        }

        final long deviceId = ((EditDeviceActivity)context).getIntent().getExtras().getLong(EXTRA_DEVICE_ID);
        if (deviceId != 0) {
            final WebService service = application.getService();
            service.getDevices().enqueue(new WebServiceCallback<List<Device>>(context) {
                @Override
                public void onSuccess(Response<List<Device>> response) {
                    for (Device device: response.body()) {
                        if (device.getId() == deviceId) {
                            EditDeviceFragment.this.device = device;
                            nameEditText.setText(name == null ? device.getName() : name);
                            identifierEditText.setText(identifier == null ? device.getUniqueId() : identifier);
                        }
                    }
                }
            });
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final WebService service = application.getService();

                if (device == null) {
                    device = new Device();
                    device.setName(nameEditText.getText().toString());
                    device.setUniqueId(identifierEditText.getText().toString());
                    service.addDevice(device).enqueue(new WebServiceCallback<Device>(context) {
                        @Override
                        public void onSuccess(Response<Device> response) {
                            Toast.makeText(context, R.string.device_created, Toast.LENGTH_LONG).show();
                            ((EditDeviceActivity)context).onDeviceStored(0);
                        }
                    });
                } else {
                    device.setName(nameEditText.getText().toString());
                    device.setUniqueId(identifierEditText.getText().toString());
                    service.updateDevice(device.getId(), device).enqueue(new WebServiceCallback<Device>(context) {
                        @Override
                        public void onSuccess(Response<Device> response) {
                            Toast.makeText(context, R.string.device_updated, Toast.LENGTH_LONG).show();
                            ((EditDeviceActivity)context).onDeviceStored(0);
                        }
                    });
                }
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NAME_PARAM, nameEditText.getText().toString());
        outState.putString(IDENTIFIER_PARAM, identifierEditText.getText().toString());
    }
}
