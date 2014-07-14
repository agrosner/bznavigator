package com.grosner.zoo.database;

import com.activeandroid.interfaces.ObjectReceiver;
import com.activeandroid.manager.DBManager;
import com.activeandroid.runtime.DBRequest;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.fragments.PlaceFragment;
import com.grosner.zoo.singletons.ExhibitManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by: andrewgrosner
 * Date: 7/13/14.
 * Contributors: {}
 * Description: Manages interactions with the DB on places.
 */
public class PlaceManager extends DBManager<PlaceObject>{

    private static PlaceManager manager;

    public static PlaceManager getManager(){
        if(manager==null){
            manager = new PlaceManager();
        }
        return manager;
    }

    public PlaceManager() {
        super(PlaceObject.class);
    }

    /**
     * Reads all the files into the DB
     */
    public void readAllFiles(){
        getQueue().add(new DBRequest() {
            @Override
            public void run() {
                String[] files = ExhibitManager.DATA_FILES;
                for(String file: files){
                    readDataIntoDB(file);
                }
            }
        });
    }

    private void readDataIntoDB(String fName){
        Scanner mScanner = null;
        try {
            mScanner = new Scanner(ZooApplication.getContext().getAssets().open(fName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int idIndex = -1;
        String placeType = PlaceFragment.PlaceType.getFromFileName(fName).name();
        ArrayList<PlaceObject> places = new ArrayList<>();
        while (mScanner.hasNextLine()) {
            PlaceObject placeObject = new PlaceObject();
            String line = mScanner.nextLine();
            idIndex++;
            if (idIndex != 0) {
                String[] lineArray = line.split(",");
                if (lineArray.length >= 4) {
                    placeObject.setLatitude(Double.valueOf(lineArray[2]));
                    placeObject.setLongitude(Double.valueOf(lineArray[3]));
                    placeObject.setDrawable(lineArray[1]);
                    placeObject.setName(lineArray[0]);
                    placeObject.setPlaceType(placeType);
                    //optional link included
                    if (lineArray.length >= 5) {
                        placeObject.setLink(lineArray[4]);
                    }

                    places.add(placeObject);
                }
            }
        }
        addAll(places);
    }

    @Override
    public void requestObject(ObjectReceiver<PlaceObject> objectReceiver, Object... uid) {
        //not available
    }

    public List<PlaceObject> getList(String placeType){
        return getAllWithColumnValue("placeType", placeType);
    }
}
