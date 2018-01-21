package com.recyclegrid.app;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.core.ActivityRequestCodes;
import com.recyclegrid.core.AzureStorageFileUploader;
import com.recyclegrid.core.FriendRequestModel;
import com.recyclegrid.database.SQLiteDataContext;
import com.recyclegrid.database.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountFragment extends Fragment {
    private List<FriendRequestModel> friendRequests;
    private SQLiteDataContext _db;
    private View _view;
    private RequestQueue _requestQueue;
    private Toast _toast;
    private Context _context;

    public AccountFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _requestQueue = Volley.newRequestQueue(getActivity());
        _db = new SQLiteDataContext(getActivity());

        _context = getActivity();
        _toast = new Toast((AppCompatActivity) _context);

        User user = _db.getUser();

        if (user == null) {
            _view = inflater.inflate(R.layout.fragment_anonymous_account, container, false);

            Button startSignIn = _view.findViewById(R.id.button_start_sign_in_activity);

            startSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent loginIntent = new Intent(getContext(), SignInActivity.class);
                startActivity(loginIntent);
                }
            });

            return _view;
        }

        String url = getString(R.string.base_api_url) + "/friends/getrequests";

        JsonObjectRequest searchUsersRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray requests = response.optJSONArray("FriendRequests");

                friendRequests = new ArrayList<FriendRequestModel>();

                if (requests != null && requests.length() > 0) {
                    for (int i = 0; i < requests.length(); i++) {
                        try {
                            friendRequests.add(new FriendRequestModel(requests.getJSONObject(i).getLong("Id"),
                                    requests.getJSONObject(i).getLong("UserId"),
                                    requests.getJSONObject(i).getString("Name"),
                                    requests.getJSONObject(i).getString("ProfilePictureUrl")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    TextView newRequests = (TextView) findViewById(R.id.text_friend_requests_count);
                    newRequests.setText(getString(R.string.friend_request_count, friendRequests.size()));

                    newRequests.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent friendRequestsIntent = new Intent(getContext(), FriendRequestsActivity.class);
                            startActivity(friendRequestsIntent);
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                _toast.showError(_context.getString(R.string.error_response_default));
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

        _requestQueue.add(searchUsersRequest);

        JsonObjectRequest getFriendsRequest = new JsonObjectRequest(getString(R.string.base_api_url) + "/friends/getfriends", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray friends = response.optJSONArray("Friends");

                TextView friendsCountText = (TextView) findViewById(R.id.text_friends_count);

                if (friends != null && friends.length() > 0) {
                    friendsCountText.setText(getString(R.string.friend_count, friends.length()));
                    friendsCountText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent friendsList = new Intent(getContext(), FriendsListActivity.class);
                            startActivity(friendsList);
                        }
                    });
                }
                else {
                    friendsCountText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent friendsList = new Intent(getContext(), FriendSearchActivity.class);
                            startActivity(friendsList);
                        }
                    });
                }
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

        _requestQueue.add(getFriendsRequest);

        _view = inflater.inflate(R.layout.fragment_account, container, false);

        ImageView signoutButton = _view.findViewById(R.id.button_sign_out);

        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getContext(), SignOutActivity.class);
                startActivity(loginIntent);
            }
        });

        ImageView addFriendsImage = _view.findViewById(R.id.image_add_friends);

        addFriendsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchFriends = new Intent(getContext(), FriendSearchActivity.class);
                startActivity(searchFriends);
            }
        });

        ImageView profilePicture = (ImageView) findViewById(R.id.image_profile_picture);

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectProfilePicture = new Intent();
                selectProfilePicture.setType("image/*");
                selectProfilePicture.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(selectProfilePicture, "Select Picture"), ActivityRequestCodes.PICK_IMAGE);
            }
        });

        if (user.getProfileImageUrl() != null) {

            _requestQueue.add(new ImageRequest(user.getProfileImageUrl(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    try {
                        String profilePicturePath = getActivity().getFilesDir() + "/profile_image.jpg";
                        FileOutputStream outputStream = new FileOutputStream(new File(profilePicturePath));
                        response.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), profilePicturePath);
                        roundedBitmapDrawable.setCornerRadius(300);

                        ((ImageView) findViewById(R.id.image_profile_picture)).setImageDrawable(roundedBitmapDrawable);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 300, 300, ImageView.ScaleType.FIT_CENTER, null,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    })
            );
        }

        TextView username = (TextView) findViewById(R.id.text_user_name);

        username.setText(user.getName());

        return _view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ActivityRequestCodes.PICK_IMAGE:
                try {
                    InputStream imageStream = getActivity().getContentResolver().openInputStream(intent.getData());

                    Bitmap image = BitmapFactory.decodeStream(imageStream);
                    Bitmap croppedImage;

                    int width = image.getWidth();
                    int height = image.getHeight();

                    if (width > height) {
                        croppedImage = Bitmap.createBitmap(image, image.getWidth() / 2 - image.getHeight() / 2, 0, image.getHeight(), image.getHeight());
                    } else {
                        croppedImage = Bitmap.createBitmap(image, 0, image.getHeight() / 2 - image.getWidth() / 2, image.getWidth(), image.getWidth());
                    }

                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(croppedImage, 300, 300, false);

                    String profilePicturePath = getActivity().getFilesDir() + "/profile_image.jpg";

                    FileOutputStream outputStream = new FileOutputStream(new File(profilePicturePath));

                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

                    User user = _db.getUser();

                    new UploadImage().execute(profilePicturePath, "user-images", "profile-picture/" + user.getId() + ".jpg");

                    ImageView profilePicture = (ImageView) findViewById(R.id.image_profile_picture);

                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), profilePicturePath);
                    roundedBitmapDrawable.setCornerRadius(400);

                    profilePicture.setImageDrawable(roundedBitmapDrawable);

                    user.setProfileImagePath(profilePicturePath);

                    _db.updateUser(user);

                    JSONObject requestData = new JSONObject();

                    try {
                        requestData.put("ProfilePictureUrl", getActivity()
                                .getString(R.string.azure_storage_base_url) + "/user-images/profile-picture/" + user.getId() + ".jpg");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    _requestQueue.add(new JsonObjectRequest(Request.Method.PUT, getString(R.string.base_api_url) + "/account/userprofile", requestData,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                    _toast.showError(_context.getString(R.string.error_response_default));
                                }
                            })
                            {
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    String token = _db.getUserToken("RecycleGrid");

                                    Map<String, String> headers = new HashMap<String, String>();
                                    headers.put("Authorization", "Bearer " + token);

                                    return headers;
                                }
                            }
                    );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        _db.close();
        super.onDestroy();
    }

    private View findViewById(int id) {
        return _view.findViewById(id);
    }

    private class UploadImage extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            AzureStorageFileUploader uploader = new AzureStorageFileUploader(getString(R.string.azure_storage_connection_string));
            uploader.uploadFile(params[0], params[1], params[2]);

            return null;
        }
    }
}
