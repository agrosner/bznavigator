package com.grosner.zoo.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.R;

/**
 * Adapter that uses IconTextItems and inflates them for use as adapter in ListFragment
 * @author Andrew Grosner
 * @version 1.0
 */
public class SlidingScreenListAdapter extends BaseAdapter {


    private TypedArray mTitles;
    private TypedArray mDrawableArray;
	
	public SlidingScreenListAdapter(Context context, int arrayId, int drawables){
		mTitles = context.getResources().obtainTypedArray(arrayId);
        mDrawableArray = context.getResources().obtainTypedArray(drawables);
	}
	
	
	@Override
	public int getCount() {
		return mTitles.length();
	}

	@Override
	public String getItem(int position) {
		return mTitles.getString(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        IconTextItem iconTextItem;
        if(convertView==null){
            iconTextItem = new IconTextItem(parent.getContext());
        } else{
            iconTextItem = (IconTextItem) convertView;
        }

        iconTextItem.setItem(getItem(position), mDrawableArray.getResourceId(position, 0));

		return iconTextItem;
	}
	
	private class IconTextItem extends LinearLayout{

		@SResource private ImageView image;
		
		@SResource private TextView name;
		
		@SuppressWarnings("deprecation")
		public IconTextItem(Context context){
			super(context);
            SmartInflater.inflate(this, R.layout.icontextlistitem);
		}

        public void setItem(String title, int resId){
            name.setText(title);
            image.setImageResource(resId);
        }
		
	}

}
