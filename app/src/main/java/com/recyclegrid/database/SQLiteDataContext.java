package com.recyclegrid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDataContext extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 1001;
    private static String DATABASE_NAME = "RecycleGrid.App.db";

    public SQLiteDataContext(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Scripts.CREATE_USERDATA_TABLE);
        db.execSQL(Scripts.CREATE_TOKENS_TABLE);
        db.execSQL(Scripts.CREATE_CHECKINS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public User getUser() {
        User user = null;

        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                User.Table.Fields._ID,
                User.Table.Fields.USER_NAME,
                User.Table.Fields.PROFILE_PICTURE_URL
        };

        Cursor cursor = db.query(User.Table.NAME, projection, null, null, null, null, null);

        if (cursor.moveToNext()) {
            user = new User();
            user.fromCursor(cursor);
        }

        return user;
    }

    public void createUser(User user){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(User.Table.Fields._ID, user.getId());
        values.put(User.Table.Fields.USER_NAME, user.getName());
        values.put(User.Table.Fields.PROFILE_PICTURE_URL, user.getProfileImageUrl());

        db.insert(User.Table.NAME, null, values);
    }

    public void updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(User.Table.Fields.PROFILE_PICTURE_URL, user.getProfileImageUrl());
        values.put(User.Table.Fields.USER_NAME, user.getName());

        String selection = User.Table.Fields._ID + "=";

        int count = db.update(
                User.Table.NAME,
                values,
                User.Table.Fields._ID + "=" + user.getId(),
                null
        );
    }

    public void deleteUser(){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(User.Table.NAME, null, null);
    }

    public void addToken(Token token){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Token.Table.Fields.TOKEN_PROVIDER, token.getProvider());
        values.put(Token.Table.Fields.TOKEN_VALUE, token.getValue());

        db.insert(Token.Table.NAME, null, values);
    }

    public String getUserToken(String providerName) {
        Token token = null;

        SQLiteDatabase db = getReadableDatabase();

        String[] projection = { Token.Table.Fields._ID, Token.Table.Fields.TOKEN_PROVIDER, Token.Table.Fields.TOKEN_VALUE };
        String selction = Token.Table.Fields.TOKEN_PROVIDER + " = ?";
        String[] queryArgs = { providerName };

        Cursor cursor = db.query(Token.Table.NAME, projection, selction, queryArgs, null, null, null);

        if (cursor.moveToNext()) {
            token = new Token();
            token.fromCursor(cursor);
        }

        return token.getValue();
    }

    public void deleteUserToken(){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(Token.Table.NAME, null, null);
    }

    public void addCheckIn(CheckIn checkIn){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CheckIn.Table.Fields._ID, checkIn.getId());
        values.put(CheckIn.Table.Fields.USER_NAME, checkIn.getName());
        values.put(CheckIn.Table.Fields.PROFILE_PICTURE_URL, checkIn.getProfileImageUrl());
        values.put(CheckIn.Table.Fields.VENUE_NAME, checkIn.getVenueName());
        values.put(CheckIn.Table.Fields.RECYCLE_MATERIAL, checkIn.getRecycleMaterial());
        values.put(CheckIn.Table.Fields.DATE, checkIn.getCheckInDate().getTime());

        db.insert(CheckIn.Table.NAME, null, values);
    }

    public List<CheckIn> getCheckIns() {
        List<CheckIn> checkIns = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                CheckIn.Table.Fields._ID,
                CheckIn.Table.Fields.USER_NAME,
                CheckIn.Table.Fields.PROFILE_PICTURE_URL,
                CheckIn.Table.Fields.VENUE_NAME,
                CheckIn.Table.Fields.RECYCLE_MATERIAL,
                CheckIn.Table.Fields.DATE
        };

        Cursor cursor = db.query(CheckIn.Table.NAME, projection, null, null, null, null, CheckIn.Table.Fields.DATE + " DESC");

        while (cursor.moveToNext()) {
            CheckIn checkIn = new CheckIn();
            checkIn.fromCursor(cursor);
            checkIns.add(checkIn);
        }

        return checkIns;
    }

    private static class Scripts {
        public static String CREATE_USERDATA_TABLE =
            "CREATE TABLE " + User.Table.NAME + " (" +
                User.Table.Fields._ID + " INTEGER PRIMARY KEY," +
                User.Table.Fields.USER_NAME + " TEXT," +
                User.Table.Fields.PROFILE_PICTURE_URL + " TEXT)";

        public static String CREATE_TOKENS_TABLE =
                "CREATE TABLE " + Token.Table.NAME + " (" +
                        Token.Table.Fields._ID + " INTEGER PRIMARY KEY," +
                        Token.Table.Fields.TOKEN_PROVIDER + " TEXT," +
                        Token.Table.Fields.TOKEN_VALUE + " TEXT)";

        public static String CREATE_CHECKINS_TABLE =
                "CREATE TABLE " + CheckIn.Table.NAME + " (" +
                        CheckIn.Table.Fields._ID + " INTEGER PRIMARY KEY," +
                        CheckIn.Table.Fields.USER_NAME + " TEXT," +
                        CheckIn.Table.Fields.PROFILE_PICTURE_URL + " TEXT," +
                        CheckIn.Table.Fields.VENUE_NAME + " TEXT," +
                        CheckIn.Table.Fields.RECYCLE_MATERIAL + " INTEGER," +
                        CheckIn.Table.Fields.DATE + " INTEGER)";
    }
}
