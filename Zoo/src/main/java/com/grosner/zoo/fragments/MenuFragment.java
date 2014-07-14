package com.grosner.zoo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.grosner.smartinflater.annotation.SMethod;
import com.grosner.zoo.FragmentUtils;
import com.grosner.zoo.R;
import com.grosner.zoo.activities.ZooActivity;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.constants.UserConstants;
import com.grosner.zoo.adapters.SlidingScreenListAdapter;

public class MenuFragment extends ZooFragment implements UserConstants{

    private SlidingScreenListAdapter mAdapter;

	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);

        mLayout = R.layout.fragment_slide_list;
        mTitle = getString(R.string.bronx_zoo);
	}

    @SMethod
    private void onCreateListView(ListView listView){
        listView.setAdapter(mAdapter = new SlidingScreenListAdapter(listView.getContext(),
                R.array.splash_list2, R.array.menu_icons));
    }
	

	@SMethod
	public void onItemClickListView(int position) {
        Bundle bundle;
        String title = mAdapter.getItem(position);

        if (title.equals(ZooApplication.getResourceString(R.string.menu_view_map))) {
            switchToMap();
        } else {
            String tag = null;
            PlaceFragment.PlaceType placeType = null;
            bundle = new Bundle();
            if(title.equals(ZooApplication.getResourceString(R.string.menu_find_nearby))){
                placeType = PlaceFragment.PlaceType.NEARBY;
                tag = "Nearby";
            } else if(title.equals(ZooApplication.getResourceString(R.string.menu_shops))) {
                placeType = PlaceFragment.PlaceType.SHOPS;
                tag = "Shops";
            } else if(title.equals(ZooApplication.getResourceString(R.string.menu_special_exhibits))) {
                placeType = PlaceFragment.PlaceType.SPECIAL;
                tag = "Special";
            } else if(title.equals(ZooApplication.getResourceString(R.string.menu_food))) {
                placeType = PlaceFragment.PlaceType.FOOD;
                tag = "Food";
            } else if(title.equals(ZooApplication.getResourceString(R.string.menu_exhibits))) {
                placeType = PlaceFragment.PlaceType.EXHIBITS;
                tag = "Exhibits";
            } else if(title.equals(ZooApplication.getResourceString(R.string.menu_admin))) {
                placeType = PlaceFragment.PlaceType.ADMIN;
                tag = "Admin";
            }
            if(placeType!=null) {
                bundle.putSerializable("Type", placeType);
                FragmentUtils.goToFragment(getZooActivity(), tag+PlaceFragment.class.getSimpleName(),
                        PlaceFragment.class, bundle, true);
            }
        }

        getZooActivity().getDrawer().closeDrawers();
	}
	
	/**
	 * Switches list to the map fragment
	 */
	public MapViewFragment switchToMap(){
		return (MapViewFragment) switchFragment("MapViewFragment", MapViewFragment.class, null);
	}
	
}
