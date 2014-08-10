package com.grosner.zoo.adapters;

import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.grosner.painter.IconPainter;
import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.R;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.utils.ViewUtils;

public class PlaceMarkerWindowAdapter implements InfoWindowAdapter{

	private final View mWindow;

    @SResource private ImageView info;
	
	public PlaceMarkerWindowAdapter(){
		mWindow = SmartInflater.inflate(this, R.layout.overlay_exhibit);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		ViewUtils.setViewText(mWindow, marker.getTitle(), R.id.name);
        new IconPainter(ZooApplication.getContext().getResources()
                .getColor(R.color.description_text_color)).paint(info);
		return mWindow;
	}

}
