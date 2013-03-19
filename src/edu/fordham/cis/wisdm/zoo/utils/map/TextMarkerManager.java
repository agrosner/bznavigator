package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;

public class TextMarkerManager {

	private ArrayList<TextMarker> mMarkers = new ArrayList<TextMarker>();
	
	private Context mContext;
	
	private GoogleMap mGoogleMap;
	
	private int mTextColor = Color.BLACK;
	
	public TextMarkerManager(Context con, GoogleMap map){
		reset(con, map);
	}
	
	public void reset(Context con, GoogleMap map){
		mContext = con;
		mGoogleMap = map;
	}
	
	public static void readInData(TextMarkerManager manager, Activity act, String...fNames) throws IOException{
		for(String file: fNames){
			readInData(manager, act, file);
		}
	}
	
	public static void readInData(TextMarkerManager manager, Activity act, String fName) throws IOException{
		Scanner mScanner = new Scanner(manager.mContext.getAssets().open(fName));
		int idIndex = -1;
		while(mScanner.hasNextLine()){
			String line = mScanner.nextLine();
			idIndex++;
			if(idIndex!=0){
				String[] lineArray = line.split(",");
				if(lineArray.length>=4){
					double lat = Double.valueOf(lineArray[2]);
					double lon = Double.valueOf(lineArray[3]);
					Location loc = new Location("");
					loc.setLatitude(lat);
					loc.setLongitude(lon);
					if(lineArray[0].toLowerCase().contains("restroom")){
						lineArray[0] = "Restroom";
					}
					
					if(fName.equals("admin.txt")){
						lineArray[0]+="(Staff Only)";
					}
					int drawableId = act.getResources().getIdentifier(lineArray[1], "drawable", act.getPackageName());

					manager.mMarkers.add(((TextMarker) 
							new TextMarker()
							.point(new LatLng(lat, lon))
							.name(lineArray[0])).setImage(act.getResources(), drawableId));
				} 
			}
		}
		mScanner.close();
	}
	
	/**
	 * add specific location to have focus
	 * @param place
	 */
	public boolean addFocus(PlaceItem place){
		if(!contains(place))	return false;
		for(TextMarker text: mMarkers){
			if(text.equals(place)){
				text.useImage(true);
				text.setFocus(true);
				text.refresh(mGoogleMap);
			}
		}
		return true;
	}
	
	/**
	 * Clears all places from focus
	 * @param zoom - current zoom level
	 */
	public void clearFocus(float zoom){
		for(TextMarker text: mMarkers){
			text.useImage(false);
			text.setFocus(false);
			text.refreshWithZoom(mGoogleMap, zoom);
		}
	}
	
	public boolean contains(PlaceItem place){
		for(TextMarker text: mMarkers){
			if(text.equals(place))
				return true;
		}
		return false;
	}
	
	public void addToMap(){
		for(TextMarker text: mMarkers){
			text.addMarker(mGoogleMap);
		}
	}
	
	
	public void refreshData(float zoom){
		for(TextMarker text: mMarkers){
			text.refreshWithZoom(mGoogleMap, zoom);
		}
	}
	
	public void changeTextLabelColor(int color, float zoom){
		if(color!=mTextColor){
			mTextColor = color;
			for(TextMarker text: mMarkers){
				text.color(color);
				text.refreshWithZoom(mGoogleMap, zoom);
			}
		}
	}
	
	
}
