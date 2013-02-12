package edu.fordham.cis.wisdm.zoo.main.places;

import cis.fordham.edu.wisdm.utils.Operations;
import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenController;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Handles displaying layout for displaying information about a place including name, distance, and icon.
 * @author Andrew Grosner
 *
 */
public class PlaceWidget extends RelativeLayout {

	private RelativeLayout exhibitItem;
	
	private PlaceItem mPlace;
	
	public PlaceWidget(Context con, LayoutInflater inflater, boolean wrap){
		super(con);
		
		exhibitItem = (RelativeLayout) inflater.inflate(R.layout.exhibititem, null, false);
		exhibitItem.setClickable(true);
		
		//if request to not fill, will request smaller size
		if(wrap && SplashScreenActivity.isLargeScreen)
			exhibitItem.setLayoutParams(new LayoutParams(SplashScreenController.SCREEN_WIDTH/4, LayoutParams.WRAP_CONTENT));
		addView(exhibitItem);	
	}
	
	public PlaceWidget title(String title){
		((TextView) exhibitItem.findViewById(R.id.title))
			.setText(title);
		return this;
	}
	
	public PlaceWidget distance(String distance){
		if(!distance.equals("")){
			TextView distText = (TextView) exhibitItem.findViewById(R.id.distancetext);
			distText.setText(distance);
		}
		return this;
	}
	
	public PlaceWidget drawablePath(Activity act, String drawablePath){
		if(!drawablePath.equals("0")){
			ImageView image = (ImageView) exhibitItem.findViewById(R.id.image);
			int drawableId = act.getResources().getIdentifier(drawablePath, "drawable", act.getPackageName());
			image.setImageResource(drawableId);
		}
		return this;
	}
	
	public PlaceWidget place(PlaceItem place){
		mPlace = place;
		return this;
	}
	
	public PlaceItem getPlace(){
		return mPlace;
	}
}
