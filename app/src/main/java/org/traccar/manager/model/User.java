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
package org.traccar.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String speedUnit;

    public String getSpeedUnit() { return speedUnit; }

    public void setSpeedUnit(String speedUnit) { this.speedUnit = speedUnit; }

    private double latitude;

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    private double longitude;

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    private int zoom;

    public int getZoom() { return zoom; }

    public void setZoom(int zoom) { this.zoom = zoom; }

    private boolean twelveHourFormat;

    public boolean getTwelveHourFormat() { return twelveHourFormat; }

    public void setTwelveHourFormat(boolean twelveHourFormat) { this.twelveHourFormat = twelveHourFormat; }
}
