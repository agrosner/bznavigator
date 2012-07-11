package edu.fordham.cis.wisdm.zoo.main;

import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;


public class SplashScreenActivity extends SherlockFragmentActivity implements OnMenuItemClickListener, OnClickListener{

	private ImageButton home;
	
	private static ArrayListFragment list;
	private static RestroomsFragment restroom;
	
	private static FragmentTransaction mTransaction;
	
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		
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
    	
		
		// Create the list fragment and add it as our sole content.
        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
            list = new ArrayListFragment();
            mTransaction.add(android.R.id.content, list).commit();
        }
        
       /* DisplayMetrics display = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(display);
        int width = display.widthPixels;
        
        if(width>=500){
        	//this.getSupportFragmentManager().beginTransaction().add(R.layout.map, new Map());
        }*/
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
	
	
	
	public static class ArrayListFragment extends SherlockListFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
			inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
            
			setListAdapter(new ArrayAdapter<String>(getActivity(),
	                    android.R.layout.simple_list_item_1, this.getResources().getStringArray(R.array.splash_list)));
			return inflater.inflate(R.layout.splash_screen, container, false);
		}
		
		
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
          
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            if(position == 0){
            	
            } else if(position ==1){
            	mTransaction.remove(list).commit();
            	mTransaction.add(android.R.id.content, restroom).commit();
            } else if(position == 2){
            	this.getActivity().startActivity(new Intent(this.getActivity(), Map.class));
            } else if (position == 3){
            	
            }
        }
    }



	@Override
	public void onClick(View v) {
		if(v.getId() == 1){
			if(!list.isVisible() && restroom.isVisible()){
				mTransaction.remove(restroom).commit();
				mTransaction.add(android.R.id.content, list).commit();
			}
		}
		
	}



	
}
