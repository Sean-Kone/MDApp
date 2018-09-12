package gsi.com.mdapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MDALocationManager implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    public static final long DEFAULT_UPDATE_INTERVAL_SEC = 30;
    public static final float DEFAULT_SMALLEST_DISPLACEMENT = 0.0f;

    private boolean mIsConnected = false;
    private boolean mIsSingleUpdate = false;
    private long mLocReqUpdatesInterval;
    private float mLocReqSmallestDisplacement;
    private int mlocReqAccuracyLevel;
    private Context mContext;
    private GoogleApiClient mLocationClient;
    private OnLocationUpdateListener mListener;

    public MDALocationManager(Context context, boolean isSingleUpdate) {
        mContext = context;
        mIsSingleUpdate = isSingleUpdate;
        mLocReqUpdatesInterval = 1000*DEFAULT_UPDATE_INTERVAL_SEC;
        mLocReqSmallestDisplacement = DEFAULT_SMALLEST_DISPLACEMENT;
        mlocReqAccuracyLevel = LocationRequest.PRIORITY_HIGH_ACCURACY;
    }

    public void setOnLocationUpdateListener(@NonNull OnLocationUpdateListener listener) {
        mListener = listener;
    }

    public void build() {
        buildGoogleApiClient();
    }

    public void connect() {
        if (isLocationEnabled(mContext)) {
            mLocationClient.connect();
        }
    }

    public void disconnect() {
        try {
            if (mLocationClient.isConnected() || mLocationClient.isConnecting() || mIsConnected) {
                stopLocationUpdates();
            }
        } catch (IllegalStateException e) {
            MDALogger.logStackTrace(e);
        }
    }

    private synchronized void buildGoogleApiClient() {
        mLocationClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(mLocReqUpdatesInterval);
        locationRequest.setFastestInterval(mLocReqUpdatesInterval);
        locationRequest.setSmallestDisplacement(mLocReqSmallestDisplacement);
        locationRequest.setPriority(mlocReqAccuracyLevel);
        return locationRequest;
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mIsConnected) {
            stopLocationUpdates();
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, createLocationRequest(), this);
            mIsConnected = true;
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
        mLocationClient.disconnect();
        mIsConnected = false;
        if (mListener != null) {
            mListener.onLocationUpdatesStopped();
        }
    }

    /**
     * {@link GoogleApiClient.ConnectionCallbacks} interface methods
     */
    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (mListener != null) {
            mListener.onLocationUpdateError(null);
        }
    }

    /**
     * {@link GoogleApiClient.OnConnectionFailedListener} interface methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mListener != null) {
            mListener.onLocationUpdateError(result.getErrorMessage());
        }
    }

    /**
     * {@link LocationListener} interface methods
     */
    @Override
    public void onLocationChanged(Location location) {
        if (mIsSingleUpdate) {
            disconnect();
        }
        if (mListener != null) {
            mListener.onLocationUpdate(location);
        }
        getCurrentLocation(location);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        String locationProviders = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                MDALogger.logStackTrace(e);
                return false;
            } catch (Exception e) {
                MDALogger.logStackTrace(e);
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            try {
                locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            } catch (Exception e) {
                MDALogger.logStackTrace(e);
                return false;
            }
            return !TextUtils.isEmpty(locationProviders);
        }
    }


    private void getCurrentLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder geoCoder = null;
        List<Address> matches = null;
        Address bestMatch = null;
        String geoLocation = "";
        try {
            if (latitude == 0.0 && longitude == 0.0) {
                return;
            }
            if (latitude < 0) {
                latitude = Double.valueOf(new DecimalFormat("#.###").format((-1*latitude)));
                latitude *= -1;
            }

            if (longitude < 0) {
                longitude = Double.valueOf(new DecimalFormat("#.###").format((-1*longitude)));
                longitude *= -1;
            }
            geoCoder = new Geocoder(mContext, Locale.US);
            matches = geoCoder.getFromLocation(latitude, longitude, 1);
            bestMatch = (matches.isEmpty() ? null : matches.get(0));
        } catch (NumberFormatException e) {
            MDALogger.logStackTrace(e);
        } catch (IOException e) {
            MDALogger.logStackTrace(e);
        }
        if (bestMatch != null) {
            String addressLine = bestMatch.getAddressLine(0);
            if (addressLine != null) {
                geoLocation = addressLine;
            } else {
                if (bestMatch.getThoroughfare() != null && !bestMatch.getThoroughfare().isEmpty()) {
                    geoLocation = geoLocation + bestMatch.getThoroughfare() + ", ";
                } else {
                    if (bestMatch.getSubLocality() != null && !bestMatch.getSubLocality().isEmpty()) {
                        geoLocation = geoLocation + bestMatch.getSubLocality() + ", ";
                    }
                }
                if (bestMatch.getLocality() != null && !bestMatch.getLocality().isEmpty()) {
                    geoLocation = geoLocation + bestMatch.getLocality() + ", ";
                }
                if (bestMatch.getAdminArea() != null && !bestMatch.getAdminArea().isEmpty()) {
                    geoLocation = geoLocation + bestMatch.getAdminArea() + ", ";
                }
                if (bestMatch.getCountryName() != null && !bestMatch.getCountryName().isEmpty()) {
                    geoLocation = geoLocation + bestMatch.getCountryName();
                }
            }
        }
        if (mListener != null) {
            mListener.onLocationAdrsUpdate(location, geoLocation);
        }
    }

    public interface OnLocationUpdateListener {
        public void onLocationUpdate(Location location);
        public void onLocationAdrsUpdate(Location location, String adrs);
        public void onLocationUpdateError(String error);
        public void onLocationUpdatesStopped();
    }
}
