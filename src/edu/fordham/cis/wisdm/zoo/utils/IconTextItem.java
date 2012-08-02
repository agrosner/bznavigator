package edu.fordham.cis.wisdm.zoo.utils;

import edu.fordham.cis.wisdm.zoo.main.R;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IconTextItem extends LinearLayout{

	private Drawable image = null;
	
	private TextView text = null;
	
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
