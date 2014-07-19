package com.grosner.zoo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;

/**
 * Created by: andrewgrosner
 * Date: 7/12/14.
 * Contributors: {}
 * Description:
 */
public class InstantAutoComplete extends AutoCompleteTextView {

    public InstantAutoComplete(Context context) {
        super(context);
        setThreshold(0);
    }

    public InstantAutoComplete(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        setThreshold(0);
    }

    public InstantAutoComplete(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        setThreshold(0);
    }

    @Override
    public boolean enoughToFilter() {
        if(getThreshold() == Integer.MAX_VALUE)
            return false;

        return true;
    }

    public boolean onTouchEvent(MotionEvent event){
        boolean ret = super.onTouchEvent(event);
        if(event.getAction() == MotionEvent.ACTION_UP){
            showDropDown();
        }
        return ret;
    }
}
