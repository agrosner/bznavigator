package com.grosner.zoo.utils;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by: andrewgrosner
 * Date: 7/17/14.
 * Contributors: {}
 * Description:
 */
public class ViewUtils {

    /**
     * This method will find how to get the view from the object passed.
     * @param object
     * @return
     */
    public static View getView(Object object){
        View view;
        if(object instanceof Activity){
            view = ((Activity) object).getWindow().getDecorView();
        } else if(object instanceof Fragment){
            view = ((Fragment) object).getView();
        } else if(object instanceof ViewGroup){
            view = (View) object;
        } else if(Build.VERSION.SDK_INT>10 && object instanceof android.app.Fragment){
            view = ((android.app.Fragment) object).getView();
        }  else{
            throw new RuntimeException("Object passed needs to be a ViewGroup, Fragment, or Activity");
        }
        return view;
    }

    public static void setViewVisibility(Object view, int visibility, int fid, int...views){
        View parentView = getView(view);
        View fChild = parentView.findViewById(fid);
        if(fChild!=null){
            fChild.setVisibility(visibility);
        }
        for(int id: views){
            View child = parentView.findViewById(id);
            if(child!=null){
                child.setVisibility(visibility);
            }
        }
    }

    public static void setViewVisibility(int visibility,View...views){
        for(View child: views){
            if(child!=null){
                child.setVisibility(visibility);
            }
        }
    }


    public static void setViewsGone(Object view, int fid, int...views) {
        setViewVisibility(view, View.GONE, fid, views);
    }

    public static void setViewsInvisible(Object view, int fid, int...views) {
        setViewVisibility(view, View.INVISIBLE, fid, views);
    }

    public static void setViewsVisible(View...views){
        setViewVisibility(View.VISIBLE, views);
    }

    public static void setViewsVisible(Object view, int fid, int...views) {
        setViewVisibility(view, View.VISIBLE, fid, views);
    }

    public static TextView setViewText(View parent, String text, int id){
        TextView v = ((TextView) parent.findViewById(id));
        v.setText(text);
        return v;
    }
}
