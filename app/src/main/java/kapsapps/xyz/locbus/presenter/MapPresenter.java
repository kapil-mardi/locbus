package kapsapps.xyz.locbus.presenter;

import android.location.Location;

import com.google.android.gms.location.LocationSettingsResult;

import java.util.List;

import kapsapps.xyz.locbus.models.BusModel;
import kapsapps.xyz.locbus.models.DriverListModel;

/**
 * Created by android1 on 1/2/17.
 */

public interface MapPresenter {

    void onLocationChanged(Location location);

    void requestLocationSettings(LocationSettingsResult result);

    void showBuses(BusModel model);

    void showDriverList(List<DriverListModel> driverList);
}
