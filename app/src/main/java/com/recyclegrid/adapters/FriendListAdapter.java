package com.recyclegrid.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.recyclegrid.app.R;
import com.recyclegrid.core.UserModel;
import com.recyclegrid.database.SQLiteDataContext;

import java.util.List;

public class FriendListAdapter extends ArrayAdapter<UserModel> {
    private LayoutInflater _inflater;
    private RequestQueue _requests;
    private ImageLoader _imageLoader;
    private SQLiteDataContext _db;

    public FriendListAdapter(Context context, List<UserModel> users) {
        super(context, R.layout.venues_list_item, users);

        _db = new SQLiteDataContext(context);

        _inflater = LayoutInflater.from(context);

        _requests = Volley.newRequestQueue(context);

        _imageLoader = new ImageLoader(_requests, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(10);
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.list_item_friends, parent, false);
        }

        UserModel userModel = getItem(position);

        TextView username = convertView.findViewById(R.id.text_friend_name);
        username.setText(userModel.getName());

        NetworkImageView userProfileImage = convertView.findViewById(R.id.network_image_user);
        userProfileImage.setDefaultImageResId(R.drawable.ic_account_circle_white_24dp);

        if (userModel.getProfilePictureUrl() != null) {
            userProfileImage.setImageUrl(userModel.getProfilePictureUrl(), _imageLoader);
        }

        return convertView;
    }
}
