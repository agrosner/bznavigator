package edu.fordham.cis.wisdm.zoo.main;

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
import com.grosner.mapview.CurrentLocationOverlay;
import com.grosner.mapview.Geopoint;
import com.grosner.mapview.MapScale;
import com.grosner.mapview.MapView;
import com.grosner.mapview.OverlayItem;
import com.grosner.mapview.PlaceItem;
import com.grosner.mapview.ZoomLevel;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;


public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener, OnItemClickListener, SearchPerformListener, OnNavigationListener, TextWatcher{

	private static String email;
	
	//the button on top left of screen user presses to see the menu
	private ImageButton home;
	
	//the popup list of exhibits that shows up when a user searches for an exhibit
	private LinearLayout searchList;
	
	//the searchbar widget
	private MenuItemSearchAction menuItem;
	
	//displays loading when reading in exhibits from file
	private ProgressDialog loader;
	
	//the list fragment displays on splash screen
	private static ArrayListFragment list;
	
	//restroom locator fragment
	private static PlaceFragment food = null;
	
	//exhibit locator/descriptor fragment
	private static PlaceFragment exhibit = null;
	
	//the ride locator fragment
	private static PlaceFragment special = null;
	
	//the shops locator fragment
	private static PlaceFragment shops =null;
	
	//the administration buildings fragment
	private static PlaceFragment admin = null;
	
	//the mapview widget
	private static MapView map;
	
	//boundaries
	private int north = (int)(40.8575319641275*1E6);
	private int south = (int)(40.84133892124885 *1E6);
	private int west = (int)(-73.8843274391989*1E6);
	private int east = (int)(-73.87094804707424*1E6);
	
	
	
	//whether location should be enabled
	private boolean enableLoc = false;
	
	//the mapview's overlays
	private static List<OverlayItem> overlays; 
	
	//the last place shown on the map
	private static PlaceItem lastPlace = null;
	
	//the last group of places shown
	private static LinkedList<PlaceItem> lastPlaces = null;
	
	//handles switching in fragments
	private static FragmentTransaction mTransaction;
	
	//determine whether tablet or not to optimize screen real estate 
	public static boolean isLargeScreen = false;
	
	//determine screen width for sidebar
	public static int SCREEN_WIDTH = 0;
	
	//manages location changes
	private LocationManager lManager = null;
		
	//shows current location
	public static CurrentLocationOverlay me = null;
	
	//map list item constant
	private static final int ITEM_CASE_MAP = 0;
	
	//news list item constant
	private static final int ITEM_CASE_NEWS = 1;
	
	//shop list item constant
	private static final int ITEM_CASE_SHOPS = 2;
	
	//special exhibits
	private static final int ITEM_CASE_SPECIAL = 3;
	
	//food list item constant
	private static final int ITEM_CASE_FOOD = 4;
	
	//exhibit list item constant
	private static final int ITEM_CASE_EXHIBITS = 5;
	
	//restroom list item constant
	private static final int ITEM_CASE_RESTROOMS = 6;
	
	//exits/misc locations
	private static final int ITEM_CASE_EXITS = 7;
	
	//parking lot constant
	private static final int ITEM_CASE_PARKING = 8;
	
	//administration buildings constant
	private static final int ITEM_CASE_ADMIN = 9;
	
	private int currentFragment = -1;
	
	public static Geopoint myLocation = null;
	
	private boolean start = true;
	
	private LinkedList<PlaceItem> searchExhibits = new LinkedList<PlaceItem>();
	
	@Override
	public void onConfigurationChanged(Configuration config){
		super.onConfigurationChanged(config);
		
		map.removeAllViews();
		init();
		if(!isLargeScreen){
			showList();
		}
		if(config.orientation == Configuration.ORIENTATION_PORTRAIT && isLargeScreen){
			isLargeScreen = false;
			Operations.removeView(map);
        	home = new ImageButton(this);
    		home.setId(1);
    		home.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    		home.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_action_list));
    		ActionBar mAction = this.getSupportActionBar();
    		mAction.setCustomView(home);
    		mAction.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    		home.setOnClickListener(this);
		}
		
	}
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		setContentView(R.layout.splash_screen);
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
		if(connect.getActiveNetworkInfo()==null || (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)&& !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) ){
			dialog.setTitle("Please Check Internet Connection");
			dialog.setMessage("Please navigate to settings and make sure internet is connected");
			dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);	
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
		
		exhibit = initFrag(exhibit,PlaceFragment.TYPE_EXHIBITS);
		food = initFrag(food, PlaceFragment.TYPE_FOOD);
		special = initFrag(special, PlaceFragment.TYPE_SPECIAL);
		shops = initFrag(shops,PlaceFragment.TYPE_SHOPS);
		admin = initFrag(admin, PlaceFragment.TYPE_ADMIN);
		
		email = getIntent().getExtras().getString("email");
		
		
		setUpViews();
		
	}
	private PlaceFragment initFrag(PlaceFragment frag, int type){
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
		
		map.init(this, "map0/crop_%col%_%row%.png");

	    map.addMapScale(ZoomLevel.LEVEL_1, new MapScale("map1/crop_%col%_%row%.png", 2500, 4000));
	    map.addMapScale(ZoomLevel.LEVEL_2, new MapScale("map2/crop_%col%_%row%.png", 3800, 6000));
	    map.addMapScale(ZoomLevel.LEVEL_3, new MapScale("map3/crop_%col%_%row%.png", 5100, 8000));
	    map.addMapScale(ZoomLevel.LEVEL_4, new MapScale("map4/crop_%col%_%row%.png", 6400, 10000));
	       
	    //saves offset in memory
	    Geopoint.storeOffset(north, south, west, east);
	       
		
		//places = new PlaceOverlay(getResources().getDrawable(R.drawable.location), this, map);
		
		me = new CurrentLocationOverlay(map.getView(), this);
		startLocationUpdates();
		
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
        	home = new ImageButton(this);
    		home.setId(0);
    		home.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    		home.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_action_list));
    		mAction.setCustomView(home);
    		mAction.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    		home.setOnClickListener(this);
    		isLargeScreen = false;
        }
        
       searchList = (LinearLayout) findViewById(R.id.SearchList);
       Operations.removeView(searchList);
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
		 if(!overlays.contains(me))
			 overlays.add(me);
		 map.postInvalidate();
		 
		 String locationMessage = null;
		 
		 if (lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				locationMessage ="GPS provider enabled.";
			} else if (lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
				locationMessage = "Network provider enabled.";
			} else{
				locationMessage = "No provider enabled. Please check settings and allow locational services.";
			}
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
					if(currentFragment == ITEM_CASE_EXHIBITS){
						exhibit.refresh();
					} else if(currentFragment == ITEM_CASE_FOOD){
						food.refresh();
					}
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
			if(map.isShown() && !isLargeScreen){
				Operations.removeView(map);
			}
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
				public void onClick(DialogInterface dialog, int which) {
					
				}
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
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == 0){
			showList();
			if(map.isShown() && !isLargeScreen){
				Operations.removeView(map);
			}
		//search exhibit ids
		} else{
			mTransaction = this.getSupportFragmentManager().beginTransaction();
			PlaceItem place = searchExhibits.get(id-1);
			showMap(mTransaction, list.getView(), place);
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
			
			if(currentFragment == ITEM_CASE_EXHIBITS){
				mTransaction.remove(exhibit).commit();
			} else if(currentFragment == ITEM_CASE_FOOD){
				mTransaction.remove(food).commit();
			} else if(currentFragment == ITEM_CASE_SPECIAL){
				mTransaction.remove(special).commit();
			} else if(currentFragment == ITEM_CASE_SHOPS){
				mTransaction.remove(shops).commit();
			} else if(currentFragment == ITEM_CASE_ADMIN){
				mTransaction.remove(admin).commit();
			}
		} else{
			//if the actual view within the listfragment is not visible, make it visible
			if(!list.getView().isShown()){
				Operations.addView(list.getView());
			} else if(isLargeScreen){//else hide
				Operations.removeView(list.getView());
			}
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		//if the searchbar activated, we will remove it 
		if(searchList.isShown()){
			Operations.removeView(searchList);
			menuItem.getMenuItem().collapseActionView();
		}
		switch(position){
		case ITEM_CASE_MAP:
			//replace mapview with the list
			showMap(mTransaction, list.getView());
			break;
		case ITEM_CASE_NEWS:
			
			break;
		case ITEM_CASE_SHOPS:
			showFragment(shops, position, true);
			//
			break;
		case ITEM_CASE_SPECIAL:
			showFragment(special, position, true);
			break;
		case ITEM_CASE_FOOD:
			showFragment(food, position, true);
			break;
		case ITEM_CASE_EXHIBITS:
			showFragment(exhibit, position, true);
			break;
		case ITEM_CASE_RESTROOMS:
			readInPlaces("restrooms.txt", mTransaction, list.getView());
			break;
		case ITEM_CASE_EXITS:
			readInPlaces("gates.txt", mTransaction, list.getView());
			break;
		case ITEM_CASE_PARKING:
			readInPlaces("parking.txt", mTransaction, list.getView());
			break;
		case ITEM_CASE_ADMIN:
			showFragment(admin, position, true);
			break;
		}
	}
	
	public void getZoom(){
		
	}
	
	/**
	 * Shows the map adding a placeitem's geopoint to the map
	 * @see showMap(FragmentTransaction, View)
	 * @param mTransaction
	 * @param flag
	 * @param v
	 */
	protected void showMap(FragmentTransaction mTransaction,View v, PlaceItem place){
		showMap(mTransaction, v);
		if(place!=null){
			if(!place.isAdded()){
				place.setAdded(true);
				overlays.add(place);
				map.animateTo(place.getPoint());
				//mapControl.setZoom(19);
			}
		}
		lastPlace = place;
		
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
		if (place!=null){
			overlays.addAll(place);
		}
		lastPlaces = place;
	}
	
	/**
	 * Shows the map from any showing fragment, taking into account if the screen is large or not
	 * @param mTransaction
	 * @param v
	 */
	protected void showMap(FragmentTransaction mTransaction, View v){
		clearMapData();
		if(!isLargeScreen && !map.isShown() && list.isAdded()){
			Operations.swapViews(v, map);
		} else if(!map.isShown()){
			Operations.addView(map);
			if(currentFragment == ITEM_CASE_EXHIBITS){
				mTransaction.remove(exhibit).commit();
			} else if(currentFragment == ITEM_CASE_FOOD){
				mTransaction.remove(food).commit();
			} else if(currentFragment == ITEM_CASE_SPECIAL){
				mTransaction.remove(special).commit();
			} else if(currentFragment == ITEM_CASE_SHOPS){
				mTransaction.remove(shops).commit();
			} else if(currentFragment == ITEM_CASE_ADMIN){
				mTransaction.remove(admin).commit();
			}
		}
	}
	
	/**
	 * Handles showing fragments in the activity
	 * @param fragment
	 */
	protected void showFragment(Fragment f, int position, boolean place){
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		if(!isLargeScreen && !map.isShown()){
			mTransaction.replace(android.R.id.content, f).commit();
			Operations.removeView(list.getView());
		} else if(isLargeScreen){
			if(map.isShown()){
				Operations.removeView(map);
				if(!f.isAdded()){
					mTransaction.add(R.id.mapframe, f).commit();
				}
			} else{
				if(f.isAdded() && isLargeScreen && place){
					PlaceFragment placeFrag = (PlaceFragment) f;
					placeFrag.refresh();
					MessageBuilder.showToast("Updated", this);
				} else{
					mTransaction.replace(R.id.mapframe, f).commit();
				}
			}
		} 
		currentFragment = position;
	}
	
	
	/**
	 * Clears all showing placeitems from the screen
	 */
	private void clearMapData(){
		CurrentLocationOverlay cur = null;
		if(overlays.size()!=0){
			while(!overlays.isEmpty()){
				OverlayItem place = overlays.remove(0);
				if(place instanceof PlaceItem){
					PlaceItem i = (PlaceItem) place;
					if(i.isMenuShowing()){
						i.removeMenu(map.getView().getmContainer());
					}
					i.setAdded(false);
				}else if(place instanceof CurrentLocationOverlay){
					cur = (CurrentLocationOverlay) place;
				} else{
					map.getView().getmContainer().removeView(place.getIcon());
				}
			}
			overlays.add(cur);
		}
		if(lastPlace!=null){
			if(lastPlace.isMenuShowing()){
				lastPlace.removeMenu(map.getView().getmContainer());
			}
			lastPlace.setAdded(false);
			map.getView().getmContainer().removeView(lastPlace.getIcon());
			lastPlace = null;
		}
		if(lastPlaces != null){
			while(!lastPlaces.isEmpty()){
				PlaceItem place = lastPlaces.poll();
				place.setAdded(false);
				if(place.isMenuShowing()){
					place.removeMenu(map.getView().getmContainer());
				}
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
				this.showMap(mTransaction, list.getView(), placeFound);
			}
		} else{
			MessageBuilder.showToast("Not found", this);
		}
		
	}
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count){
		searchList.removeAllViews();
		for(int i =0; i < searchExhibits.size(); i++){
			PlaceItem place = searchExhibits.get(i);
			if(place.getDescription().toLowerCase().contains(s.toString().toLowerCase())){
				searchList.addView(PlaceFragment.createExhibitItem(this, getLayoutInflater(), null, i+1, place, this, true));
			}
		}
		
	}
    
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		return false;
	}

	@Override
	public void afterTextChanged(Editable s) {
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		searchList.removeAllViews();
	}




	
}
