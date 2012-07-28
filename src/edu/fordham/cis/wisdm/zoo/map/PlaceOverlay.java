package edu.fordham.cis.wisdm.zoo.map;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import cis.fordham.edu.wisdm.utils.Operations;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.fordham.cis.wisdm.zoo.main.R;

/**
 * Used for displaying the marker and bubble over a specified point
 * @author Andrew Grosner
 * @version 1.0
 */
public class PlaceOverlay extends ItemizedOverlay<OverlayItem>{
	
	private ArrayList<PlaceItem> mOverlays = new ArrayList<PlaceItem>();
	
	private Activity act;
	
	private MapView map;
	/**
	 * Constructor
	 * @param marker
	 */
	public PlaceOverlay(Drawable marker){
		super(boundCenterBottom(marker));
	}
	
	/**
	 * Constructor with context
	 * @param c
	 * @param marker
	 */
	public PlaceOverlay(Drawable marker, Activity act, MapView map){
		super(boundCenterBottom(marker));
		this.act = act;
		this.map = map;
	}
	
	/**
	 * Adds overlay to the list
	 * @param i
	 */
	public void addOverlay(PlaceItem i){
		mOverlays.add(new PlaceItem(i));
		populate();
	}
	
	public void removeOverlay(PlaceItem overlay) {
		map.removeView(overlay.getBubble());
        mOverlays.remove(overlay);
        populate();
    }
	
	public void clear(){
		mOverlays.clear();
		populate();
	}

	
	public void addOverlayList(LinkedList<PlaceItem> places){
		for(int i =0; i < places.size(); i++){
			PlaceItem place = places.get(i);
			if(!place.isAdded()){
				addOverlay(place);
			}
		}
	}
	
	/**
	 * Changes the marker on the overlay
	 * @param index
	 * @param marker
	 */
	public void setMarker(int index, Drawable marker){
		super.boundCenterBottom(marker);
		mOverlays.get(index).setMarker(marker);
		
	}
	
	@Override
	protected OverlayItem createItem(int i){
		return mOverlays.get(i);
	}
	
	@Override
	public int size(){
		return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index){
		addBubble(index, map);
		return true;
	}
	
	/**
	 * Adds the bubble above each point
	 * @param index
	 * @param map
	 */
	public void addBubble(int index, final MapView map){
		//acquire current overlay element
		final PlaceItem i = mOverlays.get(index);
		
		ViewParent parent = (ViewParent) map.getParent();
		
		if(!i.isMenuShowing()){
		
			final View bubble = act.getLayoutInflater().inflate(R.layout.exhibitoverlay, (ViewGroup)parent, false);
			//load viewparent of the map and inflate the bubble onto the map
			//use local variable Bubble so multiple can be removed
			bubble.setOnClickListener(new OnClickListener(){
				
				@Override
				public void onClick(View v) {
					map.removeView(bubble);
					i.setMenuShowing(false);
				}
				
			});
			
			//determine layout parameters
			MapView.LayoutParams mv = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, 
					MapView.LayoutParams.WRAP_CONTENT, i.getPoint(), MapView.LayoutParams.TOP);
			FrameLayout f = (FrameLayout) bubble;
			
			//set text to show data from each point
			TextView text = (TextView) f.findViewById(R.id.name);
			text.setText(i.getTitle());
			
			TextView dist = (TextView) f.findViewById(R.id.distance);
			dist.setText(i.getSnippet()+" ft");
			
			map.addView(bubble, mv);//add to the map
			i.setMenuShowing(true);
			i.setBubble(bubble);
		} else{
			map.removeView(i.getBubble());
			i.setMenuShowing(false);
		}
	}
}

