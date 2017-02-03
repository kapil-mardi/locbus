package kapsapps.xyz.locbus.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.util.TimerTask;

import kapsapps.xyz.locbus.BuildConfig;
import kapsapps.xyz.locbus.utils.AppRoot;
import kapsapps.xyz.locbus.utils.Constants;
import kapsapps.xyz.locbus.utils.PrefUtils;

public class LocationUpdateService extends IntentService {

    private static final String TAG = LocationUpdateService.class.getSimpleName();


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public LocationUpdateService() {
        super("LocationUpdateService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            startRepeatingTask();
        }
    }

    private void startRepeatingTask() {

        AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(),UpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),0,intent,0);

        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                1 * 5 * 1000, pendingIntent);
    }



    public static class UpdateReceiver extends BroadcastReceiver{

        private AppRoot mAppRoot;
        private final String TAG = UpdateReceiver.class.getSimpleName();

        public UpdateReceiver() {
            super();
            mAppRoot = AppRoot.getInstance();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Got broad cast");

            getLocationFromServer();
        }

        private void getLocationFromServer() {
            String url = BuildConfig.host + Constants.LOCATION_FETCH + "?routeID="+ PrefUtils.getSelectedRoute();
            JsonArrayRequest updateRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    mAppRoot.removeRequestFromQueue(TAG);
                    if(response.length() > 0) {
                        publishResult(response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            mAppRoot.addRequest(updateRequest,TAG);

        }

        private void publishResult(JSONArray response) {
            LocationPublisher publisher = new LocationPublisher();
            publisher.setLocations(response);
            EventBus.getDefault().post(publisher);
        }
    }


    public static class LocationPublisher{

        private JSONArray locations;

        public JSONArray getLocations() {
            return locations;
        }

        public void setLocations(JSONArray locations) {
            this.locations = locations;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PrefUtils.setSelectedRoute(0);
    }
}
