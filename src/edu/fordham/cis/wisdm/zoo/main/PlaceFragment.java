package edu.fordham.cis.wisdm.zoo.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cis.fordham.edu.wisdm.utils.Operations;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.maps.GeoPoint;
import com.grosner.mapview.Geopoint;
import com.grosner.mapview.PlaceItem;

/**
 * These fragments represent different, sorted locations. Each will display individual locations from a specified file and option to view all on the map.
 * @author Andrew Grosner
 * @version 1.0
 */
public class PlaceFragment extends SherlockFragment implements OnClickListener{

	public static int TYPE_EXHIBITS = 0;
	
	public static int TYPE_FOOD = 1;
	
	public static int TYPE_SPECIAL = 2;
	
	public static int TYPE_SHOPS = 3;
	
	public static int TYPE_ADMIN = 4;

	public static double METERS_TO_FT = 3.28084;
	
	/**
	 * The type of this fragment
	 */
	private int type = 0;
	
	/**
	 * The parent layout widget of the XML file
	 */
	private RelativeLayout exhibit = null;
	
	/**
	 * The view that holds all of the child placeitem views
	 */
	private LinearLayout exhibitList = null;
	
	/**
	 * The layout inflater for the view of this fragment
	 */
	private LayoutInflater inflater = null;
	
	/**
	 * Container of this fragment
	 */
	private ViewGroup container = null;
	
	/**
	 * The places that will be put onto the map
	 */
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
		if(type == TYPE_SHOPS){
			exhibitList.addView(createExhibitItem(getActivity(), inflater, container, -1, "ic_action_tshirt", "Visit Store Website", "", this, false));
		}
		exhibitList.addView(createExhibitItem(getActivity(), inflater, container, 0, "ic_action_location", "View All On Map", "", this, false));
		
		String fName = "";
		
		if(type == TYPE_EXHIBITS){
			fName = "exhibits";
		} else if(type == TYPE_FOOD){
			fName = "food";
		} else if(type == TYPE_SPECIAL){
			fName = "special-exhibits";
		} else if(type == TYPE_SHOPS){
			fName = "shops";
		} else if(type == TYPE_ADMIN){
			fName = "admin";
		}
		TextView title = (TextView) exhibit.findViewById(R.id.title);
		title.setText(fName.replace("-", " "));
		
		fName+=".txt";
		readInFile(getActivity(), inflater, container, fName, exhibitList, points, this, false);	
	}

	@Override
	public void onClick(View v) {
		
		//load up splashscreenactivity's instance
		SplashScreenActivity act = (SplashScreenActivity) this.getActivity();
		FragmentTransaction mTransaction = this.getFragmentManager().beginTransaction();
		int id = v.getId();
		
		if(id > 0){
			PlaceItem place = points.get(id-1);
			act.showMap(mTransaction, this.getView(), place);
		} else if(id == 0){
			act.showMap(mTransaction, this.getView(), points);
		} else if(id == -1){
			getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bronxzoostore.com")));
		}
			
		
	}
	
	/**
	 * Builds the layout for listing nearby exhibits with title, distance, and custom image
	 * @param act
	 * @param inflater
	 * @param container
	 * @param id
	 * @param drawablePath
	 * @param title
	 * @param distance
	 * @param mListener
	 * @return
	 */
	public static RelativeLayout createExhibitItem(Activity act, LayoutInflater inflater, ViewGroup container, int id, 
			String drawablePath, String title, String distance, OnClickListener mListener, boolean wrap){
		View v = inflater.inflate(R.layout.exhibititem, container, false);
		RelativeLayout exhibitItem = (RelativeLayout) v.findViewById(R.id.mainrel);
		final RelativeLayout expandBar = (RelativeLayout) v.findViewById(R.id.expandLayout);
		Operations.removeView(expandBar);
		
		TextView titleText = (TextView) exhibitItem.findViewById(R.id.title);
		titleText.setText(title);
		
		//if request to not fill, will request smaller size
		if(wrap && SplashScreenActivity.isLargeScreen){
			exhibitItem.setLayoutParams(new LayoutParams(SplashScreenActivity.SCREEN_WIDTH/4, LayoutParams.WRAP_CONTENT));
		}
		
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
	 * @param container
	 * @param id
	 * @param place
	 * @param mListener
	 * @param wrap
	 * @return
	 */
	public static RelativeLayout createExhibitItem(Activity act, LayoutInflater inflater, ViewGroup container, int id, PlaceItem place, OnClickListener mListener, boolean wrap){
		return createExhibitItem(act, inflater, container, id, place.getIconDrawablePath(), place.getDescription(), calculateDistance(SplashScreenActivity.myLocation, place.getPoint()) +"ft", mListener, wrap);
	}
	
	/**
	 * Reads in the place file into memory
	 * General format explained within place files /assets/
	 * @param fName
	 */
	public static void readInFile(Activity act, LayoutInflater inflater, ViewGroup container, String fName, LinearLayout exhibitList,
			LinkedList<PlaceItem> points, OnClickListener onclick, boolean wrap){
		
		try {
			Scanner read = new Scanner(act.getAssets().open(fName));
			int idIndex = -1;
			while(read.hasNextLine()){
				String line = read.nextLine();
				idIndex++;
				if(idIndex!=0){
					String[] lineArray = line.split(",");
					//	TODO: add latitude, longitude coordinates
					String distance = "0";
					if(lineArray.length>=4){
						double lat = Double.valueOf(lineArray[2]);
						double lon = Double.valueOf(lineArray[3]);
						distance = calculateDistance(SplashScreenActivity.myLocation, lineArray[2], lineArray[3]);
						
						PlaceItem place = new PlaceItem(new Geopoint(lon, lat, lineArray[0]).setId(idIndex), lineArray[0], String.valueOf(distance), R.layout.exhibitmenu, R.drawable.ic_action_location);
						place.setIconResId(lineArray[1]);
						points.add(place);
						
						int index = findIndex(exhibitList, Integer.valueOf(distance));
						if(exhibitList!=null)
							exhibitList.addView(createExhibitItem(act, inflater, container, idIndex, place, onclick, wrap), index);
					} else{
						if(exhibitList!=null)
							exhibitList.addView(createExhibitItem(act, inflater, container, idIndex, lineArray[1], lineArray[0], distance+"ft", onclick, wrap));
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
	 * Purely reads in data to list of points
	 * @see readInFile(Activity act, LayoutInflater inflater, ViewGroup container, String fName, LinearLayout exhibitList,
			LinkedList<PlaceItem> points, OnClickListener onclick)
	 * @param act
	 * @param fName
	 * @param points
	 */
	public static void readInFile(Activity act, String fName, LinkedList<PlaceItem> points){
		try {
			Scanner read = new Scanner(act.getAssets().open(fName));
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
						points.add(new PlaceItem(new Geopoint(lat, lon, lineArray[0]), lineArray[0], String.valueOf(distance), R.layout.exhibitmenu, R.drawable.ic_action_location).setIconResId(lineArray[1]));
						
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
	 * Finds where to insert the exhibit using insertion sort algorithm
	 * @param exhibitList
	 * @param distance
	 * @return
	 */
	private static int findIndex(LinearLayout exhibitList, int distance){
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
	 *Calculates distance between a geopoint and two coordinate strings and returns human-readable string
	 * @param currentLoc
	 * @return "<?>ft"
	 */
	public static String calculateDistance(Geopoint currentLoc, String latitude, String longitude){
		if(currentLoc!=null){
			Location l = new Location("start");
			Location place = new Location("place");
			
			l.setLatitude(currentLoc.getLatitude());
			l.setLongitude(currentLoc.getLongitude());
		
			place.setLatitude(Double.valueOf(latitude));
			place.setLongitude(Double.valueOf(longitude));
		
			double distance = l.distanceTo(place)*METERS_TO_FT;
			distance = Math.round(distance);
			return String.valueOf(distance).replace(".0", "");
		} else{
			return "0";
		}
	}
	
	/**
	 * Calculates distance between two geopoints and returns human-readable string
	 * @param current
	 * @param toThis
	 * @return "<?>ft"
	 */
	public static String calculateDistance(Geopoint current, Geopoint toThis){
		return calculateDistance(current, String.valueOf(toThis.getLatitude()), String.valueOf(toThis.getLongitude()));
	}
	
	
	
	
	
	
}
