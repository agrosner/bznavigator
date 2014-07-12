package com.grosner.zoo.fragments;

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

import com.grosner.zoo.R;
import com.grosner.zoo.markers.PlaceMarker;
import com.grosner.zoo.singletons.ExhibitManager;
import com.grosner.zoo.utils.MapUtils;

import java.util.LinkedList;

/**
 * This view fragment stores place information that is displayed on the map
 * @author Andrew Grosner
 *
 */
public class AmenitiesFragment extends ZooFragment implements OnCheckedChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		
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
			pts = ExhibitManager.getSharedInstance().getGates();
			break;
		case 1:
			pts = ExhibitManager.getSharedInstance().getRestrooms();
			break;
		case 2:
			pts = ExhibitManager.getSharedInstance().getShops();
			break;
		case 3:
			pts = ExhibitManager.getSharedInstance().getParking();
			break;
		case 4:
			pts = ExhibitManager.getSharedInstance().getFood();
			break;
		case 5:
			pts = ExhibitManager.getSharedInstance().getMisc();
			break;
		}
		if(isChecked) readInAmenity(pts);
		else		  MapUtils.removeList(pts);
	}
	
	/**
	 * Reads in an amenity and displays on the map
	 * @param pts
	 */
	private void readInAmenity(LinkedList<PlaceMarker> pts){

		try{
            MapViewFragment map = (MapViewFragment) getActivity().getSupportFragmentManager().findFragmentByTag("MapViewFragment");
			MapUtils.addToMap(map.getMap(), pts);
		} catch(Exception e){
			e.printStackTrace();
			setCheckboxes(false);
		}
	}
	
}
