package edu.fordham.cis.wisdm.zoo.main;

import java.util.LinkedList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import cis.fordham.edu.wisdm.utils.Operations;

import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceFragment;
import edu.fordham.cis.wisdm.zoo.utils.IconTextItem;
import edu.fordham.cis.wisdm.zoo.utils.Places;
import edu.fordham.cis.wisdm.zoo.utils.map.MapUtils;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

/**
 * Acts as the view controller for SplashScreenActivity
 * @author agrosner
 *
 */
@SuppressWarnings("deprecation")
public class SplashScreenController {

	/**
	 * The main activity its tied to
	 */
	private SplashScreenActivity mActivity = null;
	
	/**
	 * The amenities drawer
	 */
	private SlidingDrawer mDrawer;
	
	/**
	 * Layout contained inside the drawer
	 */
	private LinearLayout mDrawerFrame;
	
	/**
	 * Holds references to the sliding drawer checkboxes for quick reference
	 */
	private RelativeLayout[] mDrawerCheckBoxes = new RelativeLayout[4];
	
	/**
	 * determine screen width for sidebar
	 */
	public static int SCREEN_WIDTH = 0;
	
	/**
	 * Called when a checkbox on screen is touched
	 */
	private OnCheckedChangeListener mCheckboxListener = new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton but, boolean isChecked) {
			LinkedList<PlaceItem> pts = null;
			String fileName = "";
			
			switch(but.getId()){
			case 0:
				pts = mActivity.restrooms;
				fileName = "restrooms.txt";
				break;
			case 1:
				pts = mActivity.gates;
				fileName = "gates.txt";
				break;
			case 2:
				pts = mActivity.parking;
				fileName = "parking.txt";
				break;
			case 3:
				pts = mActivity.shop;
				fileName = "shops.txt";
			}
			
			if(isChecked)	readInPlaces(pts, fileName, SplashScreenActivity.mTransaction);
			else			MapUtils.removeList(pts);
		}
		
	};
	
	/**
	 * Constructs the controller of this activity
	 * @param act
	 */
	public SplashScreenController(SplashScreenActivity act){
		mActivity = act;
	}
	
	/**
	 * Adds sliding drawer for persistent locations
	 */
	void addDrawerList(){
		mDrawer = (SlidingDrawer) mActivity.findViewById(R.id.drawer);
	
		mDrawerFrame = (LinearLayout) mDrawer.findViewById(R.id.drawerFrame);
		//mDrawerFrame.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, SCREEN_WIDTH/4));
		
		final LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		TextView title = new TextView(mActivity.getApplicationContext());
		title.setText("Amenities");
		title.setTextSize(20);
		mDrawerFrame.addView(title, pm);
		
		mDrawerCheckBoxes[0] = createIconCheckBox("Restrooms", R.drawable.bathroom, 0);
		mDrawerCheckBoxes[1] = createIconCheckBox("Gates", R.drawable.fordham, 1);
		mDrawerCheckBoxes[2] = createIconCheckBox("Parking Lots", R.drawable.fordhamparking, 2);
		mDrawerCheckBoxes[3] = createIconCheckBox("Shops", R.drawable.shop, 3);
		
		if(mActivity.misc.isEmpty())	PlaceController.readInData(mActivity, mActivity.onInfoClickedListener, mActivity.misc, "misc.txt");
		
		mDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener(){
	   
		@SuppressWarnings("unchecked")
		@Override
		public void onDrawerOpened() {
			SplashScreenActivity.mTransaction = mActivity.getSupportFragmentManager().beginTransaction();
			removeFrag(SplashScreenActivity.mTransaction, true).commit();
			SplashScreenActivity.setCurrentFragment(Places.MAP);
			
			ImageView icon = (ImageView) mDrawer.getHandle();
			icon.setImageResource(R.drawable.ic_action_arrow_right);
			mDrawerFrame.removeAllViews();
			for(int i = 0; i < mDrawerCheckBoxes.length; i++){
				mDrawerFrame.addView(mDrawerCheckBoxes[i]);
			}
			PlaceController.reCalculateDistance(SplashScreenActivity.myLocation, mActivity.misc);
			PlaceController.readInDataIntoList(SplashScreenActivity.myLocation, mActivity, mDrawerFrame, mActivity.misc, new OnClickListener(){

				@Override
				public void onClick(View v) {
					int id = v.getId();
					mDrawer.animateClose();
					PlaceItem place = mActivity.misc.get(id-1);
					LinkedList<PlaceItem> places = new LinkedList<PlaceItem>();
					places.add(place);
					mActivity.showMap(SplashScreenActivity.mTransaction, SplashScreenActivity.list.getView(), places);
					MapUtils.moveRelativeToCurrentLocation(place.getPoint(), mActivity.getMap());
				}
				
			}, true);
			
		}
	    	 
	    });
	    mDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener(){
	    	
	    @Override
	    public void onDrawerClosed() {
	    	ImageView icon = (ImageView) mDrawer.getHandle();
	    	icon.setImageResource(R.drawable.ic_action_arrow_left);
		}
	    
	    });
		
	}
	
	/**
	 * Clears all showing place items from the screen
	 */
	void clearMapData(){
		GoogleMap map = mActivity.getMap();
		MapUtils.removeList(mActivity.lastPlaces);
		mActivity.getMapFrag().clearFocus();
	}
	
	/**
	 * If the drawer is opened, closes the drawer, otherwise will return false
	 * @return
	 */
	public boolean closeDrawer(){
		if(mDrawer.isOpened()){
			mDrawer.animateClose();
			return true;
		} else	return false;
	}
	
	public boolean openDrawer(){
		if(!mDrawer.isOpened()){
			mDrawer.animateOpen();
			return true;
		} else	return false;
	}
	
	/**
	 * Changes screen orientation and layout flow depending on whether screen is larger that LARGE qualifier
	 */
	void determineScreenLayout(){
       //get screen width to optimize layout
       DisplayMetrics display = new DisplayMetrics();
       mActivity.getWindowManager().getDefaultDisplay().getMetrics(display);
       if(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
    	   SCREEN_WIDTH = display.widthPixels;
       else	SCREEN_WIDTH = display.heightPixels;
       
       //list layout params
       LayoutParams lp;
        
       int screensize = mActivity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
       if((screensize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screensize == Configuration.SCREENLAYOUT_SIZE_LARGE)){
    	   //scale the width of the sidebar to specified size
    	   mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            
    	   lp = new LayoutParams((SCREEN_WIDTH/4), LayoutParams.FILL_PARENT);
    	   
    	   IconTextItem item = (IconTextItem) SplashScreenActivity.list.getListAdapter().getView(0, null, null);
    	   TextView text = (TextView) item.findViewById(R.id.name);
    	   text.setText("Clear Map");
        	
    	   SplashScreenActivity.isLargeScreen = true;
        	
        } else{//small screen we want to hide it
        	Operations.removeView(mActivity.getMapView());
        	mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        	
        	SplashScreenActivity.isLargeScreen = false;
    		
        	lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    		
        }
       SplashScreenActivity.list.getView().setLayoutParams(lp);
	}
	
	
	public void removeDrawer(){
		Operations.removeView(mDrawer);
	}
	
	public void addDrawer(){
		Operations.addView(mDrawer);
	}
	
	/**
	 * Reads in places from file and puts them on the map
	 * @param fName
	 */
	 public void readInPlaces(LinkedList<PlaceItem> pts, String fName, FragmentTransaction mTransaction){
		if(pts.size()==0)	PlaceController.readInData(mActivity, mActivity.onInfoClickedListener, pts, fName);
		
		MapUtils.addToMap(mActivity.getMap(), pts);
	}
	
	
	/**
	 * Removes currently showing fragment from screen, call mTransaction.commit() from called location
	 */
	public FragmentTransaction removeFrag(FragmentTransaction mTransaction, boolean removeLeft){
		int id;
		if(removeLeft)	id = R.anim.slide_out_left;
		else			id = android.R.anim.slide_out_right;
		mTransaction.setCustomAnimations(R.anim.slide_in_right, id);
		PlaceFragment p = mActivity.getCurrentPlaceFragment();
		if(p!=null)	mTransaction.remove(p);
		return mTransaction;
	}
	
	/**
	 * Builds and returns a checkbox and imageview within a relative layout view
	 * @param name
	 * @param iconId
	 * @param id
	 * @return
	 */
	public RelativeLayout createIconCheckBox(final String name, int iconId, int id){
		RelativeLayout iconCheckbox = (RelativeLayout) mActivity.getLayoutInflater().inflate(R.layout.icon_checkbox_item, null);
		CheckBox check = (CheckBox) iconCheckbox.findViewById(R.id.check);
		check.setText(name);
		check.setId(id);
		check.setOnCheckedChangeListener(mCheckboxListener);
		check.setChecked(true);
		ImageView icon = (ImageView) iconCheckbox.findViewById(R.id.icon);
		icon.setImageResource(iconId);
		return iconCheckbox;
	}
	
}
