package com.grosner.zoo.fragments;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.grosner.painter.IconPainter;
import com.grosner.painter.slider.ColorSlider;
import com.grosner.painter.slider.SliderUtils;
import com.grosner.smartinflater.annotation.SMethod;
import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.R;
import com.grosner.zoo.activities.ZooActivity;
import com.grosner.zoo.adapters.ExhibitAdapter;
import com.grosner.zoo.adapters.PlaceMarkerWindowAdapter;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.location.OnNavigateListener;
import com.grosner.zoo.database.managers.OverlayManager;
import com.grosner.zoo.database.managers.TextMarkerManager;
import com.grosner.zoo.markers.PlaceMarker;
import com.grosner.zoo.singletons.ExhibitManager;
import com.grosner.zoo.singletons.Preference;
import com.grosner.zoo.utils.DeviceInfo;
import com.grosner.zoo.utils.MapUtils;
import com.grosner.zoo.utils.Polygon;
import com.grosner.zoo.utils.StringUtils;
import com.grosner.zoo.utils.ViewUtils;
import com.grosner.zoo.utils.ZooDialog;
import com.grosner.zoo.widgets.ClearableTextView;
import com.grosner.zoo.widgets.ExhibitItemView;
import com.grosner.zoo.widgets.InstantAutoComplete;
import com.grosner.zoo.widgets.RestrictedMapView;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.appetites.android.menuItemSearchAction.SearchPerformListener;

/**
 * Class adds further functionality on top of google's SupportMapFragment such as more button options, 
 * add location information to the map, and more. Displays all of the zoo information needed
 * @author Andrew Grosner
 *
 */
public class MapViewFragment extends Fragment implements OnClickListener, OnCameraChangeListener,
        OnInfoWindowClickListener, MenuItem.OnMenuItemClickListener, GoogleMap.OnMapClickListener,
        SearchPerformListener, TextWatcher, TextView.OnEditorActionListener,
        MenuItem.OnActionExpandListener, OnNavigateListener {

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
	
	@SResource private ImageButton toggle, clear, overview, myLocation;

    private ColorSlider mSearchSlider, mLocationSlider;

    private IconPainter mMapPainter;

    /**
     * Holds a reference to the follow icon
     */
    private MenuItem followItem;

    private MenuItem parkItem;

    private MenuItem searchItem;

    MenuItem mExpandedItem = null;

    public boolean mSearchExpanded = false;

    private ClearableTextView mSearch;

    private InstantAutoComplete mSearchView;

    private Handler mHandler = new Handler();

    private Polygon mPolygon;

    private boolean mInTransition = false;

    /**
     * the popup list of exhibits that shows up when a user searches for an exhibit
     */
    //@SResource(optional = true) public LinearLayout searchList;

    /**
     * items that are selected on the map
     */
    public LinkedList<PlaceMarker> selected = new LinkedList<PlaceMarker>();

    private ExhibitAdapter mSearchAdapter;

    @SResource private RestrictedMapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        try {
            mPolygon = MapUtils.readPolygonCoords(getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMapBounds = new LatLngBounds.Builder();
        try {
            MapUtils.generatePolygon(getActivity(), mMapBounds);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mSearchSlider = new ColorSlider(getResources().getColor(R.color.actionbar_color),
                getResources().getColor(R.color.actionbar_text_color)).with(new IconPainter());
        mLocationSlider = new ColorSlider(getResources().getColor(R.color.actionbar_color),
                getResources().getColor(R.color.actionbar_text_color)).with(new IconPainter());
        mMapPainter = new IconPainter(true, getResources().getColor(R.color.icon_color));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) SmartInflater.inflate(this, R.layout.fragment_map);

        mapView.setVisibleRegion(15, 20, mMapBounds.build());
        mapView.onCreate(savedInstanceState);
        mapView.initialize();
        CurrentLocationManager.getSharedManager().registerOnNavigateListener(this);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapPainter.paint(view.findViewById(R.id.clear),
                toggle, myLocation, overview);
    }

    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
        if(mGoogleMap==null){
            mGoogleMap = mapView.getMap();
            if(mGoogleMap!=null){
                setUpMap(getActivity().getLayoutInflater());
            }
        } else{
            CurrentLocationManager.getSharedManager().activate();
        }
    }

    @Override
	public void onPause(){
		super.onPause();
        mapView.onPause();
		CurrentLocationManager.getSharedManager().deactivate();
	}
	
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        mapView = null;
        CurrentLocationManager.getSharedManager().unRegisterOnNavigateListener(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        if(mSearch==null){
            mSearch = new ClearableTextView(getActivity());
            mSearchView = mSearch.eText;

            int pad = DeviceInfo.dp(8);

            mSearchView.setTextColor(Color.BLACK);
            mSearchView.setPadding(pad, 0, pad, 0);
            mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            mSearchView.setOnEditorActionListener(this);
            mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            mSearchView.setHint(Html.fromHtml("<i>Search</i>"));
            mSearchView.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
            mSearchView.addTextChangedListener(this);
            mSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ExhibitItemView exhibitItemView = ((ExhibitItemView) view);
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

                    if(mExpandedItem!=null) {
                        mExpandedItem.collapseActionView();
                    }
                    onClickClear();
                    addPlace(new PlaceMarker().place(exhibitItemView.getPlace()));
                }
            });


            //TODO: add all exhibits adapter here
            mSearchView.setAdapter(mSearchAdapter = new ExhibitAdapter(PlaceFragment.PlaceType.SEARCH));

        }
        //adds the searchbar to the actionbar
        searchItem = menu.add(0, R.id.search, 0, getString(R.string.search))
                .setActionView(mSearch).setOnActionExpandListener(this)
                .setIcon(getResources().getDrawable(R.drawable.ic_action_search));
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        inflater.inflate(R.menu.activity_sliding_screen, menu);

        followItem = menu.findItem(R.id.follow);
        followItem.setIcon(getResources().getDrawable(isTracking? R.drawable.ic_action_location_blue : R.drawable.ic_action_location));
        followItem.setOnMenuItemClickListener(this);

        mSearchSlider.onSlide(0, searchItem);
        mLocationSlider.onSlide(0, followItem);

        parkItem = menu.findItem(R.id.park).setOnMenuItemClickListener(this);
        menu.findItem(R.id.menu_amenities).setOnMenuItemClickListener(this);

        onMenuItemCreated(menu);

        if(mInTransition) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchItem.collapseActionView();
                }
            }, 25);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                item.expandActionView();
                break;
            default:
               return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        if (!mSearchExpanded && !mInTransition) {
            mSearchExpanded = true;
            mInTransition = false;
            if (getActivity() != null)
                getActivity().supportInvalidateOptionsMenu();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if(getActivity()!=null){
                        mSearchView.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mSearchView, InputMethodManager.SHOW_FORCED);
                        mSearchView.showDropDown();
                    }

                }
            }, 150);
        }
        mExpandedItem = item;
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        if (mSearchExpanded) {
            mSearchExpanded = false;
            mInTransition = true;
            getActivity().supportInvalidateOptionsMenu();
            getZooActivity().closeKeyboards();
            mSearchView.clearFocus();
        } else {
            mInTransition = false;
        }
        return true;
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if(id== R.id.menu_amenities){
            getZooActivity().closeDrawers();
            getZooActivity().openDrawer(Gravity.RIGHT);
        } else if(id == R.id.follow){
            getMap().setOnMapClickListener(this);

            if(CurrentLocationManager.getSharedManager().isNavigating()){
                disableNavigation(item);
            } else	toggleFollow(true);
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
                if(CurrentLocationManager.getSharedManager().getLastKnownLocation()!=null){
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
            map.toggleFollow(false);
    }

    @SMethod
    private void onClickToggle(){
        if(!isSatelliteMap){
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            mTextMarkerManager.changeTextLabelColor(Color.MAGENTA, mCurrentZoom);
            toggle.setImageResource(R.drawable.ic_action_map);
        } else{
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mTextMarkerManager.changeTextLabelColor(Color.BLACK,mCurrentZoom);
            toggle.setImageResource(R.drawable.ic_action_globe);
        }

        mMapPainter.paint(toggle);

        isSatelliteMap = !isSatelliteMap;
    }

    @SMethod
    private void onClickOverview(){
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getMapBounds(), 10));
    }

    @SMethod
    private void onClickMyLocation(){
        LatLng latLng = MapUtils.locationToLatLng(mGoogleMap.getMyLocation());
        if(mMapBounds.build().contains(latLng)) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        } else{
            String message;
            if(latLng==null){
                message = getString(R.string.cannot_find_current_location);
            } else{
                message = getString(R.string.user_outside_map_error);
            }
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

	@Override
	public void onClick(View v) {
		int id = v.getId();
		PlaceMarker place = selected.get(id-1);
		onClickClear();
		addPlace(place);
	}
	
	/**
	 * Sets up UI options
	 * @param inflater
	 */
	private void setUpMap(LayoutInflater inflater){
        mapView.setOnCameraChangeListener(this);
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setInfoWindowAdapter(new PlaceMarkerWindowAdapter());
		mGoogleMap.setOnInfoWindowClickListener(this);
   		mGoogleMap.getUiSettings().setCompassEnabled(false);
   		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.setBuildingsEnabled(false);
        CurrentLocationManager.getSharedManager().setLocation(mGoogleMap.getMyLocation());
        CurrentLocationManager.getSharedManager().runOnFirstFix(new Runnable() {

            @Override
            public void run() {
                if (getActivity() != null) {
                    PlaceFragment list = ((ZooActivity) getActivity()).getCurrentPlaceFragment();
                    if (list != null) {
                        list.refresh();
                    }
                }

            }

        });
   		CurrentLocationManager.getSharedManager().activate();
   		
   		getZooActivity().notifyLocationSet();
   		
   		if(ExhibitManager.getSharedInstance().getAllPlaces().isEmpty())
   			new LoadDataTask(getZooActivity(), this).execute();
   		
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
	 * Adds a place and shows place in relation to location if the user is within the Zoo
	 * @param place
	 */
	public void addPlace(PlaceMarker place){
		addPlaceMove(place);
        if(mPolygon==null || (CurrentLocationManager.getSharedManager().getLastKnownLocation()!=null
                && mPolygon.contains(CurrentLocationManager.getSharedManager().getLastKnownLocation()))) {
            MapUtils.moveRelativeToCurrentLocation(CurrentLocationManager.getSharedManager().getLastKnownLocation(),
                    place.getPoint(), mGoogleMap);
        }
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
	public void addPlaceList(List<PlaceMarker> places){
		ViewUtils.setViewsVisible(clear);
        Location lastLoc = CurrentLocationManager.getSharedManager().getLastKnownLocation();
		MapUtils.moveToBounds(lastLoc, mGoogleMap, places,
                mPolygon!=null && lastLoc!=null && mPolygon.contains(lastLoc));
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
        MapUtils.startInfoActivity(getZooActivity(), marker);
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
	
	public float getCurrentZoom(){
		return mCurrentZoom;
	}
	
	/**
	 * Toggles map tracking on screen
	 * @param item
	 * @return whether we follow or not
	 */
	public boolean toggleFollow(boolean showMessage){
		if(!isTracking){
			if(CurrentLocationManager.getSharedManager().getLastKnownLocation()!=null) {
                mLocationSlider.setStartColor(getResources().getColor(R.color.curious_blue));
                mLocationSlider.setEndColor(getResources().getColor(R.color.curious_blue));
                MapUtils.animateTo(mGoogleMap, CurrentLocationManager.getSharedManager().getLastKnownLocation());
                isTracking = true;
                Toast.makeText(getActivity(), getString(R.string.now_following_current_location),
                        Toast.LENGTH_SHORT).show();
            } else if(showMessage){
                Toast.makeText(getActivity(), getString(R.string.cannot_find_current_location),
                        Toast.LENGTH_LONG).show();
            }
		} else{
			mLocationSlider.setStartColor(getResources().getColor(R.color.actionbar_color));
            mLocationSlider.setEndColor(Color.WHITE);
			isTracking = false;
			Toast.makeText(getActivity(), "Following Off", Toast.LENGTH_SHORT).show();
		}

        getZooActivity().onDrawerSlide(null, getZooActivity().getDrawerOffset());

		if(CurrentLocationManager.getSharedManager().getLastKnownLocation()!=null){
			CurrentLocationManager.getSharedManager().follow(isTracking);
			return true;
		} else{
			return false;
		}
	}
	
	public boolean enableNavigation(MenuItem item, Location locationTo){
		isTracking = false;
		if(toggleFollow(true)){
			CurrentLocationManager.getSharedManager().navigate(locationTo);
			return true;
		} else return false;
	}
	
	public void disableNavigation(MenuItem item){
		//this will disable following
		isTracking = true;
		toggleFollow(true);
		
		CurrentLocationManager.getSharedManager().disableNavigation();
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
		toggleParking(CurrentLocationManager.getSharedManager().getLastKnownLocation(), item);
	}
	
	public void addParking(MenuItem item){
		addParking(CurrentLocationManager.getSharedManager().getLastKnownLocation(), item);
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

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        /*if(searchList!=null) {
            searchList.removeAllViews();
        }*/
    }

    @Override
    public void onTextChanged(final CharSequence s, int start, int before, int count) {
        if(s.length()>0){
            mSearchAdapter.getFilter().filter(s);
        }
    }

    @Override
    public void performSearch(String query) {

        if (StringUtils.stringNotNullOrEmpty(query)) {
            if (mSearchView != null) {
                mSearchView.setText("");
            }
            if (mExpandedItem != null) {
                mExpandedItem.collapseActionView();
            }

            if(getActivity()!=null){
                getZooActivity().closeDrawers();
            }
            String querie = query.toLowerCase();

            boolean found = false;
            PlaceObject placeFound = null;

            List<PlaceObject> placeObjects = mSearchAdapter.getObjects();
            for(PlaceObject place: placeObjects){
                String name = place.getName().toLowerCase();
                if(name.equals(querie)){
                    found = true;
                    placeFound = place;
                    break;
                }
            }

            if(found){
                onClickClear();
                addPlace(new PlaceMarker().place(placeFound));
            } else{
                Toast.makeText(getActivity(), "Not found", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
            return true;
        }
        if (StringUtils.stringNotNullOrEmpty(mSearchView.getText().toString())
                && getActivity() != null
                && !getActivity
                ().isFinishing()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

            performSearch(mSearchView.getText().toString());
            return true;
        }
        return true;
    }

    @Override
    public void onNavigated(LatLng location, float bearing) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .bearing(bearing)
                        .zoom(mGoogleMap.getCameraPosition().zoom)
                        .tilt(mGoogleMap.getCameraPosition().tilt)
                        .target(location)
                        .build()
        ));
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
				mTextMarkerManager.loadMarkers();
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
			ViewUtils.setViewsGone(MapViewFragment.this, R.id.loading, R.id.loading_text);
		}
	}



    public void onDrawerSlide(float offset){
        mSearchSlider.onSlide(offset, searchItem);
        mLocationSlider.onSlide(offset, followItem);
        TextView titleView = getZooActivity().getActionBarTitleView();
        if(titleView!=null){
            titleView.setTextColor(SliderUtils.calculateColor(offset, Color.BLACK, getResources().getColor(R.color.actionbar_text_color)));
        }
    }

    protected ZooActivity getZooActivity(){
        return (ZooActivity) getActivity();
    }

    protected GoogleMap getMap(){
        return mGoogleMap;
    }
}
