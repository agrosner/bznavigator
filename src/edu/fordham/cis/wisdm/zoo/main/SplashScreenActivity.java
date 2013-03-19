package edu.fordham.cis.wisdm.zoo.main;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.Operations;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;
import edu.fordham.cis.wisdm.zoo.main.constants.UserConstants;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragment;
import edu.fordham.cis.wisdm.zoo.utils.ActionEnum;
import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.Places;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import edu.fordham.cis.wisdm.zoo.utils.map.CurrentLocationManager;
import edu.fordham.cis.wisdm.zoo.utils.map.MapUtils;
import edu.fordham.cis.wisdm.zoo.utils.map.MapViewFragment;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;

/**
 * The main screen activity that handles the map, each placefragment, and displaying everything else 
 * @author Andrew Grosner
 *
 */
public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener, OnItemClickListener, SearchPerformListener, OnNavigationListener, TextWatcher, OnTouchListener, UserConstants, OnMapClickListener{

	private static final String TAG = "SplashScreenActivity";

	/**
	 * Performs most of the view manipulation and handling
	 */
	private SplashScreenController mController = null;
	
	/**
	 * user's email from Login
	 */
	private static String email;

	/**
	 * the popup list of exhibits that shows up when a user searches for an exhibit
	 */
	private LinearLayout searchList;
	
	/**
	 * the searchbar widget
	 */
	private MenuItemSearchAction searchItem;
	
	/**
	 * displays loading when reading in exhibits from file
	 */
	private ProgressDialog loader;
	
	/**
	 * the list fragment displays on splash screen
	 */
	static ArrayListFragment list = null;

	/**
	 * restroom locator fragment
	 */
	private static PlaceFragment food = null;
	
	/**
	 * exhibit locator/descriptor fragment
	 */
	private static PlaceFragment exhibit = null;
	
	/**
	 * the special exhibits locator fragment
	 */
	private static PlaceFragment special = null;
	
	/**
	 * the shops locator fragment
	 */
	private static PlaceFragment shops =null;
	
	/**
	 * the administration buildings fragment
	 */
	private static PlaceFragment admin = null;
	
	/**
	 * The nearby exhibit fragment
	 */
	private static PlaceFragment nearby = null;
	
	/**
	 * Animation that slides in a view from the right
	 */
	private Animation showViewRight = null;
	
	/**
	 * Slides a view in from the left
	 */
	private Animation showViewLeft = null;
	
	/**
	 * Hides a view out to the left
	 */
	private Animation hideViewLeft = null;
	
	/**
	 * Hides a view out to the right
	 */
	private Animation hideViewRight;

	/**
	 * the map fragment
	 */
	private static MapViewFragment map;
	
	/**
	 * the map object contained in the map fragment
	 */
	private static GoogleMap mGoogleMap;
	
	/**
	 * Upper Y-coordinate of map
	 */
	public static double north = 40.85988281739409;
	
	/**
	 * Lower y-coordinate of map
	 */
	public static double south = 40.83874984609;
	
	/**
	 * Left-most x-coordinate on map
	 */
	public static double west = -73.88877402139309;
	
	/**
	 * Right-most x-coordinate on map
	 */
	public static double east = -73.86642400707782;
	
	/**
	 * whether location should be enabled
	 */
	private boolean enableLoc = false;
	
	/**
	 * the last group of places shown
	 */
	static LinkedList<PlaceItem> lastPlaces = new LinkedList<PlaceItem>();
	
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
	 * Misc places that are too few to be categorized
	 */
	LinkedList<PlaceItem> misc = new LinkedList<PlaceItem>();
	
	/**
	 * handles switching in fragments
	 */
	static FragmentTransaction mTransaction;
	
	/**
	 * determine whether tablet or not to optimize screen real estate 
	 */
	public static boolean isLargeScreen = false;
	
	/**
	 * manages location changes
	 */
	private LocationManager lManager = null;
		
	/**
	 * point contained in the current location "me"
	 */
	public static Location myLocation = null;
	
	/**
	 * Whether the map is following the users location on the map
	 */
	private boolean isTracking = false;
	
	/**
	 * The actionbar icon for follow current location
	 */
	private MenuItem follow  = null;
	
	/**
	 * Whether the user has picked a location or not for parking
	 */
	boolean isParked = false;
	
	/**
	 * If the user has internet and GPS connection while using the app
	 */
	private boolean isLocation = true;
	
	/**
	 * Location that will be stored in preferences
	 */
	private LatLng mParkingLocation = null;
	
	/**
	 * Item that will go on map when user saves parking spot
	 */
	PlaceItem mParkingPlace = null;

	private CurrentLocationManager mManager= null;
	
	/**
	 * Provides an action when the user's current location changes
	 */
	private Runnable meListener = new Runnable(){

		@Override
		public void run() {
			myLocation = mManager.getLastKnownLocation();
			PlaceController.reCalculateDistance(myLocation, searchExhibits, parking, gates, restrooms);
		}
		
	};
	
	/**
	 * The actionbar icon that allows a user to find where they parked
	 */
	private MenuItem park = null;
	
	/**
	 * current fragment enum 
	 */
	private static Places currentFragment = Places.LIST;
	
	/**
	 * Called when the info button on placeitem menu is clicked
	 */
	public OnClickListener onInfoClickedListener = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			//Intent i = new Intent(this, InfoActivity.this);
			//i.putStringExtra("name", map.getMenuItemTitle().split("\n")[0]);
			//startActivity(i);
			Toast.makeText(getApplicationContext(), "Info Clicked", Toast.LENGTH_SHORT).show();
		}
		
	};
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		setContentView(R.layout.acitivity_splash);
		
		mController = new SplashScreenController(this);
		
		//load listfragment into memory
		list = (ArrayListFragment) this.getSupportFragmentManager().findFragmentById(R.id.listfragment);
		list.getListView().setOnItemClickListener(this);
		
		try {
			MapsInitializer.initialize(this);
		} catch (GooglePlayServicesNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this), this, 0, new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
				
			});
			dialog.setOnDismissListener(new OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface dialog) {
					start();
				}
				
			});
			dialog.show();
		}
		map = (MapViewFragment) this.getSupportFragmentManager().findFragmentById(R.id.mapfragment);
		mGoogleMap = map.getMap();
		if(mGoogleMap!=null) start();
	}
		
	private void start(){
		mGoogleMap.setOnMapClickListener(this);
		
		loader = ProgressDialog.show(this, "Loading Data", "Please Wait");
		
		LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//gives user an option to accept terms or leave the app
		final HoloAlertDialogBuilder termsDialogBuilder = new HoloAlertDialogBuilder(this);
	
		//cancel button used for both dialogs
		DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		};
		
		termsDialogBuilder.setNegativeButton("I do not accept", cancel);
		
		//terms and conditions
		termsDialogBuilder.setTitle("Terms and Conditions");
		termsDialogBuilder.setMessage("Terms and conditions go here");
		termsDialogBuilder.setPositiveButton("I Accept", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				init();
				termsDialogBuilder.create().dismiss();
			}
					
		});
	
		termsDialogBuilder.setCancelable(false).create().show();
		
		//	if network error message
		if(connect.getActiveNetworkInfo()==null || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			final HoloAlertDialogBuilder gpsInternetDialogBuilder = new HoloAlertDialogBuilder(this);
			
			gpsInternetDialogBuilder.setNegativeButton("Quit", cancel);
			gpsInternetDialogBuilder.setTitle("Please Turn on GPS and Internet");
			gpsInternetDialogBuilder.setMessage("Please navigate to settings and make sure GPS and Internet is turned on for the full experience.");
			gpsInternetDialogBuilder.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);	
					finish();
				}
				
			});
			gpsInternetDialogBuilder.setNeutralButton("Continue Anyways", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					isLocation = false;
					gpsInternetDialogBuilder.create().dismiss();
					
				}
			});
			
			gpsInternetDialogBuilder.create().show();
		} 
	}
	private void init(){
		enableLoc = true;
		
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		exhibit = PlaceFragment.initFrag(exhibit,PlaceFragment.PlaceType.EXHIBITS);
		food = PlaceFragment.initFrag(food, PlaceFragment.PlaceType.FOOD);
		special = PlaceFragment.initFrag(special, PlaceFragment.PlaceType.SPECIAL);
		shops = PlaceFragment.initFrag(shops,PlaceFragment.PlaceType.SHOPS);
		admin = PlaceFragment.initFrag(admin, PlaceFragment.PlaceType.ADMIN);
		
		email = getIntent().getExtras().getString("email");
		isParked = Preference.getBoolean("parking", false);
		if(isParked){
			float lat = Preference.getFloat("parking-lat", 0);
			float lon = Preference.getFloat("parking-lon", 0);
			
			Location loc = new Location("My Parking Location");
			loc.setLatitude(lat);
			loc.setLongitude(lon);
			createParkingItems(loc);
			park.setIcon(R.drawable.ic_action_key_blue);
		} else{
			park.setIcon(R.drawable.ic_action_key);
		}
		
		setUpViews();
	}
	
	public void setUpViews(){
		//hide name, icon, 
		ActionBar mAction = this.getSupportActionBar();
		mAction.setDisplayHomeAsUpEnabled(true);
		
		showViewRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
		showViewLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
		hideViewLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
		hideViewRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
		
		if(mParkingPlace!=null) mParkingPlace.addDraggableMarker(mGoogleMap);
		
		
	    //map.hideItemMenu();
		mManager = new CurrentLocationManager(this, mGoogleMap);
		new LoadDataTask(this, this).execute();
		
		startLocationUpdates();
		nearby = new PlaceFragment(PlaceFragment.PlaceType.NEARBY, searchExhibits);
		
		mManager.runOnFirstFix(meListener);
		
		Intent i = new Intent(this.getApplicationContext(), LocationUpdateService.class);
		i.putExtra("email", String.copyValueOf(email.toCharArray()));
		i.putExtra("password", String.copyValueOf(getIntent().getExtras().getString("password").toCharArray()));
		startService(i);
        
		//hide search list until it is used
        searchList = (LinearLayout) findViewById(R.id.SearchList);
        Operations.removeView(searchList);
       
   		mController.addDrawerList();
   		mController.determineScreenLayout();
   		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(enableLoc)	stopLocationUpdates();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(enableLoc)	startLocationUpdates();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(enableLoc) stopLocationUpdates();
		
	}
	
	public static void saveParkingSpot(LatLng lat){
		Preference.putFloat("parking-lat", (float) lat.latitude);
		Preference.putFloat("parking-lon", (float) lat.longitude);
	}
	
	/**
	 * Starts location
	 */
	private void startLocationUpdates(){
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		mManager.start();
		mGoogleMap.setMyLocationEnabled(true);
		
		String locationMessage = null;
		 
		if (lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationMessage ="GPS provider enabled.";
			isLocation = true;
		} else if (lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationMessage = "Network provider enabled.";
		 	isLocation = true;
		} else	{
			locationMessage = "No provider enabled. Please check settings and allow locational services.";
		 	isLocation = false;
		}
		if(isLocation){
			searchItem.getMenuItem().setOnMenuItemClickListener(this);
		} else{
			final Context con = getApplicationContext();
			searchItem.getMenuItem().setOnMenuItemClickListener(new OnMenuItemClickListener(){

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						MessageBuilder.showToast("Location must be enabled to use this feature", con);
						return false;
					}
		 			 
			});
		}
		 	 
		Toast.makeText(this, locationMessage, Toast.LENGTH_SHORT).show();
		final ContextWrapper con = this;
		mManager.runOnFirstFix(new Runnable(){

			@Override
			public void run() {
				//map.animateTo(me.getPoint());
				myLocation = mManager.getLastKnownLocation();
				if(!map.getView().isShown() && !list.isVisible()){
					try{
						getCurrentPlaceFragment().refresh();
					} catch(NullPointerException r){
						
					}
				}
				
				//once we get a fix, store it in locationupdateservice
				LocationUpdateService.storeFirstLocation(myLocation, con);
				PlaceController.reCalculateDistance(myLocation, searchExhibits, misc);
				//loader.dismiss();
				
				MessageBuilder.showToast("GPS lock found", getApplicationContext());
			}
			 
		 });
		loader.dismiss();
		 
	 }
	

	/**
	 * Stops updates and removes mylocationoverlay from the map
	 */
	 private void stopLocationUpdates(){
		 mManager.stop();
		 mGoogleMap.setMyLocationEnabled(false);
	 }
	
	@Override
	public void onBackPressed(){
		 if(!mController.closeDrawer()){
			 if((currentFragment.isPlaceFragment()) || (!isLargeScreen && currentFragment!=Places.LIST)){
				 showList();
			 } else{
				 
				 //ask user whether quit or not
				 AlertDialog.Builder message = new AlertDialog.Builder(this);
				 message.setTitle("Logout?");
				 final Activity act = this;
				 message.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
					 @Override
					 public void onClick(DialogInterface dialog, int which) {
						 stopService(new Intent(getApplicationContext(), LocationUpdateService.class));
						 Preference.putBoolean(REMEMBER_ME_LOC, false);
						 
						 Intent upIntent = new Intent(act, Entry.class);
						 if (NavUtils.shouldUpRecreateTask(act, upIntent)) {
							 // This activity is not part of the application's task, so create a new task
							 // with a synthesized back stack.
							 TaskStackBuilder.from(act)
		                        	.addNextIntent(upIntent)
		                        	.startActivities();
							 finish();
						 } else {
							 // 	This activity is part of the application's task, so simply
							 // navigate up to the hierarchical parent activity.
							 NavUtils.navigateUpTo(act, upIntent);
						 }
					 }
				 });
				 message.setNegativeButton("No", new DialogInterface.OnClickListener() {
					 
					 @Override	
					 public void onClick(DialogInterface dialog, int which) {}
				 });
				 message.create().show();
			 }
		}
	}
	
	/**
	 * Creates the actionbar icons
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		searchList = (LinearLayout) findViewById(R.id.SearchList);
		
		//adds the searchbar to the actionbar
		searchItem = new MenuItemSearchAction(this, menu, this, 
				getResources().getDrawable(R.drawable.ic_action_search), 
				this, searchList);
		searchItem.setTextColor(getResources().getColor(R.color.forestgreen));
		searchItem.getMenuItem().setOnMenuItemClickListener(this);
		
		//add action items
		follow = menu.add(ActionEnum.FOLLOW.toString()).setOnMenuItemClickListener(this).setIcon(R.drawable.ic_action_location);
			follow.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		park = menu.add(ActionEnum.PARK.toString()).setOnMenuItemClickListener(this);
			park.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add(ActionEnum.ABOUT.toString()).setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add(ActionEnum.SETTINGS.toString()).setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * User has clicked on an actionbar icon
	 */
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		if(item.getTitle().equals(ActionEnum.SETTINGS.toString())){
			startActivity(new Intent(this, PrefActivity.class));
		} else if(item.getTitle().equals(ActionEnum.ABOUT.toString())){
			//ask user whether quit or not
			HoloAlertDialogBuilder message = new HoloAlertDialogBuilder(this);
			message.setTitle("About:");
			message.setMessage("\nDeveloper: Andrew Grosner\n\t\t\t\t  agrosner@fordham.edu\n" +
					"\nAssistant Developer: Isaac Ronan\n" 
					+ "\nWireless Sensor Data Mining (WISDM)"
					+ "\nSpecial Thanks to: Fordham University");
			message.setNeutralButton("Ok", null);
			message.create().show();
		} else if(item.getTitle().equals("Search")){
			showMap(mTransaction, getCurrentFragmentView());
		} else if(item.getTitle().equals(ActionEnum.FOLLOW.toString())){
			if(!map.getView().isShown()){	
				showMap(mTransaction, getCurrentFragmentView());
			}
			if(!isTracking){
				if (myLocation!=null){
					item.setIcon(R.drawable.ic_action_location_blue);
					MapUtils.animateTo(map.getMap(), myLocation);
					isTracking = true;
					MessageBuilder.showToast("Now Following Current Location, Tap Map to Cancel", this);
				}	else MessageBuilder.showLongToast("Cannot find current location",  this);
			} else{
				item.setIcon(R.drawable.ic_action_location);
				isTracking = false;
				MessageBuilder.showToast("Following Off", this);
			}
			mManager.follow(isTracking);
		} else if(item.getTitle().equals(ActionEnum.PARK.toString())){
			if(isParked){
				//bring up menu
				final HoloAlertDialogBuilder confirm = new HoloAlertDialogBuilder(this);
				confirm.setTitle("Parking Location Options");
				confirm.setMessage("Saves a spot on the map for reference to where you may have parked today.");
				confirm.setPositiveButton("Delete Location", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deactivateParkingSpot();
					}
					
				});
				confirm.setNeutralButton("Reset", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activateParkingSpot();
					}
				});
				
				confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						confirm.create().dismiss();
					}
				});
				
				confirm.create().show();
				
				
			} else{
				if(myLocation!=null){
					activateParkingSpot();
				} else{
					MessageBuilder.showToast("Saving Parking Location Not Available When GPS is Not Found", this);
				}
			}
		}
		return false;
	}
	
	/**
	 * Initializes the parking location geopoint and placeitem
	 */
	private void createParkingItems(Location location){
		mParkingLocation = new LatLng(location.getLatitude(), location.getLongitude());
		mParkingPlace = new PlaceItem().point(mParkingLocation).iconId(R.drawable.car).name("My Parking Spot");
	}
	
	private void activateParkingSpot(){
		park.setIcon(R.drawable.ic_action_key_blue);
		MessageBuilder.showLongToast("Parking Location Now Saved. Press and hold icon to drag to desired location", this);
		Preference.putFloat("parking-lat", (float) myLocation.getLatitude());
		Preference.putFloat("parking-lon", (float) myLocation.getLongitude());
		Preference.putBoolean("parking", true);
		isParked = true;
		
		if(mParkingPlace!=null){
			mParkingPlace.remove();
		}
		createParkingItems(myLocation);
		
		mParkingPlace.addDraggableMarker(mGoogleMap);
	}
	
	private void deactivateParkingSpot(){
		park.setIcon(R.drawable.ic_action_key);
		isParked = false;
		Preference.putBoolean("parking", false);
		Preference.putFloat("parking-lat", 0);
		Preference.putFloat("parking-lon", 0);
		mParkingPlace.remove();
	}
	
	/**
	 * When a user clicks on the top left of the screen to go back
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home)	onBackPressed();
		return false;
	}
	
	/**
	 * User clicks on a search exhibit
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		PlaceItem place = searchExhibits.get(id-1);
		LinkedList<PlaceItem> places = new LinkedList<PlaceItem>();
		places.add(place);
		
		showMap(mTransaction, list.getView(), places);
		if(searchList.isShown()){
			Operations.removeView(searchList);
			searchItem.getMenuItem().collapseActionView();
		}
		MapUtils.moveRelativeToCurrentLocation(place.getPoint(), mGoogleMap);
		sendSearchQuery(place.getName());
	}
	
	/**
	 * Handles showing the list widget
	 */
	private void showList(){
	
		if(!isLargeScreen&&currentFragment!=Places.LIST && currentFragment==Places.MAP){
			Operations.swapViewsAnimate(map.getView(), list.getView(), hideViewRight, showViewLeft);
		} else{
		
			if(isLargeScreen && currentFragment!=Places.MAP){
				mTransaction = this.getSupportFragmentManager().beginTransaction();
				mTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
				mController.removeFrag(mTransaction, true).commit();
			} else if(!isLargeScreen && currentFragment!=Places.LIST){
				mTransaction = this.getSupportFragmentManager().beginTransaction();
				mController.removeFrag(mTransaction, false).commit();
				Operations.addViewAnimate(list.getView(), showViewLeft);
			}
		}
		currentFragment = Places.LIST;
	}

	/**
	 * User clicks on the main menu item
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		//if the searchbar activated, we will remove it 
		if(searchList.isShown()){
			Operations.removeView(searchList);
			searchItem.getMenuItem().collapseActionView();
		}
		mController.removeFrag(mTransaction, true);
	
			 if(position == Places.MAP.toInt()) 		showMap(mTransaction, list.getView());
		else if(position == Places.NEWS.toInt()) 		MessageBuilder.showToast("Coming soon", this);
		else if(position == Places.AMENITIES.toInt()){	showMap(mTransaction, list.getView());
														mController.openDrawer();}
		else if(Places.isIntPlaceFragment(position)){
			showFragment(getFragmentFromPlace(Places.toPlace(position)), position);}
		
		try{	 
			mTransaction.commit();
		} catch (IllegalStateException i){
			//if commit was already called, this will happen
		}
				
	}
	
	/**
	 * Returns the current view of showing fragment
	 * @return
	 */
	private View getCurrentFragmentView(){
		if(currentFragment == Places.MAP)		return map.getView();
		else if(currentFragment == Places.LIST)	return list.getView();
		else 									return getCurrentPlaceFragment().getView();
	}
	
	/**
	 * Returns current showing/focused place fragment
	 * @return
	 */
	public PlaceFragment getCurrentPlaceFragment(){
		return getFragmentFromPlace(currentFragment);
	}
	
	/**
	 * Converts the places enum into a placefragment (if its a place fragment)
	 * @param place
	 * @return
	 */
	private PlaceFragment getFragmentFromPlace(Places place){
		if(place == Places.EXHIBITS)		return exhibit;
		else if(place == Places.FOOD)		return food;
		else if(place == Places.SPECIAL)	return special;
		else if(place == Places.ADMIN)		return admin;
		else if(place == Places.SHOPS)		return shops;
		else if(place == Places.FIND)		return nearby;
		else								return null;
	}

	
	/**
	 * Shows the map from any showing fragment, taking into account if the screen is large or not
	 * @param mTransaction
	 * @param v
	 */
	protected void showMap(FragmentTransaction mTransaction, View v){
		mController.closeDrawer();
		mController.clearMapData();
		if(!isLargeScreen && currentFragment!=Places.MAP)	Operations.swapViewsAnimate(v, map.getView(), hideViewLeft, showViewRight);
		else if(!map.getView().isShown())	Operations.addView(map.getView());
		if(isLargeScreen){
			try{
				mController.removeFrag(mTransaction, true).commit();
			} catch(IllegalStateException i){
				i.printStackTrace();
			}
		}
		
		mController.addDrawer();
		currentFragment = Places.MAP;
	}
	
	/**
	 * Shows the map with all items from a list of places (usually shown for view all on map option)
	 * @param mTransaction
	 * @param v
	 * @param flag
	 * @param place
	 */
	@SuppressWarnings("unchecked")
	public void showMap(FragmentTransaction mTransaction,View v, LinkedList<PlaceItem> place){
		showMap(mTransaction, v);
		if (place!=null)	{
			MapUtils.moveToBounds(mGoogleMap, place);
			int size = place.size();
			for(int i =0 ; i < size; i++){
				PlaceItem p = place.poll();
				if(!map.setMarkerFocus(p)){
					p.addMarker(mGoogleMap);
					lastPlaces.add(p);
				}
			}
		}
	}
	
	/**
	 * Handles showing fragments in the activity
	 * @param fragment
	 */
	protected void showFragment(Fragment f, int position){
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		mTransaction.setCustomAnimations(R.anim.slide_in_right, 0);
		mController.closeDrawer();
		
		if(!isLargeScreen && currentFragment!=Places.MAP){
			Operations.removeViewAnimate(list.getView(), hideViewLeft);
			if(!f.isAdded())	mTransaction.replace(android.R.id.content, f).commit();
			else				Operations.addViewAnimate(f.getView(), showViewRight);
			
			mController.removeDrawer();
		} else if(isLargeScreen){
			if(currentFragment==Places.MAP){
				mController.removeFrag(mTransaction, true);
				if(!f.isAdded())	mTransaction.add(R.id.PlaceView, f).commit();
				else				mTransaction.commit();
			} else{
				if(f.isAdded()){
					PlaceFragment placeFrag = (PlaceFragment) f;
					placeFrag.refresh();
				} else{
					mController.removeFrag(mTransaction, true).add(R.id.PlaceView, f).commit();
				}
			}
		} 
		currentFragment = Places.toPlace(position);
	}
	
	
	@Override
	public void performSearch(String query) {
		String querie = query.toLowerCase();
		boolean found = false;
		PlaceItem placeFound = null;
		
		for(PlaceItem place: searchExhibits){
			String name = place.getName().toLowerCase();
			if(name.contains(querie)){
				found = true;
				placeFound = place;
				break;
			}
		}
		
		if(found){
			if(placeFound!=null){
				LinkedList<PlaceItem> place = new LinkedList<PlaceItem>();
				place.add(placeFound);
				this.showMap(mTransaction, list.getView(), place);
				mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(placeFound.getPoint()));
				//map.setItemMenu(placeFound, onInfoClickedListener);
				sendSearchQuery(querie);
			}
		} else{
			MessageBuilder.showToast("Not found", this);
		}
	}
	
	public GoogleMap getMap(){
		return mGoogleMap;
	}
	
	public View getMapView(){
		return map.getView();
	}
	
	public MapViewFragment getMapFrag(){
		return map;
	}
	
	public static Places getCurrentFragment() {
		return currentFragment;
	}
	
	public CurrentLocationManager getCurrentLocationManager(){
		return mManager;
	}
	
	public static void setCurrentFragment(Places currentFragment) {
		SplashScreenActivity.currentFragment = currentFragment;
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count){
		if(s.length()>0){
			searchList.removeAllViews();
			for(int i =0; i < searchExhibits.size(); i++){
				PlaceItem place = searchExhibits.get(i);
				if(place.getName().toLowerCase().contains(s.toString().toLowerCase()))
					searchList.addView(PlaceController.createExhibitItem(SplashScreenActivity.myLocation, this, i+1, place, this, true));
				}
		}
	}
    
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {return false;}

	@Override
	public void afterTextChanged(Editable s) {}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		searchList.removeAllViews();}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if(v.equals(map)){
			int action = e.getAction() & MotionEvent.ACTION_MASK;
	     	if(action == MotionEvent.ACTION_MOVE){
	        	if(isTracking){
	        		follow.setIcon(R.drawable.ic_action_location);
					isTracking = false;
					mManager.follow(isTracking);
					MessageBuilder.showToast("Following Off", this);
	        	}
	        	
	     	}
		}
	     return super.onTouchEvent(e);
	}
	
	/**
	 * 
	 * @param querie
	 */
	private void sendSearchQuery(final String querie){
		new Thread(){
			@Override
			public void run(){
			if(!Connections.sendSearchQuery(
				new Connections(email, getIntent().getExtras().getString("password"), 
						Secure.getString(getContentResolver(), Secure.ANDROID_ID)),
						querie, myLocation))
				Log.e(TAG, "Failed sending query: " + querie);
				else Log.d(TAG, "Query sent: " + querie); 
			}
		}.start();
	}
	
	/**
	 * Loads data in the background, updating the UI along the way
	 * @author Andrew Grosner
	 *
	 */
	private class LoadDataTask extends AsyncTask<Void, Void, Void>{

		private Activity mActivity = null;
		private OnClickListener mOnClick = null;
		
		public LoadDataTask(Activity act, OnClickListener onClick){
			this.mActivity = act;
			this.mOnClick = onClick;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			//reads exhibits into the searchbar list of places
			PlaceController.readInData(mActivity, onInfoClickedListener, searchExhibits, "exhibits.txt", "food.txt", "shops.txt",
					"gates.txt", "parking.txt", "admin.txt", "special.txt", "restrooms.txt", "misc.txt");
			return null;
		}
		
		protected Void onPostExecute(){
			PlaceController.readInDataIntoList(SplashScreenActivity.myLocation, mActivity, searchList, searchExhibits, mOnClick, false);
			return null;
			
		}
	}

	@Override
	public void onMapClick(LatLng point) {
		if(isTracking){
    		follow.setIcon(R.drawable.ic_action_location);
			isTracking = false;
			mManager.follow(isTracking);
			MessageBuilder.showToast("Following Off", this);
    	}
	}
	
}