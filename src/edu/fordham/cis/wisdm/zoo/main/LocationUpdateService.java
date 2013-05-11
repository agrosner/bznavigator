package edu.fordham.cis.wisdm.zoo.main;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.maps.model.LatLng;

import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.GPSWriter;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import edu.fordham.cis.wisdm.zoo.utils.map.Polygon;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

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
	 * Stream rate (every 5 minutes)
	 */
	private long STRUpdate = 300000;
	
	/**
	 * GPS file objects
	 */
	private static GPSWriter[] files = new GPSWriter[2];
	
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
	 * The number of times a location is said to not exist within the zoo map (approx 5 mins)
	 */
	private static final int SHUTDOWN_NUMBER = 15;
	
	/**
	 * map polygon
	 */
	private Polygon mPolygon;
	
	private static boolean isStreaming = false;
	
	private NotificationManager mNotificationManager;
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.v(TAG, "Service started");
		
		//establish wakelock to keep CPU running while user is logged in
		pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wLock = pManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myTag");
		wLock.acquire();
		
		mNotificationManager = (NotificationManager) 
				getSystemService(Context.NOTIFICATION_SERVICE);
	
		try {
			readPolygonCoords();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//we want to catch crashes within the service,
		//so we perform some cleanup before service is destroyed
		final UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		UncaughtExceptionHandler appHandler = new UncaughtExceptionHandler() {

		       @Override
		       public void uncaughtException(Thread thread, Throwable ex) {
		    	   mNotificationManager.cancelAll();
		    	   wLock.release();
		    	   defaultHandler.uncaughtException(thread, ex);
		       }
		};
		Thread.setDefaultUncaughtExceptionHandler(appHandler);
		
	}
	
	/**
	 * Reads in polygon from text file
	 * @throws IOException 
	 */
	private void readPolygonCoords() throws IOException{
		Scanner file = new Scanner(this.getResources().getAssets().open("mapraw.txt"));
		LinkedList<Integer> x = new LinkedList<Integer>();
		LinkedList<Integer> y = new LinkedList<Integer>();
		
		while(file.hasNext()){
			String line = file.nextLine();
			String[] lValues = line.split(",");
			double latitude = Double.valueOf(lValues[1]);
			double longitude = Double.valueOf(lValues[0]);
			y.add((int)(latitude*1E6));
			x.add((int)(longitude*1E6));
		}
		
		int[] xs = new int[x.size()];
		int[] ys = new int[y.size()];
		for(int i =0; i < xs.length; i++){
			xs[i] = x.poll();
			ys[i] = y.poll();
		}
		
		mPolygon = new Polygon(xs, ys,xs.length);
		
		
		file.close();
		
	}
	
	@Override
	public int onStartCommand(Intent i, int flags, int startid){
		super.onStartCommand(i, flags, startid);
		
		startNotification();
		try{ 
			mConnection = (Connections) i.getExtras().getSerializable("user");
		} catch(Exception e){
			e.printStackTrace();
		}
		
		//attempt to save connection in an event of restart
		if(mConnection!=null && mConnection.getmEmail()!=null && mConnection.getmPassword()!=null){
			Preference.putString("service-email", mConnection.getmEmail());
			Preference.putString("service-pass", mConnection.getmPassword());
		} else{
			//try to recover, otherwise will take device id
			mConnection = new Connections(Preference.getString("service-email", ""), Preference.getString("service-pass", ""),
						Secure.getString(getContentResolver(), Secure.ANDROID_ID));
		}
		
		email = mConnection.getmEmail();
		
		lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		startLocation();
		
		if(!openFiles(this)){
			Log.e(TAG, "Root cannot write");
			stopLocation();
			stopNotification();
			stopSelf();
		}
			
		startTimer();
		startStream();
		
		return START_STICKY;
	}
	
	/**
	 * Starts showing notification indicating service is running
	 */
	private void startNotification(){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
		builder.setSmallIcon(R.drawable.appicon)
				.setContentTitle("BZNavigator is running")
				.setContentText("Click to view map .")
				.setOngoing(true);
		Intent click = new Intent(this, SlidingScreenActivity.class);
		click.putExtra("user", mConnection);
		click.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(SlidingScreenActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(click);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, click, 0);
		builder.setContentIntent(resultPendingIntent);
			// mId allows you to update the notification later on.
		startForeground(0, builder.build());
		mNotificationManager.notify(0, builder.build());
	}
	
	/**
	 * Removes the notification
	 */
	private void stopNotification(){
		stopForeground(true);
		mNotificationManager.cancel(0);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		stopLocation();
		stopTimer();
		stopStream();
		closeFiles();
		Log.d(TAG, "Service Stopped");
		stopNotification();
		wLock.release();
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
		if(success)GPSWriter.storeLocation(l);
	}
	
	private void startLocation(){
		Criteria cr = new Criteria();
		cr.setAccuracy(Criteria.ACCURACY_FINE);
		lManager.requestLocationUpdates(lManager.getBestProvider(cr, true), GPSUpdate, 0, this);
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
				if(outsideCount==0) stream();
			}
			
		}, 0, STRUpdate);
	}
	
	/**
	 * Streams data to the server
	 * TODO: send data to server
	 */
	private void stream(){
		isStreaming = true;
		Connections.sendData(mConnection, fName + "1.txt", this);
			
		isStreaming = false;
		Connections.sendData(mConnection, fName + "2.txt", this);
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
					LatLng g = files[0].getLatLng();
					if(/**Geopoint.isInsideMap(g)**/mPolygon.contains(g)){
						outsideCount = 0;
						if(!isStreaming)	files[0].writeLocation(TAG);
						else				files[1].writeLocation(TAG);
						Log.v(TAG, "Location in map");
					} else if(outsideCount>=SHUTDOWN_NUMBER){
						stopSelf();
						Log.v(TAG, "User outside of zoo, shutting down.");
					} else{
						outsideCount++;
						Log.v(TAG, "User outside of map");
					}
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
			GPSWriter.storeLocation(location);
	}
	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
