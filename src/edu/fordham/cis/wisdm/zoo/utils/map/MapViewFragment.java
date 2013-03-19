package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.LinkedList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import cis.fordham.edu.wisdm.utils.Operations;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;
import edu.fordham.cis.wisdm.zoo.main.places.PlaceController;

public class MapViewFragment extends SupportMapFragment implements OnClickListener, OnCameraChangeListener, OnInfoWindowClickListener{

	private GoogleMap mGoogleMap = null;

	/**
	 * Building objects overlayed on the map
	 */
	private Exhibits mExhibits;
	
	/**
	 * Manages the text overlays displayed on the map
	 */
	private TextMarkerManager mTextMarkerManager;
	
	/**
	 * The current zoom of the map
	 */
	private float mCurrentZoom = 0;
	
	private LatLngBounds.Builder mBuilder = new LatLngBounds.Builder();
	
	private View mLayout = null;
	
	private LayoutInflater mInflater = null;
	
	private CurrentLocationManager mManager = null;
	
	/**
	 * Restrooms list
	 */
	LinkedList<PlaceItem> restrooms = new LinkedList<PlaceItem>();
	
	/**
	 * Gates list
	 */
	LinkedList<PlaceItem> gates = new LinkedList<PlaceItem>();
	
	/**
	 * Parking list
	 */
	LinkedList<PlaceItem> parking = new LinkedList<PlaceItem>();
	
	/**
	 * Shops list
	 */
	LinkedList<PlaceItem> shop = new LinkedList<PlaceItem>();
	/**
	 * List of searchable places
	 */
	private LinkedList<PlaceItem> searchExhibits = new LinkedList<PlaceItem>();
	
	/**
	 * Provides an action when the user's current location changes
	 */
	private Runnable meListener = new Runnable(){

		@Override
		public void run() {
			PlaceController.reCalculateDistance(mManager.getLastKnownLocation(), 
					searchExhibits, parking, gates, restrooms);
		}
		
	};
	
	@Override
	public void onResume(){
		super.onResume();

		mGoogleMap = super.getMap();
		if(mGoogleMap!=null){
			setUpMap(mInflater);
		}
	}
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
		ViewGroup v = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		mLayout = inflater.inflate(R.layout.fragment_map, container, false);
		
		Operations.setViewOnClickListeners(mLayout, this, R.id.satellite, R.id.normal, R.id.overview);
		
		mInflater = inflater;
		
		v.addView(mLayout);
		return v;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.satellite){
			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			mTextMarkerManager.changeTextLabelColor(Color.MAGENTA,mCurrentZoom);
		} else if(id == R.id.normal){
			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mTextMarkerManager.changeTextLabelColor(Color.BLACK,mCurrentZoom);
		} else if(id == R.id.overview){
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getMapBounds(), 10));
		}
	}
	
	/**
	 * Sets up UI options
	 * @param inflater
	 */
	private void setUpMap(LayoutInflater inflater){
		mGoogleMap.setOnCameraChangeListener(this);
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setInfoWindowAdapter(new PlaceItemWindowAdapter(inflater));
		mGoogleMap.setOnInfoWindowClickListener(this);
   		mGoogleMap.getUiSettings().setCompassEnabled(false);
   		mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
		
   		try{
   			MapUtils.generatePolygon(getActivity(), mBuilder);
			Paths.readFiles(getActivity());
			Paths.addToMap(mGoogleMap);
			
			Exhibits.readFiles(getActivity());
			Exhibits.addToMap(mGoogleMap);
			
			if(mTextMarkerManager==null){
				mTextMarkerManager = new TextMarkerManager(getActivity(), mGoogleMap);
				TextMarkerManager.readInData(mTextMarkerManager, getActivity(),"exhibits.txt", "special.txt");
			} else{
				mTextMarkerManager.reset(getActivity(), mGoogleMap);
			}
			
			mTextMarkerManager.addToMap();
			mTextMarkerManager.refreshData(getMap().getCameraPosition().zoom);
			
			mManager = new CurrentLocationManager(getActivity(), mGoogleMap);
			mManager.runOnFirstFix(meListener);
			mManager.runOnFirstFix(new Runnable(){

				@Override
				public void run() {
					mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mBuilder.build(), 10));
				}
				
			});
			mManager.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(getActivity(), e1.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onCameraChange(CameraPosition position) {
		float zoom = position.zoom;
		if(zoom!=mCurrentZoom){
			mCurrentZoom = zoom;
			mTextMarkerManager.refreshData(zoom);
			//Toast.makeText(getActivity(), "Zoom: " + position.zoom, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		MapUtils.showExhibitInfoDialog(
				getActivity().getLayoutInflater(), 
				getActivity(), marker);
	}
	
	public LatLngBounds getMapBounds(){
		return mBuilder.build();
	}
	
	/**
	 * This will put the focus on a location that the user navigates to, 
	 * adding an icon to the text label
	 */
	public boolean setMarkerFocus(PlaceItem place){
		return mTextMarkerManager.addFocus(place);
	}
	
	/**
	 * Clears all icons from text markers that are focused
	 */
	public void clearFocus(){
		mTextMarkerManager.clearFocus(mCurrentZoom);
	}
	

}
