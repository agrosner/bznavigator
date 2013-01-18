package edu.fordham.cis.wisdm.zoo.utils.map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.Marker;

import edu.fordham.cis.wisdm.zoo.main.R;

public class PlaceItemWindowAdapter implements InfoWindowAdapter, OnInfoWindowClickListener{

	private final View mWindow;
	
	public PlaceItemWindowAdapter(LayoutInflater inflater){
		mWindow = inflater.inflate(R.layout.exhibitoverlay, null);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		TextView text = (TextView)(mWindow.findViewById(R.id.name));
		text.setText(marker.getTitle());
		
		return mWindow;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		// TODO Auto-generated method stub
		
	}

}
