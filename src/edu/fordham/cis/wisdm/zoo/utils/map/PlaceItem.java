package edu.fordham.cis.wisdm.zoo.utils.map;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * It will hold information regarding a place to display on the map as well as distance and resource information.
 * @author Andrew Grosner
 *
 */
public class PlaceItem {
	
	protected Marker mMarker = null;
	
	private LatLng mPoint = null;
	
	private Location mLocation = null;
	
	private int mIconId = 0;
	
	private int mId = -1;
	
	private String mDrawablePath = "0";
	
	private String mName = "";
	
	/**
	 * Sets icon thats used on the map
	 * @param resId
	 * @return
	 */
	public PlaceItem iconId(int resId){
		mIconId = resId;
		return this;
	}
	
	/**
	 * Unique ID number when clicked
	 * @param id
	 * @return
	 */
	public PlaceItem id(int id){
		mId = id;
		return this;
	}
	
	public PlaceItem drawablePath(String path){
		mDrawablePath = path;
		return this;
	}
	
	public PlaceItem point(LatLng point){
		mPoint = point;
		return this;
	}
	
	public PlaceItem name(String name){
		mName = name;
		return this;
	}
	
	protected Marker addMarker(GoogleMap map, boolean draggable, OnMarkerDragListener listener){
		if(mPoint!=null){
			MarkerOptions options = 
					new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(mIconId))
					.title(mName).position(mPoint);
			if(draggable){
				options.draggable(draggable);
				map.setOnMarkerDragListener(listener);
			}
			mMarker = map.addMarker(options);
			return mMarker;
			
			
		} else return null;
	}
	
	/**
	 * Adds non-draggable marker to the map
	 * @param map
	 * @return
	 */
	public Marker addMarker(GoogleMap map){
		return addMarker(map, false, null);
	}
	
	/**
	 * Adds a marker with dragging to move enabled
	 * @param map
	 * @param listener
	 * @return
	 */
	public Marker addDraggableMarker(GoogleMap map, OnMarkerDragListener listener){
		return addMarker(map, true, listener);
	}
	
	/**
	 * Removes marker from the map
	 */
	public void remove(){
		try{
			mMarker.remove();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public LatLng getPoint(){
		return mPoint;
	}
	
	public String getDrawablePath(){
		return mDrawablePath;
	}
	
	public String getName(){
		return mName;
	}
	
	public int getId(){
		return mId;
	}
	
	/**
	 * Returns location associated with this place.
	 * Creates a new Location if the current one is null
	 * @return
	 */
	public Location getLocation(){
		if(mPoint==null)	
			throw new IllegalStateException("This PlaceItem's LatLng cannot be null.");
		if(mLocation==null){
			mLocation = new Location(mName);
			mLocation.setLatitude(mPoint.latitude);
			mLocation.setLongitude(mPoint.longitude);
			return mLocation;
		} else{
			return mLocation;
		}
	}
	
	/**
	 * If two items are equal location and name
	 * @param place
	 * @return
	 */
	public boolean equals(PlaceItem place){
		return (mPoint.equals(place.mPoint)) && mName.equals(place.getName());
	}

	
}
