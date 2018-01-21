package com.recyclegrid.core;

public class FriendRequestModel {
    private long _id;
    private long _userId;
    private String _name;
    private String _profilePictureUrl;


    public FriendRequestModel(long id, long userId, String name, String profilePictureUrl) {
        _id = id;
        _userId = userId;
        _name = name;
        _profilePictureUrl = profilePictureUrl;
    }

    public long getId(){ return _id; }
    public long getUserId(){ return _userId; }
    public String getName(){ return _name; }
    public String getProfilePictureUrl(){ return _profilePictureUrl; }
}
