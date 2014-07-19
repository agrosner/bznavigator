package com.grosner.zoo.database;

import android.database.Cursor;
import android.location.Location;

import com.activeandroid.IModel;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.PrimaryKey;
import com.activeandroid.util.SQLiteUtils;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by: andrewgrosner
 * Date: 7/13/14.
 * Contributors: {}
 * Description: Holds data on the items that go on the map.
 */
@EqualsAndHashCode(doNotUseGetters = true)
public class PlaceObject extends LocationBaseObject implements Serializable{

    @Column @Getter @Setter
    private String drawable;

    @Column @Getter @Setter
    private String name;

    @Column @Getter @Setter
    private String link;

    @Column @Getter @Setter
    private String placeType;

    @Column @Getter @Setter
    private boolean isFavorite;

    private Location mLocation;

    public Location getLocation(){
        if(mLocation==null) {
            mLocation = new Location(name);
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
        }
        return mLocation;
    }
}
