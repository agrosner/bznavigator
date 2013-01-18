package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;

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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.main.SplashScreenActivity;

public class MapViewFragment extends SherlockFragment implements OnClickListener, OnCameraChangeListener, OnInfoWindowClickListener{

	private SupportMapFragment mMap = null;
	
	private GoogleMap mGoogleMap = null;

	/**
	 * Paths overlayed on the map
	 */
	private Path mPaths;
	
	/**
	 * Building objects overlayed on the map
	 */
	private Exhibit mExhibits;
	
	/**
	 * Manages the text overlays displayed on the map
	 */
	private TextMarkerManager mTextMarkerManager;
	
	/**
	 * The current zoom of the map
	 */
	private float mCurrentZoom = 0;
	
	private LatLngBounds.Builder mBuilder = new LatLngBounds.Builder();
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
           Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View layout = inflater.inflate(R.layout.fragment_map, container, false);
		mMap = (SupportMapFragment) this.getFragmentManager().findFragmentById(R.id.mapFragment);
		mGoogleMap = mMap.getMap();
		if(mGoogleMap!=null){
			setUpMap(layout, inflater);
		}
		
		return layout;
	}

	public SupportMapFragment getMap() {
		return mMap;
	}
	
	public GoogleMap getGoogleMap(){
		return mMap.getMap();
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
	
	private void setUpMap(View layout, LayoutInflater inflater){
		mGoogleMap.setOnCameraChangeListener(this);
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setInfoWindowAdapter(new PlaceItemWindowAdapter(inflater));
		mGoogleMap.setOnInfoWindowClickListener(this);
   		mGoogleMap.getUiSettings().setCompassEnabled(false);
   		mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
		
		Operations.setViewOnClickListeners(layout, this, R.id.satellite, R.id.normal, R.id.overview);
		
		try {

			MapUtils.generatePolygon(getActivity(), mBuilder);
			mPaths = new Path(mGoogleMap).readFiles(getActivity());
			mExhibits = new Exhibit(mGoogleMap).readFiles(getActivity());
			mTextMarkerManager = new TextMarkerManager(getActivity(), mGoogleMap);
			mTextMarkerManager.readInData("exhibits.txt");
			mTextMarkerManager.addToMap();
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
	

}
