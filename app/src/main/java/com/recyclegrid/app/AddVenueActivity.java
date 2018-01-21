package com.recyclegrid.app;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.recyclegrid.adapters.CitiesListAdapter;
import com.recyclegrid.core.RecycleMaterial;
import com.recyclegrid.database.SQLiteDataContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddVenueActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView _mapView;
    private GoogleMap _map;
    private LocationManager _locationManager;
    private SQLiteDataContext _db;
    private LatLng _venueLocation;
    private Marker _marker;

    private long _cityId;

    private TextInputLayout _venueNameInput;
    private TextInputLayout _venueAddressInput;
    private TextInputLayout _descriptionInput;
    private TextInputLayout _cityInput;
    private AutoCompleteTextView _cityAutocomplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_venue);

        _db = new SQLiteDataContext(this);

        _venueNameInput = findViewById(R.id.input_venue_name);
        _cityInput = findViewById(R.id.input_venue_city);
        _venueAddressInput = findViewById(R.id.input_venue_address);
        _descriptionInput = findViewById(R.id.input_description);

        _cityAutocomplete = _cityInput.findViewById(R.id.autocomplete_city);

        _cityAutocomplete.setAdapter(new CitiesListAdapter(this));
        _cityAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _cityId = id;
            }
        });

        Button submitButton = findViewById(R.id.button_submit_venue);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVenueFormValid()) {
                    RequestQueue queue = Volley.newRequestQueue(v.getContext());
                    String url = getString(R.string.base_api_url) + "/recyclevenues/add";

                    JSONObject r = new JSONObject();
                    try {
                        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.accepted_material_radio_group);
                        switch (radioGroup.getCheckedRadioButtonId()) {
                            case R.id.radio_accept_batteries:
                                r.put("AcceptedMaterial", RecycleMaterial.BATTERY);
                                break;
                            case  R.id.radio_accept_plastic:
                                r.put("AcceptedMaterial", RecycleMaterial.PLASTICS);
                                break;
                        }
                        r.put("Lat", _venueLocation.latitude);
                        r.put("Lng", _venueLocation.longitude);
                        r.put("CityId", _cityId);
                        r.put("City", _cityAutocomplete.getText());
                        r.put("Address", _venueAddressInput.getEditText().getText());
                        r.put("VenueName", _venueNameInput.getEditText().getText().toString());
                        r.put("Description", _descriptionInput.getEditText().getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest jsObjRequest = new JsonObjectRequest
                            (Request.Method.POST, url, r, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    finish();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                    showRequestError();
                                }
                            })
                    {
                        @Override
                        public Map<String, String> getHeaders(){
                            String token = _db.getUserToken("RecycleGrid");

                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Authorization", "Bearer " + token);

                            return params;
                        }
                    };

                    queue.add(jsObjRequest);
                }
            }
        });

        _mapView = findViewById(R.id.map_select_location);
        _mapView.onCreate(savedInstanceState);
        _mapView.getMapAsync(this);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;

        _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location _lastLocation = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (_lastLocation == null) {
            _lastLocation = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (_lastLocation != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(_lastLocation.getLatitude(), _lastLocation.getLongitude()), 17));
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                _venueLocation = latLng;

                if (_marker != null) {
                    _marker.remove();
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_add_location_black_24dp));

                _marker = _map.addMarker(markerOptions);

                findViewById(R.id.text_map_selection_error).setVisibility(View.GONE);
            }
        });
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
    }

    public boolean isVenueFormValid() {
        boolean isValid = true;

        if (findViewById(R.id.text_map_selection_error).getVisibility() != View.GONE) {
            TextView mapError = findViewById(R.id.text_map_selection_error);
            mapError.setTextColor(ContextCompat.getColor(this, R.color.error));
            isValid = false;
        }

        if (TextUtils.isEmpty(_venueNameInput.getEditText().getText())) {
            _venueNameInput.setError(getString(R.string.error_input_name));
            isValid = false;
        }
        else {
            _venueNameInput.setError(null);
        }

        if (TextUtils.isEmpty(_venueAddressInput.getEditText().getText())) {
            _venueAddressInput.setError(getString(R.string.error_input_address));
            isValid = false;
        }
        else {
            _venueAddressInput.setError(null);
        }

        if (TextUtils.isEmpty(_cityAutocomplete.getText())) {
            _cityInput.setError(getString(R.string.error_input_city));
            isValid = false;
        }
        else {
            _cityInput.setError(null);
        }

        return  isValid;
    }

    private void showRequestError() {
        Toast.makeText(this, R.string.error_response_default, Toast.LENGTH_SHORT).show();
    }
}