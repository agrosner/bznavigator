package com.grosner.zoo.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.activeandroid.interfaces.CollectionReceiver;
import com.grosner.zoo.PlaceController;
import com.grosner.zoo.database.PlaceManager;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.fragments.PlaceFragment;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.markers.PlaceMarker;
import com.grosner.zoo.views.ExhibitItemView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by: andrewgrosner
 * Date: 7/12/14.
 * Contributors: {}
 * Description:
 */
public class ExhibitAdapter extends BaseAdapter implements CollectionReceiver<PlaceObject>{

    private List<PlaceObject> mObjects;

    public ExhibitAdapter(PlaceFragment.PlaceType placeType) {
        if(!placeType.equals(PlaceFragment.PlaceType.NEARBY)) {
            PlaceManager.getManager().fetchAllWithColumnValue(placeType.name(), "placeType", this);
        } else{
            PlaceManager.getManager().fetchAll(this);
        }
    }

    @Override
    public int getCount() {
        return mObjects==null?0:mObjects.size()+1;
    }

    @Override
    public PlaceObject getItem(int position) {
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
        exhibitItemView.setPlace(getItem(position));
        return exhibitItemView;
    }

    public List<PlaceObject> getObjects() {
        return mObjects;
    }

    @Override
    public void onCollectionReceived(List<PlaceObject> object) {
        mObjects = object;
        PlaceController.reOrderByDistance(mObjects,
                CurrentLocationManager.getSharedManager().getLastKnownLocation());
        notifyDataSetChanged();
    }
}
