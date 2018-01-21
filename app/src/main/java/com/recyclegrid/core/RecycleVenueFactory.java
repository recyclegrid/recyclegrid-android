package com.recyclegrid.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecycleVenueFactory {
    public static RecycleVenue fromJson(JSONObject jsonObject) {

        RecycleVenue venue = null;
        try {
            venue = new RecycleVenue(
                    jsonObject.getLong("Id"),
                    jsonObject.getString("Name"),
                    jsonObject.getString("Address"),
                    jsonObject.getInt("AcceptedMaterial"),
                    jsonObject.getDouble("Latitude"),
                    jsonObject.getDouble("Longitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return venue;
    }

    public static ArrayList<RecycleVenue> fromJsonArray(JSONArray jsonArray) {

        ArrayList<RecycleVenue> venues = new ArrayList<RecycleVenue>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                venues.add(fromJson(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return venues;
    }
}
