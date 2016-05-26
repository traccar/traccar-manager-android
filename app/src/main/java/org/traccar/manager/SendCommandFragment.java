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

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.traccar.manager.model.Command;
import org.traccar.manager.model.CommandType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class SendCommandFragment extends Fragment {

    class CommandTypeDataHolder {
        private String type;
        private String name;

        public CommandTypeDataHolder(String type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class CommandTypeAdapter extends ArrayAdapter<CommandTypeDataHolder> {

        CommandTypeAdapter(List<CommandTypeDataHolder> items) {
            super(getActivity(), R.layout.list_item, android.R.id.text1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            View view = super.getView(position, convertView, container);

            CommandTypeDataHolder commandTypeDataHolder = getItem(position);
            TextView popupText = (TextView) view.findViewById(android.R.id.text1);
            popupText.setText(commandTypeDataHolder.name);
            view.setTag(commandTypeDataHolder.type);

            return view;
        }
    }

    private static Map<String, Integer> i10nMapping = new HashMap<>();

    static {
        i10nMapping.put(Command.TYPE_ALARM_ARM, R.string.command_alarm_arm);
        i10nMapping.put(Command.TYPE_ALARM_DISARM, R.string.command_alarm_disarm);
        i10nMapping.put(Command.TYPE_ENGINE_STOP, R.string.command_engine_stop);
        i10nMapping.put(Command.TYPE_ENGINE_RESUME, R.string.command_engine_resume);
        i10nMapping.put(Command.TYPE_POSITION_PERIODIC, R.string.command_position_periodic);
    }

    public static final String EXTRA_DEVICE_ID = "deviceId";

    private Spinner commandsSpinner;
    private LinearLayout frequencyGroup;
    private EditText frequencyEditText;
    private LinearLayout unitGroup;
    private Spinner unitSpinner;
    private View sendButton;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_command, container, false);

        commandsSpinner = (Spinner) view.findViewById(R.id.commands);
        frequencyGroup = (LinearLayout) view.findViewById(R.id.frequencyGroup);
        frequencyEditText = (EditText) view.findViewById(R.id.frequency);
        unitGroup = (LinearLayout) view.findViewById(R.id.unitGroup);
        unitSpinner = (Spinner) view.findViewById(R.id.unit);
        sendButton = (Button) view.findViewById(R.id.button_send);

        frequencyGroup.setVisibility(View.GONE);
        unitGroup.setVisibility(View.GONE);

        commandsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(view.getTag().toString().equals(Command.TYPE_POSITION_PERIODIC)) {
                    frequencyGroup.setVisibility(View.VISIBLE);
                    unitGroup.setVisibility(View.VISIBLE);
                } else {
                    frequencyGroup.setVisibility(View.GONE);
                    unitGroup.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                frequencyGroup.setVisibility(View.GONE);
                unitGroup.setVisibility(View.GONE);
            }
        });

        final long deviceId = getActivity().getIntent().getExtras().getLong(EXTRA_DEVICE_ID);
        final MainApplication application = (MainApplication) getActivity().getApplication();
        final WebService service = application.getService();
        service.getCommandTypes(deviceId).enqueue(new WebServiceCallback<List<CommandType>>(getContext()) {
            @Override
            public void onSuccess(Response<List<CommandType>> response) {
                List<CommandTypeDataHolder> commandTypeDataHolders = new ArrayList<>();

                for (CommandType commandType: response.body()) {
                    String name = getI10nString(commandType.getType());
                    commandTypeDataHolders.add(new CommandTypeDataHolder(commandType.getType(), name));
                }

                commandsSpinner.setAdapter(new CommandTypeAdapter(commandTypeDataHolders));
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Command command = new Command();
                command.setDeviceId(deviceId);
                command.setType((String) commandsSpinner.getSelectedView().getTag());

                if (command.getType().equals(Command.TYPE_POSITION_PERIODIC)) {
                    int value = 0;
                    try {
                        value = Integer.parseInt(frequencyEditText.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), R.string.error_invalid_frequency, Toast.LENGTH_LONG).show();
                        return;
                    }
                    int multiplier = getResources().getIntArray(R.array.unit_values)[unitSpinner.getSelectedItemPosition()];
                    command.set(Command.KEY_FREQUENCY, value * multiplier);
                }

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

    private String getI10nString(String key) {
        String result = key;

        Integer resId = i10nMapping.get(key);
        if(resId != null) {
            try {
                CharSequence nameCharSequence = getContext().getResources().getText(resId);
                result = nameCharSequence.toString();
            } catch (Resources.NotFoundException e) {
                Log.w(SendCommandFragment.class.getSimpleName(), e);
            }
        }

        return result;
    }
}
