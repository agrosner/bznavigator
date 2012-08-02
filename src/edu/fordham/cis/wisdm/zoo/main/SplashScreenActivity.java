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
import android.graphics.Paint.Style;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.location.LocationManager;
import android.net.Uri;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.Operations;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.WazaBe.HoloEverywhere.R.style;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;
import edu.fordham.cis.wisdm.zoo.map.PlaceItem;
import edu.fordham.cis.wisdm.zoo.map.PlaceOverlay;


public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener, OnItemClickListener, SearchPerformListener, OnNavigationListener, TextWatcher{

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
	private static PlaceFragment food;
	
	//exhibit locator/descriptor fragment
	private static PlaceFragment exhibit;
	
	//the ride locator fragment
	private static PlaceFragment special;
	
	//the mapview widget
	private static MapView map;
	
	private static List<Overlay> overlays; 
	
	private static PlaceItem lastPlace = null;
	
	private static LinkedList<PlaceItem> lastPlaces = null;
	
	private static FragmentTransaction mTransaction;
	
	//determine whether tablet or not to optimize screen real estate 
	public static boolean isLargeScreen = false;
	
	//determine screen width for sidebar
	public static int SCREEN_WIDTH = 0;
	
	public static double SCREEN_INCHES = 0;
	
	//manages location changes
	private LocationManager lManager = null;
		
	//controls the map
	private MapController mapControl = null;
	
	//shows current location
	public static MyLocationOverlay me = null;
	
	//map list item constant
	private static final int ITEM_CASE_MAP = 0;
	
	//news list item constant
	private static final int ITEM_CASE_NEWS = 1;
	
	//store list item constant
	private static final int ITEM_CASE_STORE = 2;
	
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
	
	private int currentFragment = -1;
	
	public static GeoPoint myLocation = null;
	
	private boolean start = true;
	
	private PlaceOverlay places;
	
	private LinkedList<PlaceItem> searchExhibits = new LinkedList<PlaceItem>();
	
	@Override
	public void onConfigurationChanged(Configuration config){
		super.onConfigurationChanged(config);
		
		
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
		
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		if(start){
			exhibit = new PlaceFragment(PlaceFragment.TYPE_EXHIBITS);
			food = new PlaceFragment(PlaceFragment.TYPE_FOOD);
			special = new PlaceFragment(PlaceFragment.TYPE_SPECIAL);
			start = false;
		} 
		
		loader = ProgressDialog.show(this, "Loading Data", "Please Wait");
		
		setUpViews();
	}
	public void setUpViews(){
		//hide name, icon, 
		ActionBar mAction = this.getSupportActionBar();
		mAction.setDisplayHomeAsUpEnabled(false);
		mAction.setDisplayShowHomeEnabled(false);
		mAction.setDisplayShowTitleEnabled(false);
				
		
		//load listfragment into memory
		list = (ArrayListFragment) this.getSupportFragmentManager().findFragmentById(R.id.listfragment);
		list.getListView().setOnItemClickListener(this);
		
        //init map objects
    	map = (MapView) findViewById(R.id.Map);
    	map.setBuiltInZoomControls(false);
		map.setSatellite(true);
		overlays = map.getOverlays();
		 
		mapControl = map.getController();
		mapControl.setZoom(17);
		
		places = new PlaceOverlay(getResources().getDrawable(R.drawable.pin), this, map);
		
		me = new MyLocationOverlay(this, map);
		startLocationUpdates();
		startService(new Intent(this.getApplicationContext(), LocationUpdateService.class));
		
		//get screen width to optimize layout
        DisplayMetrics display = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(display);
        double x = Math.pow(display.widthPixels/display.xdpi,2);
        double y = Math.pow(display.heightPixels/display.ydpi,2);
        SCREEN_INCHES = Math.sqrt(x+y);
        SCREEN_WIDTH = display.widthPixels;
        
        //if in landscape of a larger sized screen, we change to large screen mode
        int orient = this.getResources().getConfiguration().orientation;
        if(SCREEN_INCHES>=7 && (orient == Configuration.ORIENTATION_LANDSCAPE)){
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
    		
        }
        
       searchList = (LinearLayout) findViewById(R.id.SearchList);
       Operations.removeView(searchList);
       
	}
	
	@Override
	public void onPause(){
		super.onPause();
		stopLocationUpdates();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		startLocationUpdates();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		stopLocationUpdates();
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
				mapControl.animateTo(me.getMyLocation());
				myLocation = me.getMyLocation();
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
		} else{
			mTransaction = this.getSupportFragmentManager().beginTransaction();
			PlaceItem place = searchExhibits.get(id-1);
			showMap(mTransaction, list.getView(), place);
			if(searchList.isShown()){
				Operations.removeView(searchList);
				menuItem.getMenuItem().collapseActionView();
			}
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
		case ITEM_CASE_STORE:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bronxzoostore.com")));
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
			showMap(mTransaction, list.getView());
			break;
		case ITEM_CASE_EXITS:
			readInPlaces("gates.txt", mTransaction, list.getView());
			break;
		case ITEM_CASE_PARKING:
			readInPlaces("parking.txt", mTransaction, list.getView());
			break;
		}
	}
	
	/**
	 * Shows the map with placefragment flags and a geopoint to add as overlay
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
				places.addOverlay(place);
				places.addBubble(0, map);
				overlays.add(places);
				map.getController().animateTo(place.getPoint());
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
			places.addOverlayList(place);
			overlays.add(places);
			map.getController().animateTo(places.getItem(0).getPoint());
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
		if(places.size()!=0){
			while(!places.isEmpty()){
				PlaceItem place = places.poll();
				place.setAdded(false);
				if(place.isMenuShowing()){
					map.removeView(place.getBubble());
				}
			}
		}
		if(lastPlace!=null){
			if(lastPlace.isMenuShowing()){
				map.removeView(lastPlace.getBubble());
			}
			lastPlace.setAdded(false);
			lastPlace = null;
		}
		if(lastPlaces != null){
			while(!lastPlaces.isEmpty()){
				PlaceItem place = lastPlaces.poll();
				place.setAdded(false);
				if(place.isMenuShowing()){
					map.removeView(place.getBubble());
				}
			}
			lastPlaces = null;
		}
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
			String name = searchArray[i].getTitle().toLowerCase();
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
			if(place.getTitle().toLowerCase().contains(s.toString().toLowerCase())){
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
