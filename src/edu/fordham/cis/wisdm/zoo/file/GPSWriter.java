package edu.fordham.cis.wisdm.zoo.file;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.grosner.mapview.Geopoint;

import android.location.Location;
import android.util.Log;

/**
 * writes GPS data to internal memory in native data types
 * @author Andrew Grosner
 * @version 1.0
 */
public class GPSWriter extends DataOutputStream{

	//the values of the GPS reading
	private String email;
	
	private long time = 0L;
	
	private double[] coords = new double[2];
	
	private float[] values = new float[4];
	
	private String provider;
	/**
	 * Constructs new instance of GFile with specified name and file root
	 * @param name
	 * @param fRoot
	 * @throws IOException 
	 */
	public GPSWriter(String email, FileOutputStream stream) throws IOException{
		super(stream);
		this.email = email;
	}
	
	/**
	 * Stores location in object
	 * @param l
	 */
	public void storeLocation(Location l){
		coords[0] = l.getLatitude();
		coords[1] = l.getLongitude();
		values[0] = l.getAccuracy();
		values[1] = l.getSpeed();
		values[2] = l.getAccuracy();
		values[3] = l.getBearing();
		provider = l.getProvider();
	}
	
	/**
	 * Prints out the line of data in human-readable format
	 */
	public void getDataLine(String TAG){
		String line = email;
		line+="," + time;
		line+="," + coords[0];
		line+="," + coords[1];
		line+="," + values[0];
		line+="," + values[1];
		line+="," + values[2];
		line+="," + values[3];
		line+="," + provider;
		Log.d(TAG, line);
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
			
			this.writeByte(email.length());
			this.writeChars(email);
			this.writeLong(time);
			this.writeDouble(coords[0]);
			this.writeDouble(coords[1]);
			for(int i =0; i < values.length; i++){
				this.writeFloat(values[i]);
			}
			
			if(provider == null){
				provider = "unknown";
			}
			this.writeByte(provider.length());
			this.writeChars(provider);
			this.writeChar('\n');
			this.flush();
			getDataLine(TAG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
