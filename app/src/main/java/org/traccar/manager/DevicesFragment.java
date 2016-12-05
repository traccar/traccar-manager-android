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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;
import org.traccar.manager.model.Device;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DevicesFragment extends ListFragment {

    public interface Listener {
        void onEditDevice(long deviceId);
        void onShowOnMap(long deviceId);
        void onSendCommand(long deviceId);
    }

    public static final String EXTRA_DEVICE_ID = "deviceId";

    private Context context;
    private MainApplication application;

    class PopupAdapter extends ArrayAdapter<Device> {

        PopupAdapter(List<Device> items) {
            super(context, R.layout.list_item, android.R.id.text1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            View view = super.getView(position, convertView, container);
            View popupText = view.findViewById(android.R.id.text1);
            popupText.setTag(getItem(position));
            popupText.setOnClickListener((DevicesActivity)context);
            return view;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.application = (MainApplication) ((Activity)context).getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        FrameLayout root = (FrameLayout)view;
        FloatingActionButton addButton = new FloatingActionButton(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        float scaleRatio = getResources().getDisplayMetrics().density;
        float marginInPixel = getResources().getDimension(R.dimen.fab_margin);
        int margin = (int)(marginInPixel / scaleRatio);
        params.setMargins(margin, margin, margin, margin);
        addButton.setLayoutParams(params);
        addButton.setId(android.R.id.button1);
        addButton.setImageResource(android.R.drawable.ic_input_add);
        addButton.setOnClickListener((DevicesActivity)context);
        root.addView(addButton);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshDevices();
    }

    public void refreshDevices() {
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service) {
                service.getDevices().enqueue(new WebServiceCallback<List<Device>>(context) {
                    @Override
                    public void onSuccess(Response<List<Device>> response) {
                        setListAdapter(new PopupAdapter(response.body()));
                    }
                });
            }

            @Override
            public boolean onFailure() {
                return false;
            }
        });
    }

    public void showPopupMenu(View view) {
        final Device device = (Device) view.getTag();
        PopupMenu popup = new PopupMenu(context, view);

        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_edit_device:
                        ((DevicesActivity)context).onEditDevice(device.getId());
                        return true;
                    case R.id.action_remove_device:
                        onRemoveDeviceAction(device.getId());
                        return true;
                    case R.id.action_show_on_map:
                        ((DevicesActivity)context).onShowOnMap(device.getId());
                        return true;
                    case R.id.action_send_command:
                        ((DevicesActivity)context).onSendCommand(device.getId());
                        return true;
                }
                return false;
            }
        });

        popup.show();
    }

    private void onRemoveDeviceAction(final long deviceId) {
        ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();
        confirmationDialogFragment.setLongParam(deviceId);
        confirmationDialogFragment.show(getFragmentManager(), "Confirmation");
    }

    public void removeDevice(final long deviceId) {
        final WebService service = application.getService();
        service.removeDevice(deviceId).enqueue(new WebServiceCallback<Void>(context) {
            @Override
            public void onSuccess(Response<Void> response) {
                Toast.makeText(context, R.string.device_removed, Toast.LENGTH_LONG).show();
                refreshDevices();
            }
        });
    }

}
