package edu.fordham.cis.wisdm.zoo.utils;

import java.util.ArrayList;
import java.util.List;

import edu.fordham.cis.wisdm.zoo.main.R;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class IconTextListAdapter extends BaseAdapter {

	private List<IconTextItem> mItems = new ArrayList<IconTextItem>();
	
	public IconTextListAdapter(FragmentActivity act, int arrayId, int[] drawables){
		String[] items = act.getResources().getStringArray(arrayId);
		for(int i =0; i < items.length; i++){
			mItems.add(new IconTextItem(act, items[i], drawables[i]));
		}
	}
	
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mItems.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mItems.get(position);
	}

}
