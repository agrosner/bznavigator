package com.grosner.zoo.fragments;

import android.os.Bundle;
import android.widget.ListView;

import com.grosner.smartinflater.annotation.SMethod;
import com.grosner.zoo.FragmentUtils;
import com.grosner.zoo.R;
import com.grosner.zoo.adapters.MenuAdapter;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.constants.UserConstants;

public class MenuFragment extends ZooFragment implements UserConstants{

    private MenuAdapter mAdapter;

	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);

        mLayout = R.layout.fragment_listview;
        mTitle = getString(R.string.bronx_zoo);
	}

    @SMethod
    private void onCreateListView(ListView listView){
        listView.setAdapter(mAdapter = new MenuAdapter(listView.getContext(),
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
            if(title.equals(ZooApplication.getResourceString(R.string.menu_view_pins))){
                placeType = PlaceFragment.PlaceType.PINS;
                tag = "Pins";
            } else if(title.equals(ZooApplication.getResourceString(R.string.menu_find_nearby))){
                placeType = PlaceFragment.PlaceType.NEARBY;
                tag = "Nearby";
            } else if(title.equals(ZooApplication.getResourceString(R.string.menu_place_categories))){
                FragmentUtils.goToFragment(getZooActivity(), getString(R.string.fragment_place_categories),
                        PlaceCategoriesFragment.class,
                        null, true, false, R.id.MenuView);
            }
            if(placeType!=null) {
                bundle.putSerializable("Type", placeType);
                FragmentUtils.goToFragment(getZooActivity(), tag+PlaceFragment.class.getSimpleName(),
                        PlaceFragment.class, bundle, true, false, R.id.MenuView);
            }
        }
	}
	
	/**
	 * Switches list to the map fragment
	 */
	public MapViewFragment switchToMap(){
		return (MapViewFragment) switchFragment("MapViewFragment", MapViewFragment.class, null);
	}
	
}
