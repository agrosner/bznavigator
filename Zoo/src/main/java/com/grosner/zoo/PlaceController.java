package com.grosner.zoo;

import android.location.Location;

import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.markers.PlaceMarker;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Class provides operations on creating and manipulating place data
 * @author Andrew Grosner
 *
 */
public class PlaceController {
	
	/**
	 * Meters to FT conversion
	 */
	public static double METERS_TO_FT = 3.28084;
	
	public static double MILES_TO_FT = 5280;
	
	public static final String MILES = " Miles";
	/**
	 *Calculates distance between a geopoint and two coordinate strings and returns human-readable string
	 * @param currentLoc
	 * @return "<?>ft"
	 */
	public static String calculateDistanceString(Location currentLoc, Location next){
		if(currentLoc!=null){
			double distance = currentLoc.distanceTo(next)*METERS_TO_FT;
			distance = Math.round(distance);
			if(distance>=500){
				distance/=5280;
				return String.format("%.2f" + MILES, distance);
			} else	return String.valueOf(distance).replace(".0", "") + " ft";
		} else{
			return "0";
		}
	} 
	
	/**
	 * Reorders the list of elements by distance to the place
	 * @param currentLoc
	 * @param points
	 */
	public static void reOrderByDistance(List<PlaceObject> points, final Location currentLoc){
		Collections.sort(points, new Comparator<PlaceObject>(){

			@Override
			public int compare(PlaceObject lhs, PlaceObject rhs) {
				if(currentLoc!=null)
					return Float.valueOf(lhs.getLocation().distanceTo(currentLoc)).compareTo(rhs.getLocation().distanceTo(currentLoc));
				else return 0;
			}
				
		});
	}
	
	
}
