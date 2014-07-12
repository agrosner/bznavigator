package com.grosner.zoo.adapters;

import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.android.gms.maps.MapFragment;
import com.grosner.zoo.PlaceController;
import com.grosner.zoo.R;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.fragments.MapViewFragment;
import com.grosner.zoo.markers.PlaceMarker;
import com.grosner.zoo.views.ExhibitItemView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by: andrewgrosner
 * Date: 7/12/14.
 * Contributors: {}
 * Description:
 */
public class ExhibitAdapter extends BaseAdapter {

    private LinkedList<PlaceMarker> mObjects;

    private Location mLocation;

    public ExhibitAdapter(MapViewFragment map, LinkedList<PlaceMarker> mObjects) {
        this.mObjects = mObjects;
        mLocation = map==null? null : map.getLastKnownLocation();
        PlaceController.reOrderByDistance(mObjects, mLocation);
    }

    @Override
    public int getCount() {
        return mObjects==null?0:mObjects.size()+1;
    }

    @Override
    public PlaceMarker getItem(int position) {
        return position==0? null : mObjects.get(position-1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExhibitItemView exhibitItemView;
        if(convertView==null){
            exhibitItemView = new ExhibitItemView(parent.getContext());
        } else{
            exhibitItemView = (ExhibitItemView) convertView;
        }
        exhibitItemView.setPlace(getItem(position), mLocation);
        return exhibitItemView;
    }

    public LinkedList<PlaceMarker> getObjects() {
        return mObjects;
    }
}
