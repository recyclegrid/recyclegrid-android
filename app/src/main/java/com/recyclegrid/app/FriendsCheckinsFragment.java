package com.recyclegrid.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.adapters.FriendCheckInsAdapter;
import com.recyclegrid.broadcast.FriendsEventStreamBroadcastReceiver;
import com.recyclegrid.database.CheckIn;
import com.recyclegrid.database.SQLiteDataContext;
import com.recyclegrid.database.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FriendsCheckinsFragment extends Fragment {
    private RequestQueue _requests;
    private View _view;
    private ListView _friendCheckIns;
    private Context _context;
    private SQLiteDataContext _db;
    private LocalBroadcastManager _broadcastManager;
    private FriendsEventStreamBroadcastReceiver _broadcastReceiver;

    public FriendsCheckinsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _context = getActivity();

        _db = new SQLiteDataContext(_context);

        User user = _db.getUser();

        if (user == null) {
            _view = inflater.inflate(R.layout.activity_prompt_sign_in, container, false);

            Button startSignIn = _view.findViewById(R.id.button_sign_in);

            startSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent loginIntent = new Intent(getContext(), SignInActivity.class);
                    startActivity(loginIntent);
                }
            });
        } else {
            _broadcastManager = LocalBroadcastManager.getInstance(_context);
            _requests = Volley.newRequestQueue(_context);

            _view = inflater.inflate(R.layout.fragment_friends_activity, container, false);

            _friendCheckIns = _view.findViewById(R.id.list_friend_checkins);
            _friendCheckIns.setAdapter(new FriendCheckInsAdapter(_context));

            refresh();

            _broadcastReceiver = new FriendsEventStreamBroadcastReceiver(this);
            _broadcastManager.registerReceiver(_broadcastReceiver, new IntentFilter("refresh-friends-event-stream"));
        }

        return  _view;
    }

    public void refresh() {
        FriendCheckInsAdapter adapter = (FriendCheckInsAdapter) _friendCheckIns.getAdapter();

        CheckIn lastCheckin = null;

        if (adapter.getCount() > 0) {
            lastCheckin = (CheckIn) adapter.getItem(0);
        }
        else {
            lastCheckin = new CheckIn();
        }

        String url = getString(R.string.base_api_url) + "/checkins?since=" + lastCheckin.getCheckInDate().getTime();

        JsonArrayRequest searchUsersRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject checkIn = response.getJSONObject(i);

                            _db.addCheckIn(new CheckIn(
                                    checkIn.getLong("Id"),
                                    checkIn.getString("Username"),
                                    checkIn.getString("ProfilePictureUrl"),
                                    checkIn.getString("VenueName"),
                                    new Date(checkIn.getLong("Timestamp")),
                                    checkIn.getInt("VenueAcceptedMaterial")
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                FriendCheckInsAdapter adapter = (FriendCheckInsAdapter) _friendCheckIns.getAdapter();
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
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

        _requests.add(searchUsersRequest);
    }

    @Override
    public void onDestroy(){
        _broadcastManager.unregisterReceiver(_broadcastReceiver);
        super.onDestroy();
    }
}
