package com.recyclegrid.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.adapters.FriendListAdapter;
import com.recyclegrid.core.UserModel;
import com.recyclegrid.database.SQLiteDataContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsListActivity extends AppCompatActivity {
    private ListView _friendsList;
    private Toast _toast;
    private Context _context;

    private SQLiteDataContext _db;

    private RequestQueue _requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_requests);

        _db = new SQLiteDataContext(this);
        _context = this;
        _requests = Volley.newRequestQueue(_context);
        _toast = new Toast(this);

        _friendsList = findViewById(R.id.list_friend_requests);

        String url = getString(R.string.base_api_url) + "/friends/getfriends";

        JsonObjectRequest searchUsersRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                List<UserModel> friends = new ArrayList<>();

                JSONArray searchResults = response.optJSONArray("Friends");

                if (searchResults != null && searchResults.length() > 0) {
                    for (int i = 0; i < searchResults.length(); i++) {
                        try {
                            friends.add(new UserModel(searchResults.getJSONObject(i).getLong("Id"),
                                    searchResults.getJSONObject(i).getString("Name"),
                                    searchResults.getJSONObject(i).getString("ProfilePictureUrl")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    _friendsList.setAdapter(new FriendListAdapter(_context, friends));
                }
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

        _requests.add(searchUsersRequest);
    }
}
