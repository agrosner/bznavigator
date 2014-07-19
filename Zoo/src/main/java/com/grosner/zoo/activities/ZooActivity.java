package com.grosner.zoo.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.grosner.painter.actionbar.ActionBarAlphaSlider;
import com.grosner.zoo.FragmentUtils;
import com.grosner.zoo.R;
import com.grosner.zoo.fragments.AmenitiesFragment;
import com.grosner.zoo.fragments.MapViewFragment;
import com.grosner.zoo.fragments.MenuFragment;
import com.grosner.zoo.fragments.PlaceFragment;
import com.grosner.zoo.utils.DeviceUtils;
import com.grosner.zoo.utils.ZooDialog;

import java.lang.ref.WeakReference;

/**
 * Class displays the main menu that will switch between different fragments
 * @author Andrew Grosner
 *
 */
public class ZooActivity extends FragmentActivity implements OnClickListener, DrawerLayout.DrawerListener {

	protected static final String TAG = "SlidingScreenActivity";

	/**
	 * The currently selected placefragment
	 */
	private WeakReference<PlaceFragment> mCurrentPlaceFragment = new WeakReference<PlaceFragment>(null);

	private boolean isLargeScreen = false;

    private DrawerLayout mDrawer;

    private ActionBarDrawerToggle mToggle;

    private ActionBarAlphaSlider mSlider;

    private TextView mActionBarTitleView;

    private float mDrawerOffset;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(mToggle!=null) {
            mToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_zoo);
        getActionBar().show();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(true);
        setTitle("Bronx Zoo");
		
		DisplayMetrics display = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(display);
    	int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        if(!DeviceUtils.isTablet()) {
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

        View drawer = findViewById(R.id.drawer_layout);
        if(drawer!=null && drawer instanceof DrawerLayout) {
            mDrawer = (DrawerLayout) drawer;
            mDrawer.setDrawerListener(this);
            if(!DeviceUtils.isTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_drawer, R.string.open, R.string.close);
                mSlider = new ActionBarAlphaSlider(false, getActionBar(), getResources().getColor(R.color.actionbar_color));
            }
        }

        Fragment map = FragmentUtils.getFragment(MapViewFragment.class);
        //mContent = new WeakReference<Fragment>(map);
        FragmentUtils.replaceFragment(this, map, false, R.id.ContentView, "MapViewFragment");

        Fragment amentities = FragmentUtils.getFragment(AmenitiesFragment.class);
        FragmentUtils.replaceFragment(this, amentities, false, R.id.AmenitiesView, getString(R.string.fragment_amenities));

        if(!DeviceUtils.isTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
            if (actionBarTitleId > 0) {
                mActionBarTitleView = (TextView) findViewById(actionBarTitleId);
                mActionBarTitleView.setTextColor(Color.BLACK);
            }
        }
	}

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()==0 && mDrawer.isDrawerOpen(Gravity.LEFT)){
            mDrawer.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
        //call this since the actionbar gets cached somehow
        supportInvalidateOptionsMenu();
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle==null || !mToggle.onOptionsItemSelected(item)) {
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
        if(mToggle!=null) {
            mToggle.syncState();
        }
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

    public void closeDrawers(){
        if(mDrawer!=null){
            mDrawer.closeDrawers();
        }
    }

    public void openDrawer(int gravity){
        if(mDrawer!=null){
            mDrawer.openDrawer(gravity);
        }
    }

    public float getDrawerOffset(){
        return mDrawerOffset;
    }

    public void setDrawerIndicatorEnabled(boolean enabled){
        if(mToggle!=null){
             mToggle.setDrawerIndicatorEnabled(enabled);
        }
    }

    @Override
    public void onDrawerSlide(View view, float v) {
        if(mSlider!=null) {
            mSlider.onSlide(v);
            MapViewFragment mapViewFragment = (MapViewFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_map));
            if (mapViewFragment != null) {
                mapViewFragment.onDrawerSlide(v);
            }
        }
    }

    @Override
    public void onDrawerOpened(View view) {

    }

    @Override
    public void onDrawerClosed(View view) {

    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    /**
     * Returns the title view of the actionbar
     * @return
     */
    public TextView getActionBarTitleView(){
        return mActionBarTitleView;
    }

    public void closeKeyboards() {

    }
}
