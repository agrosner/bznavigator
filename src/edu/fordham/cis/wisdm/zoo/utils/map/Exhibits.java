package edu.fordham.cis.wisdm.zoo.utils.map;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Class that reads in and displays exhibit polygons on the map
 * @author andrewgrosner
 *
 */
public class Exhibits {

	private static final String mFolder = "polygons";

	private static LinkedList<PolygonOptions> mPolygonOptions = null;
	
	private static LinkedList<Polygon> mPolygons = new LinkedList<Polygon>();
	
	public static void readFiles(Context con) throws IOException{
		if(mPolygonOptions!=null) return;
		
		mPolygonOptions = new LinkedList<PolygonOptions>();
		
		AssetManager assets = con.getAssets();
		String[] list = assets.list(mFolder);
		for(String fName: list){
			if(fName.endsWith(".txt")){
				Scanner scan = new Scanner(assets.open(mFolder+"/"+fName));
				PolygonOptions options = new PolygonOptions().geodesic(true).strokeWidth(1);
				int zIndex = 0;
				int i =0;
				while(scan.hasNext()){
					String line = scan.nextLine();
					String[] values = line.split(",");
					if(i!=0){
						options.add(new LatLng(Double.valueOf(values[0]), Double.valueOf(values[1])));
					} else{
						zIndex = Integer.valueOf(values[1]);
						i++;
					}
				}
				scan.close();
				int color = Color.BLACK;
				if(zIndex==1){
					color = Color.parseColor("#F0F0F0");
				} else if(zIndex==0){
					color = Color.parseColor("#FCF357");
				}
				mPolygonOptions.add(options.fillColor(color).zIndex(zIndex));
			}
		}
	}
	
	public static void addToMap(GoogleMap map){
		if(mPolygons == null){
			mPolygons = new LinkedList<Polygon>();
		} else{
			mPolygons.clear();
		}
		
		for(PolygonOptions option: mPolygonOptions){
			mPolygons.add(map.addPolygon(option));
		}
	}
}
