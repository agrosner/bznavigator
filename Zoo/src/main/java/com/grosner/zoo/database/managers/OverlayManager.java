package com.grosner.zoo.database.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;

import com.activeandroid.manager.SingleDBManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.grosner.zoo.database.PathObject;
import com.grosner.zoo.database.PathPointObject;
import com.grosner.zoo.utils.DeviceInfo;

/**
 * Manages polygons and polylines displayed on the map. Reads them from files and puts them on the map
 * @author andrewgrosner
 *
 */
public class OverlayManager {
	
	private static final String mPolylineFolder = "paths";
	
	private static final String mPolygonFolder = "polygons";

	private static LinkedList<Polyline> mPolyLines = null;

	private static LinkedList<PolylineOptions> mPolylineOptions = null;

	private static LinkedList<PolygonOptions> mPolygonOptions = null;
	
	private static LinkedList<Polygon> mPolygons = new LinkedList<Polygon>();
	
	public static void readFiles(Context con) throws IOException{
		readPolylineFiles(con);
		readPolygonFiles(con);
	}
	
	private static void readPolylineFiles(Context con) throws IOException{
		if(mPolylineOptions !=null)	return;
	
		mPolylineOptions = new LinkedList<>();

        //if data not in DB yet
        if(SingleDBManager.getSharedInstance().getCount(PathPointObject.class)==0) {
            AssetManager assets = con.getAssets();
            String[] list = assets.list(mPolylineFolder);
            for (String fName : list) {
                if (fName.endsWith(".txt")) {
                    Scanner scan = new Scanner(assets.open(mPolylineFolder + "/" + fName));
                    PathObject pathObject = new PathObject();
                    pathObject.setName(fName);
                    pathObject.save();

                    List<PathPointObject> pathPointObjects = new ArrayList<>();

                    int order = 0;
                    while (scan.hasNext()) {
                        String line = scan.next();
                        String[] values = line.split(",");
                        if (values.length == 2) {
                            PathPointObject pathPointObject = new PathPointObject();
                            pathPointObject.setSort_order(order);
                            pathPointObject.setLatitude(Double.valueOf(values[0]));
                            pathPointObject.setLongitude(Double.valueOf(values[1]));
                            pathPointObject.setPath_name(fName);
                            pathPointObjects.add(pathPointObject);
                        }
                        order++;
                    }
                    scan.close();
                    SingleDBManager.getSharedInstance().addAll(pathPointObjects);
                }
            }
        }

        List<PathObject> pathObjects = SingleDBManager.getSharedInstance().getAll(PathObject.class);
        for(PathObject pathObject: pathObjects){
            PolylineOptions polylineOptions = new PolylineOptions().color(Color.parseColor("#F3FFF4"))
                    .width(DeviceInfo.dpFloat(5)).geodesic(true);
            List<PathPointObject> pathPointObjects = pathObject.points();
            for(PathPointObject pathPointObject: pathPointObjects){
                polylineOptions.add(new LatLng(pathPointObject.getLatitude(), pathPointObject.getLongitude()));
            }
            mPolylineOptions.add(polylineOptions);
        }
	}
	
	public static void readPolygonFiles(Context con) throws IOException{
		if(mPolygonOptions!=null) return;
		
		mPolygonOptions = new LinkedList<>();
		
		AssetManager assets = con.getAssets();
		String[] list = assets.list(mPolygonFolder);
		for(String fName: list){
			if(fName.endsWith(".txt")){
				Scanner scan = new Scanner(assets.open(mPolygonFolder+"/"+fName));
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
					color = Color.parseColor("#A9BDB0");
				} else if(zIndex==0){
					color = Color.parseColor("#E7E7E7");
				}
				mPolygonOptions.add(options.fillColor(color).zIndex(zIndex));
			}
		}
	}
	
	/**
	 * Adds all of the objects to the map
	 * @param map
	 */
	public static void addToMap(GoogleMap map){
		if(mPolyLines==null){
			mPolyLines = new LinkedList<Polyline>();
		} else{
			mPolyLines.clear();
		}
		for(PolylineOptions option:  mPolylineOptions){
			mPolyLines.add(map.addPolyline(option.zIndex(2)));
		}
		
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
