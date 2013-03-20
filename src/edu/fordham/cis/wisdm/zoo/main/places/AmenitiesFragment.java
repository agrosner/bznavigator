package edu.fordham.cis.wisdm.zoo.main.places;

import java.util.LinkedList;

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

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SlidingScreenActivity;
import edu.fordham.cis.wisdm.zoo.utils.map.MapUtils;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;

public class AmenitiesFragment extends SherlockFragment implements OnCheckedChangeListener {

	private LinkedList<PlaceItem> gates = new LinkedList<PlaceItem>();
	
	private LinkedList<PlaceItem> restrooms = new LinkedList<PlaceItem>();
	
	private LinkedList<PlaceItem> shops = new LinkedList<PlaceItem>();
	
	private LinkedList<PlaceItem> parking = new LinkedList<PlaceItem>();
	
	private LinkedList<PlaceItem> food = new LinkedList<PlaceItem>();
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_amenities, null, false);
		
		TextView title = new TextView(getActivity());
		title.setText("Amenities Menu");
		title.setTextSize(25);
		layout.addView(title);
		
		RelativeLayout gatesBox = createIconCheckBox("Gates", R.drawable.fordham, 0);
		RelativeLayout restroomsBox = createIconCheckBox("Restrooms", R.drawable.bathroom, 1);
		RelativeLayout shopsBox = createIconCheckBox("Shops", R.drawable.shop, 2);
		RelativeLayout parkingBox = createIconCheckBox("Parking", R.drawable.car, 3);
		RelativeLayout foodBox = createIconCheckBox("Food", R.drawable.food, 4);
		
		layout.addView(gatesBox);
		layout.addView(restroomsBox);
		layout.addView(shopsBox);
		layout.addView(parkingBox);
		layout.addView(foodBox);
		
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
	
	public void setCheckboxes(boolean check){
		for(int i =0; i < 5; i++){
			CheckBox ch = (CheckBox) (getView().findViewById(i));
			ch.setChecked(check);
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		LinkedList<PlaceItem> pts = null;
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
		}
		if(isChecked) readInAmenity(pts, fName);
		else		  MapUtils.removeList(pts);
	}
	
	private void readInAmenity(LinkedList<PlaceItem> pts, String fName){
		SlidingScreenActivity act = (SlidingScreenActivity) getActivity();
		if(pts.size()==0)	PlaceController.readInData(act.mList.getMapFragment().getManager().getLastKnownLocation(),
							act, act, pts, fName);
		
		MapUtils.addToMap(act.mList.getMapFragment().getMap(), pts);
	}
	
}
