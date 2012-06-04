package edu.fordham.cis.wisdm.zoo.map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.View;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import edu.fordham.cis.wisdm.zoo.main.R;

/**
 * This class enhances mylocationoverlay, giving it more functionality
 * @author Andrew Grosner
 * @version 1.0
 */
public class MyLocOverlay extends MyLocationOverlay{

	//map widget linked to this overlay
	private MapView map;
	
	//activity associated with overlay
	private Activity act;
	
	//the view widget associated with "You are here"
	private View here;
	
	private boolean showMarker = true;
	
	//adds view to screen 
	private Runnable run = new Runnable(){

		@Override
		public void run() {
			MapView.LayoutParams mv = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, 
					MapView.LayoutParams.WRAP_CONTENT, getMyLocation(), MapView.LayoutParams.BOTTOM_CENTER);
			try{
				map.addView(here, mv);
			} catch (IllegalStateException e){
				e.printStackTrace();
			}

		}
		 
	 };
	
	 /**
	  * Constructor initializing the overlay
	  * @param act
	  * @param context
	  * @param mapView
	  */
	public MyLocOverlay(Activity act, Context context, MapView mapView) {
		super(context, mapView);
		map = mapView;
		this.act = act;
		here = act.getLayoutInflater().inflate(R.layout.basicoverlay, null);
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onLocationChanged(Location l){
		super.onLocationChanged(l);
		
		hideMarker();
		addMarker();
	}
	
	/**
	 * Adds to screen only if toggle enabled
	 */
	public void addMarker(){
		if(showMarker)
			act.runOnUiThread(run);
	}
	
	/**
	 * Hides marker from the map
	 */
	public void hideMarker(){
		map.removeView(here);
	}
	
	/**
	 * Disables all future displaying of marker on screen
	 */
	public void disableMarker(){
		hideMarker();
		showMarker = false;
	}
	
	/**
	 * Enables all future displaying of the marker
	 */
	public void enableMarker(){
		showMarker = true;
		addMarker();
	}
	
	public boolean getMarkerToggleState(){
		return showMarker;
	}
	
}
