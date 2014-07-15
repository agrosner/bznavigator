package com.grosner.zoo.location;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import com.grosner.zoo.R;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.singletons.Preference;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Manages location when showing the app in the foreground on the map. 
 * Allows for tracking of current location on map, add listeners for location change, 
 * @author andrewgrosner
 *
 */
public class CurrentLocationManager implements LocationSource{

    private static CurrentLocationManager manager;

    public static CurrentLocationManager getSharedManager(){
        if(manager==null){
            manager = new CurrentLocationManager(ZooApplication.getContext());
        }
        return manager;
    }

	/**
	 * listens for location updates
	 */
	private LocationListener mListener;
	
	private LocationManager mManager = null;
	
	private Context mContext;
	
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
	
	private LinkedList<Runnable> mFirstFix = new LinkedList<Runnable>();
	
	private LinkedList<WeakReference<OnLocationChangedListener>> mLocationListeners = new LinkedList<>();

    private ArrayList<OnNavigateListener> mNavigationListeners = new ArrayList<>();
	
	/**
	 * Constructs a new instance
	 * @param context
	 * @param map
	 */
	public CurrentLocationManager(Context context){
		mContext = context;
		mManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (mManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationAvailable = true;
		} else if (mManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationAvailable = true;
		} else	{
			Toast.makeText(context, context.getString(R.string.no_provider_error),
                    Toast.LENGTH_SHORT).show();
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
					if(isTracking && mListener!=null){
                        if(mNavigationListeners !=null && location!=null){
                            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                            float bearing = (isNavigating && mDestination!=null) ?
                                    location.bearingTo(mDestination) : location.getBearing();
                            for(OnNavigateListener onNavigateListener: mNavigationListeners) {
                                onNavigateListener.onNavigated(latLng, bearing);
                            }
                        }
						/*if(!isNavigating){

							mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
										new CameraPosition.Builder()
										.bearing(location.getBearing())
										.zoom(mMap.getCameraPosition().zoom)
										.tilt(mMap.getCameraPosition().tilt)
										.target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
										.build()));
						} else if(mDestination!=null){
							mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
									new CameraPosition.Builder()
									.bearing(location.bearingTo(mDestination))
									.zoom(mMap.getCameraPosition().zoom)
									.tilt(mMap.getCameraPosition().tilt)
									.target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
									.build()));
						}*/
					}
					for(WeakReference<OnLocationChangedListener> run: mLocationListeners){
                        OnLocationChangedListener onLocationChangedListener = run.get();
                        if(onLocationChangedListener!=null) {
                            onLocationChangedListener.onLocationChanged(location);
                        }
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

    public void registerOnNavigateListener(OnNavigateListener onNavigateListener){
        if(!mNavigationListeners.contains(onNavigateListener)) {
            this.mNavigationListeners.add(onNavigateListener);
        }
    }

    public void unRegisterOnNavigateListener(OnNavigateListener onNavigateListener){
        this.mNavigationListeners.remove(onNavigateListener);
    }

    /**
     * Whether the current location is being followed
     * @param onNavigateListener
     * @return
     */
    public boolean isTracking(OnNavigateListener onNavigateListener){
        return mNavigationListeners.contains(onNavigateListener);
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
		if(mLocation!=null){
			mDestination = locationTo;
			isNavigating = true;
			follow(true);
		}
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
		if(listener!=null){
            boolean found = false;
            for(WeakReference<OnLocationChangedListener> run: mLocationListeners){
                OnLocationChangedListener onLocationChangedListener = run.get();
                if(onLocationChangedListener!=null && onLocationChangedListener.equals(listener)) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                mLocationListeners.add(new WeakReference<>(listener));
            }
			Log.d("CurrentLocationManager", mLocationListeners.size() + " are activated");
		}
		if (!mMyLocationEnabled) {
            try {
            	Criteria cr = new Criteria();
            	cr.setAccuracy(Criteria.ACCURACY_FINE);
            	String provider = mManager.getBestProvider(cr, true);
            	mManager.requestLocationUpdates(provider, 1000, 0, mListener);
            	Toast.makeText(mContext, provider + " enabled", Toast.LENGTH_LONG).show();
            } catch(Exception e) {
            	deactivate();
                Toast.makeText(mContext, "Location is not turned on", Toast.LENGTH_LONG).show();
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


    public void setLocation(Location location) {
        this.mLocation = location;
    }
}
