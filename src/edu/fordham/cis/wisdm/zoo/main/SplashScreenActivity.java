package edu.fordham.cis.wisdm.zoo.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.Operations;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;
import edu.fordham.cis.wisdm.zoo.map.MyLocOverlay;


public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener, OnItemClickListener, SearchPerformListener, OnNavigationListener{

	//the button on top left of screen user presses to see the menu
	private ImageButton home;
	
	//layout that holds all of the fragments and the mapview
	private FrameLayout contentFrame;
	
	//the list fragment displays on splash screen
	private static ArrayListFragment list;
	
	//restroom locator fragment
	private static RestroomsFragment restroom;
	
	//exhibit locator/descriptor fragment
	private static ExhibitFragment exhibit;
	
	//the mapview widget
	private static MapView map;
	
	private static FragmentTransaction mTransaction;
	
	//determine whether tablet or not to optimize screen real estate 
	private boolean isLargeScreen = false;
	
	//manages location changes
	private LocationManager lManager = null;
		
	//controls the map
	private MapController mapControl = null;
	
	//shows current location
	private MyLocOverlay me = null;
	
	//map list item constant
	private static final int ITEM_CASE_MAP = 0;
	
	//news list item constant
	private static final int ITEM_CASE_NEWS = 1;
	
	//store list item constant
	private static final int ITEM_CASE_STORE = 2;
	
	//food list item constant
	private static final int ITEM_CASE_FOOD = 3;
	
	//exhibit list item constant
	private static final int ITEM_CASE_EXHIBITS = 4;
	
	//restroom list item constant
	private static final int ITEM_CASE_RESTROOMS = 5;
	
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		setContentView(R.layout.splash_screen);
		
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		//hide name, icon, 
		ActionBar mAction = this.getSupportActionBar();
		mAction.setDisplayHomeAsUpEnabled(false);
		mAction.setDisplayShowHomeEnabled(false);
		mAction.setDisplayShowTitleEnabled(false);
		
		/**
		 * Adds the show list button on the top left of actionbar
		 */
		home = new ImageButton(this);
		home.setId(1);
		home.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		home.setImageDrawable(this.getResources().getDrawable(R.drawable.list));
		mAction.setCustomView(home);
		mAction.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		home.setOnClickListener(this);
		
		contentFrame = (FrameLayout) this.findViewById(R.id.mapframe);
		exhibit = new ExhibitFragment();
		
		restroom = new RestroomsFragment();

		//load listfragment into memory
		list = (ArrayListFragment) this.getSupportFragmentManager().findFragmentById(R.id.listfragment);
		list.getListView().setOnItemClickListener(this);
		
        //init map objects
    	map = (MapView) findViewById(R.id.Map);
    	map.setBuiltInZoomControls(false);
		map.setSatellite(true);
		 
		mapControl = map.getController();
		mapControl.setZoom(17);
		
		me = new MyLocOverlay(this,this, map);
		me.disableMarker();
		startLocationUpdates();
		
		//get screen width to optimize layout
        DisplayMetrics display = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(display);
        int width = display.widthPixels;
        
        if(width>=1000){
        	//scale the width of the sidebar to specified size
        	LayoutParams lp = new LayoutParams((width/5), LayoutParams.FILL_PARENT);
        	list.getView().setLayoutParams(lp);
        	isLargeScreen = true;
        	//mTransaction.add(android.R.id.content, restroom);
        } else{//small screen we want to hide it
        	Operations.removeView(map);
        }
        
        
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		stopLocationUpdates();
	}
	
	private void startLocationUpdates(){
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         
		
		 me.enableMyLocation();
		 map.getOverlays().add(me);
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
			
		 me.runOnFirstFix(new Runnable(){

			@Override
			public void run() {
				mapControl.animateTo(me.getMyLocation());
				me.addMarker();
			}
			 
		 });
			
		 
	 }

	
	 private void stopLocationUpdates(){
		 me.disableMyLocation();
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
		
		//adds the searchbar to the actionbar
		MenuItemSearchAction menuItem = new MenuItemSearchAction(this, menu, this, getResources().getDrawable(R.drawable.ic_action_search));
		menuItem.setTextColor(getResources().getColor(R.color.forestgreen));
		
		//add action items
		menu.add("About").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add("Settings").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if(item.getTitle().equals("Settings")){
			
		} else if(item.getTitle().equals("News")){
			
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == 1){
			showList();
			if(map.isShown() && !isLargeScreen){
				Operations.removeView(map);
			}
		}
		
	}
	
	/**
	 * Handles the list widget
	 */
	private void showList(){
		if(!list.isVisible()){
			Operations.addView(list.getView());
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
		switch(position){
		case ITEM_CASE_MAP:
			//replace mapview with the list
			if(!isLargeScreen && !map.isShown() && list.isAdded()){
				Operations.swapViews(list.getView(), map);
			} else if(!map.isShown()){
				Operations.addView(map);
			}
			break;
		case ITEM_CASE_NEWS:
			
			break;
		case ITEM_CASE_STORE:
			
			break;
			
		case ITEM_CASE_FOOD:
			
			break;
		case ITEM_CASE_EXHIBITS:
			showFragment(exhibit);
			break;
		case ITEM_CASE_RESTROOMS:
			showFragment(restroom);
			break;
		}
	}
	
	/**
	 * Handles showing fragments in the activity
	 * @param fragment
	 */
	private void showFragment(Fragment f){
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
				mTransaction.replace(R.id.mapframe, f).commit();
			}
		} 
	}
	
	@Override
	public void performSearch(String query) {}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		return false;
	}




	
}
