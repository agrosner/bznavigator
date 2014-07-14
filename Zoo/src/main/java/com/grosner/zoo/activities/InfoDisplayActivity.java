package com.grosner.zoo.activities;

import java.io.IOException;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import org.apache.http.client.ClientProtocolException;

import com.google.android.gms.maps.model.LatLng;

import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.R;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.utils.HTMLScraper;
import com.grosner.zoo.utils.MapUtils;
import com.grosner.zoo.fragments.MapViewFragment;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InfoDisplayActivity extends FragmentActivity implements MenuItem.OnMenuItemClickListener {

	private LatLng mPosition;

    public static ZooActivity SCREEN;

    @SResource private TextView title, distance, info;
    private String mTitle, mDistance, mSnippet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(SmartInflater.inflate(this, R.layout.activity_info_display));

        //PowerInflater.loadBundle(this, getIntent().getExtras());
		
		getActionBar().setTitle("Information Page");
		title.setText(mTitle);
        distance.setText(mDistance);
		info.setVisibility(View.GONE);

		//now we PWN their web page code for information
		try {
			new HTMLScraper().getInfoContent(this, 
					(LinearLayout)findViewById(R.id.info_page), 
					mSnippet);
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
		getMenuInflater().inflate(R.menu.info_display, menu);
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
		CurrentLocationManager.getSharedManager().navigate(MapUtils.latLngToLocation(mPosition));
		
		String message = null;
		/*if(MAP.enableNavigation(SCREEN.getFollowItem(), MapUtils.latLngToLocation(mPosition)))
			message =  "Now navigating to "
				+ ((TextView)findViewById(R.id.title)).getText();
		else
			message = "Current location not found";
		Toast.makeText(this,message,Toast.LENGTH_SHORT).show(); */
		return true;
	}

}
