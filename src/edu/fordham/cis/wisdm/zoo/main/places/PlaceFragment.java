package edu.fordham.cis.wisdm.zoo.main.places;

import java.util.LinkedList;
import java.util.Locale;

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
import android.widget.Toast;

import cis.fordham.edu.wisdm.utils.Operations;

import com.actionbarsherlock.app.SherlockFragment;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;

/**
 * These fragments represent different, sorted locations. Each will display individual locations from a specified file and option to view all on the map.
 * @author Andrew Grosner
 * @version 1.0
 */
@SuppressLint("ValidFragment")
public class PlaceFragment extends SherlockFragment implements OnClickListener{
	
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
		Operations.setViewOnClickListeners(exhibit, this, R.id.refresh, R.id.exit);
		
		refresh();
		
		return exhibit;
	}
	
	
	/**
	 * Refreshes a list based on the amount of params passed to it
	 * @param custom
	 */
	@SuppressWarnings("unchecked")
	public void refresh(){
		exhibitList.removeAllViews();
		exhibitList.addView(PlaceController.createExhibitItem(getActivity(), 0, "ic_action_location", "View All On Map", "", this, true));

		((TextView) exhibit.findViewById(R.id.title)).setText(type.toTitleString());
		
		String fName = type.toString() + ".txt";
		SplashScreenActivity act = (SplashScreenActivity) getActivity();
		
		if(points.isEmpty())	PlaceController.readInData(act, act.onInfoClickedListener, points, fName);
		else 	PlaceController.reCalculateDistance(SplashScreenActivity.myLocation, points);
		
		PlaceController.readInDataIntoList(SplashScreenActivity.myLocation, getActivity(), exhibitList, points, this, true);
		
			
		//new GetDataTask(fName).execute();
	}
	

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
		} else if(id > 0){
			PlaceItem place = points.get(id-1);
			LinkedList<PlaceItem> places = new LinkedList<PlaceItem>();
			places.add(place);
			act.showMap(mTransaction, getView(), places);
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

}