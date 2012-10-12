package edu.fordham.cis.wisdm.zoo.utils;

import java.util.LinkedList;

import com.nutiteq.components.Label;
import com.nutiteq.components.Place;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.wrappers.Image;

public class PlaceItem extends Place{

	private String distance;
	
	private String drawablePath;
	
	public PlaceItem(WgsPoint point, int id, Label label, String distance, Image icon) {
		super(id, label, icon, point);
		
		this.distance = distance;
	}

	/**
	 * Returns an array representation of a linkedlist
	 * @param places
	 * @return
	 */
	public static PlaceItem[] getArray(LinkedList<PlaceItem> places){
		return places.toArray(new PlaceItem[places.size()]);
	}
	
	public String getDistance(){
		return distance;
	}

	public void setDistance(String dist){
		distance = dist;
	}
	public String getDrawablePath() {
		return drawablePath;
	}

	public void setDrawablePath(String drawablePath) {
		this.drawablePath = drawablePath;
	}
}
