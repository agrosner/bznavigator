package edu.fordham.cis.wisdm.zoo.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

import edu.fordham.cis.wisdm.zoo.utils.IconTextListAdapter;

public class ArrayListFragment extends SherlockListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		View v = inflater.inflate(R.layout.arraylist, container, false);
		
		int[] drawables = {R.drawable.ic_action_map, R.drawable.ic_action_document, R.drawable.ic_action_tshirt, R.drawable.ic_action_ticket, R.drawable.ic_action_restaurant, R.drawable.ic_action_globe, R.drawable.ic_action_happy, R.drawable.ic_action_exit, R.drawable.ic_action_car};
		IconTextListAdapter icontextlist=  new IconTextListAdapter(this.getActivity(), R.array.splash_list, drawables);
		
		
		setListAdapter(icontextlist);
		return v;
	}
	
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
      
    }

    
}
