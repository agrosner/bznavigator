package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.zip.Inflater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;
import edu.fordham.cis.wisdm.zoo.utils.Polygon;

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
	public static void moveToBounds(GoogleMap map, LinkedList<PlaceItem> places){
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for(PlaceItem place: places){
			builder.include(place.getPoint());
		}
		if(SplashScreenActivity.myLocation!=null)
			builder.include(locationToLatLng(SplashScreenActivity.myLocation));
		try{
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
		} catch(IllegalStateException i){
			
		}
	}
	
	public static void removeList(LinkedList<PlaceItem> places){
		for(PlaceItem place: places){
			place.remove();
		}
	}
	
	public static void animateTo(GoogleMap map, Location loc){
		map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
	}
	
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
	
	
	public static void moveRelativeToCurrentLocation(LatLng point, GoogleMap map){
		map.animateCamera(CameraUpdateFactory.newLatLngBounds(getBounds(point, locationToLatLng(SplashScreenActivity.myLocation)), 30));
	}
	
	
	public static LatLngBounds getBounds(LatLng...latLngs){
		LatLngBounds.Builder builder=  new LatLngBounds.Builder();
		for(LatLng l: latLngs){
			builder.include(l);
		}
		return builder.build();
	}
	
	public static void showExhibitInfoDialog(LayoutInflater inflater, Context con, Marker marker){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(con);
		View v = inflater.inflate(R.layout.dialog_exhibit, null);
		
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setText(marker.getTitle());
		
		TextView info = (TextView) v.findViewById(R.id.distance);
		info.setText(PlaceController.calculateDistanceString(SplashScreenActivity.myLocation, latLngToLocation(marker.getPosition())) + " ft");
	
		builder.setView(v);
		final AlertDialog dialog = builder.create();
		
		OnClickListener onclick = new OnClickListener(){

			@Override
			public void onClick(View v) {
				int id = v.getId();
				if(id == R.id.exit){
					dialog.cancel();
				} else if(id == R.id.navigate){
					
				}
			}
			
		};
		ImageButton cancel = (ImageButton) v.findViewById(R.id.exit);
		cancel.setOnClickListener(onclick);
		
		ImageButton navigate = (ImageButton) v.findViewById(R.id.navigate);
		navigate.setOnClickListener(onclick);
		
		dialog.show();
		
	}
	
	public static float getDip(Context con, float size){
		return con.getResources().getDisplayMetrics().density*size;
	}
	
	public static boolean locationsEqual(Location loc1, Location loc2){
		return (loc1.getLatitude()==loc2.getLatitude()) && (loc1.getLongitude()==loc2.getLongitude());
	}
	
}
