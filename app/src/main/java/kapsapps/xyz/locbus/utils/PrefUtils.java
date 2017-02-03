package kapsapps.xyz.locbus.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Created by android1 on 7/11/16.
 */
public class PrefUtils implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String prefName = "default_prefs";

    public static final String isLoggedKey = "IS_LOGGED";
    public static final boolean defaultLoggedState = false;

    public static final String userRoleKey = "USER_ROLE";
    public static final int defaultUserRole = 1;

    private static String defaultUserRoleName = "Employee";
    private static String userRoleName = "userRoleName";

    public static final String imeiNumber = "imei_number";
    public static final String defaultImei = "";

    public static final String userDetails = "user_details";
    public static final String defaultUser = "";

    public static final String sessionId = "sessionId";
    public static final String defaultSession = "";

    private static String isDataDownloaded = "isDataDownloaded";
    private static boolean defaultDownloadStatus = false;

    private static String isDownloadComplete = "isDownloadComplete";
    private static boolean defaultDownloadProgress = false;

    private static String notificationCount = "notificationCount";
    private static int defaultNotificationCount = 0;

    private static String userName = "userName";
    private static String defaultUserName = "";

    private static String userId = "userId";
    private static int defaultUserId = 0;

    private static String routedId = "routeId";

    private static String routeUserAssociationId = "routeAssociationId";

    private static String selectedRoute = "route";



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    public static SharedPreferences getPreference(){
        Context context = AppRoot.getInstance().getContext();
        SharedPreferences preferences = context.getSharedPreferences(prefName,0);
        return preferences;
    }

    public static void setIsLoggedStatus(boolean isLoggedStatus){
        getPreference().edit().putBoolean(isLoggedKey,isLoggedStatus).apply();
    }

    public static boolean getIsLoggedStatus(){
        return getPreference().getBoolean(isLoggedKey,defaultLoggedState);
    }

    public static void setUserRole(int role) {
        getPreference().edit().putInt(userRoleKey,role).apply();
    }

    public static int getUserRole(){
        return  getPreference().getInt(userRoleKey,defaultUserRole);
    }

    public static void setUserRoleName(String roleStr) {
        getPreference().edit().putString(userRoleName,roleStr).apply();
    }

    public static String getUserRoleName(){
        return getPreference().getString(userRoleName,defaultUserRoleName);
    }

    public static void setImeiNumber(String imei) {
        getPreference().edit().putString(imeiNumber,imei).apply();
    }

    public static String getImeiNumber() {
        return getPreference().getString(imeiNumber,defaultImei);
    }

    public static void setUserDetails(JSONObject user) {
        try {
            setUserRole(user.getInt("RoleID"));
            setUserRoleName(user.getString("Role"));
            setUserName(user.getString("FullName"));
            setUserId(user.getInt("UserID"));
            setRoutedId(user.getInt("RouteID"));
            setRouteUserAssociationId(user.getInt("RouteUserAssociationID"));
        }catch (Exception e){
            e.printStackTrace();
        }
        if(user != null)
            getPreference().edit().putString(userDetails,user.toString()).apply();
    }

    public static String getUserDertails(){
        return getPreference().getString(userDetails,defaultUser);
    }

    public static void setUserSesssion(String session) {
        getPreference().edit().putString(sessionId,session).apply();
    }

    public static String getUserSession(){
        return getPreference().getString(sessionId,defaultSession);
    }

    public static void setDownaloadedStatus(boolean status){
        getPreference().edit().putBoolean(isDataDownloaded,status).apply();
    }

    public static boolean isDataDownloaded() {
        return getPreference().getBoolean(isDataDownloaded,defaultDownloadStatus);
    }

    public static void setDownloadProgress(boolean progress){
        getPreference().edit().putBoolean(isDownloadComplete,progress).apply();
    }

    public static boolean isDownloadProgress() {
        return getPreference().getBoolean(isDownloadComplete,defaultDownloadProgress);
    }

    public static void setNotificationCount(int count) {
        getPreference().edit().putInt(notificationCount,count).apply();
    }

    public static int getNotificationCount(){
        return getPreference().getInt(notificationCount,defaultNotificationCount);
    }

    public static String getUserName() {
        return getPreference().getString(userName,defaultUserName);
    }

    public static void setUserName(String userNameStr) {
        getPreference().edit().putString(userName,userNameStr).apply();
    }

    public static int getUserId() {
        return getPreference().getInt(userId,defaultUserId);
    }

    public static void setUserId(int userIdInt) {
        getPreference().edit().putInt(userId,userIdInt).apply();
    }

    public static int getRoutedId() {
        return getPreference().getInt(routedId,0);
    }

    public static void setRoutedId(int routedIdInt) {
        getPreference().edit().putInt(routedId,routedIdInt).apply();
    }

    public static int getRouteUserAssociationId() {
        return getPreference().getInt(routeUserAssociationId,0);
    }

    public static void setRouteUserAssociationId(int routeUserAssociationIdInt) {
        getPreference().edit().putInt(routeUserAssociationId,routeUserAssociationIdInt).apply();
    }

    public static int getSelectedRoute() {
        return getPreference().getInt(selectedRoute,0);
    }

    public static void setSelectedRoute(int selectedRouteInt) {
        getPreference().edit().putInt(selectedRoute,selectedRouteInt).apply();
    }

    public static void deletePrefs() {
        setIsLoggedStatus(false);
        getPreference().edit().remove(userDetails);
        setUserDetails(null);
        setUserSesssion("");
        setUserRole(0);
        setUserId(0);
        setUserName("");
        setUserRoleName("");
        setDownaloadedStatus(false);
        setDownloadProgress(false);
        setNotificationCount(0);
        setRoutedId(0);
        setRouteUserAssociationId(0);
    }

}
