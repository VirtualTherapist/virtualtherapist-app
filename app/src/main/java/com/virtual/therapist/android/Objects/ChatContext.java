package com.virtual.therapist.android.Objects;

import android.location.Location;

/**
 * Created by bas on 23-10-14.
 */
public class ChatContext {
    private String mood;
    private Location location;

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        if(location != null)
            return "Mood: " + mood + " Location: " + location.getLatitude() + "/" + location.getLongitude();

        return "Mood: " + mood + " Location: null";
    }
}
