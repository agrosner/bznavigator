package edu.fordham.cis.wisdm.zoo.main.places;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.location.Location;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.utils.Operations;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;

/**
 * Class provides operations on creating and manipulating place data
 * @author Andrew Grosner
 *
 */
public class PlaceController {
	
	/**
	 * Meters to FT conversion
	 */
	public static double METERS_TO_FT = 3.28084;
	
	public static double MILES_TO_FT = 5280;
	
	public static final String MILES = " Miles";
	/**
	 * Builds the layout for listing nearby exhibits with title, distance, and custom image
	 * @param act
	 * @param id
	 * @param drawablePath
	 * @param title
	 * @param distance
	 * @param mListener
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static RelativeLayout createExhibitItem(Activity act, int id, String drawablePath, 
			String title, String distance, OnClickListener mListener){
		View v = act.getLayoutInflater().inflate(R.layout.exhibititem, null, false);
		RelativeLayout exhibitItem = (RelativeLayout) v.findViewById(R.id.mainrel);
		Operations.setViewText(exhibitItem, title, R.id.title);
			
		if(!drawablePath.equals("0")){
			ImageView image = (ImageView) exhibitItem.findViewById(R.id.image);
			int drawableId = act.getResources().getIdentifier(drawablePath, "drawable", act.getPackageName());
			image.setBackgroundDrawable(act.getResources().getDrawable(drawableId));
		}
		
		exhibitItem.setId(id);
		exhibitItem.setOnClickListener(mListener);
		
		if(!distance.equals("")){
			Operations.setViewText(exhibitItem, distance, R.id.distancetext);
		}
		
		return exhibitItem;
	}
	
	/**
	 * Builds the layout for listing nearby exhibits with title, distance, and custom image using placeitem
	 * @param act
	 * @param inflater
	 * @param id
	 * @param place
	 * @param mListener
	 * @param wrap
	 * @return
	 */
	public static RelativeLayout createExhibitItem(Location loc, Activity act, int id, PlaceItem place, OnClickListener mListener){
		return createExhibitItem(act, id, place.getDrawablePath(), place.getName(), calculateDistanceString(loc, place.getLocation()), mListener);
	}

	/**
	 * Reads a place text file from assets, then constructs the layout of each into an exhibititem thats placed into an exhibitList
	 * @param act
	 * @param inflater
	 * @param exhibitList
	 * @param points
	 * @param onClick
	 * @param wrap
	 */
	public static void readInDataIntoList(Location loc, Activity act, LinearLayout exhibitList, LinkedList<PlaceItem> points, OnClickListener onClick){
		if(exhibitList==null)	return;
		int idIndex = 0;
			synchronized(points){
				for(PlaceItem place: points){
					idIndex++;
					exhibitList.addView(createExhibitItem(loc, act,idIndex, place, onClick));
				}
				exhibitList.postInvalidate();
		}
	}

	/**
	 * Purely reads in data to list of points passed
	 * @see readInFile(Activity act, LayoutInflater inflater, ViewGroup container, String fName, LinearLayout exhibitList,
			LinkedList<PlaceItem> points, OnClickListener onclick)
	 * @param act
	 * @param fName
	 * @param points
	 */
	private static void readInData(Location myLocation, Activity act, String fName, List<PlaceItem> points, OnClickListener onInfoClick){
		try {
			Scanner mScanner = new Scanner(act.getAssets().open(fName));
			int idIndex = -1;
			while(mScanner.hasNextLine()){
				String line = mScanner.nextLine();
				idIndex++;
				if(idIndex!=0){
					String[] lineArray = line.split(",");
					float distance = 0;
					if(lineArray.length>=4){
						double lat = Double.valueOf(lineArray[2]);
						double lon = Double.valueOf(lineArray[3]);
						Location loc = new Location("");
						loc.setLatitude(lat);
						loc.setLongitude(lon);
						
						if(myLocation==null)	distance = 0; 
						else	distance = loc.distanceTo(myLocation);
						
						if(lineArray[0].toLowerCase().contains("restroom")){
							lineArray[0] = "Restroom";
						}
						int drawableId = act.getResources().getIdentifier(lineArray[1], "drawable", act.getPackageName());
						
						if(fName.equals("admin.txt")){
							lineArray[0]+="(Staff Only)";
						}
						
						points.add(new PlaceItem().point(new LatLng(lat, lon)).name(lineArray[0]).id(idIndex).iconId(drawableId).drawablePath(lineArray[1]).distance(distance));
					} 
				}
			}
			mScanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Reads in data from any number of files into a singular linked list
	 * @param act
	 * @param points
	 * @param fNames
	 */
	public static void readInData(Location myLocation, Activity act, OnClickListener onInfoClick, LinkedList<PlaceItem> points, String... fNames){
		for(String fName: fNames){
			readInData(myLocation, act, fName, points, onInfoClick);
		}
	}
	
	
	/**
	 *Calculates distance between a geopoint and two coordinate strings and returns human-readable string
	 * @param currentLoc
	 * @return "<?>ft"
	 */
	public static String calculateDistanceString(Location currentLoc, Location next){
		if(currentLoc!=null){
			double distance = currentLoc.distanceTo(next)*METERS_TO_FT;
			distance = Math.round(distance);
			if(distance>=500){
				distance/=5280;
				return String.format("%.2f" + MILES, distance);
			} else	return String.valueOf(distance).replace(".0", "") + " ft";
		} else{
			return "0";
		}
	} 
	
	/**
	 * Regenerates the distances to the exhibits
	 * @param currentLoc
	 * @param points
	 */
	public static void reCalculateDistance(Location currentLoc, LinkedList<PlaceItem>... points){
		if(currentLoc!=null){
			for (LinkedList<PlaceItem> point: points){
				point = reCalculateDistance(currentLoc, point);
			}
		}
	}
	
	/**
	 * Recalculates a linkedlist of placeitems
	 * @param currentLoc
	 * @param points
	 * @return
	 */
	public static LinkedList<PlaceItem> reCalculateDistance(Location currentLoc, LinkedList<PlaceItem> points){
		LinkedList<PlaceItem> temp = new LinkedList<PlaceItem>();
		for(PlaceItem place: points){
			float distance = 0;
			if(currentLoc!=null) distance = currentLoc.distanceTo(place.getLocation());
			temp.add(place.distance(distance));
		}
		return temp;
	}
	
	/**
	 * Reorders the list of elements by distance to the place
	 * @param currentLoc
	 * @param points
	 */
	public static void reOrderByDistance(LinkedList<PlaceItem> points){
		Collections.sort(points, new Comparator<PlaceItem>(){

			@Override
			public int compare(PlaceItem lhs, PlaceItem rhs) {
				return Float.valueOf(lhs.getDistance()).compareTo(rhs.getDistance());
			}
				
		});
	}
	
	
}
