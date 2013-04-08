package edu.fordham.cis.wisdm.zoo.main.places;

import java.util.LinkedList;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SlidingScreenActivity;
import edu.fordham.cis.wisdm.zoo.utils.map.CurrentLocationManager;
import edu.fordham.cis.wisdm.zoo.utils.map.MapUtils;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceMarker;

/**
 * This view fragment stores place information that is displayed on the map
 * @author Andrew Grosner
 *
 */
public class AmenitiesFragment extends SherlockFragment implements OnCheckedChangeListener {

	private LinkedList<PlaceMarker> gates = new LinkedList<PlaceMarker>();
	
	private LinkedList<PlaceMarker> restrooms = new LinkedList<PlaceMarker>();
	
	private LinkedList<PlaceMarker> shops = new LinkedList<PlaceMarker>();
	
	private LinkedList<PlaceMarker> parking = new LinkedList<PlaceMarker>();
	
	private LinkedList<PlaceMarker> food = new LinkedList<PlaceMarker>();
	
	private LinkedList<PlaceMarker> misc = new LinkedList<PlaceMarker>();
	
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_amenities, null, false);
		
		TextView title = new TextView(getActivity());
		title.setText("Amenities Menu");
		title.setTextSize(25);
		layout.addView(title);
		
		layout.addView(createIconCheckBox("Gates", R.drawable.fordham, 0));
		layout.addView(createIconCheckBox("Restrooms", R.drawable.bathroom, 1));
		layout.addView(createIconCheckBox("Shops", R.drawable.shop, 2));
		layout.addView(createIconCheckBox("Parking", R.drawable.car, 3));
		layout.addView(createIconCheckBox("Food", R.drawable.food, 4));
		layout.addView(createIconCheckBox("Misc.", R.drawable.info, 5));
		
		return layout;
	}
	
	
	/**
	 * Builds and returns a checkbox and imageview within a relative layout view
	 * @param name
	 * @param iconId
	 * @param id
	 * @return
	 */
	public RelativeLayout createIconCheckBox(final String name, int iconId, int id){
		RelativeLayout iconCheckbox = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.icon_checkbox_item, null);
		CheckBox check = (CheckBox) iconCheckbox.findViewById(R.id.check);
		check.setText(name);
		check.setId(id);
		check.setOnCheckedChangeListener(this);
		ImageView icon = (ImageView) iconCheckbox.findViewById(R.id.icon);
		icon.setImageResource(iconId);
		return iconCheckbox;
	}
	
	/**
	 * Sets all of the checkboxes, when the location manager has found a location
	 * @param check
	 */
	public void setCheckboxes(boolean check){
		for(int i =0; i < 6; i++){
			CheckBox ch = (CheckBox) (getView().findViewById(i));
			ch.setChecked(check);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		LinkedList<PlaceMarker> pts = null;
		String fName = "";
		
		switch(buttonView.getId()){
		case 0:
			fName+="gates.txt";
			pts = gates;
			break;
		case 1:
			fName+="restrooms.txt";
			pts = restrooms;
			break;
		case 2:
			fName+="shops.txt";
			pts = shops;
			break;
		case 3:
			fName+="parking.txt";
			pts = parking;
			break;
		case 4:
			fName+="food.txt";
			pts = food;
			break;
		case 5:
			fName+="misc.txt";
			pts = misc;
			break;
		}
		if(isChecked) readInAmenity(pts, fName);
		else		  MapUtils.removeList(pts);
	}
	
	/**
	 * Reads in an amenity and displays on the map
	 * @param pts
	 * @param fName
	 */
	private void readInAmenity(LinkedList<PlaceMarker> pts, String fName){
		SlidingScreenActivity act = (SlidingScreenActivity) getActivity();
		if(pts.isEmpty())	PlaceController.readInData(act, act, pts, fName);
		
		try{
			MapUtils.addToMap(act.mList.getMapFragment().getMap(), pts);
		} catch(Exception e){
			e.printStackTrace();
			setCheckboxes(false);
		}
	}
	
}
