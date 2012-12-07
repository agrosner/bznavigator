package edu.fordham.cis.wisdm.zoo.main.places;

import java.util.LinkedList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.grosner.mapview.PlaceItem;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;

/**
 * These fragments represent different, sorted locations. Each will display individual locations from a specified file and option to view all on the map.
 * @author Andrew Grosner
 * @version 1.0
 */
@SuppressLint("ValidFragment")
public class PlaceFragment extends SherlockFragment{
	
	public enum PlaceType{EXHIBITS, FOOD, SPECIAL, SHOPS, ADMIN, NEARBY;
		public String toString(){
			return name().toLowerCase(Locale.ENGLISH);
		}
		
		/**
		 * Returns capitalized name
		 * @return
		 */
		public String toTitleString(){
			String s = toString();
			String first = s.substring(0, 1);
			return s.replaceFirst(first, first.toUpperCase());
		}
	};

	/**
	 * The type of this fragment
	 */
	private PlaceType type = PlaceType.EXHIBITS;
	
	/**
	 * The parent layout widget of the XML file
	 */
	private RelativeLayout exhibit = null;
	
	/**
	 * The view that holds all of the child placeitem views
	 */
	private LinearLayout exhibitList = null;

	/**
	 * The places that will be put onto the map
	 */
	private LinkedList<PlaceItem> points = new LinkedList<PlaceItem>();

	/**
	 * Default constructor, sets type to "Exhibits"
	 */
	public PlaceFragment(){
		type = PlaceType.EXHIBITS;
	}
	
	/**
	 * Constructor specifying type
	 * @param type
	 */
	public PlaceFragment(PlaceType type){
		this.type = type;
	}
	
	/**
	 * Constructor specifying type and custom list
	 * @param type
	 */
	public PlaceFragment(PlaceType type, LinkedList<PlaceItem> places){
		this.type = type;
		points = places;
	}
	
	/**
	 * Creates a new fragment if the fragment is null , else returns the original instance
	 * @param frag
	 * @param type
	 * @return
	 */
	public static PlaceFragment initFrag(PlaceFragment frag, PlaceFragment.PlaceType type){
		if(frag==null){
			frag = new PlaceFragment(type);
		}
		return frag;
	}
	
	/**
	 * Called when fragment's view is created
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		exhibit = (RelativeLayout) inflater.inflate(R.layout.placefragment, null, false);
		exhibitList = (LinearLayout) exhibit.findViewById(R.id.exhibitList);
		ImageButton refresh = (ImageButton) exhibit.findViewById(R.id.refresh);
		refresh.setOnClickListener(mOnClickListener);
		ImageButton exit = (ImageButton) exhibit.findViewById(R.id.exit);
		exit.setOnClickListener(mOnClickListener);
		
		refresh();
		
		return exhibit;
	}
	
	/**
	 * Removes all views from list, adds a generic "view all" option
	 */
	private void init(){
		
		exhibitList.removeAllViews();
		exhibitList.addView(PlaceController.createExhibitItem(getActivity(), 0, "ic_action_location", "View All On Map", "", mOnClickListener, true));
	}
	
	
	/**
	 * Refreshes a list based on the amount of params passed to it
	 * @param custom
	 */
	@SuppressWarnings("unchecked")
	public void refresh(){
		init();
		
		TextView title = (TextView) exhibit.findViewById(R.id.title);
		title.setText(type.toTitleString());
		
		String fName = type.toString() + ".txt";
		
		if(points.isEmpty())	PlaceController.readInData(getActivity(), points, fName);
		else 	PlaceController.reCalculateDistance(SplashScreenActivity.myLocation, points);
		
		PlaceController.readInDataIntoList(getActivity(), exhibitList, points, mOnClickListener, true);
		
			
		//new GetDataTask(fName).execute();
	}
	private OnClickListener mOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			//load up splashscreenactivity's instance
			SplashScreenActivity act = (SplashScreenActivity) getActivity();
			FragmentTransaction mTransaction = getFragmentManager().beginTransaction();
			int id = v.getId();
			
			if(id == R.id.refresh){
				refresh();
				Toast.makeText(getActivity(), "Refreshed", Toast.LENGTH_SHORT).show();
			} else if(id == R.id.exit){
				act.onBackPressed();
			}
			
			else if(id > 0){
				PlaceItem place = points.get(id-1);
				LinkedList<PlaceItem> places = new LinkedList<PlaceItem>();
				places.add(place);
				act.showMap(mTransaction, getView(), places);
				
				//tries to move map to center on both current location and point
				//if(SplashScreenActivity.myLocation!=null){
					//ZoomLevel zoom = place.getPoint().pixelsToZoom(act.getMap().getView(), SplashScreenActivity.myLocation);
					//MapInternalView view = act.getMap().getView();
					//view.center(place.getPoint(), SplashScreenActivity.myLocation);
					//view.changeZoomLevel(zoom);
				//} else{
					act.getMap().animateTo(place.getPoint());
				//}
			} else if(id == 0){
				if(type !=  PlaceType.NEARBY) act.showMap(mTransaction, getView(), points);
				else{
					LinkedList<PlaceItem> reducePoints = new LinkedList<PlaceItem>();
					while(reducePoints.size()!=10){
						reducePoints.add(points.get(reducePoints.size()));
					}
					act.showMap(mTransaction, getView(), reducePoints);
				}
			} else if(id == -1){
				getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bronxzoostore.com")));
			} 
		}
		
	};

	
	/**
	 * Performs reading in data into the fragment, then adds the data on the UI afterwards
	 * @author agrosner
	 *
	 */
	/**private class GetDataTask extends AsyncTask<Void, Void, Void>{

		private String fName = null;
		
		public GetDataTask(String fname){
			fName = fname;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			if(points.isEmpty())	PlaceController.readInData(getActivity(), points, fName);
			else 	PlaceController.reCalculateDistance(SplashScreenActivity.myLocation, points);
			
			return null;
		}
		
		
		protected Void onPostExecute(Void... param){
			PlaceController.readInDataIntoList(getActivity(), exhibitList, points, mOnClickListener, true);
			return null;
		}
		
	}**/
}