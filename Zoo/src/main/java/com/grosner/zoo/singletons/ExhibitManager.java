package com.grosner.zoo.singletons;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import com.grosner.zoo.R;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.fragments.PlaceFragment;
import com.grosner.zoo.markers.PlaceMarker;

import java.io.IOException;
import java.util.LinkedList;
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
            readInData(DATA_FILES);
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

    public LinkedList<PlaceMarker> getList(PlaceFragment.PlaceType type){
        if(type.equals(PlaceFragment.PlaceType.EXHIBITS)){
            return mExhibits;
        } else if(type.equals(PlaceFragment.PlaceType.FOOD)){
            return mFood;
        } else if(type.equals(PlaceFragment.PlaceType.SHOPS)){
            return mShops;
        } else if(type.equals(PlaceFragment.PlaceType.GATES)){
            return mGates;
        } else if(type.equals(PlaceFragment.PlaceType.PARKING)){
            return mParking;
        } else if(type.equals(PlaceFragment.PlaceType.ADMIN)){
            return mAdmin;
        } else if(type.equals(PlaceFragment.PlaceType.SPECIAL)){
            return mSpecial;
        } else if(type.equals(PlaceFragment.PlaceType.RESTROOMS)){
            return mRestrooms;
        } else if(type.equals(PlaceFragment.PlaceType.MISC)){
            return mMisc;
        } else{
            return mAllPlaces;
        }
    }

    /**
     * Reads in data from any number of files into a singular linked list
     * @param fNames
     */
    private void readInData(String... fNames){
        for(String fName: fNames){
            readInExhibits(fName);
        }
    }

    private void readInExhibits(String fName){
        try {
            PlaceFragment.PlaceType type = PlaceFragment.PlaceType.getFromFileName(fName);
            Scanner mScanner = new Scanner(ZooApplication.getContext().getAssets().open(fName));
            int idIndex = -1;
            while(mScanner.hasNextLine()){
                String line = mScanner.nextLine();
                idIndex++;
                if(idIndex!=0){
                    String[] lineArray = line.split(",");
                    if(lineArray.length>=4){
                        double lat = Double.valueOf(lineArray[2]);
                        double lon = Double.valueOf(lineArray[3]);
                        Location loc = new Location("");
                        loc.setLatitude(lat);
                        loc.setLongitude(lon);

                        if(lineArray[0].toLowerCase().contains("restroom")){
                            lineArray[0] = "Restroom";
                        }
                        int drawableId = ZooApplication.getContext().getResources().getIdentifier(lineArray[1], "drawable", ZooApplication.getContext().getPackageName());

                        if(fName.equals("admin.txt")){
                            lineArray[0]+="(Staff Only)";
                        }

                        PlaceMarker marker = new PlaceMarker().point(new LatLng(lat, lon)).name(lineArray[0]).id(idIndex).iconId(drawableId).drawablePath(lineArray[1]);
                        mAllPlaces.add(marker);
                        addToList(type, marker);
                    }
                }
            }
            mScanner.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
