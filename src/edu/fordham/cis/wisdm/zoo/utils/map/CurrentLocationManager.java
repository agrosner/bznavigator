package edu.fordham.cis.wisdm.zoo.utils.map;

import java.util.LinkedList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import edu.fordham.cis.wisdm.zoo.utils.Preference;

import android.content.Context;
import android.location.Criteria;
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
	
	/**
	 * Whether user has indicated that they want map to follow current location
	 */
	private boolean isTracking = false;
	
	/**
	 * Whether user wants to navigate to place, points map in direction of location
	 */
	private boolean isNavigating = false;
	
	/**
	 * The destination that the provider tells the map to point towards
	 */
	private Location mDestination = null;
	
	/**
	 * When we get a first fix, we do not want to run any of our first fixes anymore
	 */
	private boolean mFirstFixSuccess = false;
	
	/**
	 * Whether location is enabled natively
	 */
	private boolean mMyLocationEnabled = false;
	
	/**
	 * Whether location is currently available or not
	 */
	private boolean locationAvailable = false;
	
	public static String locationMessage = "";
	
	private LinkedList<Runnable> mFirstFix = new LinkedList<Runnable>();
	
	private LinkedList<OnLocationChangedListener> mLocationListeners = new LinkedList<OnLocationChangedListener>();
	
	/**
	 * Constructs a new instance
	 * @param context
	 * @param map
	 */
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
						if(!isNavigating){
							mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
										new CameraPosition.Builder()
										.bearing(location.getBearing())
										.target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
										.build()));
						}else if(mDestination!=null){
							mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
									new CameraPosition.Builder()
									.bearing(location.bearingTo(mDestination))
									.target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
									.build()));
						}
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
		
		Float latitude = Preference.getFloat("lat", null);
		Float longitude = Preference.getFloat("lon", null);
		if(latitude!=null && longitude!=null){
			mLocation = new Location("my location");
			mLocation.setLatitude(latitude);
			mLocation.setLongitude(longitude);
		}
	}
	
	public void setFields(Context con, GoogleMap map){
		mCtx = con;
		mMap = map;
	}
	
	public void runOnFirstFix(Runnable run){
		mFirstFix.add(run);
	}
	
	/**
	 * Sets whether the map will follow the current location
	 * @param follow
	 */
	public void follow(boolean follow){
		isTracking = follow;
	}
	
	/**
	 * We will navigate to the specified location
	 * @param locationTo
	 */
	public void navigate(Location locationTo){
		mDestination = locationTo;
		isNavigating = true;
		follow(true);
	}
	
	/**
	 * We will disable navigation 
	 */
	public void disableNavigation(){
		mDestination = null;
		isNavigating = false;
		follow(false);
	}
	
	/**
	 * Retrieves the last known location found by location changes
	 * @return
	 */
	public Location getLastKnownLocation(){
		return mLocation;
	}
	
	public boolean isNavigating(){
		return isNavigating;
	}
	
	public void activate(){
		activate(null);
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		if(!locationAvailable)	return;
		if(listener!=null && !mLocationListeners.contains(listener))
			mLocationListeners.add(listener);
		if (!mMyLocationEnabled) {
            try {
            	Criteria cr = new Criteria();
            	cr.setAccuracy(Criteria.ACCURACY_FINE);
            	mManager.requestLocationUpdates(mManager.getBestProvider(cr, true), 1000, 0, mListener);
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
		if(mLocation!=null){
			Preference.putFloat("lat", (float) mLocation.getLatitude());
			Preference.putFloat("lon", (float) mLocation.getLongitude());
		}
	}
	
	
}
