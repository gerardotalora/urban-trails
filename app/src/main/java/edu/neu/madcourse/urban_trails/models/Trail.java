package edu.neu.madcourse.urban_trails.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Trail {

    private String name;

    public Trail() {
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
}
