package com.recyclegrid.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.core.RecycleVenue;
import com.recyclegrid.database.SQLiteDataContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckInActivity extends AppCompatActivity {
    private com.recyclegrid.app.Toast _toast;
    private Context _context;
    private RecycleVenue _currentVenue;
    private EditText _checkInComment;

    private SQLiteDataContext _db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        _context = this;
        _toast = new com.recyclegrid.app.Toast(this);

        _db = new SQLiteDataContext(this);

        _checkInComment = findViewById(R.id.text_check_in_comment);
        _currentVenue = getIntent().getParcelableExtra("check_in_venue");

        TextView venueName = findViewById(R.id.text_check_in_venue_name);
        LinearLayout checkInHeader = findViewById(R.id.layout_check_in_header);

        if (_currentVenue != null) {
            venueName.setText(_currentVenue.getName());

            int venueMaterial = _currentVenue.getAcceptedMaterial();

            ImageView checkInIcon = findViewById(R.id.image_check_in_material_icon);

            switch (venueMaterial) {
                case 1:
                    checkInHeader.setBackgroundResource(R.color.battery);
                    checkInIcon.setBackgroundResource(R.drawable.ic_battery_accept_white_32dp_1x);
                    break;
                case 2:
                    checkInHeader.setBackgroundResource(R.color.plastic);
                    checkInIcon.setBackgroundResource(R.drawable.ic_bottle_accept_white_32px_1x);
                    break;
            }
        }
        else {
            venueName.setText(R.string.check_in_select_venue);
        }

        checkInHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVenueList(v);
            }
        });

        Button checkInButton = findViewById(R.id.button_check_in);

        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckIn(v);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    _currentVenue = intent.getParcelableExtra("venue");

                    TextView venueName = findViewById(R.id.text_check_in_venue_name);
                    LinearLayout checkInHeader = findViewById(R.id.layout_check_in_header);
                    ImageView checkInIcon = findViewById(R.id.image_check_in_material_icon);

                    venueName.setText(_currentVenue.getName());

                    switch (_currentVenue.getAcceptedMaterial()) {
                        case 1:
                            checkInHeader.setBackgroundResource(R.color.battery);
                            checkInIcon.setBackgroundResource(R.drawable.ic_battery_accept_white_32dp_1x);
                            break;
                        case 2:
                            checkInHeader.setBackgroundResource(R.color.plastic);
                            checkInIcon.setBackgroundResource(R.drawable.ic_bottle_accept_white_32px_1x);
                            break;
                    }
                }
                break;
        }
    }

    private void openVenueList(View view) {
        Intent selectVenue = new Intent(this, SelectVenueActivity.class);
        startActivityForResult(selectVenue, 0);
    }

    private void onCheckIn(View v) {
        if (_currentVenue == null) {
            _toast.showInformation(R.string.check_in_select_venue);
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(v.getContext());
        String url = getString(R.string.base_api_url) + "/checkins/checkin";

        JSONObject r = new JSONObject();
        try {
            r.put("RecycleVenueId", Long.toString(_currentVenue.getId()));
            r.put("Comment", _checkInComment.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, r, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        long id = 0;

                        try {
                            id = response.getLong("Id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        LocalBroadcastManager.getInstance(_context).sendBroadcast(new Intent("refresh-friends-event-stream"));
                        finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        _toast.showError(R.string.error_response_default);
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
