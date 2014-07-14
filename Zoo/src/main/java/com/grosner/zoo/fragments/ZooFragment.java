package com.grosner.zoo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.FragmentUtils;
import com.grosner.zoo.R;
import com.grosner.zoo.activities.ZooActivity;
import com.grosner.zoo.application.ZooApplication;
/**
 * Created by: andrewgrosner
 * Date: 6/28/14.
 * Contributors: {}
 * Description:
 */
public class ZooFragment extends Fragment {

    protected String mTitle = ZooApplication.getContext().getString(R.string.app_name);

    protected int mLayout = -1;

    protected boolean showBack = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mLayout==-1){
            throw new RuntimeException("You must define a layout for: " + getClass().getSimpleName());
        }

        return SmartInflater.inflate(this, mLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(mTitle);

        if(showBack){
            getZooActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getZooActivity().getToggle().setDrawerIndicatorEnabled(false);
        } else{
            getZooActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
            getZooActivity().getToggle().setDrawerIndicatorEnabled(true);
        }
    }

    protected Fragment switchFragment(String tag, Class clazz, Bundle bundle) {
        if (getActivity() == null)
            return null;

        Fragment fragment = FragmentUtils.getFragment(getActivity(), clazz, tag, bundle);

        if(fragment!=null){
            if (getActivity() instanceof ZooActivity) {
                ZooActivity ra = (ZooActivity) getActivity();
                ra.switchContent(fragment);
            }
        }
        return fragment;
    }


    protected ZooActivity getZooActivity(){
        return (ZooActivity) getActivity();
    }
}
