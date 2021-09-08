package com.example.lab2gps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lab2gps.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, OnNmeaMessageListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Marker myMarker;
    private Marker newMarker;
    private Double distance;
    private boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
           ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
        locationManager.addNmeaListener(this,null);

        binding.calcVal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = true;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        LatLng myloc = new LatLng(location.getLatitude(), location.getLongitude());
        myMarker = mMap.addMarker(new MarkerOptions().position(myloc).title("My location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(myloc));
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {

        String[] mess = message.split(",");
        String tmp = mess[0].substring(3,6);
        if ("GGA".equals(tmp)){
            binding.szVal.setText(mess[2].toString() + mess[3].toString());
            binding.dlVal.setText(mess[4].toString() + mess[5].toString());
            binding.wyVal.setText(mess[9].toString());
            binding.ilVal.setText(mess[7].toString());
            binding.hdVal.setText(mess[8].toString());
        }else{
            binding.prVal.setText(mess[7].toString());
            binding.kuVal.setText(mess[8].toString());
        }

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if ( clicked ){
            if (newMarker != null){
                newMarker.remove();
            }
            newMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Chosen destination"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            calculateDistance();

            binding.distance.setText("Odległość: " + distance.toString() + " km");
            clicked = false;
        }
    }

    private void calculateDistance(){
        double x1 = myMarker.getPosition().latitude;
        double y1 = myMarker.getPosition().longitude;
        double x2 = newMarker.getPosition().latitude;
        double y2 = newMarker.getPosition().longitude;
        double x,y;

        if ((x1 >= 0 && x2 >= 0) || (x1 < 0 && x2 < 0)){
            if ( x1 > x2 ){
                x = x1 - x2;
            }else{ x = x2 - x1;}
        }else if (x1 >= 0 && x2 < 0){
                x = x1 - x2;
        }else if (x1 < 0 && x2 >= 0){
                x = x2 - x1;
        }else{
            x = 0;
        }

        if ((y1 >= 0 && y2 >= 0) || (y1 < 0 && y2 < 0)){
            if ( y1 > y2 ){
                y = y1 - y2;
            }else{ y = y2 - y1;}
        }else if (y1 >= 0 && y2 < 0){
            y = y1 - y2;
        }else if (y1 < 0 && y2 >= 0){
            y = y2 - y1;
        }else{
            y = 0;
        }
        distance = (Math.sqrt((Math.pow(x,2) + Math.pow(y,2)))) *111;
    }
}
