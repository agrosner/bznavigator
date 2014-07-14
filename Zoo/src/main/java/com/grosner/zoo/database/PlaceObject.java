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
public class PlaceObject implements IModel, Serializable{

    @PrimaryKey @Column @Getter @Setter
    private double latitude;

    @PrimaryKey @Column @Getter @Setter
    private double longitude;

    @Column @Getter @Setter
    private String drawable;

    @Column @Getter @Setter
    private String name;

    @Column @Getter @Setter
    private String link;

    @Column @Getter @Setter
    private String placeType;

    private Location mLocation;

    private long rowId = -1;

    @Override
    public void save() {
        SQLiteUtils.save(this);
    }

    @Override
    public void delete() {
        SQLiteUtils.delete(this);
    }

    @Override
    public boolean exists() {
        return SQLiteUtils.exists(this);
    }

    @Override
    public void loadFromCursor(Cursor cursor) {
        SQLiteUtils.loadFromCursor(cursor, this);
    }

    @Override
    public void setRowId(long id) {
        rowId = id;
    }

    @Override
    public long getRowId() {
        return rowId;
    }

    @Override
    public String getId() {
        return "(" + latitude + "," + longitude + ")";
    }

    public Location getLocation(){
        if(mLocation==null) {
            mLocation = new Location(name);
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
        }
        return mLocation;
    }
}
