package edu.fordham.cis.wisdm.zoo.utils.map;

import java.util.LinkedList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Manages location when showing the app in the foreground on the map. 
 * Allows for tracking of current location on map, add listeners for location change, 
 * @author andrewgrosner
 *
 */
public class CurrentLocationManager implements LocationSource{

	private LocationListener mListener;
	
	private LocationManager mManager = null;
	
	private Context mCtx;
	
	private GoogleMap mMap;
	
	private Location mLocation = null;
	
	private boolean isTracking = false;
	
	private boolean mFirstFixSuccess = false;
	
	private boolean mMyLocationEnabled = false;
	
	private boolean locationAvailable = false;
	
	private boolean gpsAvail = false;
	
	private boolean networkAvail = false;
	
	public static String locationMessage = "";
	
	private LinkedList<Runnable> mFirstFix = new LinkedList<Runnable>();
	
	private LinkedList<OnLocationChangedListener> mLocationListeners = new LinkedList<OnLocationChangedListener>();
	
	public CurrentLocationManager(Context context, GoogleMap map){
		mCtx = context;
		mMap = map;
		mManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (mManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationMessage ="GPS provider enabled.";
			locationAvailable = true;
			gpsAvail = true;
		} else if (mManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationMessage = "Network provider enabled.";
			locationAvailable = true;
			networkAvail = true;
		} else	{
			locationMessage = "No provider enabled. Please check settings and allow locational services.";
			locationAvailable = false;
		}
		mListener = new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				if(location!=null)
					mLocation = location;
				if(mLocation!=null){
					if(!mFirstFixSuccess){
						mFirstFixSuccess = true;
						for(Runnable run: mFirstFix){
							run.run();
						}
						
					}
					if(isTracking){
						mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
									new CameraPosition.Builder()
									.bearing(location.getBearing())
									.tilt(45)
									.target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
									.build()));
					}	
					for(OnLocationChangedListener run: mLocationListeners){
						run.onLocationChanged(location);
					}
				}
			
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
			
		};
	}
	
	public void setFields(Context con, GoogleMap map){
		mCtx = con;
		mMap = map;
	}
	
	public void runOnFirstFix(Runnable run){
		mFirstFix.add(run);
	}
	
	public void follow(boolean follow){
		isTracking = follow;
	}
	
	public Location getLastKnownLocation(){
		return mLocation;
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		if(!locationAvailable)	return;
		if(!mLocationListeners.contains(listener))
			mLocationListeners.add(listener);
		if (!mMyLocationEnabled) {
            try {
            	mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, mListener);
            	mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 2, mListener);
            } catch(Exception e) {
            	deactivate();
                Toast.makeText(this.mCtx, "Location is not turned on", Toast.LENGTH_LONG).show();
                mMyLocationEnabled = false;
            }
        }
        mMyLocationEnabled = true;
	}

	@Override
	public void deactivate() {
		mManager.removeUpdates(mListener);
		mMyLocationEnabled = false;
	}
	
	
}
