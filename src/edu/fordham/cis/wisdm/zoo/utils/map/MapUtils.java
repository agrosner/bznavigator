package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SlidingScreenActivity;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;
import edu.fordham.cis.wisdm.zoo.utils.Operations;

public class MapUtils {

	/**
	 * Adds a list to the map and moves map relative to location
	 * @param map
	 * @param places
	 */
	public static void addToMap(GoogleMap map, LinkedList<PlaceItem> places){
		for(PlaceItem place: places){
			place.addMarker(map);
		}
	}
	
	/**
	 * Generates a LatLngBounds.Builder() object that the passed map moves to and includes current location
	 * @param map
	 * @param places
	 */
	public static void moveToBounds(Location myLocation, GoogleMap map, LinkedList<PlaceItem> places){
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for(PlaceItem place: places){
			builder.include(place.getPoint());
		}
		if(myLocation!=null)
			builder.include(locationToLatLng(myLocation));
		try{
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
		} catch(IllegalStateException i){
			
		}
	}
	
	/**
	 * Removes a list of placeitems from the map
	 * @param places
	 */
	public static void removeList(LinkedList<PlaceItem> places){
		for(PlaceItem place: places){
			place.remove();
		}
	}
	
	public static void animateTo(GoogleMap map, Location loc){
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
	}
	
	/**
	 * Reads in polygon information from mapraw and 
	 * puts it within the builder for when we want to display overview of the map
	 * @param con
	 * @param builder
	 * @throws IOException
	 */
	public static void generatePolygon(Context con, LatLngBounds.Builder builder) throws IOException{
		Scanner file = new Scanner(con.getResources().getAssets().open("mapraw.txt"));
		while(file.hasNext()){
			String line = file.nextLine();
			String[] lValues = line.split(",");
			double latitude = Double.valueOf(lValues[1]);
			double longitude = Double.valueOf(lValues[0]);
			builder.include(new LatLng(latitude, longitude));
		}
		
		file.close();
		
	}
	
	public static LatLng locationToLatLng(Location loc){
		return new LatLng(loc.getLatitude(), loc.getLongitude());
	}
	
	public static Location latLngToLocation(LatLng lat){
		Location loc = new Location("");
		loc.setLatitude(lat.latitude);
		loc.setLongitude(lat.longitude);
		return loc;
	}
	
	
	public static void moveRelativeToCurrentLocation(Location myLocation, LatLng point, GoogleMap map){
		if(myLocation==null){
			map.animateCamera(CameraUpdateFactory.newLatLng(point));
			return;
		}
		map.animateCamera(CameraUpdateFactory.newLatLngBounds(getBounds(point, locationToLatLng(myLocation)), 20));
	}
	
	
	public static LatLngBounds getBounds(LatLng...latLngs){
		LatLngBounds.Builder builder=  new LatLngBounds.Builder();
		for(LatLng l: latLngs){
			builder.include(l);
		}
		return builder.build();
	}
	
	/**
	 * Shows exhibit information in the form of a custom dialog. This dialog should
	 * poll the server for exhibit information in the near future
	 * @param myLocation
	 * @param inflater
	 * @param con
	 * @param marker
	 */
	public static void showExhibitInfoDialog(final CurrentLocationManager manager, LayoutInflater inflater, final Activity act, final Marker marker){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		View v = inflater.inflate(R.layout.dialog_exhibit, null);
		
		Operations.setViewText(v, marker.getTitle(), R.id.title);
		Operations.setViewText(v, 
				PlaceController.calculateDistanceString(manager.getLastKnownLocation(), 
						latLngToLocation(marker.getPosition())), 
				R.id.distance);

		builder.setView(v);
		final AlertDialog dialog = builder.create();
		
		OnClickListener onclick = new OnClickListener(){

			@Override
			public void onClick(View v) {
				int id = v.getId();
				if(id == R.id.exit){
					dialog.cancel();
				} else if(id == R.id.navigate){
					manager.navigate(latLngToLocation(marker.getPosition()));
					
					if(act instanceof SlidingScreenActivity){
						SlidingScreenActivity sa = (SlidingScreenActivity) act;
						sa.mList.getMapFragment().enableNavigation(sa.getFollowItem(), latLngToLocation(marker.getPosition()));
					}
					
					dialog.dismiss();
					Toast.makeText(act, "Now navigating to " + marker.getTitle(), Toast.LENGTH_SHORT).show();
				}
			}
			
		};
		Operations.setOnClickListeners(v, onclick, R.id.exit, R.id.navigate);
		
		dialog.show();
		
	}
	
	/**
	 * Convert pixel size of text into density-independent pixels
	 * @param con
	 * @param size
	 * @return
	 */
	public static float getDip(Context con, float size){
		return con.getResources().getDisplayMetrics().density*size;
	}
	
	/**
	 * Determines whether two locations are exactly the same (such that the latitude and longitude are exact matches)
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public static boolean locationsEqual(Location loc1, Location loc2){
		return (loc1.getLatitude()==loc2.getLatitude()) && (loc1.getLongitude()==loc2.getLongitude());
	}
	
}
