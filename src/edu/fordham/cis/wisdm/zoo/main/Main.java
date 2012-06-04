package edu.fordham.cis.wisdm.zoo.main;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import edu.fordham.cis.wisdm.utils.view.Operations;
import edu.fordham.cis.wisdm.zoo.map.MyLocOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * The main dashboard screen the user first sees when logging in
 * @author Andrew Grosner
 * @version 1.0
 */
public class Main extends MapActivity implements OnClickListener{

	//manages location changes
	private LocationManager lManager = null;
	
	//the map widget
	private MapView map = null;
	
	//controls the map
	private MapController mapControl = null;
	
	//shows current location
	private MyLocOverlay me = null;
	
	private long GPSUpdate = 20000;
	
	//the action bar buttons
	private ImageButton[] actions = new ImageButton[4];
	
	private String email = null;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.dashboard);
	        
	     email = getIntent().getStringExtra("email");
	     
	     setUpView();
	     start();
	 }
	 
	 /**
	  * Adds the menu to screen when option button is pressed
	  */
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu){
		 MenuInflater inflater = getMenuInflater();
		 inflater.inflate(R.layout.menu, menu);
		 return true;
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
		 map.setBuiltInZoomControls(true);
		 
		 mapControl = map.getController();
		 mapControl.setZoom(17);
		 
		 me = new MyLocOverlay(this,this, map);
		 startLocationUpdates();
			
		 int id[] = {R.id.Logout, R.id.Search, R.id.Photos, R.id.Settings};
		 Operations.findImageButtonViewsByIds(this, actions, id);
		 Operations.setOnClickListeners(this, actions);
		 
		 
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
			case R.id.Logout:
				finish();
				break;
			case R.id.Settings:

				break;
		}
	}
	
}
