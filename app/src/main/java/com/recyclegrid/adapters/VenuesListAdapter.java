package com.recyclegrid.adapters;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.recyclegrid.app.R;
import com.recyclegrid.core.RecycleVenue;

import java.util.List;

public class VenuesListAdapter extends ArrayAdapter<RecycleVenue> {
    private LayoutInflater _inflater;
    private Location _curentLocation;

    public VenuesListAdapter(Context context, List<RecycleVenue> venues, Location currentLocation) {
        super(context, R.layout.venues_list_item, venues);

        _inflater = LayoutInflater.from(context);
        _curentLocation = currentLocation;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.venues_list_item, parent, false);
        }

        TextView venueName = convertView.findViewById(R.id.text_venue_name);
        TextView venueAddress = convertView.findViewById(R.id.text_venue_address);
        TextView venueDistance = convertView.findViewById(R.id.text_venue_distance);
        ImageView venueIcon = convertView.findViewById(R.id.image_venue_icon);

        RecycleVenue venue = getItem(position);

        Location venueLocation = new Location(LocationManager.GPS_PROVIDER);
        venueLocation.setLatitude(venue.getLatitude());
        venueLocation.setLongitude(venue.getLongitude());

        float distance = _curentLocation.distanceTo(venueLocation);

        venueName.setText(venue.getName());
        venueAddress.setText(venue.getAddress());
        venueDistance.setText(String.format("%.2f " + getContext().getString(R.string.kilometers), distance / 1000));

        switch (venue.getAcceptedMaterial()) {
            case RecycleVenue.AcceptedMaterial.BATTERY :
                venueIcon.setBackgroundResource(R.drawable.ic_battery_std_white_48dp);
                break;
            case RecycleVenue.AcceptedMaterial.PLASTIC :
                venueIcon.setBackgroundResource(R.drawable.ic_bottle_white_blue_circle_36dp);
                break;
        }

        return convertView;
    }
}
