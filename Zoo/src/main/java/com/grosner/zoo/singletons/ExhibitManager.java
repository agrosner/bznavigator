package com.grosner.zoo.singletons;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import com.grosner.zoo.R;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.database.PlaceManager;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.fragments.PlaceFragment;
import com.grosner.zoo.markers.PlaceMarker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created By: andrewgrosner
 * Date: 8/31/13
 * Contributors:
 * Description: once exhibits are read in, then it keeps them in memory for usage within the app
 */
public class ExhibitManager {

    private LinkedList<PlaceMarker> mAllPlaces, mExhibits,mFood,mShops,mGates,mParking,mAdmin,mSpecial,mRestrooms,mMisc;

    private static ExhibitManager mManager;

    public static final String[] DATA_FILES = ZooApplication.getContext()
            .getResources().getStringArray(R.array.search_list);


    public ExhibitManager(){
        mAllPlaces = new LinkedList<>();
        mExhibits = new LinkedList<>();
        mFood = new LinkedList<>();
        mShops = new LinkedList<>();
        mGates = new LinkedList<>();
        mParking = new LinkedList<>();
        mAdmin = new LinkedList<>();
        mSpecial = new LinkedList<>();
        mRestrooms = new LinkedList<>();
        mMisc = new LinkedList<>();
    }

    public static ExhibitManager getSharedInstance(){
        if(mManager==null){
            mManager = new ExhibitManager();
        }
        return mManager;
    }

    public LinkedList<PlaceMarker> getAllPlaces(){
        if(mAllPlaces ==null || mAllPlaces.isEmpty()){
            readInData();
        }

        return mAllPlaces;
    }

    private void addToList(PlaceFragment.PlaceType type, PlaceMarker marker){
        if(type.equals(PlaceFragment.PlaceType.EXHIBITS)){
            mExhibits.add(marker);
        } else if(type.equals(PlaceFragment.PlaceType.FOOD)){
            mFood.add(marker);
        } else if(type.equals(PlaceFragment.PlaceType.SHOPS)){
            mShops.add(marker);
        } else if(type.equals(PlaceFragment.PlaceType.GATES)){
            mGates.add(marker);
        } else if(type.equals(PlaceFragment.PlaceType.PARKING)){
            mParking.add(marker);
        } else if(type.equals(PlaceFragment.PlaceType.ADMIN)){
            mAdmin.add(marker);
        } else if(type.equals(PlaceFragment.PlaceType.SPECIAL)){
            mSpecial.add(marker);
        } else if(type.equals(PlaceFragment.PlaceType.RESTROOMS)){
            mRestrooms.add(marker);
        } else mMisc.add(marker);
    }

    /**
     * Reads in data from any number of files into a singular linked list
     * @param fNames
     */
    public void readInData(){
        List<PlaceObject> placeObjects = PlaceManager.getManager().getAll();
        for(PlaceObject placeObject: placeObjects){
            PlaceMarker marker = new PlaceMarker().place(placeObject);
            mAllPlaces.add(marker);
            addToList(PlaceFragment.PlaceType.valueOf(placeObject.getPlaceType()), marker);
        }
    }

    public LinkedList<PlaceMarker> getExhibits() {
        return mExhibits;
    }

    public LinkedList<PlaceMarker> getFood() {
        return mFood;
    }

    public LinkedList<PlaceMarker> getShops() {
        return mShops;
    }

    public LinkedList<PlaceMarker> getGates() {
        return mGates;
    }

    public LinkedList<PlaceMarker> getParking() {
        return mParking;
    }

    public LinkedList<PlaceMarker> getAdmin() {
        return mAdmin;
    }

    public LinkedList<PlaceMarker> getSpecial() {
        return mSpecial;
    }

    public LinkedList<PlaceMarker> getRestrooms() {
        return mRestrooms;
    }

    public LinkedList<PlaceMarker> getMisc() {
        return mMisc;
    }

}
