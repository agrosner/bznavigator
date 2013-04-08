package edu.fordham.cis.wisdm.zoo.main.places;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SlidingScreenActivity;
import edu.fordham.cis.wisdm.zoo.main.SlidingScreenList;
import edu.fordham.cis.wisdm.zoo.utils.Operations;
import edu.fordham.cis.wisdm.zoo.utils.map.MapViewFragment;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceMarker;

public class PlaceFragmentList extends SherlockFragment implements OnClickListener{
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
	private LinkedList<PlaceMarker> points = new LinkedList<PlaceMarker>();
	
	/**
	 * Default constructor, sets type to "Exhibits"
	 */
	public PlaceFragmentList(){
		type = PlaceType.EXHIBITS;
	}
	
	/**
	 * Constructor specifying type
	 * @param type
	 */
	public PlaceFragmentList(PlaceType type){
		this.type = type;
	}
	
	/**
	 * Constructor specifying type and custom list
	 * @param type
	 */
	public PlaceFragmentList(PlaceType type, LinkedList<PlaceMarker> places){
		this.type = type;
		points = places;
	}
	
	public void setPoints(LinkedList<PlaceMarker> places){
		points = places;
	}
	
	public boolean isEmpty(){
		return points.isEmpty();
	}
	
	/**
	 * Creates a new fragment if the fragment is null , else returns the original instance
	 * @param frag
	 * @param type
	 * @return
	 */
	public static PlaceFragmentList initFrag(PlaceFragmentList frag, PlaceFragmentList.PlaceType type){
		if(frag==null){
			frag = new PlaceFragmentList(type);
		}
		return frag;
	}
	
	/**
	 * Called when fragment's view is created
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		exhibit = (RelativeLayout) inflater.inflate(R.layout.fragment_place, null, false);
		exhibitList = (LinearLayout) exhibit.findViewById(R.id.exhibitList);
		Operations.setOnClickListeners(exhibit, this, R.id.refresh, R.id.exit);
		
		return exhibit;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		refresh();
	}
	
	/**
	 * Refreshes a list based on the amount of params passed to it
	 * @param custom
	 */
	public void refresh(){
		exhibitList.removeAllViews();
		exhibitList.addView(PlaceController.createExhibitItem(getActivity(), 0, "ic_action_location", "View All On Map", "", this));
	
		((TextView) exhibit.findViewById(R.id.title)).setText(type.toTitleString());
		
		String fName = type.toString() + ".txt";
		SlidingScreenActivity act = (SlidingScreenActivity) getActivity();
		
		if(points.isEmpty())	PlaceController.readInData(act, act, points, fName);
		
		PlaceController.reOrderByDistance(points, act.mList.getMapFragment().getLastKnownLocation());
		PlaceController.readInDataIntoList(act.mList.getMapFragment().getLastKnownLocation(), getActivity(), exhibitList, points, this);
			
		//new GetDataTask(fName).execute();
	}
	
	
	@Override
	public void onClick(View v) {
		//load up splashscreenactivity's instance

		final SlidingScreenActivity act = (SlidingScreenActivity) getActivity();
		int id = v.getId();
		
		if(id == R.id.refresh){
			refresh();
			Toast.makeText(getActivity(), "Refreshed", Toast.LENGTH_SHORT).show();
		} else if(id == R.id.exit){
			act.onBackPressed();
		} else {
			SlidingScreenList list = act.mList;
			MapViewFragment map = act.mList.getMapFragment();
			list.switchToMap();
			map.clearMap();
			
			if(id > 0){
				map.addPlace(points.get(id-1));
			} else if(id == 0){
				LinkedList<PlaceMarker> placeList = null;
				if(type !=  PlaceType.NEARBY){
					placeList = points;
				} else{
					Collections.sort(points, new Comparator<PlaceMarker>(){

						@Override
						public int compare(PlaceMarker lhs, PlaceMarker rhs) {
							return Float.valueOf(lhs.getLocation().distanceTo(act.mList.getMapFragment().getLastKnownLocation()))
									.compareTo(rhs.getLocation().distanceTo(act.mList.getMapFragment().getLastKnownLocation()));
						}
						
					});
					LinkedList<PlaceMarker> reducePoints = new LinkedList<PlaceMarker>();
					while(reducePoints.size()!=10){
						reducePoints.add(points.get(reducePoints.size()));
					}
					placeList = reducePoints;
				}
				map.addPlaceList(placeList);
				
			} else if(id == -1){
				getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bronxzoostore.com")));
			} 
		}
	}
}

