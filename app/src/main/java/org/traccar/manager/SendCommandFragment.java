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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.traccar.manager.model.Command;
import org.traccar.manager.model.CommandType;
import org.traccar.manager.model.Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class SendCommandFragment extends Fragment {

    class CommandTypeAdapter extends ArrayAdapter<CommandType> {

        CommandTypeAdapter(List<CommandType> items) {
            super(getActivity(), R.layout.list_item, android.R.id.text1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            View view = super.getView(position, convertView, container);

            CommandType commandType = getItem(position);
            CharSequence name = commandType.getType();
            Integer resId = i10nMapping.get(commandType.getType());
            if(resId != null) {
                name = getContext().getResources().getText(resId);
            }

            TextView popupText = (TextView) view.findViewById(android.R.id.text1);
            popupText.setText(name);
            popupText.setTag(commandType);

            return view;
        }
    }

    private static Map<String, Integer> i10nMapping = new HashMap<>();

    static {
        i10nMapping.put(Command.TYPE_ALARM_ARM, R.string.command_alarm_arm);
        i10nMapping.put(Command.TYPE_ALARM_DISARM, R.string.command_alarm_disarm);
        i10nMapping.put(Command.TYPE_ENGINE_STOP, R.string.command_engine_stop);
        i10nMapping.put(Command.TYPE_ENGINE_RESUME, R.string.command_engine_resume);
    }

    public static final String EXTRA_DEVICE_ID = "deviceId";

    private Spinner commandsSpinner;
    private View sendButton;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_command, container, false);

        commandsSpinner = (Spinner) view.findViewById(R.id.commands);
        sendButton = (Button) view.findViewById(R.id.button_send);

        final long deviceId = getActivity().getIntent().getExtras().getLong(EXTRA_DEVICE_ID);
        final MainApplication application = (MainApplication) getActivity().getApplication();
        final WebService service = application.getService();
        service.getCommandTypes(deviceId).enqueue(new WebServiceCallback<List<CommandType>>(getContext()) {
            @Override
            public void onSuccess(Response<List<CommandType>> response) {
                commandsSpinner.setAdapter(new CommandTypeAdapter(response.body()));
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Command command = new Command();
                command.setDeviceId(deviceId);
                command.setType((String) commandsSpinner.getSelectedItem());

                final MainApplication application = (MainApplication) getActivity().getApplication();
                final WebService service = application.getService();
                service.sendCommand(command).enqueue(new WebServiceCallback<Command>(getContext()) {
                    @Override
                    public void onSuccess(Response<Command> response) {
                        Toast.makeText(getContext(), R.string.command_sent, Toast.LENGTH_LONG).show();
                    }
                });

                getActivity().finish();
            }
        });

        return view;
    }

}
