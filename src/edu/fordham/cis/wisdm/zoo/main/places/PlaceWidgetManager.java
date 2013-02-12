package edu.fordham.cis.wisdm.zoo.main.places;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

public class PlaceWidgetManager extends BaseAdapter{

	private List<RelativeLayout> mItems = new ArrayList<RelativeLayout>();
	
	private LinkedList<PlaceItem> mPoints = new LinkedList<PlaceItem>();
	
	private Activity mActivity;
	
	private boolean mWrap = false;
	
	private String mFname;
	
	public PlaceWidgetManager(Activity act, boolean wrap, String fName){
		mActivity = act;
		mWrap = wrap;
		mFname = fName;
	}
	
	public void getData(){
		if(mPoints.isEmpty()){
			//PlaceController.readInData(mActivity, mPoints, mFname);
		} else{
			PlaceController.reCalculateDistance(SplashScreenActivity.myLocation, mPoints);
		}
		Collections.sort(mPoints, new Comparator<PlaceItem>(){

			@Override
			public int compare(PlaceItem lhs, PlaceItem rhs) {
				return lhs.getDistance().compareTo(rhs.getDistance());
			}
			
		});
		update(mPoints);
		notifyDataSetChanged();
	}
	
	public void update(LinkedList<PlaceItem> points){
		mPoints = points;
		mItems.clear();
		for(PlaceItem place: points){
			PlaceWidget w = new PlaceWidget(mActivity, mActivity.getLayoutInflater(), mWrap)
				.title(place.getName()).distance(place.getDistance())
				.drawablePath(mActivity, place.getDrawablePath())
				.place(place);
			mItems.add(w);
		}
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mItems.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return mItems.get(position);
	}
	
	public LinkedList<PlaceItem> getPoints(){
		return mPoints;
	}

}
