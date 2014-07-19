package com.grosner.zoo.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.graphics.Color;
import android.location.Location;
import com.grosner.zoo.activities.ZooActivity;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.database.PlaceManager;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.fragments.MapViewFragment;
import com.grosner.zoo.fragments.PlaceFragment;
import com.grosner.zoo.markers.PlaceMarker;
import com.grosner.zoo.markers.TextMarker;
import com.grosner.zoo.singletons.ExhibitManager;

/**
 * This class manages and provides functionality to display text labels
 * that are clickable and changeable on the new GoogleMaps Api V2. 
 * @author Andrew Grosner
 *
 */
public class TextMarkerManager implements OnMarkerClickListener{

    private Context mContext;
    /**
	 * Holds the textmarker data
	 */
	private final ArrayList<TextMarker> mMarkers = new ArrayList<TextMarker>();
	
	/**
	 * The parent fragment associated with this manager
	 */
	private GoogleMap mMap;

	/**
	 * The text color of the items labels
	 */
	private int mTextColor = Color.BLACK;
	
	/**
	 * Whether any icons are focused on the map
	 */
	private boolean hasAnyFocus = false;
	
	/**
	 * Constructs a new instance
	 * @param map
	 */
	public TextMarkerManager(GoogleMap map, Context context){
		reset(map, context);
	}
	
	/**
	 * Changes the context and map (in case of new map created)
	 * @param map
	 */
	public void reset(GoogleMap map, Context context){
		mMap = map;
        mContext = context;
        synchronized (mMarkers) {
            for (TextMarker m : mMarkers) {
                m.remove();
            }
            if (!mMarkers.isEmpty())
                mMarkers.clear();
        }
	}

    public void loadMarkers(){
        List<PlaceObject> exhibits = PlaceManager.getManager().getList(PlaceFragment.PlaceType.EXHIBITS.name());
        for(PlaceObject placeObject: exhibits){
            mMarkers.add(new TextMarker().place(placeObject));
        }

        List<PlaceObject> special = PlaceManager.getManager().getList(PlaceFragment.PlaceType.SPECIAL.name());
        for(PlaceObject placeObject: special){
            mMarkers.add(new TextMarker().place(placeObject));
        }

    }
	
	/**
	 * Reads in multiple files into the textmarkermanager
	 * @param fNames
	 * @throws IOException
	 */
	public void readInData(String...fNames) throws IOException{
		if(!mMarkers.isEmpty()) mMarkers.clear();

		for(String file: fNames){
			readInData(file, false);
		}
	}

	/**
	 * Reads in data into A textmarkermanager to display text labels on the map
	 * @param fName
	 * @throws IOException
	 */
	public void readInData(String fName, boolean dummy) throws IOException{
		Scanner mScanner = new Scanner(ZooApplication.getContext().getAssets().open(fName));
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
					int drawableId = ZooApplication.getContext().getResources().getIdentifier(lineArray[1], "drawable", ZooApplication.getContext().getPackageName());

					TextMarker marker = ((TextMarker)
							new TextMarker()
							.point(new LatLng(lat, lon))
							.name(lineArray[0])).setImage(drawableId);

					//optional link included
					if(lineArray.length>=5){
						marker.link(lineArray[4]);
					}

					mMarkers.add(marker);
				}
			}
		}
		mScanner.close();
	}
	
	/**
	 * add specific location to have focus (icon appears next to text and text is displayed at all zoom levels)
	 * @param place
	 */
	public boolean addFocus(PlaceMarker place){
		if(!contains(place))	return false;
		for(TextMarker text: mMarkers){
			if(text.equals(place)){
				text.useImage(true);
				text.setFocus(true);
				text.refresh(mMap);
				place.mMarker = text.mMarker;
			}
		}
		hasAnyFocus = true;
		return true;
	}
	
	/**
	 * finds the textmarker (icon appears next to text and text is displayed at all zoom levels)
	 * @param text
	 * @return
	 */
	public boolean addFocus(TextMarker text){
		if(!mMarkers.contains(text)) return false;
		text.useImage(true);
		text.setFocus(true);
		text.refresh(mMap);
		hasAnyFocus = true;
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
			text.refreshWithZoom(mMap, zoom);
			if(text.mMarker!=null) text.mMarker.hideInfoWindow();
		}
		hasAnyFocus = false;
	}
	
	/**
	 * Whether there are markers with focus or not
	 * @return
	 */
	public boolean hasAnyFocus(){
		return hasAnyFocus;
	}
	
	/**
	 * Whether the specified place is within the text markers
	 * @param place
	 * @return
	 */
	public boolean contains(PlaceMarker place){
		for(TextMarker text: mMarkers){
			if(text.equals(place))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns whether the marker is in the list
	 * @param marker
	 * @return
	 */
	public boolean contains(Marker marker){
		for(TextMarker text: mMarkers){
			if(text.equals(marker))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns an index of a text marker that shares same location as marker specified
	 * @param marker
	 * @return
	 */
	public int indexOf(Marker marker){
		for(int i =0; i < mMarkers.size(); i++){
			if(mMarkers.get(i).equals(marker))
				return i;
		}
		return -1;
	}
	
	/**
	 * Adds all text markers to the map
	 */
	public void addToMap(){
		for(TextMarker text: mMarkers){
			text.addMarker(mMap);
		}
	}
	
	/**
	 * Refreshes all of the markers based on the zoom, 
	 * if the specified zoom is below the zoom min, 
	 * then the markers will disappear
	 * @param zoom
	 */
	public void refreshData(float zoom){
        synchronized (mMarkers) {
            for (TextMarker text : mMarkers) {
                text.refreshWithZoom(mMap, zoom);
            }
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
            synchronized (mMarkers) {
                for (TextMarker text : mMarkers) {
                    text.color(color);
                    text.refreshWithZoom(mMap, zoom);
                }
            }
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if(contains(marker))	{
            MapViewFragment fragment = (MapViewFragment) ((ZooActivity)mContext).getSupportFragmentManager().findFragmentByTag("MapViewFragment");
            if(fragment!=null)
			    fragment.addPlaceMove(mMarkers.get(indexOf(marker)));
			return true;
		}
		return false;
	}
	
}
