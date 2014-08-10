package com.grosner.zoo.fragments;

import android.os.Bundle;
import android.widget.ListView;

import com.grosner.smartinflater.annotation.SMethod;
import com.grosner.zoo.FragmentUtils;
import com.grosner.zoo.R;
import com.grosner.zoo.adapters.MenuAdapter;
import com.grosner.zoo.application.ZooApplication;

/**
 * Created by: andrewgrosner
 * Date: 8/10/14.
 * Contributors: {}
 * Description:
 */
public class PlaceCategoriesFragment extends ZooFragment {

    private MenuAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayout = R.layout.fragment_listview;
    }

    @SMethod
    private void onCreateListView(ListView listView){
        listView.setAdapter(mAdapter = new MenuAdapter(listView.getContext(), R.array.place_categories_list
        ,R.array.place_icons));
    }

    @SMethod
    private void onItemClickListView(int position){
        PlaceFragment.PlaceType placeType = null;
        String tag = "";
        String title = mAdapter.getItem(position);
        if(title.equals(ZooApplication.getResourceString(R.string.menu_shops))) {
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
            Bundle bundle = new Bundle();
            bundle.putSerializable("Type", placeType);
            FragmentUtils.goToFragment(getZooActivity(), tag + PlaceFragment.class.getSimpleName(),
                    PlaceFragment.class, bundle, true, false, R.id.MenuView);
        }
    }
}
