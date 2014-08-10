package com.grosner.zoo.adapters;

import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.activeandroid.interfaces.CollectionReceiver;
import com.google.android.gms.maps.LocationSource;
import com.grosner.zoo.PlaceController;
import com.grosner.zoo.database.PlaceManager;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.fragments.PlaceFragment;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.utils.StringUtils;
import com.grosner.zoo.widgets.ExhibitItemView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: andrewgrosner
 * Date: 7/12/14.
 * Contributors: {}
 * Description:
 */
public class ExhibitAdapter extends BaseAdapter implements CollectionReceiver<PlaceObject>,
        LocationSource.OnLocationChangedListener, Filterable {

    private ArrayList<PlaceObject> mObjects;
    private ArrayList<PlaceObject> mFilterObjects;

    private String mPreviousKeyword;

    private boolean isDropDown = false;

    public ExhibitAdapter(PlaceFragment.PlaceType placeType) {
        if(placeType.equals(PlaceFragment.PlaceType.PINS)){
            PlaceManager.getManager().fetchAllWithColumnValue("1", "isFavorite", this);
        } else if(!placeType.equals(PlaceFragment.PlaceType.NEARBY) && !placeType.equals(PlaceFragment.PlaceType.SEARCH)) {
            PlaceManager.getManager().fetchAllWithColumnValue(placeType.name(), "placeType", this);
        } else{
            isDropDown = placeType.equals(PlaceFragment.PlaceType.SEARCH);
            PlaceManager.getManager().fetchAll(this);
        }
        CurrentLocationManager.getSharedManager().activate(this);
    }

    @Override
    public int getCount() {
        return mFilterObjects==null?0:mFilterObjects.size()+1;
    }

    @Override
    public PlaceObject getItem(int position) {
        return position==0? null : mFilterObjects.get(position-1);
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
            if(isDropDown){
                exhibitItemView.setDropDownView();
            }
        } else{
            exhibitItemView = (ExhibitItemView) convertView;
        }
        exhibitItemView.setPlace(getItem(position));
        return exhibitItemView;
    }

    public List<PlaceObject> getObjects() {
        return mFilterObjects;
    }

    @Override
    public void onCollectionReceived(List<PlaceObject> object) {
        mObjects = (ArrayList<PlaceObject>) object;
        PlaceController.reOrderByDistance(mObjects,
                CurrentLocationManager.getSharedManager().getLastKnownLocation());
        mFilterObjects = mObjects;
        notifyDataSetChanged();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mObjects!=null) {
            PlaceController.reOrderByDistance(mObjects, location);
            PlaceController.reOrderByDistance(mFilterObjects, location);
            notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        return new ExhibitFilter();
    }

    public class ExhibitFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            ArrayList<PlaceObject> copy = new ArrayList<>();
            ArrayList<PlaceObject> retObjects = new ArrayList<>();
            boolean reset = false;

            String newKeyword = constraint.toString().trim().toLowerCase();

            if(StringUtils.stringNotNullOrEmpty(mPreviousKeyword)){
                //user types in more letters from previous
                if(newKeyword.startsWith(mPreviousKeyword)){
                    copy =  (ArrayList<PlaceObject>) mFilterObjects.clone();
                } else{
                    reset = true;
                }
            } else{
                reset = true;
            }

            if(reset) {
                copy = (ArrayList<PlaceObject>) mObjects.clone();
            }

            for (PlaceObject object : copy) {
                if (object.getName().toLowerCase().startsWith(newKeyword)) {
                    retObjects.add(object);
                }
            }
            filterResults.values = retObjects;

            return filterResults;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilterObjects = (ArrayList<PlaceObject>) results.values;
            mPreviousKeyword = constraint.toString();
            notifyDataSetChanged();
        }
    }
}
