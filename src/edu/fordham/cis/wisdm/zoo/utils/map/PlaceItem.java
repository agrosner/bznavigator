package edu.fordham.cis.wisdm.zoo.utils.map;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;

/**
 * It will hold information regarding a place to display on the map as well as distance and resource information.
 * @author Andrew Grosner
 *
 */
public class PlaceItem {
	
	protected Marker mMarker = null;
	
	private LatLng mPoint = null;
	
	private Location mLocation = null;
	
	private String mDistance = "0";
	
	private int mResId = 0;
	
	private int mId = -1;
	
	private String mDrawablePath = "0";
	
	private String mName = "";
	
	public PlaceItem iconId(int resId){
		mResId = resId;
		return this;
	}
	
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
	
	public PlaceItem distance(String distance){
		mDistance = distance;
		return this;
	}
	
	public PlaceItem name(String name){
		mName = name;
		return this;
	}
	
	protected Marker addMarker(GoogleMap map, boolean draggable){
		if(mPoint!=null){
			MarkerOptions options = 
					new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(mResId))
					.title(mName).position(mPoint);
			if(draggable){
				options.draggable(draggable);
				map.setOnMarkerDragListener(new OnMarkerDragListener(){

					@Override
					public void onMarkerDrag(Marker marker) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onMarkerDragEnd(Marker marker) {
						if(marker.getTitle().equals("My Parking Spot")){
							SplashScreenActivity.saveParkingSpot(marker.getPosition());
						}
					}

					@Override
					public void onMarkerDragStart(Marker marker) {
						// TODO Auto-generated method stub
						
					}
					
				});
			}
			mMarker = map.addMarker(options);
			return mMarker;
			
			
		} else return null;
	}
	
	public Marker addMarker(GoogleMap map){
		return addMarker(map, false);
	}
	
	
	public Marker addDraggableMarker(GoogleMap map){
		return addMarker(map, true);
	}
	
	public void remove(){
		try{
			mMarker.remove();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String getDistance(){
		return mDistance;
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
	 * Returns location associated with this place
	 * @return
	 */
	public Location getLocation(){
		if(mLocation==null){
			mLocation = new Location(mName);
			mLocation.setLatitude(mPoint.latitude);
			mLocation.setLongitude(mPoint.longitude);
			return mLocation;
		} else{
			return mLocation;
		}
	}
	
	public boolean equals(PlaceItem place){
		return (mPoint.equals(place.mPoint)) && mName.equals(place.getName());
	}

	
}
