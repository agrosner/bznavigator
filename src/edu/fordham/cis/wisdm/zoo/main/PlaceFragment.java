package edu.fordham.cis.wisdm.zoo.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.maps.GeoPoint;

import edu.fordham.cis.wisdm.zoo.map.PlaceItem;

public class PlaceFragment extends SherlockFragment implements OnClickListener{

	public static int TYPE_EXHIBITS = 0;
	
	public static int TYPE_FOOD = 1;
	
	public static int TYPE_SPECIAL = 2;
	

	public static double METERS_TO_FT = 3.28084;
	
	private int type = 0;
	
	private RelativeLayout exhibit = null;
	
	private LinearLayout exhibitList = null;
	
	private LayoutInflater inflater = null;
	
	private ViewGroup container = null;
	
	private LinkedList<PlaceItem> points = new LinkedList<PlaceItem>();
	
	/**
	 * Constructor specifying type
	 * @param type
	 */
	public PlaceFragment(int type){
		super();
		this.type = type;
	}
	
	/**
	 * Called only when fragment is instantiated
	 */
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		//TODO: check for any updates to server file when in "online mode"
	}
	
	/**
	 * Called when fragment's view is created
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		
		//parent layout
		this.inflater = inflater;
		this.container = container;
		refresh();
		
		return exhibit;
		
	}
	
	public void refresh(){
		//parent layout
		exhibit = (RelativeLayout) inflater.inflate(R.layout.placefragment, container, false);
		exhibitList = (LinearLayout) exhibit.findViewById(R.id.exhibitList);
		exhibitList.addView(createExhibitItem(inflater, container, 0, "ic_action_location", null, ""));
		
		String fName = "";
		
		if(type == TYPE_EXHIBITS){
			fName = "exhibits";
		} else if(type == TYPE_FOOD){
			fName = "food";
		} else if(type == TYPE_SPECIAL){
			fName = "special-exhibits";
		}
		TextView title = (TextView) exhibit.findViewById(R.id.title);
		title.setText(fName.replace("-", " "));
		
		fName+=".txt";
		readInFile(inflater, container, fName, exhibitList);	
	}

	@Override
	public void onClick(View v) {
		
		SplashScreenActivity act = (SplashScreenActivity) this.getActivity();
		FragmentTransaction mTransaction = this.getFragmentManager().beginTransaction();
		int id = v.getId();
		
		if(id != 0){
			PlaceItem place = points.get(id-1);
			act.showMap(mTransaction, this.getView(), place);
		} else{
			act.showMap(mTransaction, this.getView(), points);
		}
			
		
	}
	
	/**
	 * Builds the layout for listing nearby exhibits with title, distance, and custom image
	 * @param inflater
	 * @param container
	 * @param id
	 * @param drawableId
	 * @param title
	 * @param distance
	 * @return
	 */
	private RelativeLayout createExhibitItem(LayoutInflater inflater, ViewGroup container, int id, String drawablePath, String title, String distance){
		RelativeLayout exhibitItem = (RelativeLayout) inflater.inflate(R.layout.exhibititem, container, false);
		exhibitItem.setId(id);
		exhibitItem.setOnClickListener(this);
		
		if(!drawablePath.equals("0")){
			ImageView image = (ImageView) exhibitItem.findViewById(R.id.image);
			int drawableId = getActivity().getResources().getIdentifier(drawablePath, "drawable", this.getActivity().getPackageName());
			image.setBackgroundDrawable(getResources().getDrawable(drawableId));
		}
		
		if(title!=null){
			TextView titleText = (TextView) exhibitItem.findViewById(R.id.title);
			titleText.setText(title);
		}
		
		if(!distance.equals("")){
			TextView distText = (TextView) exhibitItem.findViewById(R.id.distancetext);
			distText.setText(distance);
		}
		
		return exhibitItem;
	}
	
	/**
	 * Reads in the place file into memory
	 * General format explained within the place file
	 * @param fName
	 */
	private void readInFile(LayoutInflater inflater, ViewGroup container, String fName, LinearLayout exhibitList){
		
		try {
			Scanner read = new Scanner(this.getActivity().getAssets().open(fName));
			int idIndex = -1;
			while(read.hasNextLine()){
				String line = read.nextLine();
				idIndex++;
				if(idIndex!=0){
					String[] lineArray = line.split(",");
					//	TODO: add latitude, longitude coordinates
					String distance = "0";
					if(lineArray.length>=4){
						int lat = (int) (Double.valueOf(lineArray[2])*1E6);
						int lon = (int) (Double.valueOf(lineArray[3])*1E6);
						distance = calculateDistance(SplashScreenActivity.myLocation, lineArray[2], lineArray[3]);
						points.add(new PlaceItem(new GeoPoint(lat, lon), lineArray[0], String.valueOf(distance)));
						int index = findIndex(exhibitList, Integer.valueOf(distance));
						exhibitList.addView(createExhibitItem(inflater, container, idIndex, lineArray[1], lineArray[0], distance+"ft"), index);
					} else{
						exhibitList.addView(createExhibitItem(inflater, container, idIndex, lineArray[1], lineArray[0], distance+"ft"));
					}
				}
			}
			read.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Finds where to insert the read exhibit using insertion sort algorithm
	 * @param exhibitList
	 * @param distance
	 * @return
	 */
	private int findIndex(LinearLayout exhibitList, int distance){
		int index = 0;
		for(int i =0; i < exhibitList.getChildCount(); i++){
			RelativeLayout view = (RelativeLayout) exhibitList.getChildAt(i);
			TextView distanceText = (TextView) view.findViewById(R.id.distancetext);
			String text = distanceText.getText().toString().replace("ft", "");
			int dist = Integer.valueOf(text);
			if(distance<dist){
				break;
			}
			index++;
		}
		
		return index;
	}
	
	/**
	 * Updates the nearby location items' distance from current location
	 * @param currentLoc
	 */
	public static String calculateDistance(GeoPoint currentLoc, String latitude, String longitude){
		if(currentLoc!=null){
			Location l = new Location("start");
			Location place = new Location("place");
			
			l.setLatitude(currentLoc.getLatitudeE6()/1E6);
			l.setLongitude(currentLoc.getLongitudeE6()/1E6);
		
			place.setLatitude(Double.valueOf(latitude));
			place.setLongitude(Double.valueOf(longitude));
		
			double distance = l.distanceTo(place)*METERS_TO_FT;
			distance = Math.round(distance);
			return String.valueOf(distance).replace(".0", "");
		} else{
			return "0";
		}
	}
	
	
	
	
	
	
	
}
