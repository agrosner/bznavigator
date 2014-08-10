package com.grosner.zoo.database.content;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by: andrewgrosner
 * Date: 7/22/14.
 * Contributors: {}
 * Description:
 */
public class FeatureObject extends Model {

    @PrimaryKey @Column @Getter @Setter
    private String imageUrl;

    @Column @Getter @Setter
    private String label;

    @Column @Getter @Setter
    private String activity_url;

    @Override
    public String getId() {
        return imageUrl;
    }
}
