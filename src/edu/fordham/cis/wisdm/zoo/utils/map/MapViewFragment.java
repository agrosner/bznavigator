package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.Operations;

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

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SlidingScreenActivity;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragmentList;
import edu.fordham.cis.wisdm.zoo.utils.Preference;

public class MapViewFragment extends SupportMapFragment implements OnClickListener, OnCameraChangeListener, OnInfoWindowClickListener{

	private GoogleMap mGoogleMap = null;

	/**
	 * Manages the text overlays displayed on the map
	 */
	private TextMarkerManager mTextMarkerManager;
	
	/**
	 * The current zoom of the map
	 */
	private float mCurrentZoom = 0;
	
	private LatLngBounds.Builder mBuilder = new LatLngBounds.Builder();
	
	private View mLayout = null;
	
	private LayoutInflater mInflater = null;
	
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
	private PlaceItem mParkingPlace = null;

	/**
	 * Restrooms list
	 */
	LinkedList<PlaceItem> restrooms = new LinkedList<PlaceItem>();
	
	/**
	 * Gates list
	 */
	LinkedList<PlaceItem> gates = new LinkedList<PlaceItem>();
	
	/**
	 * Parking list
	 */
	LinkedList<PlaceItem> parking = new LinkedList<PlaceItem>();
	
	/**
	 * Shops list
	 */
	LinkedList<PlaceItem> shop = new LinkedList<PlaceItem>();
	/**
	 * List of searchable places
	 */
	private LinkedList<PlaceItem> searchExhibits = new LinkedList<PlaceItem>();
	
	/**
	 * Provides an action when the user's current location changes
	 */
	private Runnable meListener = new Runnable(){

		@Override
		public void run() {
			PlaceController.reCalculateDistance(mManager.getLastKnownLocation(), 
					searchExhibits, parking, gates, restrooms);
		}
		
	};
	
	private LinkedList<Marker> mLastMarkers = new LinkedList<Marker>();
	
	@Override
	public void onPause(){
		super.onPause();
		mManager.stop();
	}
	
	@Override
	public void onResume(){
		super.onResume();

		mGoogleMap = super.getMap();
		if(mGoogleMap!=null){
			setUpMap(mInflater);
		}
	}
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
		ViewGroup v = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		mLayout = inflater.inflate(R.layout.fragment_map, container, false);
		
		Operations.setViewOnClickListeners(mLayout, this, R.id.satellite, R.id.normal, R.id.overview, R.id.clear);
		Operations.removeView(mLayout.findViewById(R.id.clear));
		
		mInflater = inflater;
		
		v.addView(mLayout);
		return v;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.satellite){
			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			mTextMarkerManager.changeTextLabelColor(Color.MAGENTA,mCurrentZoom);
		} else if(id == R.id.normal){
			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mTextMarkerManager.changeTextLabelColor(Color.BLACK,mCurrentZoom);
		} else if(id == R.id.overview){
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getMapBounds(), 10));
		} else if(id == R.id.clear){
			clearMap();
		} else{
			SlidingScreenActivity act = ((SlidingScreenActivity) getActivity());
			PlaceItem place = act.selected.get(id-1);
			
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
		mGoogleMap.setOnCameraChangeListener(this);
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setInfoWindowAdapter(new PlaceItemWindowAdapter(inflater));
		mGoogleMap.setOnInfoWindowClickListener(this);
   		mGoogleMap.getUiSettings().setCompassEnabled(false);
   		mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
		
   		if(mManager==null){
   			mManager = new CurrentLocationManager(getActivity(), mGoogleMap);
   			mManager.runOnFirstFix(meListener);
   		}
   		else	mManager.setFields(getActivity(), mGoogleMap);
   		
   		((SlidingScreenActivity)getActivity()).notifyLocationSet();
   		
   		final MapViewFragment frag = this;
   		mManager.runOnFirstFix(new Runnable(){
   			
			@Override
			public void run() {
				mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mBuilder.build(), 10));
				PlaceFragmentList list = ((SlidingScreenActivity)getActivity()).getCurrentPlaceFragment();
				if(list!=null){
					list.refresh();
				}
				new LoadDataTask(((SlidingScreenActivity)getActivity()), frag).execute();
			}
				
		});
		mManager.start();
		
		try{
		   		
   			MapUtils.generatePolygon(getActivity(), mBuilder);
			Paths.readFiles(getActivity());
			Paths.addToMap(mGoogleMap);
			
			Exhibits.readFiles(getActivity());
			Exhibits.addToMap(mGoogleMap);
			
			if(mTextMarkerManager==null){
				mTextMarkerManager = new TextMarkerManager(getActivity(), mGoogleMap);
				TextMarkerManager.readInData(mTextMarkerManager, getActivity(),"exhibits.txt", "special.txt");
			} else{
				mTextMarkerManager.reset(getActivity(), mGoogleMap);
			}
			
			mTextMarkerManager.addToMap();
			mTextMarkerManager.refreshData(getMap().getCameraPosition().zoom);
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(getActivity(), e1.getMessage(), Toast.LENGTH_SHORT).show();
		}
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
	public void addPlace(PlaceItem place){
		Operations.addView(getView().findViewById(R.id.clear));
		MapUtils.moveRelativeToCurrentLocation(mManager.getLastKnownLocation(),place.getPoint(), mGoogleMap);
		
		//if not within the text marker manager, we add icon to map
		if(!mTextMarkerManager.addFocus(place))
			mLastMarkers.add(place.addMarker(mGoogleMap));
	}
	
	/**
	 * Adds a list of placeitems to the map
	 * @param places
	 */
	public void addPlaceList(LinkedList<PlaceItem> places){
		Operations.addView(getView().findViewById(R.id.clear));
		MapUtils.moveToBounds(mManager.getLastKnownLocation(), mGoogleMap, places);
		for(PlaceItem place: places){
			//if not within the text marker manager, we add icon to map
			if(!mTextMarkerManager.addFocus(place))
				mLastMarkers.add(place.addMarker(mGoogleMap));
		}
	}
	
	public void onMenuItemCreated(Menu menu){
		isParked = Preference.getBoolean("parking", false);
		
		//switch so toggle works as expected
		isParked = !isParked;
		if(!isParked){
			float lat = Preference.getFloat("parking-lat", 0);
			float lon = Preference.getFloat("parking-lon", 0);
			
			Location loc = new Location("My Parking Location");
			loc.setLatitude(lat);
			loc.setLongitude(lon);
			toggleParking(loc, menu.findItem(R.id.park));
		} else toggleParking(menu.findItem(R.id.park));
	}
	
	@Override
	public void onCameraChange(CameraPosition position) {
		float zoom = position.zoom;
		if(zoom!=mCurrentZoom){
			mCurrentZoom = zoom;
			mTextMarkerManager.refreshData(zoom);
			//Toast.makeText(getActivity(), "Zoom: " + position.zoom, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		MapUtils.showExhibitInfoDialog(mManager.getLastKnownLocation(),
				getActivity().getLayoutInflater(), 
				getActivity(), marker);
	}
	
	public LatLngBounds getMapBounds(){
		return mBuilder.build();
	}
	
	/**
	 * This will put the focus on a location that the user navigates to, 
	 * adding an icon to the text label
	 */
	public boolean setMarkerFocus(PlaceItem place){
		return mTextMarkerManager.addFocus(place);
	}
	
	/**
	 * Clears all icons from text markers that are focused
	 */
	public void clearFocus(){
		mTextMarkerManager.clearFocus(mCurrentZoom);
	}
	
	public CurrentLocationManager getManager(){
		return mManager;
	}
	
	/**
	 * Toggles map tracking on screen
	 * @param item
	 */
	public void toggleFollow(MenuItem item){
		if(!isTracking){
			if(mManager.getLastKnownLocation()!=null){
				item.setIcon(R.drawable.ic_action_location_blue);
				MapUtils.animateTo(mGoogleMap, mManager.getLastKnownLocation());
				isTracking = true;
				MessageBuilder.showToast("Now Following Current Location, Tap Map to Cancel", getActivity());
			}	else MessageBuilder.showLongToast("Cannot find current location",  getActivity());
		
			
		} else{
			item.setIcon(R.drawable.ic_action_location);
			isTracking = false;
			MessageBuilder.showToast("Following Off", getActivity());
		}
		mManager.follow(isTracking);
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
	
	public void toggleParking(MenuItem item){
		toggleParking(mManager.getLastKnownLocation(), item);
	}
	
	/**
	 * Initializes the parking location geopoint and placeitem
	 */
	private void createParkingItems(Location location){
		mParkingLocation = new LatLng(location.getLatitude(), location.getLongitude());
		mParkingPlace = new PlaceItem().point(mParkingLocation).iconId(R.drawable.car).name("My Parking Spot");
	}
	
	public boolean isParked(){
		return isParked;
	}
	
	public LinkedList<PlaceItem> getSearchExhibits(){
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
			PlaceController.readInData(mManager.getLastKnownLocation(), mActivity, mOnClick, searchExhibits, "exhibits.txt", "food.txt", "shops.txt",
					"gates.txt", "parking.txt", "admin.txt", "special.txt", "restrooms.txt", "misc.txt");
			return null;
		}
		
		protected Void onPostExecute(){
			PlaceController.readInDataIntoList(mManager.getLastKnownLocation(), mActivity, mActivity.searchList, searchExhibits, mOnClick, false);
			return null;
			
		}
	}

}
