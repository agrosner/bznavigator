package com.grosner.zoo.utils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import android.support.v4.app.FragmentActivity;

import com.grosner.zoo.activities.ZooActivity;
import com.grosner.zoo.fragments.MapViewFragment;
import com.grosner.zoo.R;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.markers.PlaceMarker;
import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import com.grosner.zoo.activities.InfoDisplayActivity;
import com.grosner.zoo.PlaceController;

/**
 * This class is useful for performing certain map operations such as conversion, simplification, and generation
 * @author Andrew Grosner
 *
 */
public class MapUtils {

	/**
	 * Adds a list to the map and moves map relative to location
	 * @param map
	 * @param places
	 */
	public static void addToMap(GoogleMap map, LinkedList<PlaceMarker> places){
		for(PlaceMarker place: places){
			place.addMarker(map);
		}
	}
	
	/**
	 * Generates a LatLngBounds.Builder() object that the passed map moves to and includes current location
	 * @param map
	 * @param places
	 */
	public static void moveToBounds(Location myLocation, GoogleMap map, List<PlaceMarker> places){
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for(PlaceMarker place: places){
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
	public static void removeList(LinkedList<PlaceMarker> places){
		for(PlaceMarker place: places){
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
	 * Displays information about the place clicked on
	 * @param manager
	 * @param act
	 * @param marker
	 */
	public static void startInfoActivity(ZooActivity act, final Marker marker){
		Intent i = new Intent(act, InfoDisplayActivity.class);
		i.putExtra("Title", marker.getTitle());
		i.putExtra("LatLng", marker.getPosition());
		i.putExtra("Snippet", marker.getSnippet());
		//i.putExtra("act", act);
		i.putExtra("Position", marker.getPosition());
		act.startActivity(i);
	}
	
	/**
	 * Shows exhibit information in the form of a custom dialog. This dialog should
	 * poll the server for exhibit information in the near future
    	 * @param marker
	 */
	public static void showExhibitInfoDialog(final FragmentActivity act, final Marker marker){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		View v = LayoutInflater.from(act).inflate(R.layout.dialog_exhibit, null);
		
		Operations.setViewText(v, marker.getTitle(), R.id.title);
		Operations.setViewText(v, 
				PlaceController.calculateDistanceString(CurrentLocationManager.getSharedManager().getLastKnownLocation(),
						latLngToLocation(marker.getPosition())), 
				R.id.distance);
		if(StringUtils.stringNotNullOrEmpty(marker.getSnippet())){
			v.findViewById(R.id.info).setVisibility(View.GONE);
			try {
				new HTMLScraper().getInfoContent((LinearLayout) v.findViewById(R.id.info_page), marker.getSnippet());
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		builder.setView(v);	
		final AlertDialog dialog = builder.create(); 
		
		OnClickListener onclick = new OnClickListener(){

			@Override
			public void onClick(View v) {
				int id = v.getId();
				if(id == R.id.exit){
					dialog.cancel();
				} else if(id == R.id.navigate){
					CurrentLocationManager.getSharedManager().navigate(latLngToLocation(marker.getPosition()));
					MapViewFragment map = (MapViewFragment) act.getSupportFragmentManager().findFragmentByTag("MapViewFragment");
						//map.enableNavigation(((SlidingScreenActivity) act).getFollowItem(), latLngToLocation(marker.getPosition()));
					dialog.dismiss();
					Toast.makeText(act, "Now navigating to " + marker.getTitle(), Toast.LENGTH_SHORT).show();
				}
			}
			
		};
		Operations.setOnClickListeners(v, onclick, R.id.exit, R.id.navigate);
		
		dialog.show();
		
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
