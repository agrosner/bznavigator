package com.grosner.zoo.database;

import android.database.Cursor;

import com.activeandroid.IModel;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.PrimaryKey;
import com.activeandroid.manager.SingleDBManager;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by: andrewgrosner
 * Date: 7/16/14.
 * Contributors: {}
 * Description:
 */
public class PathObject implements IModel {

    @Column @PrimaryKey @Setter @Getter
    private String name;

    private List<PathPointObject> mPoints;

    public List<PathPointObject> points(){
        if(mPoints==null) {
            mPoints = new Select().from(PathPointObject.class).where("path_name = ?" , name).orderBy("sort_order ASC").execute();
        }
        return mPoints;
    }

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
        return name;
    }
}
