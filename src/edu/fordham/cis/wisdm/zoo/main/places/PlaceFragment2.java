package edu.fordham.cis.wisdm.zoo.main.places;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cis.fordham.edu.wisdm.utils.Operations;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;
import edu.fordham.cis.wisdm.zoo.utils.map.PlaceItem;

public class PlaceFragment2 extends SherlockListFragment{

	public enum PlaceType implements Serializable{EXHIBITS, FOOD, SPECIAL, SHOPS, ADMIN, NEARBY;
		public String toString(){
			return name().toLowerCase(Locale.ENGLISH);
		}
		
		/**
		* Returns capitalized name
	 	* @return
	 	*/
		public String toTitleString(){
			String s = toString();
			String first = s.substring(0, 1);
			return s.replaceFirst(first, first.toUpperCase());
		}
	};

	/**
	 * The type of this fragment
	 */
	private PlaceType type = PlaceType.EXHIBITS;
	
	/**
	 * The parent layout widget of the XML file
	 */
	private RelativeLayout exhibit = null;
	
	/**
	 * Manages placewidgets
	 */
	private PlaceWidgetManager mManager;
	

	public static PlaceFragment2 newInstance(PlaceFragment2 frag, PlaceType type){
		if(frag==null){
			frag = new PlaceFragment2();
			Bundle bundle = new Bundle();
			bundle.putSerializable("type", type);
			frag.setArguments(bundle);
		}
		return frag;
	}
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		if(instance==null){
			try{
				type = (PlaceType) getArguments().getSerializable("type");
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		super.onCreateView(inflater, container, instance);
		exhibit = (RelativeLayout) inflater.inflate(R.layout.fragment_place, container, false);
		mManager = new PlaceWidgetManager(getActivity(), true,  type.toString() + ".txt");
		setListAdapter(mManager);
		
		Operations.setViewOnClickListeners(exhibit, mOnClickListener, R.id.refresh, R.id.exit);
		refresh();
		return exhibit;
	}
	
	public void refresh(){
		
		TextView title = (TextView) exhibit.findViewById(R.id.title);
		title.setText(type.toTitleString());
		
		mManager.getData();
		//mList.setAdapter(mManager);
	}
		
	private OnClickListener mOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			//load up splashscreenactivity's instance
			SplashScreenActivity act = (SplashScreenActivity) getActivity();
			int id = v.getId();
			
			if(id == R.id.refresh){
				refresh();
				Toast.makeText(getActivity(), "Refreshed", Toast.LENGTH_SHORT).show();
			} else if(id == R.id.exit){
				act.onBackPressed();
			}
			
			/**else if(id > 0){
				PlaceItem place = points.get(id-1);
				LinkedList<PlaceItem> places = new LinkedList<PlaceItem>();
				places.add(place);
				act.showMap(mTransaction, getView(), places);
				//place.getOnPressListener().onPress();
				//tries to move map to center on both current location and point
				//if(SplashScreenActivity.myLocation!=null){
					//ZoomLevel zoom = place.getPoint().pixelsToZoom(act.getMap().getView(), SplashScreenActivity.myLocation);
					//MapInternalView view = act.getMap().getView();
					//view.center(place.getPoint(), SplashScreenActivity.myLocation);
					//view.changeZoomLevel(zoom);
				//} else{
					
					//MapUtils.moveRelativeToCurrentLocation(place.getPoint(), act.getMap());
				//}
			} else if(id == 0){
				if(type !=  PlaceType.NEARBY) act.showMap(mTransaction, getView(), points);
				else{
					LinkedList<PlaceItem> reducePoints = new LinkedList<PlaceItem>();
					while(reducePoints.size()!=10){
						reducePoints.add(points.get(reducePoints.size()));
					}
					act.showMap(mTransaction, getView(), reducePoints);
				}
			} else if(id == -1){
				getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bronxzoostore.com")));
			} 
		}**/
		}
		
	};


	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		super.onListItemClick(lv, v, position, id);
		if(v instanceof PlaceWidget){
			SplashScreenActivity act = (SplashScreenActivity) getActivity();
			FragmentTransaction mTransaction = getFragmentManager().beginTransaction();
			if(position!=0){
				PlaceWidget place = (PlaceWidget) v;
				LinkedList<PlaceItem> list = new LinkedList<PlaceItem>();
				list.add(place.getPlace());
				act.showMap(mTransaction, getView(), list);
			} else{
				act.showMap(mTransaction, getView(), mManager.getPoints());
			}
		}
	}
			
}
