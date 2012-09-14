package edu.fordham.cis.wisdm.zoo.main;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import edu.fordham.cis.wisdm.zoo.utils.Places;


public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener, OnItemClickListener, SearchPerformListener, OnNavigationListener, TextWatcher{

	/**
	 * user's email from Login
	 */
	private static String email;
	
	/**
	 * the button on top left of screen user presses to see the menu
	 */
	private ImageButton home;
	
	/**
	 * the popup list of exhibits that shows up when a user searches for an exhibit
	 */
	private LinearLayout searchList;
	
	/**
	 * the searchbar widget
	 */
	private MenuItemSearchAction menuItem;
	
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
	 * the mapview widget
	 */
	private static MapView map;
	
	/**
	 * Upper Y-coordinate of map
	 */
	private int north = (int)(40.8575319641275*1E6);
	
	/**
	 * Lower y-coordinate of map
	 */
	private int south = (int)(40.84133892124885 *1E6);
	
	/**
	 * Left-most x-coordinate on map
	 */
	private int west = (int)(-73.8843274391989*1E6);
	
	/**
	 * Right-most x-coordinate on map
	 */
	private int east = (int)(-73.87094804707424*1E6);
	
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
	private static LinkedList<PlaceItem> lastPlaces = null;
	
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
	 * Provides an action when the user's current location changes
	 */
	private CurrentLocationChangedListener meListener = new CurrentLocationChangedListener(){

		@Override
		public void OnCurrentLocationChange(Geopoint point) {
			myLocation = me.getPoint();
			PlaceFragment.reCalculateDistance(myLocation, searchExhibits);
			if(isTracking) map.animateTo(myLocation);
		}
		
	};
	
	/**
	 * current fragment enum 
	 */
	private static Places currentFragment = Places.LIST;
	
	private LinkedList<PlaceItem> searchExhibits = new LinkedList<PlaceItem>();
	
	@Override
	public void onConfigurationChanged(Configuration config){
		super.onConfigurationChanged(config);
		
		map.removeAllViews();
		init();
		if(config.orientation == Configuration.ORIENTATION_PORTRAIT && isLargeScreen){
			isLargeScreen = false;
			Operations.removeView(map);
        	addListButton();
		} else if(isLargeScreen){
			if(currentFragment==Places.MAP){
				showMap(mTransaction, list.getView());
			}
		} else if(!isLargeScreen && currentFragment == Places.MAP){
			showMap(mTransaction, list.getView());
		}
		map.getView().refresh();
		
	}
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		setContentView(R.layout.splash);
		loader = ProgressDialog.show(this, "Loading Data", "Please Wait");
		
		LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		HoloAlertDialogBuilder dialog = new HoloAlertDialogBuilder(this);
		dialog.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		
		//if network error message
		if(/*connect.getActiveNetworkInfo()==null || (*/!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)&& !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			dialog.setTitle("Please Turn on GPS");
			dialog.setMessage("Please navigate to settings and make sure GPS is turned on");
			dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);	
					finish();
				}
				
			});
			
		} else{
			//terms and conditions
			dialog.setTitle("Terms and Conditions");
			dialog.setMessage("Terms and conditions go here");
			dialog.setPositiveButton("I Accept", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					init();
					dialog.dismiss();
				}
				
			});
		}
		
		dialog.show();
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
	 * Creates a new fragment if the fragment is null pointing, else returns the original instance
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
		mAction.setDisplayHomeAsUpEnabled(false);
		mAction.setDisplayShowHomeEnabled(false);
		mAction.setTitle("Bronx Zoo Navigator");
				
		
		//load listfragment into memory
		list = (ArrayListFragment) this.getSupportFragmentManager().findFragmentById(R.id.listfragment);
		list.getListView().setOnItemClickListener(this);
		
		//init map objects
		map = null;
    	map = (MapView) findViewById(R.id.map);
		overlays = map.getOverlays();
		
		map.init(this, "map1/crop_%col%_%row%.png", 1250, 2000);
		
	    map.addMapScale(ZoomLevel.LEVEL_1, new MapScale("map2/crop_%col%_%row%.png", 2500, 4000));
	    map.addMapScale(ZoomLevel.LEVEL_2, new MapScale("map3/crop_%col%_%row%.png", 3800, 6000));
	    map.addMapScale(ZoomLevel.LEVEL_3, new MapScale("map4/crop_%col%_%row%.png", 5100, 8000));
	   // map.addMapScale(ZoomLevel.LEVEL_4, new MapScale("map5/crop_%col%_%row%.png", 6400, 10000));
	       
	    //saves offset in memory
	    Geopoint.storeOffset(north, south, west, east);
	       
		
		//places = new PlaceOverlay(getResources().getDrawable(R.drawable.location), this, map);
		
		me = new CurrentLocationOverlay(map.getView(), this);
		startLocationUpdates();
		me.addCurrentLocationChangedListener(meListener);
		
		Intent i = new Intent(this.getApplicationContext(), LocationUpdateService.class);
		i.putExtra("email", email);
		startService(i);
		
		//get screen width to optimize layout
        DisplayMetrics display = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(display);
        SCREEN_WIDTH = display.widthPixels;
        
        //if in landscape of a larger sized screen, we change to large screen mode
        int orient = this.getResources().getConfiguration().orientation;
        int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if((screensize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screensize == Configuration.SCREENLAYOUT_SIZE_LARGE) && (orient == Configuration.ORIENTATION_LANDSCAPE)){
        	//scale the width of the sidebar to specified size
			LayoutParams lp = new LayoutParams((SCREEN_WIDTH/5), LayoutParams.FILL_PARENT);
        	list.getView().setLayoutParams(lp);
        	isLargeScreen = true;
        	
        } else{//small screen we want to hide it
        	Operations.removeView(map);
        	addListButton();
    		isLargeScreen = false;
    		list.getView().setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        }
        
       searchList = (LinearLayout) findViewById(R.id.SearchList);
       Operations.removeView(searchList);
       
       
	}
	
	private void addListButton(){
		ActionBar mAction = this.getSupportActionBar();
		
		home = new ImageButton(this);
		home.setId(0);
		home.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		home.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_action_list));
		mAction.setCustomView(home);
		mAction.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		home.setOnClickListener(this);
	}
	
	
	@Override
	public void onPause(){
		super.onPause();
		if(enableLoc){
			stopLocationUpdates();
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(enableLoc){
			startLocationUpdates();
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(enableLoc){
			stopLocationUpdates();
		}
	}
	
	private void startLocationUpdates(){
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		me.enableMyLocation();
		if(!overlays.contains(me))	overlays.add(me);
		
		map.postInvalidate();
		 
		String locationMessage = null;
		 
		 	 if (lManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
				locationMessage ="GPS provider enabled.";
		else if (lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
				locationMessage = "Network provider enabled.";
		else	locationMessage = "No provider enabled. Please check settings and allow locational services.";
			
		Toast.makeText(this, locationMessage, Toast.LENGTH_SHORT).show();
		 
		final ContextWrapper con = this;
		final Activity act = this;
		final LayoutInflater inflater = this.getLayoutInflater();
		final OnClickListener onclick = this;
		me.runOnFirstFix(new Runnable(){

			@Override
			public void run() {
				map.animateTo(me.getPoint());
				myLocation = me.getPoint();
				if(!map.isShown() && !list.isVisible()){
						 if(currentFragment == Places.EXHIBITS)		exhibit.refresh();
					else if(currentFragment == Places.FOOD)			food.refresh();
					else if(currentFragment == Places.SPECIAL)		special.refresh();
					else if(currentFragment == Places.ADMIN)		admin.refresh();
					else if(currentFragment == Places.SHOPS)		shops.refresh();
				}
				
				//once we get a fix, store it in locationupdateservice
				LocationUpdateService.storeFirstLocation(me.getLastFix(), con);
				
				//reads exhibits into the searchbar list of places
				readInFile(act, inflater, null, "exhibits.txt", searchList, searchExhibits, onclick);
				loader.dismiss();
			}
			 
		 });
		 
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
	public void readInFile(final Activity act, final LayoutInflater inflater, final ViewGroup container, final String fName, final LinearLayout searchList, final LinkedList<PlaceItem> searchExhibits, final OnClickListener onclick){
		this.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				searchList.removeAllViews();
				searchExhibits.clear();
				PlaceFragment.readInFile(act, inflater, container, fName, searchList, searchExhibits, onclick, true);
			
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
		} else{
		
			//ask user whether quit or not
			HoloAlertDialogBuilder message = new HoloAlertDialogBuilder(this);
			message.setTitle("Quit?");
			message.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
				
			});
			message.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override	
				public void onClick(DialogInterface dialog, int which) {}
				
			});
			message.create().show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		searchList = (LinearLayout) findViewById(R.id.SearchList);
		
		//adds the searchbar to the actionbar
		menuItem = new MenuItemSearchAction(this, menu, this, getResources().getDrawable(R.drawable.ic_action_search), this, searchList);
		menuItem.setTextColor(getResources().getColor(R.color.forestgreen));
		menuItem.getMenuItem().setOnMenuItemClickListener(this);
		
		//add action items
		menu.add("Locate").setOnMenuItemClickListener(this).setIcon(R.drawable.ic_action_location).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add("Nearest").setOnMenuItemClickListener(this).setIcon(R.drawable.ic_action_show).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add("About").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add("Settings").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		if(item.getTitle().equals("Settings")){
			
		} else if(item.getTitle().equals("About")){
			//ask user whether quit or not
			HoloAlertDialogBuilder message = new HoloAlertDialogBuilder(this);
			message.setTitle("About:");
			message.setMessage("\nDeveloper: Andrew Grosner\n\t\t\t\t  agrosner@fordham.edu\n" +
					"\nAssistant Developer: Isaac Ronan\n" 
					+ "\nWireless Sensor Data Mining (WISDM)");
			message.setNeutralButton("Ok", null);
			message.create().show();
		} else if(item.getTitle().equals("Search")){
			showMap(mTransaction, list.getView());
		} else if(item.getTitle().equals("Locate")){
			if(!map.isShown())	showMap(mTransaction, list.getView());
			
			if(!isTracking){
				item.setIcon(R.drawable.ic_action_location_blue);
				map.animateTo(myLocation);
				isTracking = true;
			} else{
				item.setIcon(R.drawable.ic_action_location);
				isTracking = false;
			}
			
		} else if(item.getTitle().equals("Nearest")){
			
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == 0){
			showList();
			if(map.isShown() && !isLargeScreen)	Operations.removeView(map);
		//search exhibit ids
		} else{
			mTransaction = this.getSupportFragmentManager().beginTransaction();
			
			PlaceItem place = searchExhibits.get(id-1);
			LinkedList<PlaceItem> places = new LinkedList<PlaceItem>();
			places.add(place);
			
			showMap(mTransaction, list.getView(), places);
			if(searchList.isShown()){
				Operations.removeView(searchList);
				menuItem.getMenuItem().collapseActionView();
			}
			//mapControl.setZoom(19);
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
		} else{
			//if the actual view within the listfragment is not visible, make it visible
			if(!list.getView().isShown())	Operations.addView(list.getView());
			else if(isLargeScreen)			Operations.removeView(list.getView());
		}
		currentFragment = Places.LIST;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		//if the searchbar activated, we will remove it 
		if(searchList.isShown()){
			Operations.removeView(searchList);
			menuItem.getMenuItem().collapseActionView();
		}
			 if(position == Places.MAP.toInt()) 		showMap(mTransaction, list.getView());
		else if(position == Places.NEWS.toInt()) 		;
		else if(position == Places.SHOPS.toInt()) 		showFragment(shops, position);
		else if(position == Places.SPECIAL.toInt()) 	showFragment(special, position);
		else if(position == Places.FOOD.toInt()) 		showFragment(food, position);
		else if(position == Places.EXHIBITS.toInt()) 	showFragment(exhibit, position);
		else if(position == Places.RESTROOMS.toInt()) 	readInPlaces("restrooms.txt", mTransaction, list.getView());
		else if(position == Places.EXITS.toInt()) 		readInPlaces("gates.txt", mTransaction, list.getView());
		else if(position == Places.PARKING.toInt()) 	readInPlaces("parking.txt", mTransaction, list.getView());
		else if(position == Places.ADMIN.toInt()) 		showFragment(admin, position);
	
	}
	
	/**
	 * Shows the map from any showing fragment, taking into account if the screen is large or not
	 * @param mTransaction
	 * @param v
	 */
	protected void showMap(FragmentTransaction mTransaction, View v){
		clearMapData();
		if(!isLargeScreen && currentFragment!=Places.MAP && list.isAdded())
			Operations.swapViews(v, map);
		else if(!map.isShown())	Operations.addView(map);
		
		if(isLargeScreen){
			removeFrag(mTransaction);
			mTransaction.commit();
		}
		
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
	protected void showMap(FragmentTransaction mTransaction,View v, LinkedList<PlaceItem> place){
		showMap(mTransaction, v);
		if (place!=null)	overlays.addAll(place);
		
		lastPlaces = (LinkedList<PlaceItem>) place.clone();
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
		} else if(isLargeScreen){
			if(map.isShown()){
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
	 * Removes currently showing fragment from screen
	 */
	private void removeFrag(FragmentTransaction mTransaction){
			 if(currentFragment == Places.EXHIBITS)		mTransaction.remove(exhibit);
		else if(currentFragment == Places.FOOD)			mTransaction.remove(food);
		else if(currentFragment == Places.SPECIAL)		mTransaction.remove(special);
		else if(currentFragment ==Places.SHOPS)			mTransaction.remove(shops);
		else if(currentFragment == Places.ADMIN)		mTransaction.remove(admin);
	}
	
	/**
	 * Clears all showing place items from the screen
	 */
	private void clearMapData(){
		CurrentLocationOverlay cur = null;
		if(overlays.size()!=0){
			while(!overlays.isEmpty()){
				OverlayItem place = overlays.remove(0);
				if(place instanceof PlaceItem){
					PlaceItem i = (PlaceItem) place;
					if(i.isMenuShowing())	i.removeMenu(map.getView().getmContainer());
					i.setAdded(false);
				}else if(place instanceof CurrentLocationOverlay){
					cur = (CurrentLocationOverlay) place;
				} else{
					map.getView().getmContainer().removeView(place.getIcon());
				}
			}
			overlays.add(cur);
		}
		
		if(lastPlaces != null){
			while(!lastPlaces.isEmpty()){
				PlaceItem place = lastPlaces.poll();
				place.setAdded(false);
				
				if(place.isMenuShowing())	place.removeMenu(map.getView().getmContainer());
				
				map.getView().getmContainer().removeView(place.getIcon());
			}
			lastPlaces = null;
		}
		map.invalidate();
	}
	
	/**
	 * Reads in places from file and puts them on the map
	 * @param fName
	 */
	private void readInPlaces(String fName, FragmentTransaction mTransaction,View v){
		LinkedList<PlaceItem> points = new LinkedList<PlaceItem>();
		PlaceFragment.readInFile(this, fName, points);
		this.showMap(mTransaction, v, points);
	}
	
	@Override
	public void performSearch(String query) {
		String querie = query.toLowerCase();
		boolean found = false;
		PlaceItem placeFound = null;
		
		PlaceItem[] searchArray = (PlaceItem[]) searchExhibits.toArray(new PlaceItem[searchExhibits.size()]);
		for(int i =0; i < searchArray.length; i++){
			String name = searchArray[i].getDescription().toLowerCase();
			if(name.contains(querie)){
				found = true;
				placeFound = searchArray[i];
				break;
			}
		}
		
		if(found){
			if(placeFound!=null){
				LinkedList<PlaceItem> place = new LinkedList<PlaceItem>();
				place.add(placeFound);
				this.showMap(mTransaction, list.getView(), place);
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
	
}
