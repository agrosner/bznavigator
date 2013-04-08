package edu.fordham.cis.wisdm.zoo.utils.map;

import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import edu.fordham.cis.wisdm.zoo.main.R;
import edu.fordham.cis.wisdm.zoo.utils.Operations;

public class PlaceMarkerWindowAdapter implements InfoWindowAdapter{

	private final View mWindow;
	
	public PlaceMarkerWindowAdapter(LayoutInflater inflater){
		mWindow = inflater.inflate(R.layout.overlay_exhibit, null);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		Operations.setViewText(mWindow, marker.getTitle(), R.id.name);
		return mWindow;
	}

}
