package edu.fordham.cis.wisdm.zoo.main;

import java.util.List;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;

import edu.fordham.cis.wisdm.zoo.map.MyLocOverlay;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView;
import android.widget.Toast;

/**
 * The main dashboard screen the user first sees when logging in
 * @author Andrew Grosner
 * @version 1.0
 */
public class Map extends SherlockFragmentActivity implements OnClickListener, OnNavigationListener, OnMenuItemClickListener, SearchPerformListener{

	//manages location changes
	private LocationManager lManager = null;
	
	//the map widget
	private MapView map = null;
	
	//controls the map
	private MapController mapControl = null;
	
	//shows current location
	private MyLocOverlay me = null;
	
	private long GPSUpdate = 20000;
	
	//the email of the user logged in
	private String email = null;

	//the search bar widget
	private SearchView mSearchView;

	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.map);
	     
	     this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	     this.getSupportActionBar().setTitle("Survey");
	     
	     email = getIntent().getStringExtra("email");
	     
	     setUpView();
	     start();
	     
	     
	 }
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuItemSearchAction menuItem = new MenuItemSearchAction(this.getApplication(), menu, this, getResources().getDrawable(R.drawable.ic_action_search));
		menuItem.setTextColor(getResources().getColor(R.color.forestgreen));
		
		menu.add("News").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add("About").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add("Settings").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item){
		 switch(item.getItemId()){
		 case R.id.Street:
			 map.setSatellite(false);
			 map.setStreetView(true);
			 break;
		 case R.id.Satellite:
			 map.setSatellite(true);
			 map.setStreetView(false);
			 break;
		 case R.id.MarkerToggle:
			 if(me.getMarkerToggleState()){
				 me.disableMarker();
			 } else{
				 me.enableMarker();
			 }
			 break;
		default:
			return super.onOptionsItemSelected(item);
			
				 
		 }
		 return true;
		 
		 
	 }
	 
	 
	 
	 @Override
	 public void onDestroy(){
		 super.onDestroy();
		 
		 stopLocationUpdates();
		 stopService(new Intent(this, LocationUpdateService.class));
	 }
	 
	 private void setUpView(){

		 lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		  
		 map = (MapView) findViewById(R.id.Map);
		 map.setBuiltInZoomControls(false);
		 map.setSatellite(true);
		 
		 mapControl = map.getController();
		 mapControl.setZoom(17);
		 
		 me = new MyLocOverlay(this,this, map);
		 me.disableMarker();
		 startLocationUpdates();
			
	 }
	 
	 
	 private void startLocationUpdates(){
		 
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
	 
	 private void start(){
		 Intent location = new Intent(this, LocationUpdateService.class);
		 location.putExtra("email", email);
		 startService(location);
		 
		 //TODO: add nearby overlay of zoo to map
		 
	 }
	 
	 /**
	  * Displays message in activity
	  * @param message
	  */
	 public static void displayMessage(Context con, String message){
		 Toast.makeText(con, message, Toast.LENGTH_SHORT).show();
	 }
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.Settings:
				break;
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if(item.getTitle().equals("Settings")){
			
		} else if(item.getTitle().equals("News")){
			
		}
		return false;
	}

	@Override
	public void performSearch(String query) {
		
	}

}
