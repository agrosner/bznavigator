package edu.fordham.cis.wisdm.zoo.utils.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
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
public class TextMarker extends PlaceItem{
	
	private Bitmap mBitmap;
	
	public static float TEXT_SIZE = 9;
	
	public static final float MIN_TEXT_SIZE = 9;
	
	private int mColor = Color.BLACK;
	
	public static float ZOOM_MIN = 16.5f;
	
	private float mZoomMin = 0;
	
	private Bitmap mImage =null;
	
	private Resources mRes = null;
	
	private boolean mUseImage = false;
	
	private boolean hasFocus = false;
	
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
	
	public TextMarker setImage(Resources res, int id){
		mImage = BitmapFactory.decodeResource(res, id);
		mRes = res;
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
	public boolean usesImage(){
		return mUseImage;
	}

	public void setFocus(boolean focus){
		hasFocus = focus;
	}
	
	@Override
	public Marker addMarker(GoogleMap map, boolean draggable, OnMarkerDragListener listener){
		generateBitmap();
		mMarker = map.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromBitmap(mBitmap))
			.title(getName()).position(getPoint()).draggable(draggable).anchor(0.5f, 1));
		return mMarker;
	}
	
	/**
	 * Creates the image to display on the map
	 */
	private void generateBitmap(){
		//scale DIP pixels
		float scale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE, mRes.getDisplayMetrics());
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
	 * the MIN_TEXT_SIZE
	 * @param map
	 * @param scale
	 */
	public void refreshWithZoom(GoogleMap map, float zoom){
		mMarker.remove();
		if((mZoomMin!=0 && mZoomMin>=ZOOM_MIN) || zoom>=ZOOM_MIN || hasFocus){
			addMarker(map, false, null);
		}
	}
	public void refresh(GoogleMap map){
		mMarker.remove();
		addMarker(map, false, null);
	}
}
