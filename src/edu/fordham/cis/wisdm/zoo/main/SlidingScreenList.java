package edu.fordham.cis.wisdm.zoo.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapsInitializer;

import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragmentList;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragmentList.PlaceType;
import edu.fordham.cis.wisdm.zoo.utils.IconTextListAdapter;
import edu.fordham.cis.wisdm.zoo.utils.map.MapViewFragment;

public class SlidingScreenList extends SherlockListFragment {

	private PlaceFragmentList food = null;
	
	private PlaceFragmentList nearby = null;
	
	private PlaceFragmentList exhibit = null;
	
	private PlaceFragmentList special = null;
	
	private PlaceFragmentList shops = null;
	
	private PlaceFragmentList admin = null;
	
	private MapViewFragment map = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		super.onCreateView(inflater, container, instance);
		View v = inflater.inflate(R.layout.arraylist, container, false);
		
		int[] drawables = {R.drawable.map,		R.drawable.find,
						   R.drawable.shop,		
						   R.drawable.special,  R.drawable.food,		
						   R.drawable.exhibit,  R.drawable.amenities,
						   R.drawable.admin};
		
		IconTextListAdapter icontextlist=  new IconTextListAdapter(this.getActivity(), R.array.splash_list2, drawables);
		setListAdapter(icontextlist);
		return v;
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = getSelectedFragment(getActivity(), position);
		if (newContent != null)
			switchFragment(newContent);
	}
	
	public void switchToMap(){
		switchFragment(map);
	}
	
	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;
	
		if (getActivity() instanceof SlidingScreenActivity) {
			SlidingScreenActivity ra = (SlidingScreenActivity) getActivity();
			ra.switchContent(fragment);
		}
	}
	
	public Fragment getSelectedFragment(Activity act, int position){
		switch(position){
		case 0:
			if(map==null){
				try{
					MapsInitializer.initialize(act);
					map = new MapViewFragment();
					map.setRetainInstance(true);
				} catch (GooglePlayServicesNotAvailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
					Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
							GooglePlayServicesUtil.isGooglePlayServicesAvailable(
									getActivity()), getActivity(), 0, 
									new OnCancelListener(){

						@Override
						public void onCancel(DialogInterface dialog) {
							getActivity().finish();
						}
						
					});
					dialog.setOnDismissListener(new OnDismissListener(){

						@Override
						public void onDismiss(DialogInterface dialog) {
							//start();
						}
						
					});
					dialog.show();
				}
			}
			return map;
		case 1: 
			return PlaceFragmentList.initFrag(nearby, PlaceType.NEARBY);
		case 2:
			return PlaceFragmentList.initFrag(shops, PlaceType.SHOPS);
		case 3:
			return PlaceFragmentList.initFrag(special, PlaceType.SPECIAL);
		case 4:
			return PlaceFragmentList.initFrag(food, PlaceType.FOOD);
		case 5:
			return PlaceFragmentList.initFrag(exhibit, PlaceType.EXHIBITS);
		case 7:
			return PlaceFragmentList.initFrag(admin, PlaceType.ADMIN);
			
		}
		return null;
	}
}
