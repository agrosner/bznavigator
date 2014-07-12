package com.grosner.zoo.adapters;

import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import com.grosner.zoo.R;
import com.grosner.zoo.utils.Operations;

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
