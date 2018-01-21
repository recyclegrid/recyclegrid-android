package com.recyclegrid.database;

import android.database.Cursor;
import android.provider.BaseColumns;

public class Token {

    private  long _id;
    private String _tokenProvider;
    private String _tokenValue;

    public Token() {}

    public Token(String tokenProvider, String tokenValue) {
        _tokenProvider = tokenProvider;
        _tokenValue = tokenValue;
    }

    public void fromCursor(Cursor cursor) {
        _id = cursor.getLong(cursor.getColumnIndexOrThrow(Table.Fields._ID));
        _tokenProvider = cursor.getString(cursor.getColumnIndexOrThrow(Table.Fields.TOKEN_PROVIDER));
        _tokenValue = cursor.getString(cursor.getColumnIndexOrThrow(Table.Fields.TOKEN_VALUE));
    }

    public long getId(){ return _id; }
    public String getProvider(){ return _tokenProvider; }
    public String getValue(){ return _tokenValue; }

    public static class Table {
        public static String NAME = "Tokens";

        public static class Fields implements BaseColumns {
            public static String TOKEN_PROVIDER = "TokenProvider";
            public static String TOKEN_VALUE = "TokenValue";
        }
    }
}
