package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.LinkedList;

import cis.fordham.edu.wisdm.messages.MessageBuilder;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class CurrentLocationManager {

	private LocationListener mListener;
	
	private LocationManager mManager = null;
	
	private Context mCtx;
	
	private GoogleMap mMap;
	
	private Location mLocation = null;
	
	private boolean isTracking = false;
	
	private boolean mFirstFixSuccess = false;
	
	private boolean mMyLocationEnabled = false;
	
	private boolean locationAvailable = false;
	
	public static String locationMessage = "";
	
	private LinkedList<Runnable> mFirstFix = new LinkedList<Runnable>();
	
	private LinkedList<Runnable> mLocationListeners = new LinkedList<Runnable>();
	
	public CurrentLocationManager(Context context, GoogleMap map){
		mCtx = context;
		mMap = map;
		mManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (mManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationMessage ="GPS provider enabled.";
			locationAvailable = true;
		} else if (mManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationMessage = "Network provider enabled.";
			locationAvailable = true;
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
									.zoom(16)
									.target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
									.build()));
					}	
					for(Runnable run: mLocationListeners){
						run.run();
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
	
	public void schedule(Runnable run){
		mLocationListeners.add(run);
	}
	
	public void runOnFirstFix(Runnable run){
		mFirstFix.add(run);
	}
	
	public boolean start(){
		if(!locationAvailable) return false;
		if (!mMyLocationEnabled) {
            try {
            	mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, mListener);
            	mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 2, mListener);
            } catch(Exception e) {
            	stop();
                Toast.makeText(this.mCtx, "Location is not turned on", Toast.LENGTH_LONG).show();
                return mMyLocationEnabled = false;
            }
        }
        return mMyLocationEnabled = true;
	}
	
	public void stop(){
		mManager.removeUpdates(mListener);
		mMyLocationEnabled = false;
	}
	
	public void follow(boolean follow){
		isTracking = follow;
	}
	
	public Location getLastKnownLocation(){
		return mLocation;
	}
	
	
}
