package edu.neu.madcourse.urban_trails.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Stop implements Parcelable {
    private final String title;
    private final LatLng latLng;

    public Stop(String title, LatLng latLng) {
        this.title = title;
        this.latLng = latLng;
    }

    protected Stop(Parcel in) {
        title = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeParcelable(this.latLng, 0);
    }
}
