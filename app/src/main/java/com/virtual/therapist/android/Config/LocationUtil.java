package com.virtual.therapist.android.Config;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.Date;

/**
 * Created by bas on 23-10-14.
 */
public class LocationUtil {

    private static LocationUtil instance;

    private final String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private final long MAX_ELAPSED_TIME = 1000 * 60 * 60; //One hour
    private final Context context;


    private LocationUtil(Context context) {
        this.context = context;
    }

    public static LocationUtil getInstance(Context context) {
        if(instance == null)
            instance = new LocationUtil(context);

        return instance;
    }

    public Location getLastLocation() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LOCATION_PROVIDER);
        if(lastKnownLocation != null)
            if(new Date().getTime() - lastKnownLocation.getTime() < MAX_ELAPSED_TIME)
                return lastKnownLocation;

        return null;
    }

}
