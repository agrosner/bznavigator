package com.grosner.zoo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.grosner.zoo.FragmentUtils;
import com.grosner.zoo.R;
import com.grosner.zoo.activities.ZooActivity;
import com.grosner.zoo.constants.UserConstants;
import com.grosner.zoo.adapters.SlidingScreenListAdapter;

public class MenuFragment extends ListFragment implements UserConstants{

	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		super.onCreateView(inflater, container, instance);
		View v = inflater.inflate(R.layout.fragment_slide_list, container, false);
		
		SlidingScreenListAdapter icontextlist=  new SlidingScreenListAdapter(this.getActivity(), 
				R.array.splash_list2, R.array.menu_icons);
		
		setListAdapter(icontextlist);
		return v;
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		if(position==6){
			switchToMap();
			if(getActivity() instanceof ZooActivity){
				ZooActivity act = (ZooActivity) getActivity();
				//act.getSlidingMenu().showSecondaryMenu(true);
			}
		} else{
            goToFragment(position);
		}
	}
	
	/**
	 * Switches list to the map fragment
	 */
	public MapViewFragment switchToMap(){
		return (MapViewFragment) switchFragment("MapViewFragment", MapViewFragment.class, null);
	}
	
	// the meat of switching the above fragment
	private Fragment switchFragment(String tag, Class clazz, Bundle bundle) {
		if (getActivity() == null)
			return null;

        Fragment fragment = FragmentUtils.getFragment(getActivity(), clazz, tag, bundle);

        if(fragment!=null){
            if (getActivity() instanceof ZooActivity) {
                ZooActivity ra = (ZooActivity) getActivity();
                ra.switchContent(fragment);
            }
        }
        return fragment;
	}
	
	public void goToFragment(int position){
        Bundle bundle;
        switch(position){
            case 0:
            switchToMap();
            break;
		case 1:
            bundle = new Bundle();
            bundle.putSerializable("Type", PlaceFragment.PlaceType.NEARBY);
            switchFragment("Nearby", PlaceFragment.class, bundle);
            break;
		case 2:
            bundle = new Bundle();
            bundle.putSerializable("Type", PlaceFragment.PlaceType.SHOPS);
            switchFragment("Shops", PlaceFragment.class, bundle);
            break;
        case 3:
            bundle = new Bundle();
            bundle.putSerializable("Type", PlaceFragment.PlaceType.SPECIAL);
            switchFragment("Special", PlaceFragment.class, bundle);
            break;
        case 4:
            bundle = new Bundle();
            bundle.putSerializable("Type", PlaceFragment.PlaceType.FOOD);
            switchFragment("Food", PlaceFragment.class, bundle);
            break;
        case 5:
            bundle = new Bundle();
            bundle.putSerializable("Type", PlaceFragment.PlaceType.EXHIBITS);
            switchFragment("Exhibits", PlaceFragment.class, bundle);
            break;
        case 7:
            bundle = new Bundle();
            bundle.putSerializable("Type", PlaceFragment.PlaceType.ADMIN);
            switchFragment("Admin", PlaceFragment.class, bundle);
            break;
        }
	}
	
}
