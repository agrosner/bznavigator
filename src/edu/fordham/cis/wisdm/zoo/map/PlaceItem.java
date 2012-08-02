package edu.fordham.cis.wisdm.zoo.map;

import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * Adds menu showing functionality
 * @author Andrew Grosner
 * @version 1.0
 */
public class PlaceItem extends OverlayItem{

	private boolean isMenuShowing = false;
	
	private boolean isAdded = false;
	
	private View bubble = null;
	
	private String drawablePath = null;
	
	public PlaceItem(GeoPoint point, String title, String snippet, String drawablePath) {
		super(point, title, snippet);
		this.setDrawablePath(drawablePath);
	}

	/**
	 * Deep copy constructor creates exact copy of another PlaceItem object
	 * @param i
	 */
	public PlaceItem(PlaceItem i) {
		super(new GeoPoint(i.getPoint().getLatitudeE6(), i.getPoint().getLongitudeE6()), new String(i.getTitle()), new String(i.getSnippet()));
	}

	public boolean isMenuShowing() {
		return isMenuShowing;
	}

	public void setMenuShowing(boolean isMenuShowing) {
		this.isMenuShowing = isMenuShowing;
	}

	public View getBubble() {
		return bubble;
	}

	public void setBubble(View bubble) {
		this.bubble = bubble;
	}

	public boolean isAdded() {
		return isAdded;
	}

	public void setAdded(boolean isAdded) {
		this.isAdded = isAdded;
	}

	public String getDrawablePath() {
		return drawablePath;
	}

	public void setDrawablePath(String drawablePath) {
		this.drawablePath = drawablePath;
	}

	
	
}
