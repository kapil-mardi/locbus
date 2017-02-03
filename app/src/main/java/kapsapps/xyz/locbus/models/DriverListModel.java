package kapsapps.xyz.locbus.models;

/**
 * Created by android1 on 3/2/17.
 */

public class DriverListModel {

    private String FullName;

    private String Route;

    private int RouteID;

    private int RouteUserAssociationID;

    private int UserID;

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getRoute() {
        return Route;
    }

    public void setRoute(String route) {
        Route = route;
    }

    public int getRouteID() {
        return RouteID;
    }

    public void setRouteID(int routeID) {
        RouteID = routeID;
    }

    public int getRouteUserAssociationID() {
        return RouteUserAssociationID;
    }

    public void setRouteUserAssociationID(int routeUserAssociationID) {
        RouteUserAssociationID = routeUserAssociationID;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }
}
