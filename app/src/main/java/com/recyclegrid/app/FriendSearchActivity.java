package com.recyclegrid.app;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.adapters.FriendSearchListAdapter;
import com.recyclegrid.core.UserModel;
import com.recyclegrid.database.SQLiteDataContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendSearchActivity extends AppCompatActivity {
    private EditText _searchText;
    private ListView _friendsList;
    private Toast _toast;
    private Context _context;

    private SQLiteDataContext _db;

    private RequestQueue _requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_search);

        _db = new SQLiteDataContext(this);
        _context = this;
        _requests = Volley.newRequestQueue(_context);
        _toast = new Toast(this);

        _friendsList = findViewById(R.id.list_friends_search_results);
        _searchText = findViewById(R.id.edit_text_search);

        _searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_SEARCH) {
                    String url = getString(R.string.base_api_url) + "/friends/search?term=" + _searchText.getText();

                    JsonObjectRequest searchUsersRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            List<UserModel> friends = new ArrayList<>();

                            JSONArray searchResults = response.optJSONArray("Users");

                            if (searchResults != null) {
                                for (int i = 0; i < searchResults.length(); i++) {
                                    try {
                                        friends.add(new UserModel(searchResults.getJSONObject(i).getLong("Id"),
                                                searchResults.getJSONObject(i).getString("Name"),
                                                searchResults.getJSONObject(i).getString("ProfilePictureUrl")));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                _friendsList.setAdapter(new FriendSearchListAdapter(_context, friends));

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(_searchText.getWindowToken(), 0);
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

                    return true;
                }
                return false;
            }
        });
    }
}
