package com.grosner.zoo.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grosner.zoo.R;

/**
 * Adapter that uses IconTextItems and inflates them for use as adapter in ListFragment
 * @author Andrew Grosner
 * @version 1.0
 */
public class SlidingScreenListAdapter extends BaseAdapter {

	private List<IconTextItem> mItems = new ArrayList<IconTextItem>();
	
	public SlidingScreenListAdapter(Context context, int arrayId, int drawables){
		String[] items = context.getResources().getStringArray(arrayId);
        TypedArray array = context.getResources().obtainTypedArray(drawables);
		for(int i =0; i < items.length; i++){
			mItems.add(new IconTextItem(context, items[i], array.getResourceId(i, 0)));
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
	
	private class IconTextItem extends LinearLayout{

		private Drawable image = null;
		
		private TextView text = null;
		
		@SuppressWarnings("deprecation")
		public IconTextItem(Context context, String title, int resId){
			super(context);
			
			LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.icontextlistitem, null, false);
			
			image = context.getResources().getDrawable(resId);
			ImageView im = (ImageView) layout.findViewById(R.id.image);
			im.setBackgroundDrawable(image);
			
			
			text = (TextView) layout.findViewById(R.id.name);
			text.setText(title);
			
			addView(layout);
		}
		
		
	}

}
