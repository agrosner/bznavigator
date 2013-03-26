package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Class that reads in and displays path information
 * @author Andrew Grosner
 *
 */
public class Paths {

	private static final String mFolder = "paths";
	
	private static LinkedList<Polyline> mPolyLines = null;

	private static LinkedList<PolylineOptions> mOptions = null;
	
	public static void readFiles(Context con) throws IOException{
		if(mOptions !=null)	return;
	
		mOptions = new LinkedList<PolylineOptions>();
		AssetManager assets = con.getAssets();
		String[] list = assets.list(mFolder);
		for(String fName: list){
			if(fName.endsWith(".txt")){
				Scanner scan = new Scanner(assets.open(mFolder+"/"+fName));
				PolylineOptions mPolylineOptions = new PolylineOptions().color(Color.parseColor("#FCF357")).width(MapUtils.getDip(con, 5)).geodesic(true);
				while(scan.hasNext()){
					String line = scan.next();
					String[] values = line.split(",");
					if(values.length==2){
						mPolylineOptions.add(new LatLng(Double.valueOf(values[0]), Double.valueOf(values[1])));
					}
				}
				scan.close();
				mOptions.add(mPolylineOptions);
			}
		}
	}
	
	public static void addToMap(GoogleMap map){
		if(mPolyLines==null){
			mPolyLines = new LinkedList<Polyline>();
		} else{
			mPolyLines.clear();
		}
		for(PolylineOptions option:  mOptions){
			mPolyLines.add(map.addPolyline(option.zIndex(2)));
		}
	}
	
}
