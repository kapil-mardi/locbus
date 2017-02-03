package kapsapps.xyz.locbus.models;

/**
 * Created by android1 on 2/2/17.
 */

public class BusModel {

    private long CreatedDatetime;
    private double Lat;
    private double Long;

    public long getCreatedDatetime() {
        return CreatedDatetime;
    }

    public void setCreatedDatetime(long createdDatetime) {
        CreatedDatetime = createdDatetime;
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double aLong) {
        Long = aLong;
    }
}
