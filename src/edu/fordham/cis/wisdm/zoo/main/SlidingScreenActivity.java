package edu.fordham.cis.wisdm.zoo.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapsInitializer;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

import de.appetites.android.menuItemSearchAction.MenuItemSearchAction;
import de.appetites.android.menuItemSearchAction.SearchPerformListener;

import edu.fordham.cis.wisdm.zoo.utils.map.MapViewFragment;

/**
 * Class displays the main menu that will switch between different fragments
 * @author Andrew Grosner
 *
 */
public class SlidingScreenActivity extends SlidingFragmentActivity implements SearchPerformListener, TextWatcher, OnClickListener, OnMenuItemClickListener {

	private Fragment mContent = null;
	
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
	
	private boolean isTracking = false;
	
	private boolean isParked = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Bronx Zoo");
		setContentView(R.layout.activity_slide_splash);
		
		mList = new SlidingScreenList();
		
		if(findViewById(R.id.menu) == null){
			setBehindContentView(R.layout.activity_slide_menu);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		if(savedInstanceState !=null){
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		} 
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.menu, mList).commit();
	
		if(mContent ==null)	{
			mContent = mList.getSelectedFragment(this, 0);
			
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, mContent).commit();
		
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);

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
		
		followItem = menu.findItem(R.id.follow);
		parkItem = menu.findItem(R.id.park);
		
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}

	public void switchContent(final Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_content, fragment).commit();
		getSlidingMenu().showContent();
	}

	@Override
	public void onClick(View v) {
		
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
			if(!isTracking){
				
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
}
