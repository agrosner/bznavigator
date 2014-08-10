package com.grosner.zoo.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.grosner.zoo.database.content.FeatureObject;
import com.grosner.zoo.widgets.FeatureView;

import java.util.List;

/**
 * Created by: andrewgrosner
 * Date: 7/22/14.
 * Contributors: {}
 * Description:
 */
public class FeatureAdapter extends BaseAdapter {

    private List<FeatureObject> mFeatures;

    public FeatureAdapter(List<FeatureObject> featureObjects){
        mFeatures = featureObjects;
    }

    @Override
    public int getCount() {
        return mFeatures==null?0:mFeatures.size();
    }

    @Override
    public FeatureObject getItem(int position) {
        return mFeatures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeatureView featureView;
        if(convertView==null){
            featureView = new FeatureView(parent.getContext());
        } else{
            featureView = (FeatureView) convertView;
        }
        featureView.setFeature(getItem(position));
        return featureView;
    }
}
