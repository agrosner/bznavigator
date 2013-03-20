package edu.fordham.cis.wisdm.zoo.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapsInitializer;

import edu.fordham.cis.wisdm.zoo.main.constants.UserConstants;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragmentList;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragmentList.PlaceType;
import edu.fordham.cis.wisdm.zoo.utils.IconTextListAdapter;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import edu.fordham.cis.wisdm.zoo.utils.map.MapViewFragment;

public class SlidingScreenList extends SherlockListFragment implements UserConstants{

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
						   R.drawable.shop,		R.drawable.special,  
						   R.drawable.food,		R.drawable.exhibit,  
						   R.drawable.amenities, R.drawable.admin,
						   R.drawable.logout};
		
		IconTextListAdapter icontextlist=  new IconTextListAdapter(this.getActivity(), R.array.splash_list2, drawables);
		setListAdapter(icontextlist);
		return v;
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		if(position==6){
			switchToMap();
			if(getActivity() instanceof SlidingScreenActivity){
				SlidingScreenActivity act = (SlidingScreenActivity) getActivity();
				act.getSlidingMenu().showSecondaryMenu(true);
			}
		} else if(position==8){
			 //ask user whether quit or not
			 AlertDialog.Builder message = new AlertDialog.Builder(getActivity());
			 message.setTitle("Logout?");
			 final Activity act = getActivity();
			 message.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				 @Override
				 public void onClick(DialogInterface dialog, int which) {
					 getActivity().stopService(new Intent(getActivity().getApplicationContext(), LocationUpdateService.class));
					 Preference.putBoolean(REMEMBER_ME_LOC, false);
					 
					 Intent upIntent = new Intent(act, Entry.class);
					 if (NavUtils.shouldUpRecreateTask(act, upIntent)) {
						 // This activity is not part of the application's task, so create a new task
						 // with a synthesized back stack.
						 TaskStackBuilder.from(act)
	                        	.addNextIntent(upIntent)
	                        	.startActivities();
						 getActivity().finish();
					 } else {
						 // 	This activity is part of the application's task, so simply
						 // navigate up to the hierarchical parent activity.
						 NavUtils.navigateUpTo(act, upIntent);
					 }
				 }
			 });
			 message.setNegativeButton("No", new DialogInterface.OnClickListener() {
				 
				 @Override	
				 public void onClick(DialogInterface dialog, int which) {}
			 });
			 message.create().show();
		} else{
				Fragment newContent = getSelectedFragment(getActivity(), position);
				if (newContent != null)
					switchFragment(newContent);
		}
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
	
	public MapViewFragment getMapFragment(){
		return map;
	}
}
