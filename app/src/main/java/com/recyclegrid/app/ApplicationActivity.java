package com.recyclegrid.app;


import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.recyclegrid.database.SQLiteDataContext;
import com.recyclegrid.adapters.TabFragmentPagerAdapter;
import com.recyclegrid.database.User;

import java.util.HashMap;
import java.util.Map;

public class ApplicationActivity extends AppCompatActivity {

    private TabFragmentPagerAdapter _sectionsPagerAdapter;
    private ViewPager _viewPager;
    private SQLiteDataContext _db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _db = new SQLiteDataContext(this);

        _sectionsPagerAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager());

        _viewPager = findViewById(R.id.container);
        _viewPager.setOffscreenPageLimit(3);
        _viewPager.setAdapter(_sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(_viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_explore_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_group_white_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_account_circle_white_24dp);

        User user = _db.getUser();

        if (user != null) {
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("event") && getIntent().getStringExtra("event").equals("friend-request-received")) {
                openFriendRequests();
            }

            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("event") && getIntent().getStringExtra("event").equals("friend-request-accepted")) {
                openFriendsList();
            }

            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("event") && getIntent().getStringExtra("event").equals("friend-checkin")) {
                _viewPager.setCurrentItem(1, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        _db.close();
        super.onDestroy();
    }

    private void openFriendRequests() {
        Intent friendRequestsIntent = new Intent(this, FriendRequestsActivity.class);
        startActivity(friendRequestsIntent);
    }

    private void openFriendsList() {
        Intent friendsListIntent = new Intent(this, FriendsListActivity.class);
        startActivity(friendsListIntent);
    }
}
