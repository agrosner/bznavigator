package edu.fordham.cis.wisdm.zoo.main;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.Operations;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;

import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragmentList;
import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.map.MapUtils;
import edu.fordham.cis.wisdm.zoo.utils.map.MapViewFragment;

/**
 * Class displays the main menu that will switch between different fragments
 * @author Andrew Grosner
 *
 */
public class SlidingScreenActivity extends SlidingFragmentActivity implements SearchPerformListener, TextWatcher, OnClickListener, OnMenuItemClickListener, OnMapClickListener {

	private Fragment mContent = null;
	
	private PlaceFragmentList mCurrentPlaceFragment = null;
	
	public SlidingScreenList mList = null;
	
	/**
	 * the popup list of exhibits that shows up when a user searches for an exhibit
	 */
	private LinearLayout searchList;
	
	/**
	 * the searchbar widget
	 */
	private MenuItemSearchAction searchItem;
	
	private MenuItem followItem;
	
	private MenuItem parkItem;
	
	private boolean isParked = false;
	
	private Connections mUser = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Bronx Zoo");
		setContentView(R.layout.activity_slide_splash);
		
		LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//gives user an option to accept terms or leave the app
		final HoloAlertDialogBuilder termsDialogBuilder = new HoloAlertDialogBuilder(this);
		
		//cancel button used for both dialogs
		DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		};
				
		termsDialogBuilder.setNegativeButton("I do not accept", cancel);
		
		//terms and conditions
		termsDialogBuilder.setTitle("Terms and Conditions");
		termsDialogBuilder.setMessage("Terms and conditions go here");
		termsDialogBuilder.setPositiveButton("I Accept", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				termsDialogBuilder.create().dismiss();
			}
							
		});
		
		termsDialogBuilder.setCancelable(false).create().show();
		
		//	if network error message
		if(connect.getActiveNetworkInfo()==null || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			final HoloAlertDialogBuilder gpsInternetDialogBuilder = new HoloAlertDialogBuilder(this);
				
			gpsInternetDialogBuilder.setNegativeButton("Quit", cancel);
			gpsInternetDialogBuilder.setTitle("Please Turn on GPS and Internet");
			gpsInternetDialogBuilder.setMessage("Please navigate to settings and make sure GPS and Internet is turned on for the full experience.");
			gpsInternetDialogBuilder.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);	
					finish();
			}
				
			});
			gpsInternetDialogBuilder.setNeutralButton("Continue Anyways", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					gpsInternetDialogBuilder.create().dismiss();
					init();
						
				}
			});
				
			gpsInternetDialogBuilder.create().show();
		} else{
			init();
		}
		
		mList = new SlidingScreenList();
		
		if(findViewById(R.id.menu) == null){
			setBehindContentView(R.layout.activity_slide_menu);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.menu, mList).commit();
	
		if(mContent ==null)	{
			mContent = mList.getSelectedFragment(this, 0);
			
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.map_content, mContent).commit();
		
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);

	}
	
	/**
	 * Begins the location update service and acquiring login credentials
	 */
	public void init(){
		mUser = (Connections) getIntent().getExtras().getSerializable("user");
		Intent i = new Intent(this, LocationUpdateService.class);
		i.putExtra("user", mUser);
		startService(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		searchList = (LinearLayout) findViewById(R.id.SearchList);
		
		//adds the searchbar to the actionbar
		searchItem = new MenuItemSearchAction(this, menu, this, getResources().getDrawable(R.drawable.ic_action_search), this, searchList);
		searchItem.setTextColor(getResources().getColor(R.color.forestgreen));
		searchItem.getMenuItem().setOnMenuItemClickListener(this);
		searchItem.setId(0);
	
		getSupportMenuInflater().inflate(R.menu.activity_sliding_screen, menu);
		
		followItem = menu.findItem(R.id.follow).setOnMenuItemClickListener(this);
		parkItem = menu.findItem(R.id.park).setOnMenuItemClickListener(this);
		
		menu.findItem(R.id.about).setOnMenuItemClickListener(this);
		menu.findItem(R.id.settings).setOnMenuItemClickListener(this);
		
		mList.getMapFragment().onMenuItemCreated(menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Switches between fragments or mapview showing/notshowing
	 * @param fragment
	 */
	public void switchContent(final Fragment fragment) {
		if(mContent instanceof MapViewFragment){
			Operations.removeView(mContent.getView());
		}
		if(fragment instanceof MapViewFragment){
			Operations.addView(fragment.getView());
		} else{
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_content, fragment).commit();
			mCurrentPlaceFragment = (PlaceFragmentList) fragment;
		}
		mContent = fragment;
		getSlidingMenu().showContent();
	}

	@Override
	public void onClick(View v) {
		
	}
	
	@Override
	public void onBackPressed(){
		if(!getSlidingMenu().isMenuShowing()){
			getSlidingMenu().toggle();
		} 
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch(item.getItemId()){
		case R.id.settings:
			startActivity(new Intent(this, PrefActivity.class));
			return true;
		case R.id.about:
			HoloAlertDialogBuilder message = new HoloAlertDialogBuilder(this);
			message.setTitle("About:");
			message.setMessage("\nDeveloper: Andrew Grosner\n\t\t\t\t  agrosner@fordham.edu\n" +
					"\nAssistant Developer: Isaac Ronan\n" 
					+ "\nWireless Sensor Data Mining (WISDM)"
					+ "\nSpecial Thanks to: Fordham University");
			message.setNeutralButton("Ok", null);
			message.create().show();
			return true;
		case 0:
			mList.switchToMap();
			return true;
		case R.id.follow:
			mList.switchToMap();
			mList.getMapFragment().getMap().setOnMapClickListener(this);
			mList.getMapFragment().toggleFollow(item);
			return true;
		case R.id.park:
			mList.switchToMap();
			if(mList.getMapFragment().isParked()){
				//bring up menu
				final HoloAlertDialogBuilder confirm = new HoloAlertDialogBuilder(this);
				confirm.setTitle("Parking Location Options");
				confirm.setMessage("Saves a spot on the map for reference to where you may have parked today.");
				confirm.setPositiveButton("Delete Location", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mList.getMapFragment().removeParking(parkItem);
					}
					
				});
				confirm.setNeutralButton("Reset", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mList.getMapFragment().addParking(parkItem);
					}
				});
				
				confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						confirm.create().dismiss();
					}
				});
				
				confirm.create().show();
				
				
			} else{
				if(mList.getMapFragment().getManager().getLastKnownLocation()!=null){
					mList.getMapFragment().addParking(parkItem);
				} else{
					MessageBuilder.showToast("Saving Parking Location Not Available When GPS is Not Found", this);
				}
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void performSearch(String query) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapClick(LatLng point) {
		if(mList.getMapFragment().isTracking())
			mList.getMapFragment().toggleFollow(followItem);
	}	
	
	public MenuItem getParkingIcon(){
		return parkItem;
	}
	
	public PlaceFragmentList getCurrentPlaceFragment(){
		return mCurrentPlaceFragment;
	}
	
}
