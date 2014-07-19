package com.grosner.zoo.database;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.PrimaryKey;

import java.util.Comparator;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by: andrewgrosner
 * Date: 7/16/14.
 * Contributors: {}
 * Description: Holds information on the paths
 */
public class PathPointObject extends LocationBaseObject{

    @Column @PrimaryKey @Getter @Setter
    private String path_name;

    @Column @Getter @Setter
    private int sort_order;

    /**
     * Orders the paths in the order they were made
     */
    public class PathOrderer implements Comparator<PathPointObject>{

        @Override
        public int compare(PathPointObject lhs, PathPointObject rhs) {
            return Integer.valueOf(lhs.getSort_order()).compareTo(rhs.getSort_order());
        }
    }
}
