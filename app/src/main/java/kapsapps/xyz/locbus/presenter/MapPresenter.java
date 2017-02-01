package kapsapps.xyz.locbus.presenter;

import android.location.Location;

import com.google.android.gms.location.LocationSettingsResult;

/**
 * Created by android1 on 1/2/17.
 */

public interface MapPresenter {

    void onLocationChanged(Location location);

    void requestLocationSettings(LocationSettingsResult result);
}
