package edu.fordham.cis.wisdm.zoo.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.grosner.mapview.Geopoint;

import edu.fordham.cis.wisdm.zoo.file.GPSReader;
import edu.fordham.cis.wisdm.zoo.file.GPSWriter;
import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

/**
 * This service manages location and GPS. 
 * @author Andrew Grosner
 * @version 1.0
 */
public class LocationUpdateService extends Service implements LocationListener{

	/**
	 * LocationUpdateService
	 */
	public static String TAG = "LocationUpdateService";
	
	/**
	 * connection object containing user info
	 */
	private Connections mConnection = null;
	
	/**
	 * manages location updates
	 */
	private LocationManager lManager = null;
	
	/**
	 * GPS update rate
	 */
	private long GPSUpdate = 20000;
	
	/**
	 * Stream rate (every 2 minutes)
	 */
	private long STRUpdate = 120000;
	
	/**
	 * GPS file objects
	 */
	private static GPSWriter[] files = new GPSWriter[2];
	
	private static GPSReader reader;
	
	
	/**
	 * Timer that writes GPS data to file
	 */
	private Timer timer;
	
	/**
	 * Timer that streams data to the server
	 */
	private Timer streamer;
	
	/**
	 * user email string
	 */
	private static String email;
	 
	/**
	 * Power manager for our wakelock
	 */
	private PowerManager pManager;
	
	/**
	 * leaves the CPU running when the screen is off
	 */
	private PowerManager.WakeLock wLock;
	
	/**
	 * file name of the gps data
	 */
	private static final String fName = "gps";
	
	/**
	 * Keeps track of times a user is outside of the zoo, will turn off GPS when SHUTDOWN_NUMBER is specified
	 */
	private int outsideCount = 0;
	
	/**
	 * The number of times a location is said to not exist within the zoo map
	 */
	private static final int SHUTDOWN_NUMBER = 6;
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.v(TAG, "Service started");
		
		//establish wakelock to keep CPU running while user is logged in
		pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wLock = pManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myTag");
		wLock.acquire();
		
				
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent i, int startid){
		super.onStart(i, startid);
		String em = null;
		String ps = null;
		try{
			em = i.getExtras().getString("email");
			ps = i.getExtras().getString("password");
		} catch(NullPointerException n){
			
		}
		if(em!=null){
			email = em;
		}
	
		mConnection = new Connections(email, ps, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
		
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		startLocation();
		
		if(!openFiles(this)){
			Log.e(TAG, "Root cannot write");
			stopLocation();
			stopSelf();
		}
			
		startTimer();
		startStream();
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		stopLocation();
		stopTimer();
		stopStream();
		closeFiles();
		wLock.release();
		Log.d(TAG, "Service Stopped");
		stopSelf();
	}
	
	/**
	 * Stores first fix from mylocation overlay
	 * @param l
	 */
	public static void storeFirstLocation(Location l, ContextWrapper wrapper){
		boolean success = false;
		if (files[0] == null){
			success = openFiles(wrapper);
		}
		if(success)files[0].storeLocation(l);
	}
	
	private void startLocation(){
		if (lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPSUpdate, 0, this);
		}
		if (lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
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
	private static boolean openFiles(ContextWrapper wrapper){
		
		try {
			files[0] = new GPSWriter(email, wrapper.openFileOutput(fName + "1.txt", Context.MODE_APPEND));
			files[1] = new GPSWriter(email,wrapper.openFileOutput(fName + "2.txt", Context.MODE_APPEND));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		if(files[0] == null || files[1]==null) return false;
		
		return true;
	}
	
	/**
	 * Streams data to the server
	 * TODO: send data to server
	 */
	private void stream(){
		try{
			reader.sendData(TAG, mConnection, "gps1.txt", this);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes files
	 */
	private void closeFiles(){
		for(int i =0; i < files.length; i++){
			try {
				files[i].close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void startStream(){
		streamer = new Timer("Data streamer");
		streamer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				try {
					reader = new GPSReader(openFileInput("gps1.txt"));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				stream();
			}
			
		}, 0, STRUpdate);
	}
	private void stopStream(){
		try{
			streamer.cancel();
		} catch(Exception e){
			
		}
	}
	
	private void startTimer(){
		timer = new Timer("Location Writer");
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				try{
					Geopoint g = files[0].getGeopoint();
					//if(Geopoint.isPointInMap(g)){
						outsideCount = 0;
						files[0].writeLocation(TAG);
					/**} else if(outsideCount>=SHUTDOWN_NUMBER){
						stopSelf();
						Log.v(TAG, "User outside of zoo, shutting down.");
					} else{
						outsideCount++;
						Log.v(TAG, "User outside of map");
					}**/
				} catch (NullPointerException n){
					n.printStackTrace();
				}
			}
			
		}, GPSUpdate, GPSUpdate);
		
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
		
			files[0].storeLocation(location);
		
	}
	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
