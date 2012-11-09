package edu.fordham.cis.wisdm.zoo.main.places;

import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class PlaceFragment extends SherlockFragment implements OnClickListener{
	
	public enum PlaceType{EXHIBITS, FOOD, SPECIAL, SHOPS, ADMIN, NEARBY;
		public String toString(){
			if(this == EXHIBITS)		return "exhibits";
			else if(this == FOOD) 		return "food";
			else if(this == SPECIAL)	return "special-exhibits";
			else if(this == SHOPS) 		return "shops";
			else if(this == ADMIN) 		return "admin";
			else if(this == NEARBY)		return "nearby";
			else						return "";
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
	 * The layout inflater for the view of this fragment
	 */
	private LayoutInflater inflater = null;
	
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
		this.inflater = inflater;
		
		if(type!=PlaceType.NEARBY)	refresh();
		else 						refresh(points, 10);
		
		return exhibit;
	}
	
	/**
	 * Removes all views from list, adds a generic "view all" option
	 */
	private void init(){
		
		exhibit = (RelativeLayout) inflater.inflate(R.layout.placefragment, null, false);
		exhibitList = (LinearLayout) exhibit.findViewById(R.id.exhibitList);
		exhibitList.removeAllViews();
			
		//if(type == PlaceType.SHOPS)
		//exhibitList.addView(createExhibitItem(getActivity(), inflater, container, -1, "ic_action_tshirt", "Visit Store Website", "", this, true));
		
		exhibitList.addView(PlaceController.createExhibitItem(getActivity(), 0, "ic_action_location", "View All On Map", "", this, true));
	}
	
	/**
	 * Reloads data into the layout
	 */
	public void refresh(){
		init();
		
		String fName = type.toString();
		
		TextView title = (TextView) exhibit.findViewById(R.id.title);
		title.setText(fName.replace("-", " "));
		
		fName+=".txt";
		PlaceController.readInData(getActivity(), points, fName);
		PlaceController.readInDataIntoList(getActivity(), exhibitList, points, this, true);	
	}
	
	/**
	 * Loads in a custom list of places to display with custom number of elements
	 * @param custom
	 */
	public void refresh(LinkedList<PlaceItem> custom, int dispNum){
		init();
		TextView title = (TextView) exhibit.findViewById(R.id.title);
		title.setText(type.toString());
		
		LinkedList<RelativeLayout> list = new LinkedList<RelativeLayout>();
		
		for(int i =0; i < custom.size(); i++){
			PlaceItem place = custom.get(i);
			int index = PlaceController.findIndex(list, Integer.valueOf(place.getSnippet()));
			list.add(index, PlaceController.createExhibitItem(getActivity(), i+1, place, this, true));
		}
		if(exhibitList!=null)
			for(int i = 0; i < dispNum; i++){
				exhibitList.addView(list.get(i));
		}
	}

	@Override
	public void onClick(View v) {
		//load up splashscreenactivity's instance
		SplashScreenActivity act = (SplashScreenActivity) this.getActivity();
		FragmentTransaction mTransaction = this.getFragmentManager().beginTransaction();
		int id = v.getId();
		
		if(id > 0){
			PlaceItem place = points.get(id-1);
			LinkedList<PlaceItem> places = new LinkedList<PlaceItem>();
			places.add(place);
			act.showMap(mTransaction, this.getView(), places);
			
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
			if(type !=  PlaceType.NEARBY) act.showMap(mTransaction, this.getView(), points);
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
}