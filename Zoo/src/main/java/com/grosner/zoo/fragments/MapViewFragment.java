package com.grosner.zoo.fragments;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import com.grosner.smartinflater.annotation.SMethod;
import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.PlaceController;
import com.grosner.zoo.R;
import com.grosner.zoo.activities.InfoDisplayActivity;
import com.grosner.zoo.activities.ZooActivity;
import com.grosner.zoo.singletons.ExhibitManager;
import com.grosner.zoo.utils.Operations;
import com.grosner.zoo.singletons.Preference;
import com.grosner.zoo.managers.CurrentLocationManager;
import com.grosner.zoo.utils.MapUtils;
import com.grosner.zoo.managers.OverlayManager;
import com.grosner.zoo.markers.PlaceMarker;
import com.grosner.zoo.adapters.PlaceMarkerWindowAdapter;
import com.grosner.zoo.managers.TextMarkerManager;
import com.grosner.zoo.utils.ZooDialog;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;

/**
 * Class adds further functionality on top of google's SupportMapFragment such as more button options, 
 * add location information to the map, and more. Displays all of the zoo information needed
 * @author Andrew Grosner
 *
 */
public class MapViewFragment extends SupportMapFragment implements OnClickListener, OnCameraChangeListener, OnInfoWindowClickListener, MenuItem.OnMenuItemClickListener, GoogleMap.OnMapClickListener, SearchPerformListener, TextWatcher {

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
	
	@SResource private ImageButton toggle, clear;
	
	private View[] loaders;

    /**
     * the searchbar widget
     */
    private MenuItemSearchAction searchItem;

    /**
     * Holds a reference to the follow icon
     */
    private MenuItem followItem;

    private MenuItem parkItem;

    /**
     * the popup list of exhibits that shows up when a user searches for an exhibit
     */
    //@SResource(optional = true) public LinearLayout searchList;

    /**
     * items that are selected on the map
     */
    public LinkedList<PlaceMarker> selected = new LinkedList<PlaceMarker>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
	public void onPause(){
		super.onPause();
		
		mManager.deactivate();
	}
	
	@Override
	public void onResume(){
		super.onResume();

        getActivity().getActionBar().setTitle(getString(R.string.bronx_zoo));

		if(mGoogleMap==null){
			mGoogleMap = super.getMap();
			if(mGoogleMap!=null){
				setUpMap(getActivity().getLayoutInflater());
			}
		} else{
			mManager.activate();
		}
	}
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
		ViewGroup v = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		View layout = SmartInflater.inflate(this, R.layout.fragment_map);
		
		loaders = Operations.findViewByIds(layout, R.id.loading, R.id.loading_text);

		clear.setVisibility(View.GONE);
		
		v.addView(layout);
		return v;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        //adds the searchbar to the actionbar
        searchItem = new MenuItemSearchAction(getActivity(), menu, this, getResources().getDrawable(R.drawable.ic_action_search), this, null);
        searchItem.setTextColor(getResources().getColor(R.color.forestgreen));
        searchItem.getMenuItem().setOnMenuItemClickListener(this);
        searchItem.setId(0);

        inflater.inflate(R.menu.activity_sliding_screen, menu);

        followItem = menu.findItem(R.id.follow);
        toggleFollow(followItem);
        followItem.setOnMenuItemClickListener(this);
        parkItem = menu.findItem(R.id.park).setOnMenuItemClickListener(this);

        onMenuItemCreated(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.follow){
            getMap().setOnMapClickListener(this);

            if(getManager().isNavigating()){
                disableNavigation(item);
            } else	toggleFollow(item);
            return true;
        } else if(id == R.id.park){
            if(isParked()){
                //bring up menu
                final ZooDialog confirm = new ZooDialog(getActivity());
                confirm.setTitle("Parking Location Options");
                confirm.setMessage("Saves a spot on the map for reference to where you may have parked today.");
                confirm.setPositiveButton("Delete Location", new OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        removeParking(parkItem);
                        confirm.dismiss();
                    }

                });
                confirm.setNeutralButton("Reset", new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        addParking(parkItem);
                        confirm.dismiss();
                    }
                });

                confirm.setNegativeButton("Cancel", null);

                confirm.show();


            } else{
                if(getLastKnownLocation()!=null){
                    addParking(parkItem);
                } else{
                    Toast.makeText(getActivity(), "Saving Parking Location Not Available When GPS is Not Found", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }


        return false;
    }

    @Override
    public void onMapClick(LatLng point) {
        MapViewFragment map = (MapViewFragment) getFragmentManager().findFragmentByTag("MapViewFragment");
        if(map.isTracking())
            map.toggleFollow(followItem);
    }

    @SMethod
    private void onClickToggle(){
        if(!isSatelliteMap){
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mTextMarkerManager.changeTextLabelColor(Color.MAGENTA,mCurrentZoom);
            toggle.setImageResource(R.drawable.ic_action_map);
        } else{
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mTextMarkerManager.changeTextLabelColor(Color.BLACK,mCurrentZoom);
            toggle.setImageResource(R.drawable.ic_action_globe);
        }

        isSatelliteMap = !isSatelliteMap;
    }

    @SMethod
    private void onClickOverview(){
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getMapBounds(), 10));
    }

    @SMethod
    private void onClickMyLocation(){
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(MapUtils.locationToLatLng(mGoogleMap.getMyLocation())));
    }

	@Override
	public void onClick(View v) {
		int id = v.getId();
		PlaceMarker place = selected.get(id-1);
		performSearch(place);
		onClickClear();
		addPlace(place);
	}
	
	/**
	 * Sets up UI options
	 * @param inflater
	 */
	private void setUpMap(LayoutInflater inflater){

        final ZooActivity act = (ZooActivity)getActivity();
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
                    if(getActivity()!=null){
                        PlaceFragment list = ((ZooActivity)getActivity()).getCurrentPlaceFragment();
                        if(list!=null){
                            list.refresh();
                        }
                    }
   					
   				}
   					
   			});
   	   		
   		}
   		else	mManager.setFields(getActivity(), mGoogleMap);
   		mManager.activate();
   		
   		act.notifyLocationSet();
   		
   		if(ExhibitManager.getSharedInstance().getAllPlaces().isEmpty())
   			new LoadDataTask(act, frag).execute();
   		
   		mMapBounds = new LatLngBounds.Builder();
		try {
			MapUtils.generatePolygon(getActivity(), mMapBounds);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
   		new LoadMapDataTask().execute();
   		
		
	}
	
	/**
	 * Clears all showing (focused) icons, does not remove amenities icons
	 */
    @SMethod
    public void onClickClear(){
		//clean up old previous markers
		for(Marker m: mLastMarkers){
			m.remove();
		}
		mLastMarkers.clear();
        if(mTextMarkerManager!=null)
		    mTextMarkerManager.clearFocus(mCurrentZoom);
        if(clear!=null)
		    clear.setVisibility(View.GONE);
	}
	
	/**
	 * Adds a place and shows place in relation to location
	 * @param place
	 */
	public void addPlace(PlaceMarker place){
		addPlaceMove(place);
		MapUtils.moveRelativeToCurrentLocation(mManager.getLastKnownLocation(), place.getPoint(), mGoogleMap);
	}
	
	/**
	 * Adds a place to the map, moves to the location
	 * @param place
	 */
	public void addPlaceMove(PlaceMarker place){
		if(clear!=null)
            clear.setVisibility(View.VISIBLE);
		if(mTextMarkerManager!=null && !mTextMarkerManager.addFocus(place))
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
			InfoDisplayActivity.SCREEN = (ZooActivity) getActivity();
			MapUtils.startInfoActivity(mManager, (ZooActivity) getActivity(), marker);
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
		
		mParkingPlace.addDraggableMarker(mGoogleMap, new OnMarkerDragListener() {

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (marker.getTitle().equals("My Parking Spot")) {
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

    /**
     * Switches to map, collapses search bar, hides the searchlist, and sends the query to the server
     * @param place
     */
    public void performSearch(PlaceMarker place){
        MenuFragment list = (MenuFragment) getFragmentManager().findFragmentByTag
                ("MenuFragment");
        list.switchToMap();
        //Operations.removeView(searchList);
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        /*if(searchList!=null) {
            searchList.removeAllViews();
        }*/
        selected.clear();
    }

    @Override
    public void onTextChanged(final CharSequence s, int start, int before, int count) {
        if(s.length()>0){
            //searchList.removeAllViews();
            MapViewFragment map = (MapViewFragment) getFragmentManager().findFragmentByTag
                    ("MapViewFragment");
            LinkedList<PlaceMarker> exhibits = ExhibitManager.getSharedInstance().getAllPlaces();
            for(int i =0; i < exhibits.size(); i++){
                PlaceMarker place = exhibits.get(i);
                if(place.getName().toLowerCase().startsWith(s.toString().toLowerCase()))
                    selected.add(place);
            }

            Collections.sort(selected, new Comparator<PlaceMarker>() {

                @Override
                public int compare(PlaceMarker lhs, PlaceMarker rhs) {
                    Boolean lstart = lhs.getName().toLowerCase().startsWith(s.toString().toLowerCase());
                    Boolean rstart = rhs.getName().toLowerCase().startsWith(s.toString().toLowerCase());

                    return lstart.compareTo(rstart);

                }

            });

            int size = selected.size();
            for(int i = 0; i < size; i++){
                //searchList.addView(PlaceController.createExhibitItem(map.getLastKnownLocation(), i+1, selected.get(i), map));
            }
        }
    }

    @Override
    public void performSearch(String query) {
        String querie = query.toLowerCase();
        boolean found = false;
        PlaceMarker placeFound = null;

        MapViewFragment map = (MapViewFragment) getFragmentManager().findFragmentByTag
                ("MapViewFragment");
        for(PlaceMarker place: ExhibitManager.getSharedInstance().getAllPlaces()){
            String name = place.getName().toLowerCase();
            if(name.equals(querie)){
                found = true;
                placeFound = place;
                break;
            }
        }

        if(found){
            if(placeFound!=null){
                performSearch(placeFound);
                map.onClickClear();
                map.addPlace(placeFound);
            }
        } else{
            Toast.makeText(getActivity(), "Not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
	 * Loads data in the background, updating the UI along the way
	 * @author Andrew Grosner
	 *
	 */
	private class LoadDataTask extends AsyncTask<Void, Void, Void>{

		private OnClickListener mOnClick = null;
		
		public LoadDataTask(ZooActivity act, OnClickListener onClick){
			this.mOnClick = onClick;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			//reads exhibits into the searchbar list of places
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
		    //PlaceController.readInDataIntoList(mManager.getLastKnownLocation(), searchList, ExhibitManager.getSharedInstance().getAllPlaces(), mOnClick);
		}
	}
	
	private class LoadMapDataTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try{
					
	   			OverlayManager.readFiles(getActivity());
				
				if(mTextMarkerManager==null){
					mTextMarkerManager = new TextMarkerManager(MapViewFragment.this.mGoogleMap, getActivity());
				} else{
					mTextMarkerManager.reset(MapViewFragment.this.mGoogleMap, getActivity());
				}
				mTextMarkerManager.readInData("exhibits.txt", "special.txt");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result){
			OverlayManager.addToMap(mGoogleMap);
			mTextMarkerManager.addToMap();
			mTextMarkerManager.refreshData(mGoogleMap.getCameraPosition().zoom);
			mGoogleMap.setOnMarkerClickListener(mTextMarkerManager);
			
			for(View v: loaders){
				v.setVisibility(View.GONE);
			}
		}
	}

}
