package com.nam.vngnearbyplaces;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LinearLayoutManager layoutManager;

    public boolean canStoreUserLocation = false;
    public boolean hasOldLocation = false;
    public boolean isNeedToGetInfo = false;
    public static boolean storingInfo = false;

    LocationReaderDbHelper dbHelper;

    RecyclerView recyclerView;
    Spinner mSprPlaceType;
    String[] mPlaceType = null;
    String[] mPlaceTypeName = null;
    TextView tvWarning;

    public static double mLatitude = 0;
    public static double mLongitude = 0;

    public static double mLatitudeOld = 0;
    public static double mLongitudeOld = 0;

    LocationManager locationManager;

    boolean isGPSEnabled, isNetworkEnabled, canGetLocation;

    public static boolean isOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        new AlertDialog.Builder(this)
                .setTitle("Store Your Location")
                .setMessage("Let we help you save mobile data by saving your location. This means " +
                        "next time you start your app, if you still stay at the last location" +
                        "you run the app, you will not have to load information from Google again")
                .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        canStoreUserLocation = true;
                        SharedPreferences settings = getBaseContext().getSharedPreferences("com.nam.vngnearbyplaces.oldlocation", 0);
                        String oldLocationStr = settings.getString("Location", "none");
                        if (!oldLocationStr.equals("none")) {
                            hasOldLocation = true;
                            String[] values = oldLocationStr.split("\\+");
                            mLatitudeOld = Double.parseDouble(values[0]);
                            mLongitudeOld = Double.parseDouble(values[1]);
                        } else isNeedToGetInfo = true;
                    }
                })
                .setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        canStoreUserLocation = false;
                        hasOldLocation = false;
                        isNeedToGetInfo = true;
                    }
                }).show();
//                .setIcon(android.R.drawable.ic_dialog_alert);

//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.show();

        layoutManager = new LinearLayoutManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(layoutManager);

        isOnline = isOnline();
        tvWarning = (TextView) findViewById(R.id.tvWarning);
        if (isOnline)
            tvWarning.setVisibility(View.GONE);

        // Array of place types
        mPlaceType = getResources().getStringArray(R.array.place_type);

        // Array of place type names
        mPlaceTypeName = getResources().getStringArray(R.array.place_type_name);

        // Creating an array adapter with an array of Place types
        // to populate the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mPlaceTypeName);

        // Getting reference to the Spinner
        mSprPlaceType = (Spinner) findViewById(R.id.spr_place_type);

        // Setting adapter on Spinner to set place types
        mSprPlaceType.setAdapter(adapter);

        Button btnFind;

        // Getting reference to Find Button
        btnFind = (Button) findViewById(R.id.btn_find);

        Button btnClrCache;

        btnClrCache = (Button) findViewById(R.id.btn_clrCache);

        Button btnClrDB;

        btnClrDB = (Button) findViewById(R.id.btn_ClrDB);

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        } else { // Google Play Services are available

//            // Getting LocationManager object from System Service LOCATION_SERVICE
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);
//
//            // Getting Current Location From GPS
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

//            if (location != null) {
//                onLocationChanged(location);
//            }

            locationManager.requestLocationUpdates(provider, 20000, 0, this);

            //Setting clear cache button
            btnClrCache.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences settings = getBaseContext().getSharedPreferences("com.nam.vngnearbyplaces.oldlocation", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove("Location");
                    editor.commit();
                }
            });

            btnClrDB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File dbFile = getBaseContext().getDatabasePath("Location.db");
                    if (dbFile.exists())
                        dbFile.delete();
                }
            });

            // Setting click event lister for the find button
            btnFind.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

//                    if (!isOnline()) {
//                        Toast.makeText(getBaseContext(),"No Internet connection, please connect " +
//                                "to Wifi network or turn on Mobile data to load non-stored places",
//                                Toast.LENGTH_LONG).show();
//                        return;
//                    }

                    //Get current device's location
                    getLocation();

                    int selectedPosition = mSprPlaceType.getSelectedItemPosition();
                    String type = mPlaceType[selectedPosition];

                    if (!canStoreUserLocation) {

                        buildRequest(type);
                    } else {
                        // Compare
                        if (hasOldLocation) {
                            if (Utilities.distance(mLatitude, mLongitude, mLatitudeOld, mLongitudeOld) > 3) // 3 Km around old location
                                isNeedToGetInfo = true;
                            else isNeedToGetInfo = false;
                        }

                        //Check if db exists
                        File dbFile = getBaseContext().getDatabasePath("Location.db");
                        if (!dbFile.exists())
                            isNeedToGetInfo = true;

                        if (isNeedToGetInfo) {

                            storingInfo = true;
                            hasOldLocation = true;

                            dbHelper = new LocationReaderDbHelper(getBaseContext());

                            //TODO: Drop table with old latitude & longitude
                            DropOldTableTask dropOldTableTask = new DropOldTableTask();
                            dropOldTableTask.execute(mLatitudeOld + "+" + mLongitudeOld);

                            mLatitudeOld = mLatitude;
                            mLongitudeOld = mLongitude;

                            buildRequest(type);
                        } else {

                            dbHelper = new LocationReaderDbHelper(getBaseContext());

                            ReadDBTask readDBTask = new ReadDBTask(type);
                            readDBTask.execute();
                        }
                    }

                }
            });
        }
    }

    /**
     * Build request and parse response
     */
    private void buildRequest(String type) {

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&radius=5000");
        sb.append("&types=" + type);
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyBmRQnYZWHtRuKNN8YGPWaiSFeBJq7asDs");

        // Creating a new non-ui thread task to download Google place json data
        PlacesTask placesTask = new PlacesTask(type);

        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
    }

    /**
     * A class, to download Google Places
     */
    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;
        String type;

        public PlacesTask(String type) {
            this.type = type;
        }

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask(type);

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;
        String type;

        StringBuilder distanceRequest = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?"
                + "origins=" + MainActivity.mLatitude + "," + MainActivity.mLongitude
                + "&destinations=");

        public ParserTask(String type) {
            this.type = type;
        }

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            SQLiteDatabase db = null;
            if (MainActivity.storingInfo)
                db = dbHelper.getWritableDatabase();

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser(db, type);

            try {
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject, distanceRequest);

                if (storingInfo)
                    db.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            distanceRequest.deleteCharAt(distanceRequest.length() - 1);
            distanceRequest.append("&key=AIzaSyBmRQnYZWHtRuKNN8YGPWaiSFeBJq7asDs");
            GetDistanceTask getDistanceTask = new GetDistanceTask(list, type);
            getDistanceTask.execute(distanceRequest);
        }
    }

    /**
     * A class to get distance between current location and places found
     */
    private class GetDistanceTask extends AsyncTask<StringBuilder, Void, String> {
        List<HashMap<String, String>> listPlaces;
        List<String> listDistance = new ArrayList<>();
        SQLiteDatabase db;
        String type;

        public GetDistanceTask(List<HashMap<String, String>> list, String type) {
            this.listPlaces = list;
            this.type = type;
        }

        @Override
        protected String doInBackground(StringBuilder... sb) {
            String data = "";
            try {
                data = downloadUrl(sb[0].toString());
                if (storingInfo)
                    db = dbHelper.getReadableDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(data);
                JSONArray rows = jsonObject.getJSONArray("rows");
//                JSONArray destinations = jsonObject.getJSONArray("destinations");
                JSONObject elements = rows.getJSONObject(0);
                JSONArray elementArray = elements.getJSONArray("elements");
                for (int i = 0; i < elementArray.length(); i++) {
                    JSONObject distances = elementArray.getJSONObject(i);
                    JSONObject distance = distances.getJSONObject("distance");
                    String strDistance = distance.getString("text");
                    this.listDistance.add(strDistance);

                    if (storingInfo) {
                        HashMap<String, String> place = listPlaces.get(i);
                        String lat = place.get("lat");
                        String lng = place.get("lng");
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(LocationReaderDbHelper.LocationEntry.COLUMN_DISTANCE, strDistance);
                        db.update("location", contentValues, "lat = '" + lat + "'"
                                + " AND lng = '" + lng + "'", null);
                    }
                }

                if (storingInfo)
                    db.close();

                PlaceAdapter placeAdapter = new PlaceAdapter(listPlaces, listDistance, type);
                recyclerView.setAdapter(placeAdapter);
                placeAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //TODO: delete old entries
    private class DropOldTableTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("location", null, null);
            return null;
        }
    }

    private class ReadDBTask extends AsyncTask<Void, Void, Void> {
        List<HashMap<String, String>> listPlaces = new ArrayList<>();
        List<String> listDistance = new ArrayList<>();
        SQLiteDatabase db;
        String type;
        String[] projection = {
                LocationReaderDbHelper.LocationEntry.COLUMN_NAME,
                LocationReaderDbHelper.LocationEntry.COLUMN_ICON,
                LocationReaderDbHelper.LocationEntry.COLUMN_LAT,
                LocationReaderDbHelper.LocationEntry.COLUMN_LNG,
                LocationReaderDbHelper.LocationEntry.COLUMN_REFERENCE,
                LocationReaderDbHelper.LocationEntry.COLUMN_DISTANCE
        };

        public ReadDBTask(String type) {
            this.type = type;
        }

        @Override
        protected Void doInBackground(Void... params) {
            db = dbHelper.getReadableDatabase();

            Cursor c = db.query("location", projection,
                    LocationReaderDbHelper.LocationEntry.COLUMN_TYPE + " = '" + type + "'",
                    null, null, null, null);

            if (c.moveToFirst()) {
                do {
                    HashMap<String, String> place = new HashMap<>();
                    place.put("place_name", c.getString(0));
                    place.put("icon", c.getString(1));
                    place.put("lat", c.getString(2));
                    place.put("lng", c.getString(3));
                    place.put("reference", c.getString(4));
                    listPlaces.add(place);
                    listDistance.add(c.getString(5));
                } while (c.moveToNext());
            }

            c.close();
            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (listDistance.isEmpty()) {
                storingInfo = true;
                hasOldLocation = true;

                mLatitudeOld = mLatitude;
                mLongitudeOld = mLongitude;

                int selectedPosition = mSprPlaceType.getSelectedItemPosition();
                String type = mPlaceType[selectedPosition];

                buildRequest(type);
            } else {
                PlaceAdapter placeAdapter = new PlaceAdapter(listPlaces, listDistance, type);
                recyclerView.setAdapter(placeAdapter);
                placeAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Get current device's location
     */
    public void getLocation() {
        Location location = null;
        try {
//            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 20000, 0, this);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            mLatitude = location.getLatitude();
                            mLongitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, 20000, 0, this);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                mLatitude = location.getLatitude();
                                mLongitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to download json data from url
     */
    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (UnknownHostException e) {
//            Log.d("Exception while downloading url", e.toString());
        } finally {
            closeQuietly(iStream);
//            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    protected void closeQuietly(InputStream resource) {
        try {
            resource.close();
        } catch (Exception ex) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "No Internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Function to check whether there is Internet Connection
     *
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getBaseContext().getSharedPreferences("com.nam.vngnearbyplaces.oldlocation", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Location", mLatitudeOld + "+" + mLongitudeOld);
        editor.commit();
        if (dbHelper != null)
            dbHelper.close();
    }
}