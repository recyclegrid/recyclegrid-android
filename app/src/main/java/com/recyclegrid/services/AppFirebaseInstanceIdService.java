package com.recyclegrid.services;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.recyclegrid.app.R;
import com.recyclegrid.core.DeviceRegistrator;
import com.recyclegrid.database.SQLiteDataContext;

import java.util.HashMap;
import java.util.Map;

public class AppFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private SQLiteDataContext _db;
    private RequestQueue _requests;

    @Override
    public void onTokenRefresh() {
        DeviceRegistrator.Register(this);
    }
}
