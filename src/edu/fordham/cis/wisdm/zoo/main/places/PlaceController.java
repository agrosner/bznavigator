package edu.fordham.cis.wisdm.zoo.main.places;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cis.fordham.edu.wisdm.utils.Operations;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenController;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;

/**
 * Class provides operations on creating and manipulating place data
 * @author agrosner
 *
 */
public class PlaceController {
	
	/**
	 * Meters to FT conversion
	 */
	public static double METERS_TO_FT = 3.28084;
	
	public static double MILES_TO_FT = 5280;
	
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
			String title, String distance, OnClickListener mListener, boolean wrap){
		View v = act.getLayoutInflater().inflate(R.layout.exhibititem, null, false);
		RelativeLayout exhibitItem = (RelativeLayout) v.findViewById(R.id.mainrel);
		final RelativeLayout expandBar = (RelativeLayout) v.findViewById(R.id.expandLayout);
		Operations.removeView(expandBar);
		
		TextView titleText = (TextView) exhibitItem.findViewById(R.id.title);
		titleText.setText(title);
		
		//if request to not fill, will request smaller size
		if(wrap && SplashScreenActivity.isLargeScreen)
			exhibitItem.setLayoutParams(new LayoutParams(SplashScreenController.SCREEN_WIDTH/4, LayoutParams.WRAP_CONTENT));
		
		
		if(!drawablePath.equals("0")){
			ImageView image = (ImageView) exhibitItem.findViewById(R.id.image);
			int drawableId = act.getResources().getIdentifier(drawablePath, "drawable", act.getPackageName());
			image.setBackgroundDrawable(act.getResources().getDrawable(drawableId));
		}
		
		//if not displaying as search bar option
		if(!wrap && !title.equals("View All On Map") &&!title.equals("Visit Store Website")){
			
			Button locate = (Button) expandBar.findViewById(R.id.locate);
			locate.setOnClickListener(mListener);
			locate.setId(id);

			exhibitItem.setOnClickListener(new OnClickListener(){
				boolean isShown = true;
				
				@Override
				public void onClick(View v) {
					Operations.addRemoveView(expandBar, isShown);
					isShown = !isShown;
				}
				
			});
		} else{
			exhibitItem.setId(id);
			exhibitItem.setOnClickListener(mListener);
		}
		
		if(!distance.equals("")){
			TextView distText = (TextView) exhibitItem.findViewById(R.id.distancetext);
			distText.setText(distance);
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
	public static RelativeLayout createExhibitItem(Activity act, int id, PlaceItem place, OnClickListener mListener, boolean wrap){
		return createExhibitItem(act, id, place.getDrawablePath(), place.getName(), calculateDistance(SplashScreenActivity.myLocation, place.getLocation()) +"ft", mListener, wrap);
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
	public static void readInDataIntoList(Activity act, LinearLayout exhibitList, LinkedList<PlaceItem> points, OnClickListener onClick, boolean wrap){
		if(exhibitList==null)	return;
		int idIndex = 0;
			synchronized(points){
				for(PlaceItem place: points){
					idIndex++;
					String distance = place.getDistance();
					int index = findIndex(exhibitList, Integer.valueOf(distance));
					exhibitList.addView(createExhibitItem(act,idIndex, place, onClick, wrap), index);
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
	private static void readInData(Activity act, String fName, List<PlaceItem> points, OnClickListener onInfoClick){
		try {
			Scanner mScanner = new Scanner(act.getAssets().open(fName));
			int idIndex = -1;
			while(mScanner.hasNextLine()){
				String line = mScanner.nextLine();
				idIndex++;
				if(idIndex!=0){
					String[] lineArray = line.split(",");
					String distance = "0";
					if(lineArray.length>=4){
						double lat = Double.valueOf(lineArray[2]);
						double lon = Double.valueOf(lineArray[3]);
						Location loc = new Location("");
						loc.setLatitude(lat);
						loc.setLongitude(lon);
						distance = calculateDistance(SplashScreenActivity.myLocation, loc);
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
	public static void readInData(Activity act, OnClickListener onInfoClick, LinkedList<PlaceItem> points, String... fNames){
		for(String fName: fNames){
			readInData(act, fName, points, onInfoClick);
		}
	}
	
	/**
	 * Finds where to insert the exhibit using insertion sort algorithm
	 * @param exhibitList
	 * @param distance
	 * @return
	 */
	private static int findIndex(LinearLayout exhibitList, int distance){
		int index = 0;
		for(int i =0; i < exhibitList.getChildCount(); i++){
			View child = exhibitList.getChildAt(i);
			if(child instanceof RelativeLayout){
				RelativeLayout view = (RelativeLayout) exhibitList.getChildAt(i);
				TextView distanceText = (TextView) view.findViewById(R.id.distancetext);
				if(distanceText!=null){
					String text = distanceText.getText().toString().replace("ft", "");
					int dist = Integer.valueOf(text);
					if(distance<dist)	break;
				}	
			}
			index++;
			
		}
		
		return index;
	}
	
	/**
	 * Finds where to insert the exhibit using insertion sort algorithm
	 * @param exhibitList
	 * @param distance
	 * @return
	 */
	static int findIndex(LinkedList<? extends Object> exhibitList, int distance){
		int index = 0;
		for(int i =0; i < exhibitList.size(); i++){
			Object ob = exhibitList.get(i);
			String text = "";
			if(ob instanceof RelativeLayout){
				RelativeLayout view = (RelativeLayout) ob;
				TextView distanceText = (TextView) view.findViewById(R.id.distancetext);
				text = distanceText.getText().toString().replace("ft", "");
			} else if(ob instanceof PlaceItem){
				PlaceItem place = (PlaceItem) ob;
				text = place.getDistance().replace("ft", "");
			}
			int dist = Integer.valueOf(text);
			if(distance<dist)	break;
			
			index++;
		}
		
		return index;
	}
	
	/**
	 *Calculates distance between a geopoint and two coordinate strings and returns human-readable string
	 * @param currentLoc
	 * @return "<?>ft"
	 */
	public static String calculateDistance(Location currentLoc, Location next){
		if(currentLoc!=null){
			double distance = currentLoc.distanceTo(next)*METERS_TO_FT;
			distance = Math.round(distance);
			return String.valueOf(distance).replace(".0", "");
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
				int size = point.size();
				for(int i =0 ; i< size; i++){
					PlaceItem place = point.poll();
					place.distance(calculateDistance(currentLoc, place.getLocation()));
					point.addLast(place);
				}
			}
		}
	}
	
	/**
	 * Reorders the list of elements by distance to the place
	 * @param currentLoc
	 * @param points
	 */
	public static void reOrderByDistance(Location currentLoc, LinkedList<PlaceItem> points){
		if(currentLoc!=null){
			LinkedList<PlaceItem> temp = new LinkedList<PlaceItem>();
			int size = points.size();
			for(int i =0 ; i< size; i++){
				PlaceItem place = points.poll();
				String dist = calculateDistance(currentLoc, place.getLocation());
				int index = findIndex(points, Integer.valueOf(dist));
				place.distance(dist);
				temp.add(index, place);
			}
			points = temp;
		}
	}
	
	
}
