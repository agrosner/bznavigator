package edu.fordham.cis.wisdm.zoo.main;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import edu.fordham.cis.wisdm.zoo.utils.BoundedMapView;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.widget.Toast;

public class OSMTestActivity extends Activity{

	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		
		MapView map = new MapView(this, 256);
		map.setClickable(true);
		map.setBuiltInZoomControls(true);
		
		setContentView(map);
		
		map.setMultiTouchControls(true);
		map.getController().setCenter(new GeoPoint(40.85258795538737, -73.87820194901391));
		map.setUseDataConnection(false);
		
		map.setTileSource(TileSourceFactory.MAPNIK);
		
		map.getController().setZoom(16);
		//map.setScrollableAreaLimit(new BoundingBoxE6(40.859525, -73.866578,40.8387492,-73.883056));
		
		ResourceProxy mResourceProxy = new DefaultResourceProxyImpl(this);
		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		OverlayItem bears = new OverlayItem("Me", "Bears", new GeoPoint(40.84891330737599,-73.87748480503043));
		bears.setMarker(getResources().getDrawable(R.drawable.ic_action_location));
		items.add(bears);
		
		OverlayItem sea = new OverlayItem("Me", "Sea Lions", new GeoPoint(40.85342087209025,-73.8781784789433));
		bears.setMarker(getResources().getDrawable(R.drawable.ic_action_location));
		items.add(sea);
		
		
		OsmMapsItemizedOverlay overlay = new OsmMapsItemizedOverlay(items,  
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>()
                {
            @Override
            public boolean onItemSingleTapUp(final int index, final OverlayItem item)
            {

                Toast.makeText(OSMTestActivity.this,
                        "Item " + item.getPoint().toString(),
                        Toast.LENGTH_LONG).show();

                return true; // We 'handled' this event.
            }

            @Override
            public boolean onItemLongPress(final int index, final OverlayItem item)
            {

                Toast.makeText(OSMTestActivity.this,
                        "Item '" + item.mTitle + "' (index=" + index + ") got long pressed", Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        }, mResourceProxy);
		overlay.addItem(bears);
		map.getOverlays().add(overlay);
	}
	public class OsmMapsItemizedOverlay extends ItemizedIconOverlay<OverlayItem>
	{
	    private ArrayList<OverlayItem> mItemList = new ArrayList<OverlayItem>();

	    public OsmMapsItemizedOverlay(ArrayList<OverlayItem> pList,
	            ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener, ResourceProxy pResourceProxy)
	    {
	        super(pList, pOnItemGestureListener, pResourceProxy);
	        mItemList = pList;
	        // TODO Auto-generated constructor stub
	    }

	    public void addOverlay(OverlayItem aOverlayItem)
	    {
	        mItemList.add(aOverlayItem);
	        populate();
	    }

	    public void removeOverlay(OverlayItem aOverlayItem)
	    {
	        mItemList.remove(aOverlayItem);
	        populate();
	    }

	    @Override
	    protected OverlayItem createItem(int i)
	    {
	        return mItemList.get(i);
	    }

	    @Override
	    public int size()
	    {
	        if (mItemList != null)
	            return mItemList.size();
	        else
	            return 0;
	    }

	    @Override
	    public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3)
	    {
	        // TODO Auto-generated method stub
	        return false;
	    }

	}
}
