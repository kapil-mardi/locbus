package kapsapps.xyz.locbus.locationUtils;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import kapsapps.xyz.locbus.presenter.MapPresenter;
import kapsapps.xyz.locbus.ui.MapsActivity;

/**
 * Created by android1 on 1/2/17.
 */

public class LocationProvider implements LocationListener {

    private static final String TAG = "Presenter";
    private MapPresenter presenter;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public LocationProvider(MapPresenter presenter, GoogleApiClient googleApiClient) {
        this.presenter = presenter;
        this.mGoogleApiClient = googleApiClient;
    }

    public void init() {
        createLocationRequest();
        checkLocationSettings();
    }

    private void checkLocationSettings() {
        Log.d(TAG,"check location settings");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        presenter.requestLocationSettings(locationSettingsResult);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }

            }
        });
    }
    public void stopLocationUpdates(){
        Log.d(TAG,"stop location");
        if(mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    public void startLocationUpdates() {
        Log.d(TAG,"starting location Update");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"found new location");
        presenter.onLocationChanged(location);
    }
}
