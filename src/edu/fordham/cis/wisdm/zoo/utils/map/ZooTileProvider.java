package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public class ZooTileProvider implements TileProvider{

	private Activity mActivity = null;
	
	private String mTileFormat = "%d/%d/%d.png";
	
	public ZooTileProvider(Activity act){
		mActivity = act;
	}
	
	@Override
	public Tile getTile(int x, int y, int zoom) {
		Tile tile = null;
		InputStream is;
		try{
			is = mActivity.getResources().getAssets().open(getTileFile(x,y,zoom));
			Bitmap bm = BitmapFactory.decodeStream(is);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			tile = new Tile(256,256, byteArray);
		} catch(IOException e){
			e.printStackTrace();
		}
		
		return tile;
	}
	
	private String getTileFile(int x, int y, int z){
		return String.format(mTileFormat, z,x,y);
	}

}
