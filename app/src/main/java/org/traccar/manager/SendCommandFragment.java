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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.traccar.manager.model.CommandType;

import java.util.ArrayList;
import java.util.List;

public class SendCommandFragment extends Fragment {

    public static final String EXTRA_DEVICE_ID = "deviceId";

    private Spinner commandsSpinner;
    private View sendButton;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_command, container, false);

        commandsSpinner = (Spinner) view.findViewById(R.id.commands);
        sendButton = (Button) view.findViewById(R.id.button_send);

        long deviceId = getActivity().getIntent().getExtras().getLong(EXTRA_DEVICE_ID);
        final MainApplication application = (MainApplication) getActivity().getApplication();
        final WebService service = application.getService();
        service.getCommandTypes(deviceId).enqueue(new WebServiceCallback<List<CommandType>>(getContext()) {
            @Override
            public void onSuccess(retrofit2.Response<List<CommandType>> response) {
                List<String> keys = new ArrayList<>();
                for (CommandType commandType: response.body()) {
                    keys.add(commandType.getType());
                }
                commandsSpinner.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.list_item, android.R.id.text1, keys));
                //commandsSpinner.setAdapter(new ArrayAdapter<CommandType>(getContext(), R.layout.list_item, android.R.id.text1, response.body()));
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return view;
    }

}
