package com.nam.vngnearbyplaces;

/**
 * Created by Nam on 28/12/2015.
 */

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceJSONParser {

    StringBuilder sb;
    SQLiteDatabase db;
    String currentType;

    public PlaceJSONParser(SQLiteDatabase db, String type) {
        this.db = db;
        this.currentType = type;
    }

    /**
     * Receives a JSONObject and returns a list
     */
    public List<HashMap<String, String>> parse(JSONObject jObject, StringBuilder distanceRequest) {

        this.sb = distanceRequest;

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for (int i = 0; i < placesCount; i++) {
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject) jPlaces.get(i), i);
                placesList.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    /**
     * Parsing the Place JSON object
     */
    private HashMap<String, String> getPlace(JSONObject jPlace, int index) {

        HashMap<String, String> place = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String icon = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {
            // Extracting Place name, if available
            if (!jPlace.isNull("name")) {
                placeName = jPlace.getString("name");
            }

            // Extracting Place Vicinity, if available
            if (!jPlace.isNull("vicinity")) {
                vicinity = jPlace.getString("vicinity");
            }

            // Extracting Icon, if available
            if (!jPlace.isNull("icon")) {
                icon = jPlace.getString("icon");
            }

            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
            sb.append(latitude + "," + longitude + "|");

            reference = jPlace.getString("reference");

            place.put("place_name", placeName);
            place.put("vicinity", vicinity);
            place.put("icon", icon);
            place.put("lat", latitude);
            place.put("lng", longitude);
            place.put("reference", reference);

            //TODO: Put place's info to database
            if (MainActivity.storingInfo) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(LocationReaderDbHelper.LocationEntry.COLUMN_ENTRY_ID, index);
                contentValues.put(LocationReaderDbHelper.LocationEntry.COLUMN_NAME, placeName);
                contentValues.put(LocationReaderDbHelper.LocationEntry.COLUMN_ICON, icon);
                contentValues.put(LocationReaderDbHelper.LocationEntry.COLUMN_TYPE, currentType);
                contentValues.put(LocationReaderDbHelper.LocationEntry.COLUMN_LAT, latitude);
                contentValues.put(LocationReaderDbHelper.LocationEntry.COLUMN_LNG, longitude);
                contentValues.put(LocationReaderDbHelper.LocationEntry.COLUMN_REFERENCE, reference);
                db.insert("location", LocationReaderDbHelper.LocationEntry.COLUMN_NULLABLE, contentValues);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}
