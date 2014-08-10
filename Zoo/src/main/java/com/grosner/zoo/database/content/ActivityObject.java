package com.grosner.zoo.database.content;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.PrimaryKey;
import com.activeandroid.manager.SingleDBManager;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by: andrewgrosner
 * Date: 7/22/14.
 * Contributors: {}
 * Description:
 */
public class ActivityObject extends Model {

    @Column @PrimaryKey @Getter @Setter
    private String endPoint;

    @Column @Getter @Setter
    private String imageUrl;

    @Column @Getter @Setter
    private String description;

    @Column @Getter @Setter
    private String schedule;

    private List<FeatureObject> featureObjects;

    public List<FeatureObject> features(){
        if(featureObjects==null){
            featureObjects = SingleDBManager.getSharedInstance().getAllWithColumnValue(FeatureObject.class, "activity_url", endPoint);
        }
        return featureObjects;
    }

    @Override
    public String getId() {
        return endPoint;
    }
}
