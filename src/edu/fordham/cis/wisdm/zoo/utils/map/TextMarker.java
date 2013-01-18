package edu.fordham.cis.wisdm.zoo.utils.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

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
	
	public static float DENSITY_SCALE = 1;
	
	public static float ZOOM_MIN = 16.5f;
	
	private float mZoomMin = 0;
	
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
	
	@Override
	public Marker addMarker(GoogleMap map, boolean draggable){
		generateBitmap();
		mMarker = map.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromBitmap(mBitmap))
			.title(getName()).position(getPoint()).draggable(draggable));
		return mMarker;
	}
	
	private void generateBitmap(){
		float scale = TEXT_SIZE*DENSITY_SCALE + 0.5f;
		int width = (int) (getName().length()*scale);
		mBitmap = Bitmap.createBitmap(width, (int)TEXT_SIZE + 20, Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		Paint text = new Paint();
		text.setTextSize(scale);
		text.setStrokeWidth(0);
		text.setColor(mColor);
		text.setStyle(Style.FILL_AND_STROKE);
		canvas.drawText(getName(), scale, scale + TEXT_SIZE, text);
	}
	
	/**
	 * When it resizes, the label will not appear if the text is smaller than
	 * the MIN_TEXT_SIZE
	 * @param map
	 * @param scale
	 */
	public void refreshWithZoom(GoogleMap map, float zoom){
		mMarker.remove();
		if((mZoomMin!=0 && mZoomMin>=ZOOM_MIN) || zoom>=ZOOM_MIN){
			addMarker(map, false);
		}
	}
	public void refresh(GoogleMap map){
		mMarker.remove();
		addMarker(map, false);
	}
}
