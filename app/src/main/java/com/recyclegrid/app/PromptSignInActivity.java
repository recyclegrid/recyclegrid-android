package com.recyclegrid.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.recyclegrid.database.SQLiteDataContext;

public class PromptSignInActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_sign_in);

        Button signInButton = findViewById(R.id.button_sign_in);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(v.getContext(), SignInActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
