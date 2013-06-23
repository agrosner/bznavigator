package edu.fordham.cis.wisdm.zoo.main;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;

import edu.fordham.cis.wisdm.zoo.main.places.AmenitiesFragment;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragmentList;
import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.Operations;
import edu.fordham.cis.wisdm.zoo.utils.ZooDialog;
import edu.fordham.cis.wisdm.zoo.utils.map.MapViewFragment;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceMarker;

/**
 * Class displays the main menu that will switch between different fragments
 * @author Andrew Grosner
 *
 */
public class SlidingScreenActivity extends SlidingFragmentActivity implements SearchPerformListener, TextWatcher, OnClickListener, OnMenuItemClickListener, OnMapClickListener {

	protected static final String TAG = "SlidingScreenActivity";

	/**
	 * The content of the activity that is currently showing
	 */
	private Fragment mContent = null;
	
	/**
	 * The currently selected placefragment
	 */
	private PlaceFragmentList mCurrentPlaceFragment = null;
	
	/**
	 * The amenities fragment accessed by swiping from the right edge of the screen
	 */
	private AmenitiesFragment mAmenities;
	
	/**
	 * The list accessible by tapping back button, swiping from the left, or by touching the top left icon on the screen
	 */
	public SlidingScreenList mList = null;
	
	/**
	 * the popup list of exhibits that shows up when a user searches for an exhibit
	 */
	public LinearLayout searchList;
	
	/**
	 * items that are selected on the map
	 */
	public LinkedList<PlaceMarker> selected = new LinkedList<PlaceMarker>();
	
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
	 * The user login credentials in order to log into the server and send information
	 */
	private Connections mUser = null;
	
	private boolean isLargeScreen = false;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Bronx Zoo");
		
		DisplayMetrics display = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(display);
    	int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                
    	if(screensize >= Configuration.SCREENLAYOUT_SIZE_LARGE)	isLargeScreen = true;
    	
    	if(isLargeScreen){
    		setRequestedOrientation(Configuration.ORIENTATION_LANDSCAPE);
    	} else{
    		setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
    	}
		
		//cancel button used for both dialogs
		OnClickListener cancel = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		};
			
		if(savedInstanceState==null){
			final ZooDialog terms = new ZooDialog(this);
			terms.setNegativeButton("I do not accept", cancel);
			terms.setTitle("Terms and Conditions");
			terms.setMessage("Terms and conditions go here");
			terms.setPositiveButton("I accept", null);
			terms.setCancelable(false);
			terms.show();
			
		}
		LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//	if network error message
		if(connect.getActiveNetworkInfo()==null || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			final ZooDialog gpsInternet = new ZooDialog(this);
			
			
			gpsInternet.setNegativeButton("Quit", cancel);
			gpsInternet.setTitle("Please Turn on GPS and Internet");
			gpsInternet.setMessage("Please navigate to settings and make sure GPS and Internet is turned on for the full experience.");
			gpsInternet.setPositiveButton("Settings", new OnClickListener(){
				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);	
					finish();
			}
				
			});
			gpsInternet.setNeutralButton("Continue Anyways", new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					gpsInternet.dismiss();
					init(savedInstanceState);
				}
			});
				
			gpsInternet.show();
		} else{
			init(savedInstanceState);
		}
		
		setSlidingActionBarEnabled(false);
		
		SlidingMenu sm = getSlidingMenu();
		setMode(sm);
		
		
		if(savedInstanceState!=null){
			mAmenities = (AmenitiesFragment) getSupportFragmentManager().getFragment(savedInstanceState, "amenities");
			mList = (SlidingScreenList) getSupportFragmentManager().getFragment(savedInstanceState, "list");
			Toast.makeText(this, "Activity recreated.", Toast.LENGTH_SHORT).show();
		} 
		
		if(mList==null)			mList = new SlidingScreenList();
		if(mContent ==null)		mContent = mList.getSelectedFragment(this, 0);
		if(mAmenities==null)	mAmenities = new AmenitiesFragment();
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.menu, mList);
		ft.replace(R.id.map_content, mContent);
		ft.replace(R.id.fragment_amenities_content, mAmenities).commit();
	}
	
	
	private void setMode(SlidingMenu sm){
		setContentView(R.layout.activity_slide_splash);
		
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);
		sm.setSlidingEnabled(true);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		
		//if the left pane view is null, the screen size is small
		if(findViewById(R.id.menu) == null){
			sm.setMode(SlidingMenu.LEFT_RIGHT);
			setBehindContentView(R.layout.activity_slide_menu);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			sm.setSecondaryMenu(R.layout.fragment_amenities);
		} else{
			//large screen, only amenities is hidden by the menu so we choose only right sided option
			sm.setMode(SlidingMenu.RIGHT);
			setBehindContentView(R.layout.fragment_amenities);
		}
		
	}
	
	/**
	 * Called when activity destroyed, saves fragments and data
	 */
	@Override
	public void onSaveInstanceState(Bundle outState){
		
		getSupportFragmentManager().putFragment(outState, "amenities", mAmenities);
		getSupportFragmentManager().putFragment(outState, "list", mList);
		
		outState.putSerializable("user", mUser);
		super.onSaveInstanceState(outState);
		
	}
	
	
	/**
	 * Begins the location update service and acquiring login credentials
	 */
	public void init(Bundle instance){
		if(instance==null)
			mUser = (Connections) getIntent().getExtras().getSerializable("user");
		else	mUser = (Connections) instance.getSerializable("user");
		if(!Operations.isRunning(LocationUpdateService.class, getApplicationContext())){
			Intent i = new Intent(this, LocationUpdateService.class);
			i.putExtra("user", mUser);
			startService(i);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		searchList = (LinearLayout) findViewById(R.id.SearchList);
		
		//adds the searchbar to the actionbar
		searchItem = new MenuItemSearchAction(this, menu, this, getResources().getDrawable(R.drawable.ic_action_search), this, searchList);
		searchItem.setTextColor(getResources().getColor(R.color.forestgreen));
		searchItem.getMenuItem().setOnMenuItemClickListener(this);
		searchItem.setId(0);
	
		getSupportMenuInflater().inflate(R.menu.activity_sliding_screen, menu);
		
		setFollowItem(menu.findItem(R.id.follow).setOnMenuItemClickListener(this));
		parkItem = menu.findItem(R.id.park).setOnMenuItemClickListener(this);
		
		menu.findItem(R.id.about).setOnMenuItemClickListener(this);
		menu.findItem(R.id.settings).setOnMenuItemClickListener(this);
		
		mList.getMapFragment().onMenuItemCreated(menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Switches between fragments or mapview showing/notshowing
	 * @param fragment
	 */
	public void switchContent(final Fragment fragment) {
		if(mContent instanceof MapViewFragment && mContent.getView()!=null){
			Operations.removeView(mContent.getView());
		}
		if(fragment instanceof MapViewFragment){
			Operations.addView(fragment.getView());
		} else{
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_content, fragment).commit();
			mCurrentPlaceFragment = (PlaceFragmentList) fragment;
		}
		mContent = fragment;
		getSlidingMenu().showContent();
	}
	
	/**
	 *	Once the currentlocationmanager is instantiated, we perform an action here
	 */
	public void notifyLocationSet(){
		mAmenities.setCheckboxes(true);
	}

	@Override
	public void onBackPressed(){
		if(!getSlidingMenu().isMenuShowing()){
			getSlidingMenu().toggle();
		} 
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch(item.getItemId()){
		case R.id.settings:
			startActivity(new Intent(this, PrefActivity.class));
			return true;
		case R.id.about:
			ZooDialog message = new ZooDialog(this);
			message.setTitle("About:");
			message.setMessage("\nDeveloper: Andrew Grosner\n\t\t\t\t  agrosner@fordham.edu\n" +
					"\nAssistant Developer: Isaac Ronan\n" 
					+ "\nWireless Sensor Data Mining (WISDM)"
					+ "\nSpecial Thanks to: Fordham University");
			message.setNeutralButton("Ok", null);
			message.show();
			return true;
		case 0:
			mList.switchToMap();
			return true;
		case R.id.follow:
			mList.switchToMap();
			mList.getMapFragment().getMap().setOnMapClickListener(this);
			
			if(mList.getMapFragment().getManager().isNavigating()){
				mList.getMapFragment().disableNavigation(item);
			} else	mList.getMapFragment().toggleFollow(item);
			return true;
		case R.id.park:
			mList.switchToMap();
			if(mList.getMapFragment().isParked()){
				//bring up menu
				final ZooDialog confirm = new ZooDialog(this);
				confirm.setTitle("Parking Location Options");
				confirm.setMessage("Saves a spot on the map for reference to where you may have parked today.");
				confirm.setPositiveButton("Delete Location", new OnClickListener(){

					@Override
					public void onClick(View v) {
						mList.getMapFragment().removeParking(parkItem);
						confirm.dismiss();
					}
					
				});
				confirm.setNeutralButton("Reset", new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mList.getMapFragment().addParking(parkItem);
						confirm.dismiss();
					}
				});
				
				confirm.setNegativeButton("Cancel", null);
				
				confirm.show();
				
				
			} else{
				if(mList.getMapFragment().getLastKnownLocation()!=null){
					mList.getMapFragment().addParking(parkItem);
				} else{
					Toast.makeText(this, "Saving Parking Location Not Available When GPS is Not Found", Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Switches to map, collapses search bar, hides the searchlist, and sends the query to the server
	 * @param place
	 */
	public void performSearch(PlaceMarker place){
		mList.switchToMap();
		searchItem.getMenuItem().collapseActionView();
		Operations.removeView(searchList);
		sendSearchQuery(place.getName());
	}
	
	@Override
	public void afterTextChanged(Editable s) {}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		searchList.removeAllViews();
		selected.clear();
	}

	@Override
	public void onTextChanged(final CharSequence s, int start, int before, int count) {
		if(s.length()>0){
			searchList.removeAllViews();
			for(int i =0; i < mList.getMapFragment().getSearchExhibits().size(); i++){
				PlaceMarker place = mList.getMapFragment().getSearchExhibits().get(i);
				if(place.getName().toLowerCase().startsWith(s.toString().toLowerCase()))
					selected.add(place);
				}
			
			Collections.sort(selected, new Comparator<PlaceMarker>(){

				@Override
				public int compare(PlaceMarker lhs, PlaceMarker rhs) {
					Boolean lstart = lhs.getName().toLowerCase().startsWith(s.toString().toLowerCase());
					Boolean rstart = rhs.getName().toLowerCase().startsWith(s.toString().toLowerCase());
					
					return lstart.compareTo(rstart);
				
				}
				
			});
			
			int size = selected.size();
			for(int i = 0; i < size; i++){
				searchList.addView(PlaceController.createExhibitItem(mList.getMapFragment().getLastKnownLocation(),
						this, i+1, selected.get(i), mList.getMapFragment()));
			}
		}
	}

	@Override
	public void performSearch(String query) {
		String querie = query.toLowerCase();
		boolean found = false;
		PlaceMarker placeFound = null;
		
		for(PlaceMarker place: mList.getMapFragment().getSearchExhibits()){
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
				mList.getMapFragment().clearMap();
				mList.getMapFragment().addPlace(placeFound);
				sendSearchQuery(querie);
			}
		} else{
			Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 
	 * @param querie
	 */
	public void sendSearchQuery(final String querie){
		new Thread(){
			@Override
			public void run(){
			if(!Connections.sendSearchQuery(
				mUser,querie, mList.getMapFragment().getLastKnownLocation()))
				Log.e(TAG, "Failed sending query: " + querie);
				else Log.d(TAG, "Query sent: " + querie); 
			}
		}.start();
	}
	

	@Override
	public void onMapClick(LatLng point) {
		if(mList.getMapFragment().isTracking())
			mList.getMapFragment().toggleFollow(getFollowItem());
	}	
	
	public MenuItem getParkingIcon(){
		return parkItem;
	}
	
	public PlaceFragmentList getCurrentPlaceFragment(){
		return mCurrentPlaceFragment;
	}

	public MenuItem getFollowItem() {
		return followItem;
	}

	public void setFollowItem(MenuItem followItem) {
		this.followItem = followItem;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
}
