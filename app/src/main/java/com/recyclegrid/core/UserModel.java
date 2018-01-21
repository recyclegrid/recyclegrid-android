package com.recyclegrid.core;

public class UserModel {
    private long _id;
    private String _name;
    private String _profilePictureUrl;


    public UserModel(long id, String name, String profilePictureUrl) {
        _id = id;
        _name = name;
        _profilePictureUrl = profilePictureUrl;
    }

    public long getId(){ return _id; }
    public String getName(){ return _name; }
    public String getProfilePictureUrl(){ return _profilePictureUrl; }
}
