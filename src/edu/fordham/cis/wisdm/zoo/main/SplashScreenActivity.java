package edu.fordham.cis.wisdm.zoo.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.Operations;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.maps.MapView;


public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener, OnItemClickListener{

	private ImageButton home;
	
	private static ArrayListFragment list;
	private static RestroomsFragment restroom;
	private static MapView map;
	
	private static FragmentTransaction mTransaction;
	
	private static RelativeLayout splashScreen;
	
	//determine whether tablet or not to optimize screen real estate 
	private boolean isLargeScreen = false;
	
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		setContentView(R.layout.splash_screen);
		
		mTransaction = this.getSupportFragmentManager().beginTransaction();
		
		ActionBar mAction = this.getSupportActionBar();
		mAction.setDisplayHomeAsUpEnabled(false);
		mAction.setDisplayShowHomeEnabled(false);
		mAction.setDisplayShowTitleEnabled(false);
		
		home = new ImageButton(this);
		home.setId(1);
		home.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		home.setImageDrawable(this.getResources().getDrawable(R.drawable.list));
		mAction.setCustomView(home);
		mAction.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		home.setOnClickListener(this);
		
		restroom = new RestroomsFragment();
		list = (ArrayListFragment) this.getSupportFragmentManager().findFragmentById(R.id.listfragment);
		list.getListView().setOnItemClickListener(this);
		
		splashScreen = (RelativeLayout) findViewById(R.id.splashrel);
		
        //init map objects
    	map = (MapView) findViewById(R.id.Map);
        
        DisplayMetrics display = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(display);
        int width = display.widthPixels;
        
        if(width>=1000){
        	LayoutParams lp = new LayoutParams((width/4), LayoutParams.FILL_PARENT);
        	list.getView().setLayoutParams(lp);
        	isLargeScreen = true;
        	//mTransaction.add(android.R.id.content, restroom);
        } else{
        	Operations.removeView(map);
        	
        }
        
        
	}
	
	@Override
	public void onBackPressed(){
		if(!list.getView().isShown()){
			showList();
			if(map.isShown() && !isLargeScreen){
				Operations.removeView(map);
			}
		} else{
		
			//ask user whether quit or not
			HoloAlertDialogBuilder message = new HoloAlertDialogBuilder(this);
			message.setTitle("Quit?");
			message.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
				
				
			});
			message.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			message.create().show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		
		menu.add("About").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add("Settings").setOnMenuItemClickListener(this).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
		return super.onCreateOptionsMenu(menu);
	}
	

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if(item.getTitle().equals("Settings")){
			
		} else if(item.getTitle().equals("News")){
			
		}
		return false;
	}
	
	
	
	


	@Override
	public void onClick(View v) {
		if(v.getId() == 1){
			showList();
			if(map.isShown() && !isLargeScreen){
				Operations.removeView(map);
			}
		}
		
	}
	
	private void showList(){
		if(!list.getView().isShown()){
			Operations.addView(list.getView());
		} 
		if(!list.isVisible()){
			FragmentTransaction mTransaction = this.getSupportFragmentManager().beginTransaction();
			mTransaction.replace(android.R.id.content, list);
			mTransaction.addToBackStack("list").commit();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		switch(position){
		case 0:
			
			break;
		case 1:
			
			break;
		case 2:
			if(!isLargeScreen && !map.isShown() && list.isAdded()){
				Operations.addView(map);
				FragmentTransaction mTransaction = this.getSupportFragmentManager().beginTransaction();
				
				Operations.removeView(list.getView());
				
			}
			break;
			
		case 3:
			
			break;
		}
	}



	
}
