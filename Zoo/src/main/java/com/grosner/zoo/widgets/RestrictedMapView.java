package com.grosner.zoo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;

/**
 * Created by: andrewgrosner
 * Date: 7/17/14.
 * Contributors: {}
 * Description: This class will enable for a predefined {@link com.google.android.gms.maps.model.VisibleRegion}
 * that the user can scroll within.
 */
public class RestrictedMapView extends MapView implements GoogleMap.OnCameraChangeListener {

    private CameraPosition mCameraPosition;

    private LatLngBounds mRegion;

    private int mMaxZoom, mMinZoom;

    private boolean mTouchEnabled = true;

    private GoogleMap.OnCameraChangeListener mCameraChangeListener;

    private GoogleMap.CancelableCallback mCancelCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
            mTouchEnabled = true;
        }

        @Override
        public void onCancel() {

        }
    };

    public RestrictedMapView(Context context) {
        super(context);
    }

    public RestrictedMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RestrictedMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RestrictedMapView(Context context, GoogleMapOptions options) {
        super(context, options);
    }

    /**
     * Call this after map has been created
     */
    public void initialize(){
        getMap().setOnCameraChangeListener(this);
    }

    public void setOnCameraChangeListener(GoogleMap.OnCameraChangeListener onCameraChangeListener){
        this.mCameraChangeListener = onCameraChangeListener;
    }

    /**
     * Sets the visible region we restrict the map to being in
     * @param visibleRegion
     */
    public void setVisibleRegion(int minZoom, int maxZoom, LatLngBounds visibleRegion){
        mRegion = visibleRegion;
        mMinZoom = minZoom;
        mMaxZoom = maxZoom;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(mCameraChangeListener!=null){
            mCameraChangeListener.onCameraChange(cameraPosition);
        }

        if(!mRegion.contains(cameraPosition.target)){
            mTouchEnabled = false;
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(mRegion, 0), mCancelCallback);
        } else {
            mCameraPosition = cameraPosition;
            if(mCameraPosition.zoom>mMaxZoom){
                mTouchEnabled = false;
                CameraPosition cameraPosition1 = new CameraPosition(cameraPosition.target, mMaxZoom, cameraPosition.tilt, cameraPosition.bearing);
                getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1), mCancelCallback);

            } else if(mCameraPosition.zoom<mMinZoom){
                mTouchEnabled = false;
                CameraPosition cameraPosition1 = new CameraPosition(cameraPosition.target, mMinZoom, cameraPosition.tilt, cameraPosition.bearing);
                getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1), mCancelCallback);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mTouchEnabled) {
            return super.dispatchTouchEvent(ev);
        } else{
            return true;
        }
    }
}
