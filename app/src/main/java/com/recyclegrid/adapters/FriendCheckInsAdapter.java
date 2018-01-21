package com.recyclegrid.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.recyclegrid.app.R;
import com.recyclegrid.core.RecycleMaterial;
import com.recyclegrid.core.TimeSpanHumanizer;
import com.recyclegrid.database.CheckIn;
import com.recyclegrid.database.SQLiteDataContext;

import java.util.List;

public class FriendCheckInsAdapter extends BaseAdapter {
    private LayoutInflater _inflater;

    private RequestQueue _requests;
    private ImageLoader _imageLoader;
    private TimeSpanHumanizer _timespanHumanizer;
    private SQLiteDataContext _db;
    private List<CheckIn> _checkIns;

    public FriendCheckInsAdapter(Context context) {
        _inflater = LayoutInflater.from(context);
        _timespanHumanizer = new TimeSpanHumanizer(context);
        _db = new SQLiteDataContext(context);

        _checkIns = _db.getCheckIns();

        if (_checkIns.size() >= 5) {
            _checkIns.add(5, null);
        }

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
    public void notifyDataSetChanged(){
        _checkIns = _db.getCheckIns();

        if (_checkIns.size() >= 5) {
            _checkIns.add(5, null);
        }

        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return _checkIns.size();
    }

    @Override
    public Object getItem(int i) {
        return _checkIns.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CheckIn checkIn = (CheckIn)getItem(position);

        if (checkIn == null) {
            convertView = _inflater.inflate(R.layout.list_item_friend_checkins_ad, parent, false);

            NativeExpressAdView adView = convertView.findViewById(R.id.checkins_list_item_ad);

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
        else {
            convertView = _inflater.inflate(R.layout.list_item_friend_checkins, parent, false);

            TextView username = convertView.findViewById(R.id.text_user_name);
            username.setText(checkIn.getName());

            TextView checkinDetails = convertView.findViewById(R.id.text_checkin_details);
            checkinDetails.setText(checkIn.getVenueName());

            TextView checkinDate = convertView.findViewById(R.id.text_checkin_date);
            checkinDate.setText(_timespanHumanizer.Humanize(checkIn.getCheckInDate().getTime()));

            NetworkImageView userProfileImage = convertView.findViewById(R.id.network_image_user);
            userProfileImage.setDefaultImageResId(R.drawable.ic_account_circle_white_24dp);

            ImageView recycleMaterialIcon = convertView.findViewById(R.id.image_check_in_material_icon);

            if (checkIn.getRecycleMaterial() == RecycleMaterial.PLASTICS) {
                recycleMaterialIcon.setImageResource(R.drawable.ic_bottle_white_blue_circle_36dp);
            }

            if (checkIn.getProfileImageUrl() != null) {
                userProfileImage.setImageUrl(checkIn.getProfileImageUrl(), _imageLoader);
            }
        }

        return convertView;
    }
}
