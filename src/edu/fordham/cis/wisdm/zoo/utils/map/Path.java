package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Scanner;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Path {

	private GoogleMap mMap = null;
	
	private static final String mFolder = "paths";

	private LinkedList<Polyline> mPolyLines = new LinkedList<Polyline>();
	
	public Path(GoogleMap map){
		mMap = map;
		
	}
	
	public Path readFiles(Context con) throws IOException{
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
				mPolyLines.add(mMap.addPolyline(mPolylineOptions.zIndex(2)));
			}
		}
	
		
		return this;
	}
	
}
