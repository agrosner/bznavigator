package edu.fordham.cis.wisdm.zoo.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;

import edu.fordham.cis.wisdm.zoo.utils.IconTextListAdapter;

public class ArrayListFragment extends SherlockListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		View v = inflater.inflate(R.layout.arraylist, container, false);
		
		int[] drawables = {R.drawable.map,		R.drawable.news, 
						   R.drawable.shop,		R.drawable.special, 
						   R.drawable.food,		R.drawable.exhibit, 
						   R.drawable.bathroom, R.drawable.fordham, 
						   R.drawable.car,		R.drawable.admin};
		
		IconTextListAdapter icontextlist=  new IconTextListAdapter(this.getActivity(), R.array.splash_list, drawables);
		setListAdapter(icontextlist);
		return v;
	}
}
