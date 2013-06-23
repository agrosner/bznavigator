package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.LinkedList;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import edu.fordham.cis.wisdm.zoo.main.InfoDisplayActivity;
import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SlidingScreenActivity;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragmentList;
import edu.fordham.cis.wisdm.zoo.utils.Operations;
import edu.fordham.cis.wisdm.zoo.utils.Preference;

/**
 * Class adds further functionality on top of google's SupportMapFragment such as more button options, 
 * add location information to the map, and more. Displays all of the zoo information needed
 * @author Andrew Grosner
 *
 */
public class MapViewFragment extends SupportMapFragment implements OnClickListener, OnCameraChangeListener, OnInfoWindowClickListener{

	/**
	 * The handle to the map object that allows for manipulation of the map
	 */
	private GoogleMap mGoogleMap = null;

	/**
	 * Manages the text overlays displayed on the map
	 */
	private TextMarkerManager mTextMarkerManager;
	
	/**
	 * The current zoom of the map
	 */
	private float mCurrentZoom = 0;
	
	/**
	 * Map bounds of the zoo
	 */
	private LatLngBounds.Builder mMapBounds = null;
	
	
	private LayoutInflater mInflater = null;
	
	/**
	 * Provides locational information that the app can pull from while in the foreground
	 */
	private CurrentLocationManager mManager = null;
	
	/**
	 * Whether user has indicated to have the map follow his location
	 */
	private boolean isTracking = false;
	
	/**
	 * whether user has chosen to save his parking spot
	 */
	private boolean isParked = false;
	
	/**
	 * Location that will be stored in preferences
	 */
	private LatLng mParkingLocation = null;
	
	/**
	 * Item that will go on map when user saves parking spot
	 */
	private PlaceMarker mParkingPlace = null;
	
	/**
	 * List of searchable places
	 */
	private LinkedList<PlaceMarker> searchExhibits = new LinkedList<PlaceMarker>();
	
	/**
	 * The markers (excluding textmarkers, the textmarkermanager manages those) focused caused by either clicking on an item in the PlaceFragment, 
	 * searching for the item, and (soon) clicking on it on the map.
	 */
	private LinkedList<Marker> mLastMarkers = new LinkedList<Marker>();
	
	/**
	 * Whether map has changed camera for the first time or not
	 */
	private boolean firstCameraChange = true;
	
	/**
	 * Whether map on screen is satellite or not
	 */
	private boolean isSatelliteMap = false;
	
	private ImageButton mMapToggle;
	
	private View[] loaders;
	
	@Override
	public void onPause(){
		super.onPause();
		
		mManager.deactivate();
	}
	
	@Override
	public void onResume(){
		super.onResume();

		if(mGoogleMap==null){
			mGoogleMap = super.getMap();
			if(mGoogleMap!=null){
				setUpMap(mInflater);
			}
		} else{
			mManager.activate();
		}
	}
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
		ViewGroup v = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		View layout = inflater.inflate(R.layout.fragment_map, container, false);
		
		View[] btns = Operations.setOnClickListeners(layout, this, R.id.toggle, R.id.overview, R.id.clear, R.id.my_location);
		loaders = Operations.findViewByIds(layout, R.id.loading, R.id.loading_text);
		mMapToggle = (ImageButton) btns[0];
		
		Operations.removeView(layout.findViewById(R.id.clear));
		
		mInflater = inflater;
		
		v.addView(layout);
		return v;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.toggle){
			if(!isSatelliteMap){
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				mTextMarkerManager.changeTextLabelColor(Color.MAGENTA,mCurrentZoom);
				mMapToggle.setImageResource(R.drawable.ic_action_map);
			} else{
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				mTextMarkerManager.changeTextLabelColor(Color.BLACK,mCurrentZoom);
				mMapToggle.setImageResource(R.drawable.ic_action_globe);
			}
			
			isSatelliteMap = !isSatelliteMap;
		} else if(id == R.id.overview){
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getMapBounds(), 10));
		} else if(id == R.id.clear){
			clearMap();
		} else if (id == R.id.my_location){
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(MapUtils.locationToLatLng(mGoogleMap.getMyLocation())));
		} else{
			
			SlidingScreenActivity act = ((SlidingScreenActivity) getActivity());
			PlaceMarker place = act.selected.get(id-1);
			act.performSearch(place);
			clearMap();
			addPlace(place);
		}
	}
	
	/**
	 * Sets up UI options
	 * @param inflater
	 */
	private void setUpMap(LayoutInflater inflater){
		final SlidingScreenActivity act = (SlidingScreenActivity)getActivity();
		final MapViewFragment frag = this;
		
		mGoogleMap.setOnCameraChangeListener(this);
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setInfoWindowAdapter(new PlaceMarkerWindowAdapter(inflater));
		mGoogleMap.setOnInfoWindowClickListener(this);
   		mGoogleMap.getUiSettings().setCompassEnabled(false);
   		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
		
   		if(mManager==null){
   			mManager = new CurrentLocationManager(getActivity(), mGoogleMap);
   			mManager.runOnFirstFix(new Runnable(){
   	   			
   				@Override
   				public void run() {
   					PlaceFragmentList list = ((SlidingScreenActivity)getActivity()).getCurrentPlaceFragment();
   					if(list!=null){
   						list.refresh();
   					}
   					
   				}
   					
   			});
   	   		
   		}
   		else	mManager.setFields(getActivity(), mGoogleMap);
   		mManager.activate();
   		
   		act.notifyLocationSet();
   		
   		if(searchExhibits.isEmpty())
   			new LoadDataTask(act, frag).execute();
   		
   		mMapBounds = new LatLngBounds.Builder();
		try {
			MapUtils.generatePolygon(getActivity(), mMapBounds);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
   		new LoadMapDataTask(this).execute();
   		
		
	}
	
	/**
	 * Clears all showing (focused) icons, does not remove amenities icons
	 */
	public void clearMap(){
		//clean up old previous markers
		for(Marker m: mLastMarkers){
			m.remove();
		}
		mLastMarkers.clear();
		mTextMarkerManager.clearFocus(mCurrentZoom);
		Operations.removeView(getView().findViewById(R.id.clear));
	}
	
	/**
	 * Adds a place and shows place in relation to location
	 * @param place
	 */
	public void addPlace(PlaceMarker place){
		addPlaceMove(place);
		MapUtils.moveRelativeToCurrentLocation(mManager.getLastKnownLocation(),place.getPoint(), mGoogleMap);
	}
	
	/**
	 * Adds a place to the map, moves to the location
	 * @param place
	 */
	public void addPlaceMove(PlaceMarker place){
		Operations.addView(getView().findViewById(R.id.clear));
		if(!mTextMarkerManager.addFocus(place))
			mLastMarkers.add(place.addMarker(mGoogleMap));
		MapUtils.animateTo(mGoogleMap, place.getLocation());
		
		place.mMarker.showInfoWindow();
	}
	
	/**
	 * Adds a list of placeitems to the map
	 * @param places
	 */
	public void addPlaceList(LinkedList<PlaceMarker> places){
		Operations.addView(getView().findViewById(R.id.clear));
		MapUtils.moveToBounds(mManager.getLastKnownLocation(), mGoogleMap, places);
		for(PlaceMarker place: places){
			//if not within the text marker manager, we add icon to map
			if(!mTextMarkerManager.addFocus(place))
				mLastMarkers.add(place.addMarker(mGoogleMap));
		}
	}
	
	/**
	 * Called when menuitems were created in the parent activity
	 * @param menu
	 */
	public void onMenuItemCreated(Menu menu){
		isParked = Preference.getBoolean("parking", false);
		
		//switch so toggle works as expected
		isParked = !isParked;
		if(!isParked){
			float lat = Preference.getFloat("parking-lat", 0f);
			float lon = Preference.getFloat("parking-lon", 0f);
			
			Location loc = new Location("My Parking Location");
			loc.setLatitude(lat);
			loc.setLongitude(lon);
			toggleParking(loc, menu.findItem(R.id.park));
		} else toggleParking(menu.findItem(R.id.park));
	}
	
	@Override
	public void onCameraChange(CameraPosition position) {
		if(firstCameraChange){
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBounds.build(), 10));
			firstCameraChange = false;
		}
		
		float zoom = position.zoom;
		if(zoom!=mCurrentZoom && mTextMarkerManager!=null){
			mCurrentZoom = zoom;
			mTextMarkerManager.refreshData(zoom);
			//Toast.makeText(getActivity(), "Zoom: " + position.zoom, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		if(marker.getSnippet().equals(""))
			MapUtils.showExhibitInfoDialog(mManager, getActivity().getLayoutInflater(), 
				getActivity(), marker);
		else {
			InfoDisplayActivity.MAP = this;
			InfoDisplayActivity.SCREEN = (SlidingScreenActivity) getActivity();
			MapUtils.startInfoActivity(mManager, (SlidingScreenActivity) getActivity(), marker);
		}
	}
	
	/**
	 * Returns the map boundary
	 */
	public LatLngBounds getMapBounds(){
		return mMapBounds.build();
	}
	
	/**
	 * This will put the focus on a location that the user navigates to, 
	 * adding an icon to the text label
	 */
	public boolean setMarkerFocus(PlaceMarker place){
		return mTextMarkerManager.addFocus(place);
	}
	
	/**
	 * Clears all icons from text markers that are focused
	 */
	public void clearFocus(){
		mTextMarkerManager.clearFocus(mCurrentZoom);
	}
	
	/**
	 * Returns the current location manager
	 * @return
	 */
	public CurrentLocationManager getManager(){
		return mManager;
	}
	
	/**
	 * Returns the last known location
	 * @return
	 */
	public Location getLastKnownLocation(){
		return mManager.getLastKnownLocation();
	}
	
	public float getCurrentZoom(){
		return mCurrentZoom;
	}
	
	/**
	 * Toggles map tracking on screen
	 * @param item
	 * @return whether we follow or not
	 */
	public boolean toggleFollow(MenuItem item){
		if(!isTracking){
			if(mManager.getLastKnownLocation()!=null){
				item.setIcon(R.drawable.ic_action_location_blue);
				MapUtils.animateTo(mGoogleMap, mManager.getLastKnownLocation());
				isTracking = true;
				Toast.makeText(getActivity(), "Now Following Current Location, Tap Map to Cancel", Toast.LENGTH_SHORT).show();
			}	else Toast.makeText(getActivity(), "Cannot find current location",  Toast.LENGTH_LONG).show();
		
			
		} else{
			item.setIcon(R.drawable.ic_action_location);
			isTracking = false;
			Toast.makeText(getActivity(), "Following Off", Toast.LENGTH_SHORT).show();
		}
		
		if(mManager.getLastKnownLocation()!=null){
			mManager.follow(isTracking);
			return true;
		} else{
			return false;
		}
	}
	
	public boolean enableNavigation(MenuItem item, Location locationTo){
		isTracking = false;
		if(toggleFollow(item)){
			mManager.navigate(locationTo);
			return true;
		} else return false;
	}
	
	public void disableNavigation(MenuItem item){
		//this will disable following
		isTracking = true;
		toggleFollow(item);
		
		mManager.disableNavigation();
	}
	
	public boolean isTracking(){
		return isTracking;
	}
	
	/**
	 * Toggles parking icon on screen
	 * @param item
	 */
	public void toggleParking(Location loc, MenuItem item){
		if(!isParked){
			addParking(loc, item);
		} else{
			removeParking(item);
		}
	}
	
	public void toggleParking(MenuItem item){
		Location loc = null;
		if(mManager!=null){
			loc = mManager.getLastKnownLocation();
		}
		toggleParking(loc, item);
	}
	
	public void addParking(MenuItem item){
		addParking(mManager.getLastKnownLocation(), item);
	}
	
	public void addParking(final Location loc, MenuItem item){
		item.setIcon(R.drawable.ic_action_key_blue);
		Preference.putFloat("parking-lat", (float) loc.getLatitude());
		Preference.putFloat("parking-lon", (float) loc.getLongitude());
		Preference.putBoolean("parking", true);
		isParked = true;
		
		if(mParkingPlace!=null){
			mParkingPlace.remove();
		}
		createParkingItems(loc);
		
		mParkingPlace.addDraggableMarker(mGoogleMap, new OnMarkerDragListener(){

			@Override
			public void onMarkerDrag(Marker marker) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				if(marker.getTitle().equals("My Parking Spot")){
					Preference.putFloat("parking-lat", (float) loc.getLatitude());
					Preference.putFloat("parking-lon", (float) loc.getLongitude());
				}
			}

			@Override
			public void onMarkerDragStart(Marker marker) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	public void removeParking(MenuItem item){
		item.setIcon(R.drawable.ic_action_key);
		isParked = false;
		Preference.putBoolean("parking", false);
		Preference.putFloat("parking-lat", 0);
		Preference.putFloat("parking-lon", 0);
		if(mParkingPlace!=null)
			mParkingPlace.remove();
	}
	
	
	/**
	 * Initializes the parking location geopoint and placeitem
	 */
	private void createParkingItems(Location location){
		mParkingLocation = new LatLng(location.getLatitude(), location.getLongitude());
		mParkingPlace = new PlaceMarker().point(mParkingLocation).iconId(R.drawable.car).name("My Parking Spot");
	}
	
	public boolean isParked(){
		return isParked;
	}
	
	public LinkedList<PlaceMarker> getSearchExhibits(){
		return searchExhibits;
	}
	
	/**
	 * Loads data in the background, updating the UI along the way
	 * @author Andrew Grosner
	 *
	 */
	private class LoadDataTask extends AsyncTask<Void, Void, Void>{

		private SlidingScreenActivity mActivity = null;
		private OnClickListener mOnClick = null;
		
		public LoadDataTask(SlidingScreenActivity act, OnClickListener onClick){
			this.mActivity = act;
			this.mOnClick = onClick;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			//reads exhibits into the searchbar list of places
			PlaceController.readInData(mActivity, mOnClick, searchExhibits, 
					getActivity().getResources().getStringArray(R.array.search_list));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			PlaceController.readInDataIntoList(mManager.getLastKnownLocation(), mActivity, mActivity.searchList, searchExhibits, mOnClick);
			
		}
	}
	
	private class LoadMapDataTask extends AsyncTask<Void, Void, Void>{

		private MapViewFragment mMap;
		
		public LoadMapDataTask(MapViewFragment map){
			mMap = map;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try{
					
	   			OverlayManager.readFiles(getActivity());
				
				if(mTextMarkerManager==null){
					mTextMarkerManager = new TextMarkerManager(mMap);
				} else{
					mTextMarkerManager.reset(mMap);
				}
				mTextMarkerManager.readInData(getActivity(),"exhibits.txt", "special.txt");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Toast.makeText(getActivity(), e1.getMessage(), Toast.LENGTH_SHORT).show();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			OverlayManager.addToMap(mGoogleMap);
			mTextMarkerManager.addToMap();
			mTextMarkerManager.refreshData(getMap().getCameraPosition().zoom);
			mGoogleMap.setOnMarkerClickListener(mTextMarkerManager);
			
			for(View v: loaders){
				v.setVisibility(View.GONE);
			}
		}
	}

}
