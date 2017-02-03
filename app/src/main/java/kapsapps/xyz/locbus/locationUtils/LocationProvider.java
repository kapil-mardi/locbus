package kapsapps.xyz.locbus.locationUtils;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kapsapps.xyz.locbus.BuildConfig;
import kapsapps.xyz.locbus.models.BusModel;
import kapsapps.xyz.locbus.models.DriverListModel;
import kapsapps.xyz.locbus.presenter.MapPresenter;
import kapsapps.xyz.locbus.services.LocationUpdateService;
import kapsapps.xyz.locbus.ui.MapsActivity;
import kapsapps.xyz.locbus.utils.AppRoot;
import kapsapps.xyz.locbus.utils.Constants;
import kapsapps.xyz.locbus.utils.PrefUtils;

/**
 * Created by android1 on 1/2/17.
 */

public class LocationProvider implements LocationListener {

    private static final String TAG = "Presenter";
    private MapPresenter presenter;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private AppRoot mAppRoot;

    public LocationProvider(MapPresenter presenter, GoogleApiClient googleApiClient) {
        this.presenter = presenter;
        this.mGoogleApiClient = googleApiClient;
        mAppRoot = AppRoot.getInstance();
    }

    public void init() {
        createLocationRequest();
        checkLocationSettings();
        EventBus.getDefault().register(this);
    }

    public void stop(){
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getPublishedLocations(LocationUpdateService.LocationPublisher publisher){
        JSONArray array = publisher.getLocations();

        Gson gson = (new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)).create();

        Type type = new TypeToken<BusModel>(){}.getType();
        BusModel[] buses = gson.fromJson(array.toString(),BusModel[].class);
        presenter.showBuses(buses[0]);

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
        if(mGoogleApiClient.isConnected()) {
            Log.d(TAG, "starting location Update");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setSmallestDisplacement(2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        presenter.onLocationChanged(location);
        if(PrefUtils.getUserRoleName().equals("Driver")) {
            sendMyLocationToServer(location);
        }
    }

    private void sendMyLocationToServer(Location location) {

        double lat = location.getLatitude();
        double lng = location.getLongitude();
        long time = location.getTime();
        int routId = PrefUtils.getRoutedId();

        String url = BuildConfig.host + Constants.UPLOAD_LOCATION + "?RouteUserAssociationID="+routId+"&latitude="+lat+"&longitude="+lng+"&CreatedDatetime="+time;


        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mAppRoot.removeRequestFromQueue(TAG);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,error.toString());
            }
        });

        mAppRoot.addRequest(request,TAG);

    }

    public void getDriverList() {
        final String localCall = "getDrivers";

        String url = BuildConfig.host + Constants.GET_DRIVERS;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                mAppRoot.removeRequestFromQueue(localCall);
                populateDriverList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mAppRoot.removeRequestFromQueue(localCall);
            }
        });

        mAppRoot.addRequest(request,localCall);
    }

    private void populateDriverList(JSONArray response) {

        try{
            Gson gson = (new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)).create();

            Type listType = new TypeToken<List<DriverListModel>>() {}.getType();
            List<DriverListModel> driverList = gson.fromJson(response.toString(),listType);
            presenter.showDriverList(driverList);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
