package com.recyclegrid.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.app.R;
import com.recyclegrid.app.Toast;
import com.recyclegrid.core.UserModel;
import com.recyclegrid.database.SQLiteDataContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendSearchListAdapter extends ArrayAdapter<UserModel> {
    private LayoutInflater _inflater;
    private RequestQueue _requests;
    private ImageLoader _imageLoader;
    private SQLiteDataContext _db;
    private Toast _toast;
    private Context _context;

    public FriendSearchListAdapter(Context context, List<UserModel> users) {
        super(context, R.layout.venues_list_item, users);

        _toast = new Toast((AppCompatActivity) context);
        _context = context;

        _db = new SQLiteDataContext(context);
        _inflater = LayoutInflater.from(context);
        _requests = Volley.newRequestQueue(context);

        _imageLoader = new ImageLoader(_requests, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.list_item_friends_search, parent, false);
        }

        UserModel userModel = getItem(position);

        TextView username = convertView.findViewById(R.id.text_friend_name);
        username.setText(userModel.getName());

        NetworkImageView userProfileImage = convertView.findViewById(R.id.network_image_user);
        userProfileImage.setDefaultImageResId(R.drawable.ic_account_circle_white_24dp);

        if (userModel.getProfilePictureUrl() != null) {
            userProfileImage.setImageUrl(userModel.getProfilePictureUrl(), _imageLoader);
        }

        final long userId = userModel.getId();

        ImageView sendFriendRequestImage = convertView.findViewById(R.id.image_send_friend_request);
        sendFriendRequestImage.setTag(userModel);
        sendFriendRequestImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = v;

                String url = getContext().getString(R.string.base_api_url) + "/friends/sendrequest";

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        _toast.showInformation(R.string.friend_request_sent);
                        view.setAlpha(0.1f);
                        view.setOnClickListener(null);
                    }
                };

                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        _toast.showError(R.string.error_response_default);
                        error.printStackTrace();
                    }
                };

                StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
                    @Override
                    public Map<String, String> getHeaders(){
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Authorization", "Bearer " + _db.getUserToken("RecycleGrid"));

                        return headers;
                    }

                    @Override
                    public Map<String, String> getParams(){
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("friendUserId", Long.toString(userId));

                        return params;
                    }
                };

                _requests.add(request);
            }
        });

        return convertView;
    }
}
