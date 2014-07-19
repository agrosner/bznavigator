package com.grosner.zoo.database;

import android.database.Cursor;

import com.activeandroid.IModel;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Ignore;
import com.activeandroid.annotation.PrimaryKey;
import com.activeandroid.util.SQLiteUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by: andrewgrosner
 * Date: 7/16/14.
 * Contributors: {}
 * Description:
 */
@Ignore
public class LocationBaseObject implements IModel{

    @PrimaryKey @Column @Getter @Setter
    double latitude;

    @PrimaryKey @Column @Getter @Setter
    double longitude;

    private long mRowId = -1;

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
        mRowId = id;
    }

    @Override
    public long getRowId() {
        return mRowId;
    }

    @Override
    public String getId() {
        return "(" + latitude + "," + longitude +")";
    }
}
