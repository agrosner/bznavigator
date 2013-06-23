package edu.fordham.cis.wisdm.zoo.main;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.gms.maps.model.LatLng;

import edu.fordham.cis.wisdm.zoo.utils.HTMLScraper;
import edu.fordham.cis.wisdm.zoo.utils.Operations;
import edu.fordham.cis.wisdm.zoo.utils.map.MapUtils;
import edu.fordham.cis.wisdm.zoo.utils.map.MapViewFragment;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InfoDisplayActivity extends SherlockActivity implements OnMenuItemClickListener {

	public static SlidingScreenActivity SCREEN;
	
	private LatLng mPosition;
	
	public static MapViewFragment MAP = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info_display);
		
		//get the marker information that we need
		Bundle bundle = getIntent().getExtras();
		getSupportActionBar().setTitle("Information Page");
		Operations.setViewText(this, (String)bundle.get("title"), R.id.title);
		Operations.setViewText(this, (String)bundle.getString("distance"), R.id.distance);
		Operations.removeView(findViewById(R.id.info));
		
		//mScreen = (SlidingScreenActivity) bundle.get("act");
		mPosition = (LatLng) bundle.get("position");
		
		//now we PWN their web page code for information
		try {
			new HTMLScraper().getInfoContent(this, 
					(LinearLayout)findViewById(R.id.info_page), 
					(String)bundle.getString("snippet"));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.info_display, menu);
		menu.findItem(R.id.navigate).setOnMenuItemClickListener(this);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		MAP.getManager().navigate(MapUtils.latLngToLocation(mPosition));
		
		String message = null;
		if(MAP.enableNavigation(SCREEN.getFollowItem(), MapUtils.latLngToLocation(mPosition)))
			message =  "Now navigating to "
				+ ((TextView)findViewById(R.id.title)).getText();
		else
			message = "Current location not found";
		Toast.makeText(this,message,Toast.LENGTH_SHORT).show(); 
		return true;
	}

}
