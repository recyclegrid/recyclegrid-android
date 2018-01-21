package com.recyclegrid.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.recyclegrid.database.SQLiteDataContext;

public class SignOutActivity extends AppCompatActivity {

    private SQLiteDataContext _db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _db = new SQLiteDataContext(this);

        _db.deleteUser();
        _db.deleteUserToken();

        LoginManager.getInstance().logOut();

        Intent appIntent = new Intent(this, ApplicationActivity.class);
        startActivity(appIntent);
    }
}
