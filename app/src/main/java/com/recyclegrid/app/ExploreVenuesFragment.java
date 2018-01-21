package com.recyclegrid.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.recyclegrid.core.RecycleVenue;
import com.recyclegrid.database.SQLiteDataContext;
import com.recyclegrid.database.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ExploreVenuesFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleMap.OnCameraIdleListener {

    private GoogleMap _map;
    private MapView _mapView;
    private GoogleApiClient _googleApiClient;
    private LocationManager _locationManager;
    private Location _lastLocation;
    private BottomSheetBehavior _bottomSheetBehavior;
    private ArrayList<Marker> _markers;
    private Marker _currentMarker;
    private View _view;
    private FloatingActionButton _checkInFab;
    private FloatingActionButton _addVenueFab;
    private Toast _toast;
    private Context _context;

    private SQLiteDataContext _db;

    private RecycleVenue _currentVenue;

    private static final int REQUEST_LOCATION = 1001;

    public ExploreVenuesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _context = getActivity();
        _toast = new Toast((AppCompatActivity) _context);

        _db = new SQLiteDataContext(_context);

        _markers = new ArrayList<>();
        _view = inflater.inflate(R.layout.fragment_explore_venues, container, false);

        _checkInFab = (FloatingActionButton) findViewById(R.id.check_in_fab);

        _checkInFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = _db.getUser();
                if (user != null) {
                    Intent checkInIntent = new Intent(getActivity(), CheckInActivity.class);
                    checkInIntent.putExtra("check_in_venue", _currentVenue);
                    startActivityForResult(checkInIntent, 1231);
                }
                else {
                    Intent promptSignIn = new Intent(getActivity(), PromptSignInActivity.class);
                    startActivity(promptSignIn);
                }
            }
        });

        _addVenueFab = (FloatingActionButton) findViewById(R.id.fab_add_venue);

        _addVenueFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = _db.getUser();
                if (user != null) {
                    Intent addVenue = new Intent(getActivity(), AddVenueActivity.class);
                    startActivityForResult(addVenue, 1999);
                }
                else {
                    Intent promptSignIn = new Intent(getActivity(), PromptSignInActivity.class);
                    startActivity(promptSignIn);
                }
            }
        });

        View bottomSheet = findViewById(R.id.bottom_sheet);

        _bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        _bottomSheetBehavior.setPeekHeight(0);

        _bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) _checkInFab.getLayoutParams();
                if (slideOffset > 0.75) {
                    p.setAnchorId(R.id.bottom_sheet_venue_details_address);
                   _checkInFab.setLayoutParams(p);
                    _addVenueFab.hide();
                }

                if (slideOffset < 0.75) {
                    p.setAnchorId(R.id.bottom_sheet);
                    _checkInFab.setLayoutParams(p);
                }

                // TODO: Update marker icon when sheet is hidden
                if (slideOffset == 0) {
                    _bottomSheetBehavior.setPeekHeight(0);
                    _addVenueFab.show();
                }
            }
        });

        _locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        _googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        _googleApiClient.connect();

        _mapView = (MapView) findViewById(R.id.venues_map);

        _mapView.onCreate(savedInstanceState);
        _mapView.getMapAsync(this);

        return _view;
    }

    @Override
    public void onResume() {
        super.onResume();
        _mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _mapView.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        _mapView.onPause();
        _locationManager.removeUpdates(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;
        _map.setOnCameraIdleListener(this);
        _map.getUiSettings().setMapToolbarEnabled(false);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {  Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_LOCATION);
        }
        else {


            if (_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                _lastLocation = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            }
            if (_lastLocation == null && _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                _lastLocation = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
            }
        }

        if (_lastLocation != null)
        {
            _map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_lastLocation.getLatitude(), _lastLocation.getLongitude()), 14));
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                _addVenueFab.hide();

                if (_currentMarker != null && _currentMarker.getPosition().latitude != marker.getPosition().latitude && _currentMarker.getPosition().longitude != marker.getPosition().longitude) {
                    _currentMarker.remove();
                }

                _currentMarker = marker;

                View bottomSheet = findViewById( R.id.bottom_sheet );

                _bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

                _bottomSheetBehavior.setPeekHeight(300);

                JSONObject venue = (JSONObject) marker.getTag();

                TextView bottomText = (TextView) findViewById(R.id.bottom_sheet_venue_details_title);
                View acceptedMaterial = findViewById(R.id.bottom_sheet_venue_details_accept_layout);

                try {
                    _currentVenue = new RecycleVenue(venue.getLong("Id"), venue.getString("Name"), venue.getInt("AcceptedMaterial"));

                    bottomText.setText(venue.getString("Name"));

                    int bgTextColor = Color.parseColor("#FF6A00");

                    if (venue.getInt("AcceptedMaterial") == 2) {
                        bgTextColor = Color.parseColor("#00ACE0");
                    }

                    bottomText.setBackgroundColor(bgTextColor);
                    acceptedMaterial.setBackgroundColor(bgTextColor);

                    TextView addressText = (TextView) findViewById(R.id.bottom_sheet_venue_details_address);

                    String address = venue.getString("Address");

                    if (address != "null") {
                        addressText.setText(address);
                    }

                    TextView desc = (TextView) findViewById(R.id.bottom_sheet_venue_details_description);

                    String description = venue.getString("Description");

                    if (description != "null") {
                        desc.setText(description);
                    }

                    int markerIconId = R.drawable.ic_location_orange_24dp;

                    if (venue.getInt("AcceptedMaterial") == 2) {
                        markerIconId = R.drawable.ic_location_blue_24dp;
                    }

                    int accptedMaterialIconId = R.drawable.ic_battery_accept_white_32dp_1x;

                    if (venue.getInt("AcceptedMaterial") == 2) {
                        accptedMaterialIconId = R.drawable.ic_bottle_accept_white_32px_1x;
                    }

                    ImageView acceptedIcon = (ImageView) findViewById(R.id.accepted_material_icon);

                    acceptedIcon.setBackground(ResourcesCompat.getDrawable(getResources(), accptedMaterialIconId, null));

                    marker.setIcon(BitmapDescriptorFactory.fromResource(markerIconId));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return false;
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));

        _locationManager.removeUpdates(this);

        getVenuesInArea();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        getVenuesInArea();
    }

    @Override
    public void onCameraIdle() {
        if (_map.getCameraPosition().zoom > 10) {
            getVenuesInArea();
        }
    }

    private View findViewById(int id) {
        return _view.findViewById(id);
    }

    private void getVenuesInArea() {
        Projection projection = _map.getProjection();

        VisibleRegion visibleRegion = projection.getVisibleRegion();

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = getString(R.string.base_api_url) + "/RecycleVenues/GetVenuesInArea?searchString=" +
                "&south=" + visibleRegion.latLngBounds.southwest.latitude +
                "&west=" + visibleRegion.latLngBounds.southwest.longitude +
                "&north=" + visibleRegion.latLngBounds.northeast.latitude +
                "&east=" + visibleRegion.latLngBounds.northeast.longitude;

            JsonArrayRequest jsObjRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        for (Marker _m: _markers) {

                            LatLng currentMarkerPosition = new LatLng(0,0);

                            if (_currentMarker != null) {
                                currentMarkerPosition = _currentMarker.getPosition();
                            }

                            if (_m.getPosition().latitude != currentMarkerPosition.latitude && _m.getPosition().longitude != currentMarkerPosition.longitude) {
                                _m.remove();
                            }
                        }

                        _markers = new ArrayList<>();

                        for(int i = 0; i < response.length(); i++) {

                            JSONObject venue = null;
                            try {
                                venue = response.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                int markerIconId = R.drawable.ic_circle_orange;

                                if (venue.getInt("AcceptedMaterial") == 2) {
                                    markerIconId = R.drawable.ic_cirle_blue_16px;
                                }

                                Marker marker =  _map.addMarker(new MarkerOptions()
                                        .position(new LatLng(venue.getDouble("Latitude"), venue.getDouble("Longitude")))
                                        .icon(BitmapDescriptorFactory.fromResource(markerIconId)));

                                marker.setTag(venue);

                                _markers.add(marker);

                                LatLng currentMarkerPosition = new LatLng(0,0);

                                if (_currentMarker != null) {
                                    currentMarkerPosition = _currentMarker.getPosition();
                                }

                                if (marker.getPosition().latitude == currentMarkerPosition.latitude && marker.getPosition().longitude == currentMarkerPosition.longitude) {
                            marker.remove();
                            _markers.remove(marker);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        _toast.showError(R.string.error_response_get_venues);
                        error.printStackTrace();
                    }
                });

        queue.add(jsObjRequest);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
                    _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
                }
            }
        }
    }
}
