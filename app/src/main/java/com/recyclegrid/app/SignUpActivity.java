package com.recyclegrid.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.recyclegrid.core.DeviceRegistrator;
import com.recyclegrid.database.SQLiteDataContext;
import com.recyclegrid.database.Token;
import com.recyclegrid.database.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {
    private Context _context;
    private RequestQueue _requests;
    private EditText _fullnameInput;
    private EditText _emailView;
    private EditText _passwordView;
    private View _progressView;
    private View _formView;

    private AccessToken _accessToken;
    private CallbackManager _facebookCallbackManager;

    private SQLiteDataContext _db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        _context = this;

        _requests = Volley.newRequestQueue(_context);

        _db = new SQLiteDataContext(_context);

        _fullnameInput = findViewById(R.id.full_name);
        _emailView = findViewById(R.id.email);
        _passwordView = findViewById(R.id.password);

        _passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    signUp();
                    return true;
                }
                return false;
            }
        });

        _formView = findViewById(R.id.signup_form);
        _progressView = findViewById(R.id.progress_bar);

        Button signupButton = findViewById(R.id.button_signup);
        signupButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        _facebookCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email");

        loginButton.registerCallback(_facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                showProgress(true);
                _accessToken = loginResult.getAccessToken();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject json, GraphResponse response) {
                        String url = getString(R.string.base_api_url) + "/account/ExternalLoginConfirmation";

                        JSONObject externalLoginModel = new JSONObject();
                        try {
                            externalLoginModel.put("Email", json.optString("email"));
                            externalLoginModel.put("LoginProvider", "Facebook");
                            externalLoginModel.put("FullName", json.optString("first_name") + " " + json.optString("last_name"));
                            externalLoginModel.put("LoginProvider", "Facebook");
                            externalLoginModel.put("LoginProviderKey", _accessToken.getUserId());

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ", Locale.US);

                            JSONArray tokens = new JSONArray();

                            Map<String, String> token = new HashMap<>();

                            token.put("Name", "access_token");
                            token.put("Value", _accessToken.getToken());
                            tokens.put(new JSONObject(token));

                            token.clear();

                            token.put("Name", "token_type");
                            token.put("Value", "bearer");
                            tokens.put(new JSONObject(token));

                            token.clear();

                            token.put("Name", "expires_at");
                            token.put("Value", dateFormat.format(_accessToken.getExpires()));
                            tokens.put(new JSONObject(token));

                            externalLoginModel.put("AuthenticationTokens", tokens);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                                (Request.Method.POST, url, externalLoginModel, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            String authToken = response.getString("access_token");
                                            String fullName = response.getString("FullName");
                                            String profilePictureUrl = response.getString("ProfilePictureUrl");
                                            long id = response.getLong("Id");

                                            _db.createUser(new User(id, fullName, profilePictureUrl));
                                            _db.addToken(new Token("RecycleGrid", authToken));

                                            DeviceRegistrator.Register(_context);
                                            showProgress(false);
                                            openApplicationAactivity();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            showProgress(false);
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });

                        _requests.add(jsObjRequest);
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void signUp() {

        _fullnameInput.setError(null);
        _emailView.setError(null);
        _passwordView.setError(null);

        final String fullName = _fullnameInput.getText().toString();
        final String email = _emailView.getText().toString();
        final String password = _passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(fullName)) {
            _fullnameInput.setError(getString(R.string.error_signup_field_required));
            focusView = _fullnameInput;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            _passwordView.setError(getString(R.string.error_invalid_password));
            focusView = _passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            _emailView.setError(getString(R.string.error_field_required));
            focusView = _emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            _emailView.setError(getString(R.string.error_invalid_email));
            focusView = _emailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = getString(R.string.base_api_url) + "/account/registerapi";

            StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            try {
                                JSONObject responseObject = new JSONObject(response);

                                String authToken = responseObject.getString("access_token");
                                long id = responseObject.getLong("Id");

                                _db.createUser(new User(id, fullName, null));
                                _db.addToken(new Token("RecycleGrid", authToken));

                                FirebaseInstanceId.getInstance().getToken();

                                showProgress(false);
                                openApplicationAactivity();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showProgress(false);
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            VolleyError e = error;
                            e.printStackTrace();
                            showProgress(false);
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("FullName", fullName);
                    params.put("Email", email);
                    params.put("Password", password);
                    params.put("ConfirmPassword", password);
                    return params;
                }
            };

            queue.add(strRequest);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 2;
    }

    private void openApplicationAactivity(){
        Intent rg = new Intent(this, ApplicationActivity.class);
        startActivity(rg);
    }

    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        _formView.setVisibility(show ? View.GONE : View.VISIBLE);
        _formView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                _formView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        _progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        _progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                _progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

