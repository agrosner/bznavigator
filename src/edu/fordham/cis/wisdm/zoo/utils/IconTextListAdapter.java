package edu.fordham.cis.wisdm.zoo.utils;

import java.util.ArrayList;
import java.util.List;

import edu.fordham.cis.wisdm.zoo.main.R;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Adapter that uses IconTextItems and inflates them for use as adapter in ListFragment
 * @author Andrew Grosner
 * @version 1.0
 */
public class IconTextListAdapter extends BaseAdapter {

	private List<IconTextItem> mItems = new ArrayList<IconTextItem>();
	
	public IconTextListAdapter(FragmentActivity act, int arrayId, int...drawables){
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
	
	private class IconTextItem extends LinearLayout{

		private Drawable image = null;
		
		private TextView text = null;
		
		@SuppressWarnings("deprecation")
		public IconTextItem(Activity act, String title, int resId){
			super(act.getApplicationContext());
			
			LinearLayout layout = (LinearLayout) act.getLayoutInflater().inflate(R.layout.icontextlistitem, null, false);
			
			image = act.getResources().getDrawable(resId);
			ImageView im = (ImageView) layout.findViewById(R.id.image);
			im.setBackgroundDrawable(image);
			
			
			text = (TextView) layout.findViewById(R.id.name);
			text.setText(title);
			
			addView(layout);
		}
		
		
	}

}
