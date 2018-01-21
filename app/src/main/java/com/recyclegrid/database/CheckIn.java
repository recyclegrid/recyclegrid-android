package com.recyclegrid.database;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.recyclegrid.core.RecycleMaterial;

import java.util.Date;

public class CheckIn {

    private  long _id;
    private String _username;
    private String _venueName;
    private String _profileImageUrl;
    private Date _date;
    private int _recycleMaterial;

    public CheckIn() {
        _date = new Date(0);
    }

    public CheckIn(long id, String username, String profileImagePath, String venueName, Date date, int material) {
        _id = id;
        _username = username;
        _profileImageUrl = profileImagePath;
        _venueName = venueName;
        _date = date;
        _recycleMaterial = material;
    }

    public void fromCursor(Cursor cursor) {
        _id = cursor.getLong(cursor.getColumnIndexOrThrow(Table.Fields._ID));
        _username = cursor.getString(cursor.getColumnIndexOrThrow(Table.Fields.USER_NAME));
        _profileImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(Table.Fields.PROFILE_PICTURE_URL));
        _venueName = cursor.getString(cursor.getColumnIndexOrThrow(Table.Fields.VENUE_NAME));
        _date = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(Table.Fields.DATE)));
        _recycleMaterial = cursor.getInt(cursor.getColumnIndexOrThrow(Table.Fields.RECYCLE_MATERIAL));
    }

    public long getId(){ return _id; }
    public String getName(){ return _username; }
    public String getProfileImageUrl(){ return _profileImageUrl; }
    public String getVenueName(){ return _venueName; }
    public Date getCheckInDate(){ return _date; }
    public int getRecycleMaterial(){ return _recycleMaterial; }

    public static class Table {
        public static String NAME = "CheckIns";

        public static class Fields implements BaseColumns {
            public static String USER_NAME = "Username";
            public static String VENUE_NAME = "VenueName";
            public static String PROFILE_PICTURE_URL = "ProfileImageUrl";
            public static String DATE = "CheckInDate";
            public static String RECYCLE_MATERIAL = "RecycleMaterial";
        }
    }
}
