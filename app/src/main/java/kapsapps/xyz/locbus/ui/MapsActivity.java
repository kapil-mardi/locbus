package kapsapps.xyz.locbus.ui;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import kapsapps.xyz.locbus.R;
import kapsapps.xyz.locbus.locationUtils.LocationProvider;
import kapsapps.xyz.locbus.presenter.MapPresenter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,MapPresenter {

    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final String TAG = "location_update";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationProvider mLocationProvider;
    private Marker mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleClient();
        mLocationProvider = new LocationProvider(this,mGoogleApiClient);

    }

    private void buildGoogleClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    /*@Override
    protected void onPause() {
        mGoogleApiClient.disconnect();
        mLocationProvider.stopLocationUpdates();
        super.onPause();
    }*/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        onLocationChanged(loc);
        mLocationProvider.init();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHECK_SETTINGS) {
                mLocationProvider.startLocationUpdates();
            }
        }
    }



    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            LatLng newLocation = new LatLng(location.getLatitude(),location.getLongitude());
            if(mCurrentLocation != null)
                mCurrentLocation.remove();

            mCurrentLocation = mMap.addMarker(new MarkerOptions().position(newLocation));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));

            Toast.makeText(getApplicationContext(),newLocation.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void requestLocationSettings(LocationSettingsResult result) {
        try{
            result.getStatus().startResolutionForResult(MapsActivity.this,REQUEST_CHECK_SETTINGS);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
