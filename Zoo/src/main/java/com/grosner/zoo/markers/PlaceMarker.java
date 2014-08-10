package com.grosner.zoo.markers;

import java.io.Serializable;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.utils.StringUtils;

/**
 * It will hold information regarding a place to display on the map as well as distance and resource information.
 * @author Andrew Grosner
 *
 */
public class PlaceMarker implements Serializable{
	
	public transient Marker mMarker = null;
	
	private transient LatLng mPoint = null;
	
	private Location mLocation = null;
	
	protected int mIconId = 0;
	
	private int mId = -1;
	
	private String mDrawablePath = "0";
	
	private String mName = "";
	
	private String mLink = "";
	
	/**
	 * The common prefix for all links
	 */
	public static String HTML_PREFIX = "http://new.bronxzoo.com/";
	

    public PlaceMarker place(PlaceObject placeObject){
        mPoint = new LatLng(placeObject.getLatitude(), placeObject.getLongitude());
        mIconId = ZooApplication.getResourceId(placeObject.getDrawable(), "drawable");
        mName = placeObject.getName();
        mLink = placeObject.getLink();
        return this;
    }
	
	/**
	 * Sets icon thats used on the map
	 * @param resId
	 * @return
	 */
	public PlaceMarker iconId(int resId){
		mIconId = resId;
		return this;
	}
	
	/**
	 * Unique ID number when clicked
	 * @param id
	 * @return
	 */
	public PlaceMarker id(int id){
		mId = id;
		return this;
	}
	
	public PlaceMarker drawablePath(String path){
		mDrawablePath = path;
		return this;
	}
	
	public PlaceMarker point(LatLng point){
		mPoint = point;
		return this;
	}
	
	public PlaceMarker name(String name){
		mName = name;
		return this;
	}
	
	/**
	 * Sets the internet page that we want to decode information from
	 * @param link
	 * @return
	 */
	public PlaceMarker link(String link){
		mLink = link;
		return this;
	}
	
	protected Marker addMarker(GoogleMap map, boolean draggable, OnMarkerDragListener listener){
		if(mPoint!=null){
			MarkerOptions options = 
					new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(mIconId))
					.title(mName).position(mPoint);
            if(StringUtils.stringNotNullOrEmpty(mLink)){
                options.snippet(mLink);
            }
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
	 * Returns the page associated with this marker that contains information about it
	 * @return
	 */
	public String getLink(){
		if(StringUtils.stringNotNullOrEmpty(mLink))
			return HTML_PREFIX + mLink;
		else return mLink;
	}
	
	/**
	 * If two items are equal location and name
	 * @param place
	 * @return
	 */
	public boolean equals(PlaceMarker place){
		return (mPoint.equals(place.mPoint)) && mName.equals(place.getName());
	}

	
}
