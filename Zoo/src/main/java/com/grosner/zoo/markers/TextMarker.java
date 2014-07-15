package com.grosner.zoo.markers;

import java.io.Serializable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.utils.Operations;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.TypedValue;

/**
 * Very useful in displaying text-only marker labels
 * @author Andrew Grosner
 *
 */
public class TextMarker extends PlaceMarker implements Serializable{
	
	/**
	 * The image that the text is placed onto
	 */
	private Bitmap mBitmap;
	
	/**
	 * Text size of the text marker
	 */
	public static float TEXT_SIZE = 9;
	
	/**
	 * The text color
	 */
	private int mColor = Color.BLACK;
	
	/**
	 * Minimum zoom to consider text to be shown, global constraint
	 */
	private static float ZOOM_MIN = 16.5f;
	
	/**
	 * Minimum zoom of this item to be shown, local constraint
	 */
	private float mZoomMin = 0;
	
	/**
	 * The icon that displayed when this item has focus
	 */
	private Bitmap mImage =null;
	
	/**
	 * Whether to use the image associated with this marker
	 */
	private boolean mUseImage = false;
	
	/**
	 * Whether this text has focus (icon appears next to text and text is displayed at all zoom levels)
	 */
	private boolean hasFocus = false;

    public TextMarker place(PlaceObject placeObject){
        super.place(placeObject);
        return setImage(mIconId);
    }
	
	public TextMarker color(int color){
		mColor = color;
		return this;
	}
	
	/**
	 * Sets a min zoom this text appears in
	 * @param zoom
	 * @return
	 */
	public TextMarker minZoom(float zoom){
		mZoomMin = zoom;
		return this;
	}
	
	/**
	 * Sets the image resource for the textmarker when it has focus
	 * @param res
	 * @param id
	 * @return
	 */
	public TextMarker setImage(int id){
		mImage = BitmapFactory.decodeResource(ZooApplication.getContext().getResources(), id);
		return this;
	}
	
	/**
	 * Tries to use image provided to the marker, if none is provided, it will fail
	 * @param use
	 * @return
	 */
	public TextMarker useImage(boolean use){
		if(mImage!=null)
			mUseImage = use;
		return this;
	}
	
	/**
	 * Whether the item to use an image or not
	 * @return
	 */
	public boolean usesImage(){
		return mUseImage;
	}

	/**
	 * Sets this item to display persistently on the map at any zoom level
	 * @param focus
	 */
	public void setFocus(boolean focus){
		hasFocus = focus;
	}
	
	@Override
	public Marker addMarker(GoogleMap map, boolean draggable, OnMarkerDragListener listener){
		generateBitmap();
		mMarker = map.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromBitmap(mBitmap))
			.title(getName()).snippet(getLink()).position(getPoint()).draggable(draggable).anchor(0.5f, 1));
		return mMarker;
	}
	
	/**
	 * Creates the image to display on the map
	 */
	private void generateBitmap(){
		//scale DIP pixels
		float scale = Operations.dp(TEXT_SIZE);
		Paint text = new Paint();
		text.setTextSize(scale);
		text.setStrokeWidth(0);
		text.setColor(mColor);
		text.setStyle(Style.FILL_AND_STROKE);
		
		Rect bounds = new Rect();
		text.getTextBounds(getName(), 0, getName().length(), bounds);
		
		int width = (int) Math.round(bounds.width());
		int height = (int) Math.round(bounds.height());
		if(mUseImage){
			width+=mImage.getWidth()+5;
			height=mImage.getHeight();
		} else{
			height*=2;
			width+=5;
		}
		
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		
		canvas.drawText(getName(), 1, (height/2), text);
		
		if(mUseImage){
			canvas.drawBitmap(mImage, mBitmap.getWidth()-mImage.getWidth(),mBitmap.getHeight()-mImage.getWidth(), new Paint());
		}
	}
	
	/**
	 * When it resizes, the label will not appear if the text is smaller than
	 * @param map
	 * @param scale
	 */
	public void refreshWithZoom(GoogleMap map, float zoom){
		boolean info = false;
		if(mMarker!=null){
			info = mMarker.isInfoWindowShown();
			mMarker.remove();
		} 
			
		if((mZoomMin!=0 && zoom>=mZoomMin) || zoom>=ZOOM_MIN || hasFocus){
			addMarker(map, false, null);
			if(info){
				mMarker.showInfoWindow();
			}
		}
	}
	
	/**
	 * Removes and re-adds this textmarker to the map
	 * @param map
	 */
	public void refresh(GoogleMap map){
		mMarker.remove();
		addMarker(map, false, null);
	}
	
	/**
	 * If this textmarker shares title and position of the marker compared
	 * @param marker
	 * @return
	 */
	public boolean equals(Marker marker){
		return (mMarker.getTitle().equals(marker.getTitle()) && 
				mMarker.getPosition().equals(marker.getPosition()));
	}
}
