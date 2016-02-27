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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import java.io.IOException;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;
import retrofit.Retrofit;

public class MainFragment extends SupportMapFragment implements OnMapReadyCallback {

    public static final int REQUEST_DEVICE = 1;
    public static final int RESULT_SUCCESS = 1;

    private GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getMapAsync(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_devices:
                startActivityForResult(new Intent(getContext(), DevicesActivity.class), REQUEST_DEVICE);
                return true;
            case R.id.action_logout:
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit().putBoolean(MainApplication.PREFERENCE_AUTHENTICATED, false).apply();
                getActivity().finish();
                startActivity(new Intent(getContext(), LoginActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DEVICE && resultCode == RESULT_SUCCESS) {
            long deviceId = data.getLongExtra(DevicesFragment.EXTRA_DEVICE_ID, 0);
            Toast.makeText(getContext(), "device selected: " + deviceId, Toast.LENGTH_LONG).show();
            // TODO select device
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // TODO remove
        //LatLng sydney = new LatLng(-34, 151);
        //map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        getActivity().setTitle("disconnected");

        final Snackbar status = Snackbar.make(getView(), "dis", Snackbar.LENGTH_LONG);
        status.show();

        /*final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(WebService service, Retrofit retrofit) {
                status.dismiss();

                Request request = new Request.Builder().url(retrofit.baseUrl() + "api/socket").build();
                WebSocketCall call = WebSocketCall.create(retrofit.client(), request);
                call.enqueue(new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, com.squareup.okhttp.Response response) {
                    }

                    @Override
                    public void onFailure(IOException e, com.squareup.okhttp.Response response) {
                    }

                    @Override
                    public void onMessage(BufferedSource payload, WebSocket.PayloadType type) throws IOException {
                        try {
                            String message = payload.readString(Charset.defaultCharset());
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        } finally {
                            payload.close();
                        }
                    }

                    @Override
                    public void onPong(Buffer payload) {
                    }

                    @Override
                    public void onClose(int code, String reason) {
                    }
                });
            }
        });*/
    }

}
