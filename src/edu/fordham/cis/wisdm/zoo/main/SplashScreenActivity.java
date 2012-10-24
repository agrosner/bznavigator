package edu.fordham.cis.wisdm.zoo.main;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.location.LocationManager;
import android.net.ConnectivityManager;
//import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.Operations;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.grosner.mapview.CurrentLocationChangedListener;
import com.grosner.mapview.CurrentLocationOverlay;
import com.grosner.mapview.Geopoint;
import com.grosner.mapview.MapScale;
import com.grosner.mapview.MapView;
import com.grosner.mapview.OverlayItem;
import com.grosner.mapview.PlaceItem;
import com.grosner.mapview.ZoomLevel;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;
import edu.fordham.cis.wisdm.zoo.utils.ActionEnum;
import edu.fordham.cis.wisdm.zoo.utils.Places;
import edu.fordham.cis.wisdm.zoo.utils.Polygon;
import edu.fordham.cis.wisdm.zoo.utils.Preference;


public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener, OnItemClickListener, SearchPerformListener, OnNavigationListener, TextWatcher, OnTouchListener{

	/**
	 * user's email from Login
	 */
	private static String email;
	
	private SlidingDrawer mDrawer;
	
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
	private static ArrayListFragment list = null;
	
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
	 * the mapview widget
	 */
	private static MapView map;
	
	/**
	 * Upper Y-coordinate of map
	 */
	public static int north = (int)(40.85988281739409*1E6);
	
	/**
	 * Lower y-coordinate of map
	 */
	public static int south = (int)(40.83874984609*1E6);
	
	/**
	 * Left-most x-coordinate on map
	 */
	public static int west = (int)(-73.88877402139309*1E6);
	
	/**
	 * Right-most x-coordinate on map
	 */
	public static int east = (int)(-73.86642400707782*1E6);
	
	
	/**
	 * whether location should be enabled
	 */
	private boolean enableLoc = false;
	
	/**
	 * the mapview's overlays
	 */
	private static List<OverlayItem> overlays; 
	
	/**
	 * the last group of places shown
	 */
	private static LinkedList<PlaceItem> lastPlaces = new LinkedList<PlaceItem>();
	
	private LinkedList<PlaceItem> restrooms = new LinkedList<PlaceItem>();
	
	private boolean showRestrooms = true;
	
	private LinkedList<PlaceItem> gates = new LinkedList<PlaceItem>();
	
	private boolean showGates = true;
	
	private LinkedList<PlaceItem> parking = new LinkedList<PlaceItem>();
	
	private boolean showParking = true;
	
	/**
	 * handles switching in fragments
	 */
	private static FragmentTransaction mTransaction;
	
	/**
	 * determine whether tablet or not to optimize screen real estate 
	 */
	public static boolean isLargeScreen = false;
	
	/**
	 * determine screen width for sidebar
	 */
	public static int SCREEN_WIDTH = 0;
	
	/**
	 * manages location changes
	 */
	private LocationManager lManager = null;
		
	/**
	 * shows current location
	 */
	public CurrentLocationOverlay me = null;

	/**
	 * point contained in the current location "me"
	 */
	public static Geopoint myLocation = null;
	
	/**
	 * Whether the map is following the users location on the map
	 */
	private boolean isTracking = false;
	
	/**
	 * The actionbar icon for follow current location
	 */
	private MenuItem follow  = null;
	
	/**
	 * If the user has internet and GPS connection while using the app
	 */
	private boolean isLocation = true;

	/**
	 * Provides an action when the user's current location changes
	 */
	private CurrentLocationChangedListener meListener = new CurrentLocationChangedListener(){

		@Override
		public void OnCurrentLocationChange(Geopoint point) {
			myLocation = me.getPoint();
			PlaceFragment.reCalculateDistance(myLocation, searchExhibits);
			PlaceFragment.reCalculateDistance(myLocation, parking);
			PlaceFragment.reCalculateDistance(myLocation, gates);
			PlaceFragment.reCalculateDistance(myLocation, restrooms);
		}
		
	};
	
	/**
	 * current fragment enum 
	 */
	private static Places currentFragment = Places.LIST;
	
	private LinkedList<PlaceItem> searchExhibits = new LinkedList<PlaceItem>();
	
	/**@Override
	public void onConfigurationChanged(Configuration config){
		super.onConfigurationChanged(config);
		
		//map.removeAllViews();
		//init();
		determineScreenLayout();
		if(config.orientation == Configuration.ORIENTATION_PORTRAIT && isLargeScreen){
			isLargeScreen = false;
			if(currentFragment!=Places.MAP){
				Operations.removeView(map);
			}
        	//addListButton();
		} else if(config.orientation == Configuration.ORIENTATION_LANDSCAPE && isLargeScreen){
			if(!map.isShown()){
				Operations.addView(map);
			}
			if(!list.isVisible()){
				showList();
			}
		} else if(!isLargeScreen && currentFragment == Places.MAP){
			showMap(mTransaction, list.getView());
		}
		map.getView().refresh();
	}**/
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		setContentView(R.layout.acitivity_splash);
		//load listfragment into memory
		list = (ArrayListFragment) this.getSupportFragmentManager().findFragmentById(R.id.listfragment);
		list.getListView().setOnItemClickListener(this);
		//init map objects
		map = null;
		map = (MapView) findViewById(R.id.map);
		
		determineScreenLayout();
	       
		
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
		
		termsDialogBuilder.create().show();
		
		//if network error message
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
		
		exhibit = initFrag(exhibit,PlaceFragment.PlaceType.EXHIBITS);
		food = initFrag(food, PlaceFragment.PlaceType.FOOD);
		special = initFrag(special, PlaceFragment.PlaceType.SPECIAL);
		shops = initFrag(shops,PlaceFragment.PlaceType.SHOPS);
		admin = initFrag(admin, PlaceFragment.PlaceType.ADMIN);
		
		email = getIntent().getExtras().getString("email");
		
		
		setUpViews();
		
	}
	
	/**
	 * Creates a new fragment if the fragment is null , else returns the original instance
	 * @param frag
	 * @param type
	 * @return
	 */
	private PlaceFragment initFrag(PlaceFragment frag, PlaceFragment.PlaceType type){
		if(frag==null){
			frag = new PlaceFragment(type);
		}
		return frag;
	}
	
	public void setUpViews(){
		//hide name, icon, 
		ActionBar mAction = this.getSupportActionBar();
		mAction.setDisplayHomeAsUpEnabled(true);
		//mAction.setDisplayShowHomeEnabled(false);
		//mAction.setTitle("Bronx Zoo Navigator");
		
		overlays = map.getOverlays();
		
		determineScreenLayout();
		
	
		map.init(this, "map0/%col%_%row%.png", 1024, 1280);
		
	    map.addMapScale(ZoomLevel.LEVEL_1, new MapScale("map1/%col%_%row%.png", 2048, 2560));
	    map.addMapScale(ZoomLevel.LEVEL_2, new MapScale("map2/%col%_%row%.png", 4096, 5120));
	    map.addMapScale(ZoomLevel.LEVEL_3, new MapScale("map3/%col%_%row%.png", 8192, 10240));
	    map.getView().setOnTouchListener(this);
	    // map.addMapScale(ZoomLevel.LEVEL_4, new MapScale("map5/crop_%col%_%row%.png", 6400, 10000));
	       
	    //saves offset in memory
	    Geopoint.storeOffset(north, south, west, east);
		
		me = new CurrentLocationOverlay(map.getView(), this, R.drawable.location);
		startLocationUpdates();
		nearby = new PlaceFragment(PlaceFragment.PlaceType.NEARBY, searchExhibits);
		
		
		me.addCurrentLocationChangedListener(meListener);
		
		Intent i = new Intent(this.getApplicationContext(), LocationUpdateService.class);
		i.putExtra("email", new String(email));
		i.putExtra("password", new String(getIntent().getExtras().getString("password")));
		startService(i);
        
        searchList = (LinearLayout) findViewById(R.id.SearchList);
        Operations.removeView(searchList);
       
       
   		addDrawerList();
	

	}
	
	@SuppressWarnings("deprecation")
	private void determineScreenLayout(){
		//get screen width to optimize layout
        DisplayMetrics display = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(display);
        SCREEN_WIDTH = display.widthPixels;
		
        int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if((screensize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screensize == Configuration.SCREENLAYOUT_SIZE_LARGE)){
        	//scale the width of the sidebar to specified size
			LayoutParams lp = new LayoutParams((SCREEN_WIDTH/4), LayoutParams.FILL_PARENT);
        	list.getView().setLayoutParams(lp);
        	isLargeScreen = true;
        	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else{//small screen we want to hide it
        	Operations.removeView(map);
        	//addListButton();
    		isLargeScreen = false;
    		list.getView().setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
	}
	
	private void addDrawerList(){
		mDrawer = (SlidingDrawer) findViewById(R.id.drawer);
	    mDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener(){

		@Override
		public void onDrawerOpened() {
			ImageView icon = (ImageView) mDrawer.getHandle();
			icon.setImageResource(R.drawable.ic_action_arrow_right);
		}
	    	   
	    });
	    mDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener(){
	    	
	    @Override
	    public void onDrawerClosed() {
	    	ImageView icon = (ImageView) mDrawer.getHandle();
	    	icon.setImageResource(R.drawable.ic_action_arrow_left);
		}
	    
	    });
		
		LinearLayout frame = (LinearLayout) mDrawer.findViewById(R.id.drawerFrame);
		frame.removeAllViews();
		LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		pm.bottomMargin = 10;
		TextView title = new TextView(this);
		title.setText("Amenities");
		title.setTextSize(20);
		frame.addView(title, pm);
		
		
		frame.addView(createIconCheckBox("Restrooms", "restrooms.txt", R.drawable.bathroom, restrooms));
		frame.addView(createIconCheckBox("Gates", "gates.txt", R.drawable.fordham, gates));
		frame.addView(createIconCheckBox("Parking Lots", "parking.txt" , R.drawable.car, parking));
		map.getView().refresh();
	}
	
	private RelativeLayout createIconCheckBox(final String name, final String fileName, int iconId, final LinkedList<PlaceItem> pts){
		RelativeLayout iconCheckbox = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.icon_checkbox_item, null);
		CheckBox check = (CheckBox) iconCheckbox.findViewById(R.id.check);
		check.setText(name);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked){
					readInPlaces(pts, fileName, mTransaction, list.getView());
				} else{
					map.getView().removeOverlayList(pts);
				}
				if(name.equals("Restrooms")) 			showRestrooms = isChecked;
				else if(name.equals("Gates"))			showGates = isChecked;
				else if(name.equals("Parking Lots"))	showParking = isChecked;
			}
			
		});
		check.setChecked(true);
		ImageView icon = (ImageView) iconCheckbox.findViewById(R.id.icon);
		icon.setImageResource(iconId);
		return iconCheckbox;
	}
	
	@Override
	public void onLowMemory(){
		//refresh the map resources when memory is low
		map.getView().refresh();
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
	
	/**
	 * Starts location
	 */
	private void startLocationUpdates(){
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		me.enableMyLocation();
		if(!overlays.contains(me))	overlays.add(me);
		
		map.postInvalidate();
		 
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
		final Activity act = this;
		final LayoutInflater inflater = this.getLayoutInflater();
		final OnClickListener onclick = this;

		//reads exhibits into the searchbar list of places
		LinkedList<String> fNames = new LinkedList<String>();
		fNames.add("exhibits.txt");
		fNames.add("food.txt");
		fNames.add("shops.txt");
		fNames.add("gates.txt");
		fNames.add("parking.txt");
		fNames.add("admin.txt");
		fNames.add("restrooms.txt");
		
		readInFileInUI(act, inflater, null, fNames, searchList, searchExhibits, onclick);
		
		me.runOnFirstFix(new Runnable(){

			@Override
			public void run() {
				//map.animateTo(me.getPoint());
				myLocation = me.getPoint();
				if(!map.isShown() && !list.isVisible()){
						 if(currentFragment == Places.EXHIBITS)		exhibit.refresh();
					else if(currentFragment == Places.FOOD)			food.refresh();
					else if(currentFragment == Places.SPECIAL)		special.refresh();
					else if(currentFragment == Places.ADMIN)		admin.refresh();
					else if(currentFragment == Places.SHOPS)		shops.refresh();
					else if(currentFragment == Places.FIND)			nearby.refresh(searchExhibits);
				}
				
				//once we get a fix, store it in locationupdateservice
				LocationUpdateService.storeFirstLocation(me.getLastFix(), con);
				
				PlaceFragment.reCalculateDistance(myLocation, searchExhibits);
				//loader.dismiss();
				MessageBuilder.showToast("GPS lock found", getApplicationContext());
				map.getView().reDrawOverlays(true);
			}
			 
		 });
		loader.dismiss();
		 
	 }
	
	/**
	 * Runs the function readInFile from PlaceFragment in the UI thread, also removes all views from searchlist and clears all previous search exhibits to prevent duplicates
	 * @param act
	 * @param inflater
	 * @param container
	 * @param fName
	 * @param searchList
	 * @param searchExhibits
	 * @param onclick
	 */
	public void readInFileInUI(final Activity act, final LayoutInflater inflater, final ViewGroup container, final LinkedList<String> fName, final LinearLayout searchList, final LinkedList<PlaceItem> searchExhibits, final OnClickListener onclick){
		this.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				searchList.removeAllViews();
				searchExhibits.clear();
				
				for(int i = 0; i < fName.size(); i++){
					PlaceFragment.readInFile(act, inflater, container, fName.get(i), searchList, searchExhibits, onclick, true);
				}
			}
			
		});
	}

	/**
	 * Stops updates and removes mylocationoverlay from the map
	 */
	 private void stopLocationUpdates(){
		 me.disableMyLocation();
		 overlays.remove(me);
		 map.postInvalidate();
	 }
	
	@Override
	public void onBackPressed(){
		if(!list.getView().isShown()){
			showList();
			if(map.isShown() && !isLargeScreen)	Operations.removeView(map);
		} else if(isLargeScreen &&currentFragment!=Places.LIST && currentFragment!=Places.MAP){
			mTransaction = this.getSupportFragmentManager().beginTransaction();
			removeFrag(mTransaction);
			mTransaction.commit();
			currentFragment = Places.LIST;
		}else{
		
			//ask user whether quit or not
			AlertDialog.Builder message = new AlertDialog.Builder(this);
			message.setTitle("Logout?");
			final Activity act = this;
			message.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					stopService(new Intent(getApplicationContext(), LocationUpdateService.class));
	                Preference.putBoolean("edu.fordham.cis.wisdm.zoo.askagain", false);
					
					Intent upIntent = new Intent(act, Entry.class);
		            if (NavUtils.shouldUpRecreateTask(act, upIntent)) {
		                // This activity is not part of the application's task, so create a new task
		                // with a synthesized back stack.
		                TaskStackBuilder.from(act)
		                        .addNextIntent(upIntent)
		                        .startActivities();
		                finish();
		            } else {
		                // This activity is part of the application's task, so simply
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
	
	/**
	 * Creates the actionbar icons
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		searchList = (LinearLayout) findViewById(R.id.SearchList);
		
		//adds the searchbar to the actionbar
		searchItem = new MenuItemSearchAction(this, menu, this, getResources().getDrawable(R.drawable.ic_action_search), this, searchList);
		searchItem.setTextColor(getResources().getColor(R.color.forestgreen));
		searchItem.getMenuItem().setOnMenuItemClickListener(this);
		
		//add action items
		follow = menu.add(ActionEnum.FOLLOW.toString()).setOnMenuItemClickListener(this).setIcon(R.drawable.ic_action_location);
		follow.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		//menu.add(ActionEnum.NEAREST.toString()).setOnMenuItemClickListener(this).setIcon(R.drawable.ic_action_show).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				//| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
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
			AlertDialog.Builder message = new AlertDialog.Builder(this);
			message.setTitle("About:");
			message.setMessage("\nDeveloper: Andrew Grosner\n\t\t\t\t  agrosner@fordham.edu\n" +
					"\nAssistant Developer: Isaac Ronan\n" 
					+ "\nWireless Sensor Data Mining (WISDM)");
			message.setNeutralButton("Ok", null);
			message.create().show();
		} else if(item.getTitle().equals("Search")){
			showMap(mTransaction, getCurrentFragment());
		} else if(item.getTitle().equals(ActionEnum.FOLLOW.toString())){
			if(!map.isShown()){	
				showMap(mTransaction, getCurrentFragment());
			}
			if(!isTracking){
				if (myLocation!=null){
					item.setIcon(R.drawable.ic_action_location_blue);
					map.animateTo(myLocation);
					isTracking = true;
					MessageBuilder.showToast("Now Following Current Location", this);
				}	else MessageBuilder.showLongToast("Cannot find current location",  this);
			} else{
				item.setIcon(R.drawable.ic_action_location);
				isTracking = false;
				MessageBuilder.showToast("Following Off", this);
			}
			me.follow(isTracking);
		} else if(item.getTitle().equals(ActionEnum.NEAREST.toString()) || item.getTitle().equals(ActionEnum.SETTINGS.toString())){
			MessageBuilder.showToast("Coming Soon", this);
		} 
		return false;
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
		map.animateTo(place.getPoint()); 
		if(searchList.isShown()){
			Operations.removeView(searchList);
			searchItem.getMenuItem().collapseActionView();
		}
	}
	
	/**
	 * Handles showing the list widget
	 */
	private void showList(){
		
		//if a fragment is replacing the listview, remove it and add the list
		if(!list.isVisible()){
			Operations.addView(list.getView());
			
			mTransaction = this.getSupportFragmentManager().beginTransaction();
			
			if(currentFragment == Places.EXHIBITS)		mTransaction.remove(exhibit).commit();
			else if(currentFragment == Places.FOOD)		mTransaction.remove(food).commit();
			else if(currentFragment == Places.SPECIAL)	mTransaction.remove(special).commit();
			else if(currentFragment == Places.SHOPS)	mTransaction.remove(shops).commit();
			else if(currentFragment == Places.ADMIN)	mTransaction.remove(admin).commit();
			else if(currentFragment == Places.FIND)		mTransaction.remove(nearby).commit();
		} else{
			//if the actual view within the listfragment is not visible, make it visible
			if(!list.getView().isShown())	Operations.addView(list.getView());
			else if(isLargeScreen)			Operations.removeView(list.getView());
		}
		currentFragment = Places.LIST;
	}

	/**
	 * User clicks on the main menu item
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		//if the searchbar activated, we will remove it 
		if(searchList.isShown()){
			Operations.removeView(searchList);
			searchItem.getMenuItem().collapseActionView();
		}
		removeFrag(mTransaction);
	
			 if(position == Places.MAP.toInt()) 		showMap(mTransaction, list.getView());
		else if(position == Places.NEWS.toInt()) 		MessageBuilder.showToast("Coming soon", this);
		else if(position == Places.FIND.toInt())		showFragment(nearby, position);
		else if(position == Places.SHOPS.toInt()) 		showFragment(shops, position);
		else if(position == Places.SPECIAL.toInt()) 	showFragment(special, position);
		else if(position == Places.FOOD.toInt()) 		showFragment(food, position);
		else if(position == Places.EXHIBITS.toInt()) 	showFragment(exhibit, position);
		else if(position == Places.AMENITIES.toInt()){	showMap(mTransaction, list.getView());
														if(!mDrawer.isOpened()) mDrawer.open();}
		else if(position == Places.ADMIN.toInt()) 		showFragment(admin, position);
		
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
	private View getCurrentFragment(){
			 if(currentFragment == Places.EXHIBITS)	return exhibit.getView();
		else if(currentFragment == Places.FOOD)		return food.getView();
		else if(currentFragment == Places.SPECIAL)	return special.getView();
		else if(currentFragment == Places.SHOPS)	return shops.getView();
		else if(currentFragment == Places.ADMIN)	return admin.getView();
		else if(currentFragment == Places.MAP)		return map.getView();
		else if(currentFragment == Places.FIND)		return nearby.getView();
		else 										return list.getView();
	}
	
	/**
	 * Shows the map from any showing fragment, taking into account if the screen is large or not
	 * @param mTransaction
	 * @param v
	 */
	protected void showMap(FragmentTransaction mTransaction, View v){
		clearMapData();
		if(!isLargeScreen && currentFragment!=Places.MAP)	Operations.swapViews(v, map);
		else if(!map.isShown())	Operations.addView(map);
		
		if(isLargeScreen){
			removeFrag(mTransaction);
			mTransaction.commit();
		}
		
		Operations.addView(mDrawer);
		
		map.getView().invalidate();
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
	protected void showMap(FragmentTransaction mTransaction,View v, LinkedList<PlaceItem> place){
		showMap(mTransaction, v);
		if (place!=null)	overlays.addAll(place);
		
		lastPlaces = (LinkedList<PlaceItem>) place.clone();
		map.getView().reDrawOverlays(true);
		
	}
	
	/**
	 * Reshows a fragment
	 */
	protected void showFragment(){
			 if(currentFragment == Places.EXHIBITS)	showFragment(exhibit, currentFragment.toInt());
		else if(currentFragment == Places.FOOD)		showFragment(food, currentFragment.toInt());
		else if(currentFragment == Places.SPECIAL)	showFragment(special, currentFragment.toInt());
		else if(currentFragment == Places.SHOPS)	showFragment(shops, currentFragment.toInt());
		else if(currentFragment == Places.ADMIN)	showFragment(admin, currentFragment.toInt());
		else if(currentFragment == Places.FIND)		showFragment(nearby, currentFragment.toInt());
	}
	
	/**
	 * Handles showing fragments in the activity
	 * @param fragment
	 */
	protected void showFragment(Fragment f, int position){
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		if(!isLargeScreen && currentFragment!=Places.MAP){
			
			if(!f.isAdded())	mTransaction.replace(android.R.id.content, f).commit();
			else				Operations.addView(f.getView());
			
			Operations.removeView(list.getView());
			Operations.removeView(mDrawer);
		} else if(isLargeScreen){
			if(map.isShown()){
				removeFrag(mTransaction);
				if(!f.isAdded())	mTransaction.add(R.id.PlaceView, f).commit();
			} else{
				if(f.isAdded()){
					PlaceFragment placeFrag = (PlaceFragment) f;
					placeFrag.refresh();
					MessageBuilder.showToast("Updated", this);
				} else{
					removeFrag(mTransaction);
					mTransaction.add(R.id.PlaceView, f).commit();
				}
			}
		} 
		currentFragment = Places.toPlace(position);
	}
	
	/**
	 * Removes currently showing fragment from screen, call mTransaction.commit() from called location
	 */
	private void removeFrag(FragmentTransaction mTransaction){
			 if(currentFragment == Places.EXHIBITS)		mTransaction.remove(exhibit);
		else if(currentFragment == Places.FOOD)			mTransaction.remove(food);
		else if(currentFragment == Places.SPECIAL)		mTransaction.remove(special);
		else if(currentFragment == Places.SHOPS)		mTransaction.remove(shops);
		else if(currentFragment == Places.ADMIN)		mTransaction.remove(admin);
		else if(currentFragment == Places.FIND)			mTransaction.remove(nearby);
	}
	
	/**
	 * Clears all showing place items from the screen
	 */
	private void clearMapData(){
		if(overlays.size()!=0){
			while(!overlays.isEmpty()){
				OverlayItem place = overlays.remove(0);
				map.getView().getmContainer().removeView(place.getIcon());
			}
		}
		
		if(lastPlaces != null){
			while(!lastPlaces.isEmpty()){
				map.getView().getmContainer().removeView(lastPlaces.poll().getIcon());
			}
			lastPlaces = null;
		}
		
		if(showGates)		overlays.addAll(gates);
		if(showParking)		overlays.addAll(parking);
		if(showRestrooms)	overlays.addAll(restrooms);
		overlays.add(me);
		
		map.getView().reDrawOverlays(true);
		map.invalidate();
	}
	
	/**
	 * Reads in places from file and puts them on the map
	 * @param fName
	 */
	private void readInPlaces(LinkedList<PlaceItem> pts, String fName, FragmentTransaction mTransaction,View v){
		if(pts.size()==0){
			PlaceFragment.readInFile(this, fName, pts);
		}
		overlays.addAll(pts);
		map.getView().reDrawOverlays(true);
		//this.showMap(mTransaction, v, points);
	}
	
	@Override
	public void performSearch(String query) {
		String querie = query.toLowerCase();
		boolean found = false;
		PlaceItem placeFound = null;
		
		for(int i =0; i < searchExhibits.size(); i++){
			PlaceItem place = searchExhibits.get(i);
			String name = place.getDescription().toLowerCase();
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
				map.animateTo(placeFound.getPoint());
				try {
					FileOutputStream fs = openFileOutput("queries.txt", Context.MODE_APPEND);
					OutputStreamWriter ow = new OutputStreamWriter(fs);
					BufferedWriter writer = new BufferedWriter(ow);

					writer.write(System.currentTimeMillis() + ", " + placeFound + "," + myLocation.getLatitude() + "," + myLocation.getLongitude() + ",\n");
					writer.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		} else{
			MessageBuilder.showToast("Not found", this);
		}
		
	}
	
	public MapView getMap(){
		return map;
	}
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count){
		searchList.removeAllViews();
		for(int i =0; i < searchExhibits.size(); i++){
			PlaceItem place = searchExhibits.get(i);
			if(place.getDescription().toLowerCase().contains(s.toString().toLowerCase()))
				searchList.addView(PlaceFragment.createExhibitItem(this, getLayoutInflater(), null, i+1, place, this, true));
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
		if(v.equals(map.getView())){
			int action = e.getAction() & MotionEvent.ACTION_MASK;
	     	if(action == MotionEvent.ACTION_MOVE){
	        	if(isTracking){
	        		follow.setIcon(R.drawable.ic_action_location);
					isTracking = false;
					me.follow(isTracking);
					MessageBuilder.showToast("Following Off", this);
	        	}
	        	
	     	}
		}
	     return super.onTouchEvent(e);
	}
	
}

