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
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.traccar.manager.model.Command;
import org.traccar.manager.model.CommandType;
import retrofit2.Call;
import retrofit2.Response;

public class SendSavedCommandsFragment extends DialogFragment {

    static class CommandTypeDataHolder {
        private String type;
        private String name;
        private long id;
        private String description;
        private String frequency;

        public CommandTypeDataHolder(String type, String name, String description, String frequency, long id) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.frequency = frequency;
            this.id = id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    List<CommandType> comandosSalvos;
    List<SendSavedCommandsFragment.CommandTypeDataHolder> commandTypeDataHolders;
    class CommandTypeAdapter extends ArrayAdapter<SendSavedCommandsFragment.CommandTypeDataHolder> {

        CommandTypeAdapter(List<SendSavedCommandsFragment.CommandTypeDataHolder> items) {
            super(getActivity(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, android.R.id.text1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            View view = super.getView(position, convertView, container);

            SendSavedCommandsFragment.CommandTypeDataHolder commandTypeDataHolder = getItem(position);
            TextView popupText = view.findViewById(android.R.id.text1);
            popupText.setText(commandTypeDataHolder.name);
            view.setTag(commandTypeDataHolder.type);

            return view;
        }
    }

    private Spinner commandsSpinner;
    private View sendButton;


    private static Map<String, Integer> i10nMapping = new HashMap<>();

    public static final String EXTRA_DEVICE_ID = "deviceId";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_saved_command, container, false);
        // Inflate the layout for this fragment
        commandsSpinner = view.findViewById(R.id.commands);
        sendButton = view.findViewById(R.id.button_send);

        final long deviceId = getArguments().getLong(EXTRA_DEVICE_ID);
        final MainApplication application = (MainApplication) getActivity().getApplication();
        final WebService service = application.getService();

        service.getSavedCommands(deviceId).enqueue(new WebServiceCallback<List<CommandType>>(getActivity()) {
            @Override
            public void onSuccess(Response<List<CommandType>> response) {
                comandosSalvos = new ArrayList<>();
                List<SendSavedCommandsFragment.CommandTypeDataHolder> commandTypeDataHolders = new ArrayList<>();
                for (CommandType commandType: response.body()) {
                    String name = getI10nString(commandType.getDescription());
                    commandTypeDataHolders.add(new SendSavedCommandsFragment.CommandTypeDataHolder(commandType.getType(), name, commandType.getDescription(), commandType.getAttributes().get("frequency").toString(),commandType.getId()));
                    comandosSalvos.add(commandType);
                }
                commandsSpinner.setAdapter(new SendSavedCommandsFragment.CommandTypeAdapter(commandTypeDataHolders));
            }

            @Override
            public void onFailure(Call<List<CommandType>> call, Throwable t) {
                super.onFailure(call, t);
            }
        });

        commandsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String spinValue = (String) commandsSpinner.getSelectedView().getTag();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Command command = new Command();
                command.setDeviceId(deviceId);
                command.setAttributes(comandosSalvos.get(commandsSpinner.getSelectedItemPosition()).getAttributes());
                command.setType(comandosSalvos.get(commandsSpinner.getSelectedItemPosition()).getType());
                command.setDescription(comandosSalvos.get(commandsSpinner.getSelectedItemPosition()).getDescription());
                command.setId(comandosSalvos.get(commandsSpinner.getSelectedItemPosition()).getId());
                final MainApplication application = (MainApplication) getActivity().getApplication();
                final WebService service = application.getService();
                service.sendCommand(command).enqueue(new WebServiceCallback<Command>(getActivity()) {
                    @Override
                    public void onSuccess(Response<Command> response) {
                        Toast.makeText(getActivity(), R.string.command_sent, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<Command> call, Throwable t) {
                        super.onFailure(call, t);
                        Toast.makeText(getActivity(), R.string.command_not_sent, Toast.LENGTH_LONG).show();
                    }


                });
            }
        });

        return view;
    }

    private String getI10nString(String key) {
        String result = key;

        Integer resId = i10nMapping.get(key);
        if(resId != null) {
            try {
                CharSequence nameCharSequence = getActivity().getResources().getText(resId);
                result = nameCharSequence.toString();
            } catch (Resources.NotFoundException e) {
                Log.w(SendSavedCommandsFragment.class.getSimpleName(), e);
            }
        }

        return result;
    }


}
