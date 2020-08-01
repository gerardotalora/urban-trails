package edu.neu.madcourse.urban_trails.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Stop implements Serializable {
    private final String title;
    private final double latitude;

    private final double longitude;

    public Stop(String title, LatLng latLng) {
        this.title = title;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public String getTitle() {
        return title;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
