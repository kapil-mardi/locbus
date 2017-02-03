package kapsapps.xyz.locbus.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kapsapps.xyz.locbus.R;
import kapsapps.xyz.locbus.locationUtils.LocationProvider;
import kapsapps.xyz.locbus.models.BusModel;
import kapsapps.xyz.locbus.models.DriverListModel;
import kapsapps.xyz.locbus.presenter.MapPresenter;
import kapsapps.xyz.locbus.services.LocationUpdateService;
import kapsapps.xyz.locbus.utils.PrefUtils;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,MapPresenter {

    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final String TAG = "location_update";
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationProvider mLocationProvider;
    private Marker mCurrentLocation,mCurrentRoutPosition;
    private AlertDialog.Builder driverListDialog;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleClient();
        mLocationProvider = new LocationProvider(this,mGoogleApiClient);
        driverListDialog = new AlertDialog.Builder(this);
        driverListDialog.setCancelable(false);
        driverListDialog.setTitle(R.string.chooseDriver);

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
        super.onStart();
        mGoogleApiClient.connect();

        if(!PrefUtils.getUserRoleName().equals("Driver")) {
            Intent intent = new Intent(MapsActivity.this, LocationUpdateService.class);
            startService(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
        mLocationProvider.stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(MapsActivity.this, LocationUpdateService.class);
        stopService(intent);
        mLocationProvider.stop();
    }

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
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
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

    @Override
    public void showBuses(BusModel model) {
        createOrUpdateMarker(model);
    }

    @Override
    public void showDriverList(final List<DriverListModel> driverList) {

        String[] routes = new String[driverList.size()];
        boolean[] preSelection = new boolean[driverList.size()];

        for(int itr = 0; itr < driverList.size(); itr++){
            routes[itr] = driverList.get(itr).getRoute();
            preSelection[itr] = false;
        }

        final int[] selectedRoutIds = new int[1];

        driverListDialog.setMultiChoiceItems(routes, preSelection, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked) {
                    DriverListModel model = driverList.get(which);
                    selectedRoutIds[0] = model.getRouteID();
                }
            }
        });

        driverListDialog.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefUtils.setSelectedRoute(selectedRoutIds[0]);
                dialog.dismiss();
            }
        });

        driverListDialog.setNegativeButton(R.string.cancel,null);
        driverListDialog.show();
    }

    private void createOrUpdateMarker(BusModel bus) {

        if(mCurrentRoutPosition != null)
            mCurrentRoutPosition.remove();

        LatLng newLocation = new LatLng(bus.getLat(),bus.getLong());

        mCurrentRoutPosition = mMap.addMarker(new MarkerOptions().position(newLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));

        mCurrentRoutPosition.setTitle("Bus");

        Log.d(TAG,"Adding bus marker");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps,menu);

        if(PrefUtils.getUserRoleName().equals("Driver")){
            MenuItem item = menu.findItem(R.id.action_drivers);
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout : {
                doLogout();
                break;
            }

            case R.id.action_drivers : {
                mLocationProvider.getDriverList();
                break;
            }
        }
        return true;
    }

    private void doLogout() {
        PrefUtils.deletePrefs();
        finish();
    }
}
