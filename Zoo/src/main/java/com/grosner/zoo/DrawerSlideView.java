package com.grosner.zoo;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

/**
 * Author: andrewgrosner
 * Date: 6/20/14
 * Contributors: { }
 * Description: This class will make it easy to create a views that change color when the drawer slides.
 */
public class DrawerSlideView {

    /**
     * Selects how this view should mutate as the drawer slides open
     */
    public enum Mode{

        /**
         * The drawable on the view will change alpha as drawer slides
         */
        ALPHA,

        /**
         * The color will change as the drawer slides
         */
        COLOR;
    }

    private Mode mMode;

    private Drawable mDrawable;

    private int mEndColor;

    private int mStartColor;

    /**
     * Constructs the slider with the start color being a transparent color.
     * @param viewObject
     * @param endColor
     * @param mode
     */
    public DrawerSlideView(Object viewObject, int endColor, Mode mode){
        this(viewObject, Color.TRANSPARENT, endColor, mode);
    }

    /**
     * Initializes this slider with an ActionBar, support or regular
     * @param actionBar
     * @param endColor - the color the view will turn when drawer is opened.
     */
    public DrawerSlideView(Object actionBar, int startColor, int endColor, Mode mode){
        mEndColor = endColor;
        mStartColor = startColor;

        mMode = mode;
        boolean success = false;
        if(actionBar instanceof android.app.ActionBar){
            success = true;
            mDrawable = new ColorDrawable(endColor);
            ((android.app.ActionBar) actionBar).setBackgroundDrawable(mDrawable);
        } else if(actionBar instanceof View){
            success = true;
            if(actionBar instanceof ImageView && ((ImageView) actionBar).getDrawable()!=null){
                mDrawable = ((ImageView) actionBar).getDrawable();
            } else if(((View) actionBar).getBackground()!=null){
                mDrawable = ((View) actionBar).getBackground();
            } else{
                mDrawable = new ColorDrawable(endColor);
                if(Build.VERSION.SDK_INT<14) {
                    ((View) actionBar).setBackgroundDrawable(mDrawable);
                } else{
                    ((View) actionBar).setBackground(mDrawable);
                }
            }
        } else if(actionBar instanceof MenuItem){
            success = true;
            if(((MenuItem) actionBar).getIcon()!=null) {
                mDrawable = ((MenuItem) actionBar).getIcon();
            } else{
                mDrawable = new ColorDrawable(startColor);
            }
            ((MenuItem) actionBar).setIcon(mDrawable);
        }

        if(mMode.equals(Mode.ALPHA)) {
            mDrawable.setAlpha(0);
        }

        if(!success){
            throw new IllegalArgumentException("Object passed should be instance of an ActionBar or a view");
        }
    }

    /**
     * Call this within the
     * {@link android.support.v4.widget.DrawerLayout.DrawerListener#onDrawerSlide(android.view.View, float)}
     * method.
     * @param slideOffset
     */
    public void onDrawerSlide(float slideOffset){

        if(mMode.equals(Mode.ALPHA)) {
            mDrawable.setAlpha((int) (255 * slideOffset));
        } else{
            mDrawable.setColorFilter(DrawerSlideUtils.calculateColor(slideOffset, mStartColor, mEndColor),
                    PorterDuff.Mode.MULTIPLY);
        }
    }

    public void setEndColor(int mEndColor) {
        this.mEndColor = mEndColor;
    }

    public void setStartColor(int mStartColor) {
        this.mStartColor = mStartColor;
    }
}
