package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;

/**
 * This class manages and provides functionality to display text labels
 * that are clickable and changeable on the new GoogleMaps Api V2. 
 * @author Andrew Grosner
 *
 */
public class TextMarkerManager {

	/**
	 * Holds the textmarker data
	 */
	private ArrayList<TextMarker> mMarkers = new ArrayList<TextMarker>();
	
	/**
	 * Application context object
	 */
	private Context mContext;
	
	/**
	 * The googlemap
	 */
	private GoogleMap mGoogleMap;
	
	/**
	 * The text color of the items labels
	 */
	private int mTextColor = Color.BLACK;
	
	/**
	 * Whether any icons are focused on the map
	 */
	private boolean hasFocus = false;
	
	/**
	 * Constructs a new instance
	 * @param con
	 * @param map
	 */
	public TextMarkerManager(Context con, GoogleMap map){
		reset(con, map);
	}
	
	/**
	 * Changes the context and map (in case of new map created)
	 * @param con
	 * @param map
	 */
	public void reset(Context con, GoogleMap map){
		mContext = con;
		mGoogleMap = map;
		for(TextMarker m: mMarkers){
			m.remove();
		}
		if(!mMarkers.isEmpty())
			mMarkers.clear();
	}
	
	/**
	 * Reads in multiple files into the textmarkermanager
	 * @param manager
	 * @param act
	 * @param fNames
	 * @throws IOException
	 */
	public void readInData(Activity act, String...fNames) throws IOException{
		for(String file: fNames){
			readInData(act, file);
		}
	}
	
	/**
	 * Reads in data into A textmarkermanager to display text labels on the map
	 * @param manager
	 * @param act
	 * @param fName
	 * @throws IOException
	 */
	public void readInData(Activity act, String fName) throws IOException{
		Scanner mScanner = new Scanner(mContext.getAssets().open(fName));
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

					mMarkers.add(((TextMarker) 
							new TextMarker()
							.point(new LatLng(lat, lon))
							.name(lineArray[0])).setImage(act.getResources(), drawableId));
				} 
			}
		}
		mScanner.close();
	}
	
	/**
	 * add specific location to have focus (icon appears next to text and text is displayed at all zoom levels)
	 * @param place
	 */
	public boolean addFocus(PlaceItem place){
		if(!contains(place))	return false;
		for(TextMarker text: mMarkers){
			if(text.equals(place)){
				text.useImage(true);
				text.setFocus(true);
				text.refresh(mGoogleMap);
				place.mMarker = text.mMarker;
			}
		}
		hasFocus = true;
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
			if(text.mMarker!=null) text.mMarker.hideInfoWindow();
		}
		hasFocus = false;
	}
	
	/**
	 * Whether there are markers with focus or not
	 * @return
	 */
	public boolean hasFocus(){
		return hasFocus;
	}
	
	/**
	 * Whether the specified place is within the text markers
	 * @param place
	 * @return
	 */
	public boolean contains(PlaceItem place){
		for(TextMarker text: mMarkers){
			if(text.equals(place))
				return true;
		}
		return false;
	}
	
	/**
	 * Adds all text markers to the map
	 */
	public void addToMap(){
		for(TextMarker text: mMarkers){
			text.addMarker(mGoogleMap);
		}
	}
	
	/**
	 * Refreshes all of the markers based on the zoom, 
	 * if the specified zoom is below the zoom min, 
	 * then the markers will disappear
	 * @param zoom
	 */
	public void refreshData(float zoom){
		for(TextMarker text: mMarkers){
			text.refreshWithZoom(mGoogleMap, zoom);
		}
	}
	
	/**
	 * Changes the color of the labels
	 * @param color
	 * @param zoom
	 */
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
