package edu.fordham.cis.wisdm.zoo.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

public class ArrayListFragment extends SherlockListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		View v = inflater.inflate(R.layout.arraylist, container, false);
		setListAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, this.getResources().getStringArray(R.array.splash_list)));
		return v;
	}
	
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
      
    }

    
}
