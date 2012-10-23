package edu.fordham.cis.wisdm.zoo.file;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.grosner.mapview.Geopoint;

import android.location.Location;
import android.util.Log;

/**
 * writes GPS data to internal memory in native data types
 * @author Andrew Grosner
 * @version 1.0
 */
public class GPSWriter extends BufferedWriter{

	//the values of the GPS reading
	private String email;
	
	private long time = 0L;
	
	private static double[] coords = new double[2];
	
	private static float[] values = new float[4];
	
	private static String provider;
	/**
	 * Constructs new instance of GFile with specified name and file root
	 * @param name
	 * @param fRoot
	 * @throws IOException 
	 */
	public GPSWriter(String email, FileOutputStream stream) throws IOException{
		super(new OutputStreamWriter(stream));
		this.email = email;
	}
	
	/**
	 * Stores location in object
	 * @param l
	 */
	public static void storeLocation(Location l){
		if(l!=null){
			coords[0] = l.getLatitude();
			coords[1] = l.getLongitude();
			values[0] = (float) l.getAltitude();
			values[1] = l.getSpeed();
			values[2] = l.getAccuracy();
			values[3] = l.getBearing();
			provider = l.getProvider();
		}
	}
	
	/**
	 * Returns the line of data in human-readable format
	 */
	public String getDataLine(){
		String line = email;
		line+="," + time;
		line+="," + coords[0];
		line+="," + coords[1];
		line+="," + values[0];
		line+="," + values[1];
		line+="," + values[2];
		line+="," + values[3];
		line+="," + provider+"\n";
		return line;
	}
	
	public static void printDataLine(String TAG, float[] values, long time, byte tuple){
		
		String line = tuple + ", " + time + ", ";
		for(int i =0; i < values.length; i++){
			line+=values[i]+", ";
		}
		Log.v(TAG, line);
	}
	
	/**
	 * Prints out the line in human readable format
	 * @param TAG
	 */
	public void getDataLine(String TAG){
		Log.d(TAG, getDataLine());
	}
	
	public Geopoint getGeopoint(){
		return new Geopoint(coords[1],coords[0], "");
	}
	
	
	/**
	 * Writes a location reading to file
	 * @param TAG - android logcat filter tag
	 * @param l - location reading
	 */
	public void writeLocation(Location l, String TAG){
		storeLocation(l);
		writeLocation(TAG);
	}
	
	/**
	 * Writes the stored values to file
	 */
	public void writeLocation(String TAG) throws NullPointerException{
		time = System.currentTimeMillis();
		try {
			if(email == null){
				email = "Anonymous";
			}
			write(getDataLine());
			this.flush();
			getDataLine(TAG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
