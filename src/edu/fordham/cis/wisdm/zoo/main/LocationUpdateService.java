package edu.fordham.cis.wisdm.zoo.main;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import edu.fordham.cis.wisdm.zoo.file.GFile;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * This service manages location and GPS. 
 * @author Andrew Grosner
 * @version 1.0
 */
public class LocationUpdateService extends Service implements LocationListener{

	public static String TAG = "Location";
	
	//manages location updates
	private LocationManager lManager = null;
	
	//GPS update rate
	private long GPSUpdate = 20000;
	
	//GPS file objects
	private GFile[]	 GPS = new GFile[2];
		
	//root dir for the files
	private File root = new File(Environment.getExternalStorageDirectory(), ".zt");
	
	//Timer that writes GPS data to file
	private Timer timer;
	
	//email string
	private String email;
	
	private PowerManager pManager;
	private PowerManager.WakeLock wLock;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		//establish wakelock to keep CPU running while user is logged in
		pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wLock = pManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myTag");
		wLock.acquire();
				
	}
	
	@Override
	public void onStart(Intent i, int startid){
		super.onStart(i, startid);
		
		email = i.getStringExtra("email");
		
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		startLocation();
		
		if(!openFiles()){
			Log.e(TAG, "Root cannot write");
			Map.displayMessage(this, "Root cannot write");
			stopLocation();
			stopSelf();
		}
			
		startTimer();
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		stopLocation();
		stopTimer();
		closeFiles();
		wLock.release();
		stopSelf();
	}
	
	private void startLocation(){
		if (lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSUpdate, 0, this);
		} else if (lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPSUpdate, 0, this);
		} else{
			Log.v(TAG, "Locational services are not available");
		}
	}
	
	private void stopLocation(){
		lManager.removeUpdates(this);
	}
	
	/**
	 * Initializes and opens files for writing
	 */
	private boolean openFiles(){
		root.mkdirs();
		if(!root.canWrite())
			return false;
		try {
			GPS[0] = new GFile("gps.g", root);
			GPS[1] = new GFile("gps2.g", root);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Closes files
	 */
	private void closeFiles(){
		try {
			GPS[0].close();
			GPS[1].close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException n){
			n.printStackTrace();
		}
	}
	
	private void startTimer(){
		timer = new Timer("Location Writer");
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				try{
					GPS[0].writeLocation();
				} catch (NullPointerException n){
					n.printStackTrace();
				}
			}
			
		}, GPSUpdate, GPSUpdate);
		
		//GPS[0].storeLocation(lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
	}
	
	private void stopTimer(){
		try{
			timer.cancel();
		} catch(Exception n){
			
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		GPS[0].storeLocation(location);
	}
	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
