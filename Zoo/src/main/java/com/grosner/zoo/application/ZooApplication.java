package com.grosner.zoo.application;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.activeandroid.app.AAApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapsInitializer;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.R;
import com.grosner.zoo.database.PlaceManager;

/**
 * Created By: andrewgrosner
 * Date: 8/30/13
 * Contributors:
 * Description:
 */
public class ZooApplication extends AAApplication{

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        MapsInitializer.initialize(this);
        SmartInflater.initialize(this);
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(result!= ConnectionResult.SUCCESS){
            Toast.makeText(this, "Google Play Services Error: " + GooglePlayServicesUtil.getErrorString
                    (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)), Toast.LENGTH_SHORT).show();
        }

        PlaceManager.getManager().readAllFiles();
    }

    public static Context getContext(){
        return mContext;
    }

    public static LayoutInflater getSharedInflater(){
        return LayoutInflater.from(mContext);
    }

    public static String getResourceString(int resId) {
        return getContext().getString(resId);
    }

    public static int getResourceId(String name, String type){
        return getContext().getResources().getIdentifier(name,
                type, ZooApplication.getContext().getPackageName());
    }
}

