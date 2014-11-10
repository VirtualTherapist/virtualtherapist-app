package com.virtual.therapist.android.Objects;

public class ChatContext {
    private String mood;
    private double lat = -1;
    private double lng = -1;

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        if(lat != -1 && lng != -1)
            return "Mood: " + mood + " Location: " + lat + "/" + lng;

        return "Mood: " + mood + " Location: null";
    }
}
