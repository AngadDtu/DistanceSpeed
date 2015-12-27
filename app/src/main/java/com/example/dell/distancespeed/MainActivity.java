package com.example.dell.distancespeed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private TextView speed1, distance;
    private LocationManager locationManager;
    private String provider;
    int time=5;
    double speed;
    double distance_travelled;
    double latitude_prev = 0;
    double longitude_prev = 0;
    static double total_dist = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speed1 = (TextView) findViewById(R.id.txt_speed);
        distance = (TextView) findViewById(R.id.distance);

        checkGps();

        getCurrentLocation();

    }

    private void getCurrentLocation() {
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        Toast.makeText(MainActivity.this, String.valueOf(location), Toast.LENGTH_SHORT).show();


        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,500.0f, (LocationListener) location);


        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            speed1.setText("0");
            distance.setText("0");
        }
    }

    private void checkGps() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Toast.makeText(MainActivity.this, String.valueOf(enabled), Toast.LENGTH_SHORT).show();

        // Check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            showGpsSettingDialog();
        }
    }

    private void showGpsSettingDialog() {
          /*Create a dialog to tell user to enable his GPS settings to pinpoint his or her location*/
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Settings para sa lokasyon"); /*should be on a string values*/

        alertDialog
                .setMessage("Your GPS is not active, do you wish to setup your GPS?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog gpsSettingsDialog = alertDialog.create();
        gpsSettingsDialog.show();
    }


    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 1, this);
    }


    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
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
        // Toast.makeText(this, "getting location", Toast.LENGTH_SHORT).show();
        String cityName=null;
        Geocoder gcd = new Geocoder(getBaseContext(),
                Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location
                    .getLongitude(), 1);
            if (addresses.size() > 0)
                System.out.println(addresses.get(0).getLocality());
            cityName=addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  Toast.makeText(this,cityName,Toast.LENGTH_SHORT).show();
        setTitle(cityName);
        getLocation(location);

    }

    private void getLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        Log.v("The current Latitude", String.valueOf(lat));
        Log.v("The current Longitude", String.valueOf(lon));

        Log.v("The previous Latitude", String.valueOf(latitude_prev));
        Log.v("The previous Longitude", String.valueOf(longitude_prev));

        if(latitude_prev==0&&longitude_prev==0){
            latitude_prev = lat;
            longitude_prev = lon;
            distance_travelled = 0;
            speed=0;
        }else{
            /*get the distance covered from point A to point B*/
            distance_travelled = distanceTravelled(latitude_prev, longitude_prev, lat, lon);
            /*set previous latitude and longitude to the last location*/
            DecimalFormat form = new DecimalFormat("0.000");
            DecimalFormat form1 = new DecimalFormat("0.00");
            latitude_prev = lat;
            longitude_prev = lon;
            speed=distance_travelled/time;
            // latitude.setText(String.valueOf(lat));
            //longitude.setText(String.valueOf(lon));
            speed1.setText(form1.format(speed)+" Km/hr");
            distance.setText(form.format(distance_travelled)+" Km");
        }
    }

    private double distanceTravelled(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist =  earthRadius * c;


        total_dist = total_dist + dist;

            /*just in case it is needed to be converted in meters use this part*/
            /*int meterConversion = 1000;
            return Double.valueOf(dist * meterConversion);*/

        return total_dist;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,Toast.LENGTH_SHORT).show();
    }
}
