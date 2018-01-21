package com.recyclegrid.core;

import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;

public class RecycleVenue implements Parcelable {

    private long _id;
    private String _address;
    private String _name;
    private int _acceptedMaterial;
    private double _lat;
    private double _lng;

    protected RecycleVenue(Parcel in) {
        _id = in.readLong();
        _name = in.readString();
        _acceptedMaterial = in.readInt();
    }

    public RecycleVenue(long id, String name, int acceptedMaterial) {
        _id = id;
        _name = name;
        _acceptedMaterial = acceptedMaterial;
    }

    public RecycleVenue(long id, String name, String address, int acceptedMaterial, double lat, double lng) {
        _id = id;
        _name = name;
        _address = address;
        _acceptedMaterial = acceptedMaterial;
        _lat = lat;
        _lng = lng;
    }

    public static final Creator<RecycleVenue> CREATOR = new Creator<RecycleVenue>() {
        @Override
        public RecycleVenue createFromParcel(Parcel in) {
            return new RecycleVenue(in);
        }

        @Override
        public RecycleVenue[] newArray(int size) {
            return new RecycleVenue[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(_name);
        dest.writeInt(_acceptedMaterial);
    }

    public String getName() { return _name; }
    public long getId() { return _id; }
    public int getAcceptedMaterial() { return _acceptedMaterial; }
    public String getAddress() { return _address; }
    public double getLatitude() { return _lat; }
    public double getLongitude() { return _lng; }

    public float distanceTo(Location location) {
        Location venueLocation = new Location(LocationManager.GPS_PROVIDER);

        venueLocation.setLatitude(_lat);
        venueLocation.setLongitude(_lng);

        return location.distanceTo(venueLocation);
    }

    public static class AcceptedMaterial {
        public static final int BATTERY = 1;
        public static final int PLASTIC = 2;
    }
}
