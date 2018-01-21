package com.recyclegrid.database;

import android.database.Cursor;
import android.provider.BaseColumns;

public class User {

    private  long _id;
    private String _name;
    private String _profileImageUrl;

    public User() {}

    public User(long id, String name, String profileImagePath) {
        _id = id;
        _name = name;
        _profileImageUrl = profileImagePath;
    }

    public void fromCursor(Cursor cursor) {
        _id = cursor.getLong(cursor.getColumnIndexOrThrow(Table.Fields._ID));
        _name = cursor.getString(cursor.getColumnIndexOrThrow(Table.Fields.USER_NAME));
        _profileImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(Table.Fields.PROFILE_PICTURE_URL));
    }

    public long getId(){ return _id; }
    public String getName(){ return _name; }
    public String getProfileImageUrl(){ return _profileImageUrl; }

    public void setProfileImagePath(String path) {
        _profileImageUrl = path;
    }

    public static class Table {
        public static String NAME = "User";

        public static class Fields implements BaseColumns {
            public static String USER_NAME = "Name";
            public static String PROFILE_PICTURE_URL = "ProfileImageUrl";
        }
    }
}
