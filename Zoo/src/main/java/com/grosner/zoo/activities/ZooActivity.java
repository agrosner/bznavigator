package com.grosner.zoo.activities;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;

import com.grosner.zoo.FragmentUtils;
import com.grosner.zoo.fragments.MenuFragment;
import com.grosner.zoo.fragments.AmenitiesFragment;
import com.grosner.zoo.PlaceController;
import com.grosner.zoo.fragments.PlaceFragment;
import com.grosner.zoo.R;
import com.grosner.zoo.singletons.ExhibitManager;
import com.grosner.zoo.utils.Operations;
import com.grosner.zoo.utils.ZooDialog;
import com.grosner.zoo.fragments.MapViewFragment;
import com.grosner.zoo.markers.PlaceMarker;

/**
 * Class displays the main menu that will switch between different fragments
 * @author Andrew Grosner
 *
 */
public class ZooActivity extends FragmentActivity implements OnClickListener {

	protected static final String TAG = "SlidingScreenActivity";

	/**
	 * The currently selected placefragment
	 */
	private WeakReference<PlaceFragment> mCurrentPlaceFragment = new WeakReference<PlaceFragment>(null);

	private boolean isLargeScreen = false;

    private DrawerLayout mDrawer;

    private ActionBarDrawerToggle mToggle;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_slide_splash);
        getActionBar().show();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(true);
        setTitle("Bronx Zoo");
		
		DisplayMetrics display = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(display);
    	int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                
    	if(screensize >= Configuration.SCREENLAYOUT_SIZE_LARGE)	isLargeScreen = true;
    	
    	if(isLargeScreen){
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	} else{
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	}
		
		//cancel button used for both dialogs
		OnClickListener cancel = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		};
			
		LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//	if network error message
		if(connect.getActiveNetworkInfo()==null || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			final ZooDialog gpsInternet = new ZooDialog(this);
			
			
			gpsInternet.setNegativeButton("Quit", cancel);
			gpsInternet.setTitle("Please Turn on GPS and Internet");
			gpsInternet.setMessage("Please navigate to settings and make sure GPS and Internet is turned on for the full experience.");
			gpsInternet.setPositiveButton("Settings", new OnClickListener(){
				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);	
					finish();
			}
				
			});
			gpsInternet.setNeutralButton("Continue Anyways", new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					gpsInternet.dismiss();
				}
			});
				
			gpsInternet.show();
		}


        Fragment fragment = FragmentUtils.getFragment(MenuFragment.class);
        FragmentUtils.replaceFragment(this, fragment, false, R.id.MenuView, "MenuFragment");

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_drawer, R.string.open, R.string.close);

        Fragment map = FragmentUtils.getFragment(MapViewFragment.class);
        //mContent = new WeakReference<Fragment>(map);
        FragmentUtils.replaceFragment(this, map, false, R.id.ContentView, "MapViewFragment");

        Fragment amentities = FragmentUtils.getFragment(AmenitiesFragment.class);
        FragmentUtils.replaceFragment(this, amentities, false, R.id.AmenitiesView, getString(R.string.fragment_amenities));
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if(!mToggle.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    //toggle();
            }
            return super.onOptionsItemSelected(item);
        } else{
            return true;
        }
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }

    /**
	 * Switches between fragments or mapview showing/notshowing
	 * @param fragment
	 */
	public void switchContent(final Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
			.replace(R.id.ContentView, fragment).commit();
        if(fragment instanceof PlaceFragment)
			mCurrentPlaceFragment = new WeakReference<PlaceFragment>((PlaceFragment) fragment);
		//getSlidingMenu().showContent();
        mDrawer.closeDrawers();
	}
	
	/**
	 *	Once the currentlocationmanager is instantiated, we perform an action here
	 */
	public void notifyLocationSet(){
        AmenitiesFragment amenitiesFragment = (AmenitiesFragment) getSupportFragmentManager().findFragmentByTag("AmenitiesFragment");
        if(amenitiesFragment!=null){
		    amenitiesFragment.setCheckboxes(true);
        }
	}

    private void onMenuItemClickAbout(MenuItem item){
        ZooDialog message = new ZooDialog(this);
        message.setTitle("About:");
        message.setMessage("\nDeveloper: Andrew Grosner\n\t\t\t\t  agrosner@fordham.edu\n" +
                "\nAssistant Developer: Isaac Ronan\n"
                + "\nWireless Sensor Data Mining (WISDM)"
                + "\nSpecial Thanks to: Fordham University");
        message.setNeutralButton("Ok", null);
        message.show();
    }

    private void onMenuItemClickSettings(MenuItem item){
        //startActivity(new Intent(this, PrefActivity.class));
    }

	public PlaceFragment getCurrentPlaceFragment(){
		return mCurrentPlaceFragment.get();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

    public DrawerLayout getDrawer() {
        return mDrawer;
    }

    public ActionBarDrawerToggle getToggle() {
        return mToggle;
    }
}
