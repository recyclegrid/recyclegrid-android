package com.recyclegrid.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
import com.facebook.Profile;
import com.facebook.login.LoginManager;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {
    private Context _context;
    private RequestQueue _requests;
    private TextInputLayout _emailView;
    private TextInputLayout _passwordView;
    private View _progressView;
    private View _loginFormView;

    private AccessToken _accessToken;
    private CallbackManager _facebookCallbackManager;

    private SQLiteDataContext _db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        _context = this;
        _requests = Volley.newRequestQueue(_context);
        _db = new SQLiteDataContext(_context);

        _emailView = findViewById(R.id.email);
        _passwordView = findViewById(R.id.password);

        _passwordView.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button emailSignInButton = findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        _loginFormView = findViewById(R.id.login_form);
        _progressView = findViewById(R.id.progress_bar);

        Button signupButton = findViewById(R.id.button_signup);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(v.getContext(), SignUpActivity.class);
                startActivity(loginIntent);
            }
        });

        _facebookCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile", "user_friends");

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

                                            showProgress(false);
                                            openRecycleVenuesMap();
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
            public void onCancel() {
                Log.e("FB","Login canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void attemptLogin() {

        _emailView.setError(null);
        _passwordView.setError(null);

        final String email = _emailView.getEditText().getText().toString();
        final String password = _passwordView.getEditText().getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!isPasswordValid(password)) {
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

            String url = getString(R.string.base_api_url) + "/account/getauthtoken";

            StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            try {
                                JSONObject responseObject = new JSONObject(response);

                                String authToken = responseObject.getString("access_token");
                                String fullName = responseObject.getString("FullName");
                                String profilePictureUrl = null;
                                long id = responseObject.getLong("Id");

                                if (!responseObject.isNull("ProfilePictureUrl")){
                                    profilePictureUrl = responseObject.getString("ProfilePictureUrl");
                                }

                                _db.createUser(new User(id, fullName, profilePictureUrl));
                                _db.addToken(new Token("RecycleGrid", authToken));

                                DeviceRegistrator.Register(_context);
                                showProgress(false);
                                openRecycleVenuesMap();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showProgress(false);
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error){
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
                    params.put("Email", email);
                    params.put("Password", password);
                    return params;
                }
            };

            _requests.add(strRequest);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 2;
    }

    private void openRecycleVenuesMap(){
        Intent rg = new Intent(this, ApplicationActivity.class);
        startActivity(rg);
    }

    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        _loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        _loginFormView.animate().setDuration(shortAnimTime).alpha(
            show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            _loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        _facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

