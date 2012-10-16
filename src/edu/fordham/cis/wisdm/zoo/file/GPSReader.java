package edu.fordham.cis.wisdm.zoo.file;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import android.app.Service;
import android.content.Context;
import android.util.Log;

import edu.fordham.cis.wisdm.utils.SocketParser;
import edu.fordham.cis.wisdm.zoo.utils.Connections;

public class GPSReader{

	private Scanner mScanner = null;
	
	public GPSReader(InputStream fos){
		mScanner = new Scanner(fos);
		
	}
	
	public boolean sendData(String TAG, Connections con, String fName, Service mService){
		if(!Connections.prepare(con)){
			Log.v(TAG, "Sending failed");
			return false;
		}
		
		Log.v(TAG, "Sending data..");
		boolean good = true;
		while(mScanner.hasNext() && good){
			String line = mScanner.nextLine();
			String[] values = line.split(",");
			
			float[] floats = new float[6];
			for(int i =2; i < values.length-1;i++){
				floats[i-2] = Float.valueOf(values[i]);
			}
			
			if(!Connections.sendData(Long.valueOf(values[1]), floats, SocketParser.GPS_TUPLE_CODE))
				good = false;
		}
		mScanner.close();
		if(good){
			mService.deleteFile(fName);
			Log.v(TAG, "Data sent succesfully");
			try {
				mService.openFileOutput(fName, Context.MODE_APPEND);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else{
			Log.v(TAG, "Error in data");
		}
		Connections.disconnect();
		
		return true;
	}
}
