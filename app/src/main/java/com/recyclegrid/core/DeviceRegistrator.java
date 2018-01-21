package com.recyclegrid.core;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.recyclegrid.app.R;
import com.recyclegrid.database.SQLiteDataContext;

import java.util.HashMap;
import java.util.Map;

public class DeviceRegistrator {
    public static void Register(Context context) {
        final SQLiteDataContext _db = new SQLiteDataContext(context);

        RequestQueue requests = Volley.newRequestQueue(context);

        String url = context.getString(R.string.base_api_url) + "/account/registerdevice";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + _db.getUserToken("RecycleGrid"));

                return headers;
            }

            @Override
            public Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", FirebaseInstanceId.getInstance().getToken());
                params.put("deviceType", "Android");

                return params;
            }
        };

        requests.add(request);
    }
}
