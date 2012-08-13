package edu.fordham.cis.wisdm.zoo.map;

/**
 * Coordinate class
 * @author Andrew Grosner
 * @version 1.0
 */
public class Coordinate {

	private double latitude;
	
	private double longitude;
	
	public Coordinate(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}
	
	public int getLatitude1E6() {
		return (int)(latitude*1E6);
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public int getLongitude1E6() {
		return (int)(longitude*1E6);
	}


	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
}
