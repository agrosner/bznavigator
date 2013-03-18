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

public class Exhibits {

	private GoogleMap mMap;
	
	private static final String mFolder = "polygons";

	private HashMap<Polygon, String> mPolygonMap = new HashMap<Polygon, String>();
	
	private LinkedList<Polygon> mPolygons = new LinkedList<Polygon>();
	
	public Exhibits(GoogleMap map){
		mMap = map;
	}
	
	public Exhibits readFiles(Context con) throws IOException{
		AssetManager assets = con.getAssets();
		String[] list = assets.list(mFolder);
		for(String fName: list){
			if(fName.endsWith(".txt")){
				Scanner scan = new Scanner(assets.open(mFolder+"/"+fName));
				PolygonOptions options = new PolygonOptions().geodesic(true);
				String name = "";
				int zIndex = 0;
				int i =0;
				while(scan.hasNext()){
					String line = scan.nextLine();
					String[] values = line.split(",");
					if(i!=0){
						options.add(new LatLng(Double.valueOf(values[0]), Double.valueOf(values[1])));
					} else{
						name +=values[0];
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
				Polygon poly = mMap.addPolygon(options.strokeWidth(1).zIndex(zIndex).fillColor(color));
				mPolygons.add(poly);
				mPolygonMap.put(poly, name);
			}
		}
		
		return this;
	}
}
