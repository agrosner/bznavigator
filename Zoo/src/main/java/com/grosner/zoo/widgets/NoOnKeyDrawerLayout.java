package com.grosner.zoo.widgets;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by: andrewgrosner
 * Date: 7/16/14.
 * Contributors: {}
 * Description:
 */
public class NoOnKeyDrawerLayout extends DrawerLayout {
    public NoOnKeyDrawerLayout(Context context) {
        super(context);
    }

    public NoOnKeyDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoOnKeyDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }
}
