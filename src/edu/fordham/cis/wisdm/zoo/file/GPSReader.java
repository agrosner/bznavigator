package edu.fordham.cis.wisdm.zoo.file;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;

import edu.fordham.cis.wisdm.zoo.utils.Connections;

import android.util.Log;

/**
 * Reads from GPS data file and sends data to the server for processing
 * @author Andrew Grosner
 * @version 1.0
 */
public class GPSReader extends DataInputStream{
	
	/**
	 * GPS record identifier
	 */
	private static final byte GPS_TUPLE = 90;
	
	/**
	 * Socket connection object
	 */
	private static Socket sock = null;
	

	public GPSReader(FileInputStream fis) throws FileNotFoundException {
		super(fis);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * reads from file and sends it to the server
	 * @throws IOException 
	 * @param TAG - log tag to send information to
	 */
	public void sendData(String TAG, Connections connection) throws IOException{
		
		if(!Connections.prepare(connection)){
			Log.d(TAG, "Could not connect to server!");
			return;
		}
		Log.v(TAG, "Sending data...");
		boolean more = true;
		while(more){
			try{
			
				//read how long email is
				byte emailLength = this.readByte();
		
				//init and read into char[] the email
				char[] email = new char[emailLength];
				for(int i =0; i < email.length; i++){
					email[i] = readChar();
				}
				long time = readLong();
				
				//read time
				//System.out.print(time);
			
				//	read in coordinates
				double latitude = readDouble();
				double longitude = readDouble();
				//System.out.println(latitude + "," + longitude);
				
				//debug line will show what is read from file
				//String line = String.valueOf(email) +"," + time + "," + latitude + "," + longitude + ",";
				
				float[] values = new float[6];
				values[0] = (float) latitude;
				values[1] = (float) longitude;
				for(int i =2; i < 4; i++){
					values[i] = readFloat();
					//line+=values[i] + ",";
				}
				
				byte providerLength = readByte();
				char[] provider = new char[providerLength];
				for(int i =0; i < provider.length;i++){
					provider[i] = readChar();
				}
				
				//line+=String.valueOf(provider) + ",";
				
				char end = readChar();
				if(end != '\n'){
					Log.d(TAG, "Failure");
					more = false;
					break;
				}
		
				//TODO: add streaming function here
				Connections.sendData(time, values, GPS_TUPLE);
				
			} catch(EOFException e){
				more = false;
				break;
			}
		}
		
		Connections.disconnect();
		
	}
	
}
