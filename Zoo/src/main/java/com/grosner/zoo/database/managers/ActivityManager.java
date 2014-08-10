package com.grosner.zoo.database.managers;

import com.activeandroid.interfaces.ObjectReceiver;
import com.activeandroid.manager.DBManager;
import com.grosner.zoo.database.content.ActivityObject;

/**
 * Created by: andrewgrosner
 * Date: 7/22/14.
 * Contributors: {}
 * Description:
 */
public class ActivityManager extends DBManager<ActivityObject> {

    private static ActivityManager manager;

    public static ActivityManager getManager(){
        if(manager==null){
            manager = new ActivityManager();
        }
        return manager;
    }

    public ActivityManager(){
        super(ActivityObject.class);
    }


    @Override
    public void requestObject(ObjectReceiver<ActivityObject> objectReceiver, Object... uid) {

    }
}
