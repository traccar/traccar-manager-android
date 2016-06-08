/*
 * Copyright 2015 - 2016 Anton Tananaev (anton.tananaev@gmail.com)
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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.traccar.manager.model.Device;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DevicesFragment extends ListFragment implements View.OnClickListener {

    public static final String EXTRA_DEVICE_ID = "deviceId";

    class PopupAdapter extends ArrayAdapter<Device> {

        PopupAdapter(List<Device> items) {
            super(getActivity(), R.layout.list_item, android.R.id.text1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            View view = super.getView(position, convertView, container);
            View popupText = view.findViewById(android.R.id.text1);
            popupText.setTag(getItem(position));
            popupText.setOnClickListener(DevicesFragment.this);
            return view;
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        FrameLayout root = (FrameLayout)view;
        final Context context = getActivity();
        FloatingActionButton addButton = new FloatingActionButton(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        float scaleRatio = getResources().getDisplayMetrics().density;
        float marginInPixel = getResources().getDimension(R.dimen.fab_margin);
        int margin = (int)(marginInPixel / scaleRatio);
        params.setMargins(margin, margin, margin, margin);
        addButton.setLayoutParams(params);
        addButton.setImageResource(android.R.drawable.ic_input_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditDeviceActivity(0);
            }
        });
        root.addView(addButton);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshDevices();
    }

    public void refreshDevices() {
        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service) {
                service.getDevices().enqueue(new WebServiceCallback<List<Device>>(getContext()) {
                    @Override
                    public void onSuccess(Response<List<Device>> response) {
                        setListAdapter(new PopupAdapter(response.body()));
                    }
                });
            }
        });
    }

    @Override
    public void onClick(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                showPopupMenu(view);
            }
        });
    }

    private void showPopupMenu(View view) {
        final PopupAdapter adapter = (PopupAdapter) getListAdapter();
        final Device device = (Device) view.getTag();
        PopupMenu popup = new PopupMenu(getActivity(), view);

        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_edit_device:
                        startEditDeviceActivity(device.getId());
                        return true;
                    case R.id.action_remove_device:
                        removeDevice(device.getId());
                        return true;
                    case R.id.action_show_on_map:
                        finishDevicesActivity(device.getId());
                        return true;
                    case R.id.action_send_command:
                        startSendCommandActivity(device.getId());
                        return true;
                }
                return false;
            }
        });

        popup.show();
    }

    private void finishDevicesActivity(long deviceId) {
        Activity activity = getActivity();
        activity.setResult(MainFragment.RESULT_SUCCESS, new Intent().putExtra(EXTRA_DEVICE_ID, deviceId));
        activity.finish();
    }

    private void startEditDeviceActivity(long deviceId) {
        startActivityForResult(new Intent(getContext(), EditDeviceActivity.class).putExtra(EXTRA_DEVICE_ID, deviceId), 0);
    }

    private void removeDevice(final long deviceId) {
        ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();
        confirmationDialogFragment.setPositiveListener(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final MainApplication application = (MainApplication) getActivity().getApplication();
                final WebService service = application.getService();
                service.removeDevice(deviceId).enqueue(new WebServiceCallback<Void>(getContext()) {
                    @Override
                    public void onSuccess(Response<Void> response) {
                        Toast.makeText(getContext(), R.string.device_removed, Toast.LENGTH_LONG).show();
                        refreshDevices();
                    }
                });
                dialog.dismiss();
            }
        });
        confirmationDialogFragment.setNegativeListener(new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        confirmationDialogFragment.show(getFragmentManager(), "Confirmation");
    }

    private void startSendCommandActivity(long deviceId) {
        startActivity(new Intent(getContext(), SendCommandActivity.class).putExtra(EXTRA_DEVICE_ID, deviceId));
    }
}
