package kapsapps.xyz.locbus.utils;

import android.content.Context;
import android.text.Html;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by android1 on 7/11/16.
 */
public class AppRoot {
    private static AppRoot instance;
    private Context mContext;
    private RequestQueue mRequestQueue;
    private List<String> mRequestTags;

    private AppRoot(){}

    public static AppRoot getInstance(){
        if(instance == null){
            instance = new AppRoot();
        }

        return instance;
    }

    public void setUpContext(Context context) {
        instance.mContext = context;
        initVolley();
    }



    public String formatToYesterdayOrToday(long date) throws ParseException {
        Date dateTime = new Date(date * 1000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        DateFormat timeFormatter = new SimpleDateFormat("hh:mma");
        timeFormatter.setTimeZone(TimeZone.getDefault());

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            //return "Today " + timeFormatter.format(dateTime);
            return timeFormatter.format(dateTime);
        } /*else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday " + timeFormatter.format(dateTime);
        } */else {
            DateFormat format = new SimpleDateFormat("dd MMM yyyy");
            format.setTimeZone(TimeZone.getDefault());
            return format.format(dateTime);
        }
    }



    public Context getContext(){
        return instance.mContext;
    }

    private void initVolley(){
        mRequestQueue = Volley.newRequestQueue(getContext());
        mRequestTags = new ArrayList<>();
    }

    public <T> Request<T> addRequest(Request<T> request, String tag){
        if(!mRequestTags.contains(tag)) {
            request.setTag(tag);
            mRequestTags.add(tag);
            return mRequestQueue.add(request);
        }else{
            return null;
        }
    }


    public void cancelRequest(String tag){
        mRequestQueue.cancelAll(tag);
        if(mRequestTags.contains(tag)){
            mRequestTags.remove(tag);
        }
    }

    public void showError(String msg){
        Toast.makeText(getContext(),msg, Toast.LENGTH_LONG).show();
    }

    public void removeRequestFromQueue(String tag) {
        if(mRequestTags.contains(tag)){
            mRequestTags.remove(tag);
        }
    }

    public String getStringFromHtml(String htmlString) {
        String htmlShortDescription = Html.fromHtml(htmlString).toString();
        return htmlShortDescription.replaceAll("<[^>]*>","");
    }



    public int generateLocalUUID(){
        int id = (int) UUID.randomUUID().getLeastSignificantBits();
        id = -(Math.abs(id));
        return id;
    }



    public long getUTCTime(long localTime){

        TimeZone timeZone = TimeZone.getTimeZone("UTC");

        return  localTime - timeZone.getOffset(localTime);
    }
}
