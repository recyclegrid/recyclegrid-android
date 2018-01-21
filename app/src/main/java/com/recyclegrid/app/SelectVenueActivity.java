package com.recyclegrid.app;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.adapters.VenuesListAdapter;
import com.recyclegrid.core.RecycleVenue;
import com.recyclegrid.core.RecycleVenueFactory;

import org.json.JSONArray;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectVenueActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private LocationManager _locationManager;
    private ListView _venuesList;
    private Location _currentLocation;
    private Toast _toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_venue);

        _toast = new Toast(this);

        _venuesList = findViewById(R.id.list_venues);
        _venuesList.setOnItemClickListener(this);

        _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        _currentLocation = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String url = getString(R.string.base_api_url) + "/recyclevenues/getnearbyvenues?lat=" + _currentLocation.getLatitude() + "&lng=" + _currentLocation.getLongitude();

        JsonArrayRequest nearbyVenuesRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                venuesListFromJsonArray(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                _toast.showError(R.string.error_response_default);
            }
        });

        requestQueue.add(nearbyVenuesRequest);
    }

    private void venuesListFromJsonArray(JSONArray jsonArray) {
        List<RecycleVenue> venues = RecycleVenueFactory.fromJsonArray(jsonArray);

        Collections.sort(venues, new Comparator<RecycleVenue>() {
            @Override
            public int compare(RecycleVenue o1, RecycleVenue o2) {
                return (int) (o1.distanceTo(_currentLocation) - o2.distanceTo(_currentLocation));
            }
        });

        _venuesList.setAdapter(new VenuesListAdapter(this, venues, _currentLocation));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent venueSelectResultIntent = new Intent();

        RecycleVenue venue = (RecycleVenue) _venuesList.getAdapter().getItem(position);

        venueSelectResultIntent.putExtra("venue", venue);
        setResult(RESULT_OK, venueSelectResultIntent);

        finish();
    }
}
