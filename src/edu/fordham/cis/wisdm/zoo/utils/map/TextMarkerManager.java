package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;

public class TextMarkerManager {

	private ArrayList<TextMarker> mMarkers = new ArrayList<TextMarker>();
	
	private Context mContext;
	
	private GoogleMap mGoogleMap;
	
	private int mTextColor = Color.BLACK;
	
	public TextMarkerManager(Context con, GoogleMap map){
		mContext = con;
		mGoogleMap = map;
		TextMarker.DENSITY_SCALE = con.getResources().getDisplayMetrics().density;
	}
	
	public void readInData(String...fNames) throws IOException{
		for(String file: fNames){
			readInData(file);
		}
	}
	
	private void readInData(String fName) throws IOException{
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
					
					mMarkers.add((TextMarker) 
							new TextMarker()
							.point(new LatLng(lat, lon))
							.name(lineArray[0]));
				} 
			}
		}
		mScanner.close();
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
