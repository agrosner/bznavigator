package edu.fordham.cis.wisdm.zoo.main;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import android.os.AsyncTask;
//import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
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
import com.grosner.mapview.MapBoundary;
import com.grosner.mapview.MapScale;
import com.grosner.mapview.MapView;
import com.grosner.mapview.OverlayItem;
import com.grosner.mapview.PlaceItem;
import com.grosner.mapview.ZoomLevel;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragment;
import edu.fordham.cis.wisdm.zoo.utils.ActionEnum;
import edu.fordham.cis.wisdm.zoo.utils.Places;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import edu.fordham.cis.wisdm.zoo.utils.WrapSlidingDrawer;


public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener, OnItemClickListener, SearchPerformListener, OnNavigationListener, TextWatcher, OnTouchListener{

	/**
	 * user's email from Login
	 */
	private static String email;
	
	/**
	 * The amenities drawer
	 */
	private SlidingDrawer mDrawer;
	
	/**
	 * Layout contained inside the drawer
	 */
	private LinearLayout mDrawerFrame;
	

	/**
	 * the popup list of exhibits that shows up when a user searches for an exhibit
	 */
	private LinearLayout searchList;
	
	private RelativeLayout[] mDrawerCheckBoxes = new RelativeLayout[3];
	
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
	
	private Animation showViewRight = null;
	
	private Animation showViewLeft = null;
	
	private Animation hideViewLeft = null;
	
	private Animation hideViewRight;

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
	
	private LinkedList<PlaceItem> searchExhibits = new LinkedList<PlaceItem>();
	
	private LinkedList<PlaceItem> misc = new LinkedList<PlaceItem>();
	
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
	public static CurrentLocationOverlay me = null;

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

		@SuppressWarnings("unchecked")
		@Override
		public void OnCurrentLocationChange(Geopoint point) {
			myLocation = me.getPoint();
			PlaceController.reCalculateDistance(myLocation, searchExhibits, parking, gates, restrooms);
		}
		
	};
	
	/**
	 * current fragment enum 
	 */
	private static Places currentFragment = Places.LIST;
	
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
		
		termsDialogBuilder.setCancelable(false).create().show();
		
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
		
		exhibit = PlaceFragment.initFrag(exhibit,PlaceFragment.PlaceType.EXHIBITS);
		food = PlaceFragment.initFrag(food, PlaceFragment.PlaceType.FOOD);
		special = PlaceFragment.initFrag(special, PlaceFragment.PlaceType.SPECIAL);
		shops = PlaceFragment.initFrag(shops,PlaceFragment.PlaceType.SHOPS);
		admin = PlaceFragment.initFrag(admin, PlaceFragment.PlaceType.ADMIN);
		
		email = getIntent().getExtras().getString("email");
		
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
		
		overlays = map.getOverlays();
		
		determineScreenLayout();
	
		map.init(this, "map0/%col%_%row%.png", 1024, 1280);
	    map.addMapScale(ZoomLevel.LEVEL_1, new MapScale("map1/%col%_%row%.png", 2048, 2560));
	    map.addMapScale(ZoomLevel.LEVEL_2, new MapScale("map2/%col%_%row%.png", 4096, 5120));
	    map.addMapScale(ZoomLevel.LEVEL_3, new MapScale("map3/%col%_%row%.png", 8192, 10240));
	    map.getView().setOnTouchListener(this);
	       
	    Geopoint.storeOffset(north, south, west, east);
	   
		me = new CurrentLocationOverlay(map.getView(), this, R.drawable.location);
		new LoadDataTask(this, this).execute();
		
		startLocationUpdates();
		nearby = new PlaceFragment(PlaceFragment.PlaceType.NEARBY, searchExhibits);
		
		me.addCurrentLocationChangedListener(meListener);
		
		Intent i = new Intent(this.getApplicationContext(), LocationUpdateService.class);
		i.putExtra("email", String.copyValueOf(email.toCharArray()));
		i.putExtra("password", String.copyValueOf(getIntent().getExtras().getString("password").toCharArray()));
		startService(i);
        
		//hide search list until it is used
        searchList = (LinearLayout) findViewById(R.id.SearchList);
        Operations.removeView(searchList);
       
   		addDrawerList();
	}
	/**
	 * Changes screen orientation and layout flow depending on whether screen is larger that LARGE qualifier
	 */
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
	
	/**
	 * Adds sliding drawer for persistent locations
	 */
	@SuppressWarnings("deprecation")
	private void addDrawerList(){
		mDrawer = (SlidingDrawer) findViewById(R.id.drawer);
		
		final Activity act = this;
		
		mDrawerFrame = (LinearLayout) mDrawer.findViewById(R.id.drawerFrame);
		//mDrawerFrame.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, SCREEN_WIDTH/4));
		
		final LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		TextView title = new TextView(act.getApplicationContext());
		title.setText("Amenities");
		title.setTextSize(20);
		mDrawerFrame.addView(title, pm);
		
		mDrawerCheckBoxes[0] = createIconCheckBox("Restrooms", R.drawable.bathroom, 0);
		mDrawerCheckBoxes[1] = createIconCheckBox("Gates", R.drawable.fordham, 1);
		mDrawerCheckBoxes[2] = createIconCheckBox("Parking Lots", R.drawable.car, 2);
		
		if(misc.isEmpty())	PlaceController.readInData(act, misc, "misc.txt");
		
		mDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener(){
	   
		@Override
		public void onDrawerOpened() {
			mTransaction = getSupportFragmentManager().beginTransaction();
			removeFrag(mTransaction, true).commit();
			currentFragment = Places.MAP;
			
			ImageView icon = (ImageView) mDrawer.getHandle();
			icon.setImageResource(R.drawable.ic_action_arrow_right);
			mDrawerFrame.removeAllViews();
			for(int i = 0; i < mDrawerCheckBoxes.length; i++){
				mDrawerFrame.addView(mDrawerCheckBoxes[i]);
			}
			PlaceController.reCalculateDistance(myLocation, misc);
			PlaceController.readInDataIntoList(act, mDrawerFrame, misc, new OnClickListener(){

				@Override
				public void onClick(View v) {
					int id = v.getId();
					mDrawer.animateClose();
					PlaceItem place = misc.get(id-1);
					LinkedList<PlaceItem> places = new LinkedList<PlaceItem>();
					places.add(place);
					showMap(mTransaction, list.getView(), places);
					map.getView().animateTo(place.getPoint());
				}
				
			}, true);
			
		}
	    	 
	    });
	    mDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener(){
	    	
	    @Override
	    public void onDrawerClosed() {
	    	ImageView icon = (ImageView) mDrawer.getHandle();
	    	icon.setImageResource(R.drawable.ic_action_arrow_left);
		}
	    
	    });
		
	}
	
	private OnCheckedChangeListener mCheckboxListener = new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton but, boolean isChecked) {
			LinkedList<PlaceItem> pts = null;
			String fileName = "";
			
			switch(but.getId()){
			case 0:
				showRestrooms = isChecked;
				pts = restrooms;
				fileName = "restrooms.txt";
				break;
			case 1:
				showGates = isChecked;
				pts = gates;
				fileName = "gates.txt";
				break;
			case 2:
				showParking = isChecked;
				pts = parking;
				fileName = "parking.txt";
				break;
			}
			
			if(isChecked)	readInPlaces(pts, fileName, mTransaction);
			else			map.getView().removeOverlayList(pts);
		}
		
	};
	
	private RelativeLayout createIconCheckBox(final String name, int iconId, int id){
		RelativeLayout iconCheckbox = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.icon_checkbox_item, null);
		CheckBox check = (CheckBox) iconCheckbox.findViewById(R.id.check);
		check.setText(name);
		check.setId(id);
		check.setOnCheckedChangeListener(mCheckboxListener);
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
		me.runOnFirstFix(new Runnable(){

			@Override
			public void run() {
				//map.animateTo(me.getPoint());
				myLocation = me.getPoint();
				if(!map.isShown() && !list.isVisible()){
					getCurrentPlaceFragment().refresh();
				}
				
				//once we get a fix, store it in locationupdateservice
				LocationUpdateService.storeFirstLocation(me.getLastFix(), con);
				PlaceController.reCalculateDistance(myLocation, searchExhibits, misc);
				//loader.dismiss();
				
				MessageBuilder.showToast("GPS lock found", getApplicationContext());
				map.getView().reDrawOverlays(true);
			}
			 
		 });
		loader.dismiss();
		 
	 }
	

	/**
	 * Stops updates and removes mylocationoverlay from the map
	 */
	 private void stopLocationUpdates(){
		 me.disableMyLocation();
		 overlays.remove(me);
		 map.getView().refresh();
		 map.postInvalidate();
	 }
	
	@Override
	public void onBackPressed(){
		if(currentFragment!=Places.LIST){
			showList();
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
			showMap(mTransaction, getCurrentFragmentView());
		} else if(item.getTitle().equals(ActionEnum.FOLLOW.toString())){
			if(!map.isShown()){	
				showMap(mTransaction, getCurrentFragmentView());
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
	
		if(!isLargeScreen&&currentFragment!=Places.LIST && currentFragment==Places.MAP){
			Operations.swapViewsAnimate(map, list.getView(), hideViewRight, showViewLeft);
		} else{
		
			if(isLargeScreen && currentFragment!=Places.MAP){
				mTransaction = this.getSupportFragmentManager().beginTransaction();
				removeFrag(mTransaction, true).commit();
			} else if(!isLargeScreen && currentFragment!=Places.LIST){
				mTransaction = this.getSupportFragmentManager().beginTransaction();
				removeFrag(mTransaction, false).commit();
				Operations.addViewAnimate(list.getView(), showViewLeft);
			} else{
			//	if the actual view within the listfragment is not visible, make it visible
				//if(!list.getView().isShown())	Operations.addViewAnimate(list.getView(), showViewLeft);
				//else if(isLargeScreen)			Operations.removeViewAnimate(list.getView(), hideViewLeft);
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
		removeFrag(mTransaction, true);
	
			 if(position == Places.MAP.toInt()) 		showMap(mTransaction, list.getView());
		else if(position == Places.NEWS.toInt()) 		MessageBuilder.showToast("Coming soon", this);
		else if(position == Places.AMENITIES.toInt()){	showMap(mTransaction, list.getView());
														if(!mDrawer.isOpened()) mDrawer.animateOpen();}
		else if(position == Places.FIND.toInt())		showFragment(nearby, position);
		else if(position == Places.SHOPS.toInt()) 		showFragment(shops, position);
		else if(position == Places.SPECIAL.toInt()) 	showFragment(special, position);
		else if(position == Places.FOOD.toInt()) 		showFragment(food, position);
		else if(position == Places.EXHIBITS.toInt()) 	showFragment(exhibit, position);
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
	private View getCurrentFragmentView(){
		if(currentFragment == Places.MAP)		return map.getView();
		else if(currentFragment == Places.LIST)	return list.getView();
		else 									return getCurrentPlaceFragment().getView();
	}
	
	private PlaceFragment getCurrentPlaceFragment(){
		if(currentFragment == Places.EXHIBITS)			return exhibit;
		else if(currentFragment == Places.FOOD)			return food;
		else if(currentFragment == Places.SPECIAL)		return special;
		else if(currentFragment == Places.ADMIN)		return admin;
		else if(currentFragment == Places.SHOPS)		return shops;
		else if(currentFragment == Places.FIND)			return nearby;
		else											return null;
	}

	
	/**
	 * Shows the map from any showing fragment, taking into account if the screen is large or not
	 * @param mTransaction
	 * @param v
	 */
	protected void showMap(FragmentTransaction mTransaction, View v){
		if(mDrawer.isOpened()){
			mDrawer.animateClose();
		}
		clearMapData();
		if(!isLargeScreen && currentFragment!=Places.MAP)	Operations.swapViewsAnimate(v, map, hideViewLeft, showViewRight);
		else if(!map.isShown())	Operations.addView(map);
		
		if(isLargeScreen){
			try{
				removeFrag(mTransaction, true).commit();
			} catch(IllegalStateException i){
				i.printStackTrace();
			}
		}
		
		Operations.addView(mDrawer);
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
		if (place!=null)	overlays.addAll(place);
		
		lastPlaces = (LinkedList<PlaceItem>) place.clone();
		map.getView().reDrawOverlays(true);
		
	}
	
	/**
	 * Handles showing fragments in the activity
	 * @param fragment
	 */
	protected void showFragment(Fragment f, int position){
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		mTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
		if(mDrawer.isOpened()){
			mDrawer.animateClose();
		}
		
		if(!isLargeScreen && currentFragment!=Places.MAP){
			
			if(!f.isAdded())	mTransaction.replace(android.R.id.content, f).commit();
			else				Operations.addViewAnimate(f.getView(), showViewRight);
			
			Operations.removeView(list.getView());
			Operations.removeView(mDrawer);
		} else if(isLargeScreen){
			if(map.isShown()){
				removeFrag(mTransaction, true);
				if(!f.isAdded())	mTransaction.add(R.id.PlaceView, f).commit();
			} else{
				if(f.isAdded()){
					PlaceFragment placeFrag = (PlaceFragment) f;
					placeFrag.refresh();
				} else{
					removeFrag(mTransaction, true).add(R.id.PlaceView, f).commit();
				}
			}
		} 
		currentFragment = Places.toPlace(position);
	}
	
	/**
	 * Removes currently showing fragment from screen, call mTransaction.commit() from called location
	 */
	private FragmentTransaction removeFrag(FragmentTransaction mTransaction, boolean removeLeft){
		int id;
		if(removeLeft)	id = R.anim.slide_out_left;
		else			id = android.R.anim.slide_out_right;
		mTransaction.setCustomAnimations(R.anim.slide_in_right, id);
		PlaceFragment p = getCurrentPlaceFragment();
		if(p!=null)	mTransaction.remove(p);
		return mTransaction;
	}
	
	/**
	 * Clears all showing place items from the screen
	 */
	private void clearMapData(){
		if(overlays.size()!=0){
			while(!overlays.isEmpty())	map.getView().getmContainer().removeView(overlays.remove(0).getIcon());
		}
		
		if(lastPlaces != null){
			while(!lastPlaces.isEmpty())	map.getView().getmContainer().removeView(lastPlaces.poll().getIcon());
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
	private void readInPlaces(LinkedList<PlaceItem> pts, String fName, FragmentTransaction mTransaction){
		if(pts.size()==0)	PlaceController.readInData(this, pts, fName);
		
		overlays.addAll(pts);
		map.getView().reDrawOverlays(true);
	}
	
	@Override
	public void performSearch(String query) {
		String querie = query.toLowerCase();
		boolean found = false;
		PlaceItem placeFound = null;
		
		for(PlaceItem place: searchExhibits){
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
		if(s.length()>0){
			searchList.removeAllViews();
			for(int i =0; i < searchExhibits.size(); i++){
				PlaceItem place = searchExhibits.get(i);
				if(place.getDescription().toLowerCase().contains(s.toString().toLowerCase()))
					searchList.addView(PlaceController.createExhibitItem(this, i+1, place, this, true));
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
			PlaceController.readInData(mActivity, searchExhibits, "exhibits.txt", "food.txt", "shops.txt",
					"gates.txt", "parking.txt", "admin.txt", "restrooms.txt", "misc.txt");
			
			return null;
		}
		
		protected Void onPostExecute(){
			PlaceController.readInDataIntoList(mActivity, searchList, searchExhibits, mOnClick, false);
			return null;
			
		}
	}
}

