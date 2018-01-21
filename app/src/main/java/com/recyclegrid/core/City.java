package com.recyclegrid.core;

public class City {
    private long _id;
    private String _name;

    public City(long id, String name) {
        _id = id;
        _name = name;
    }

    public String getName(){ return _name; }
    public long getId(){ return _id; }

    @Override
    public String toString() { return _name; }
}
