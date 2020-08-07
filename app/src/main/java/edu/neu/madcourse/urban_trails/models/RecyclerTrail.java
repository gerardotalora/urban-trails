package edu.neu.madcourse.urban_trails.models;

import java.util.List;

public class RecyclerTrail {

    private String username;
    private Trail trail;

    public RecyclerTrail() {
        this.username = "username";
    }

    public RecyclerTrail(String username, Trail trail) {
        this.username = username;
        this.trail = trail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Trail getTrail() {
        return trail;
    }

    public void setTrail(Trail trail) {
        this.trail = trail;
    }
}
