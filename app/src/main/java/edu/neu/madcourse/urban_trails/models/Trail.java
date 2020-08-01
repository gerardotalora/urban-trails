package edu.neu.madcourse.urban_trails.models;

//import com.google.firebase.database.IgnoreExtraProperties;

import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trail implements Serializable {

    private String name;
    private String description;
    private List<Stop> stops;
    /*
     * timestamp is set automatically by Firebase when we do the following:
     * DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
     * Map map = new HashMap();
     * map.put("timestamp", ServerValue.TIMESTAMP);
     * ref.child("yourNode").updateChildren(map);
     * */
    Long timestamp = null; //ServerValue.TIMESTAMP;

    private String trailImageBase64;

    public Trail() {
        this.name = "name";
        this.description = "description";
        this.stops = new ArrayList<>();
        this.timestamp = null; //ServerValue.TIMESTAMP;
        this.trailImageBase64 = "trailImageBase64";
    }

    public Trail(String name, String description, List<Stop> stops, Long timestamp, String trailImageBase64) {
        this.name = name;
        this.description = description;
        this.stops = stops;
        this.timestamp = timestamp;
        this.trailImageBase64 = trailImageBase64;
    }

    public Trail(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTrailImageBase64() {
        return trailImageBase64;
    }

    public void setTrailImageBase64(String trailImageBase64) {
        this.trailImageBase64 = trailImageBase64;
    }
}
