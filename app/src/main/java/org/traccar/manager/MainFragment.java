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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.traccar.manager.model.Device;
import org.traccar.manager.model.Position;
import org.traccar.manager.model.Update;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import retrofit2.Retrofit;

public class MainFragment extends SupportMapFragment implements OnMapReadyCallback {

    public static final int REQUEST_DEVICE = 1;
    public static final int RESULT_SUCCESS = 1;

    private GoogleMap map;

    private Handler handler = new Handler();
    private ObjectMapper objectMapper = new ObjectMapper();

    private Map<Long, Device> devices = new HashMap<>();
    private Map<Long, Position> positions = new HashMap<>();
    private Map<Long, Marker> markers = new HashMap<>();

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
            Position position = positions.get(deviceId);
            if (position != null) {
                map.moveCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(position.getLatitude(), position.getLongitude())));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        createWebSocket();
    }

    private void handleMessage(String message) throws IOException {
        Update update = objectMapper.readValue(message, Update.class);
        if (update != null && update.positions != null) {
            for (Position position : update.positions) {
                long deviceId = position.getDeviceId();
                LatLng location = new LatLng(position.getLatitude(), position.getLongitude());
                Marker marker = markers.get(deviceId);
                if (marker == null) {
                    marker = map.addMarker(new MarkerOptions()
                            .title(devices.get(deviceId).getName()).position(location));
                    markers.put(deviceId, marker);
                } else {
                    marker.setPosition(location);
                }
                // TODO: show details: marker.setSnippet("something: 1\nelse: 2");
                positions.put(deviceId, position);
            }
        }
    }

    private void reconnectWebSocket() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                createWebSocket();
            }
        });
    }

    private void createWebSocket() {
        if (isResumed()) {
            final MainApplication application = (MainApplication) getActivity().getApplication();
            application.getServiceAsync(new MainApplication.GetServiceCallback() {
                @Override
                public void onServiceReady(final OkHttpClient client, final Retrofit retrofit, WebService service) {
                    service.getDevices().enqueue(new WebServiceCallback<List<Device>>(getContext()) {
                        @Override
                        public void onSuccess(retrofit2.Response<List<Device>> response) {
                            for (Device device : response.body()) {
                                devices.put(device.getId(), device);
                            }

                            Request request = new Request.Builder().url(retrofit.baseUrl().url().toString() + "api/socket").build();
                            WebSocketCall call = WebSocketCall.create(client, request);
                            call.enqueue(new WebSocketListener() {
                                @Override
                                public void onOpen(WebSocket webSocket, Response response) {
                                }

                                @Override
                                public void onFailure(IOException e, Response response) {
                                    reconnectWebSocket();
                                }

                                @Override
                                public void onMessage(ResponseBody message) throws IOException {
                                    final String data = message.string();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                handleMessage(data);
                                            } catch (IOException e) {
                                                Log.w(MainFragment.class.getSimpleName(), e);
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onPong(Buffer payload) {
                                }

                                @Override
                                public void onClose(int code, String reason) {
                                    reconnectWebSocket();
                                }
                            });

                        }
                    });
                }
            });
        }
    }

}
