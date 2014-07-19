package com.grosner.zoo.activities;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.grosner.painter.IconPainter;
import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.PlaceController;
import com.grosner.zoo.R;
import com.grosner.zoo.database.PlaceManager;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.utils.HTMLScraper;
import com.grosner.zoo.utils.MapUtils;
import com.grosner.zoo.utils.StringUtils;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;

import it.sephiroth.android.library.widget.HListView;

public class InfoDisplayActivity extends FragmentActivity implements MenuItem.OnMenuItemClickListener, LocationSource.OnLocationChangedListener, HTMLScraper.DownloadListener {

	private LatLng mPosition;

    @SResource private TextView title, distance, info;

    @SResource private LinearLayout infoPage;

    @SResource private HListView horizontalListView;

    private Location mLocation;

    private String mSnippet;

    private PlaceObject mPlace;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(SmartInflater.inflate(this, R.layout.activity_info_display));

        //PowerInflater.loadBundle(this, getIntent().getExtras());
		
		getActionBar().setTitle("Information Page");
        String name = getIntent().getStringExtra("Title");
		title.setText(name);
        CurrentLocationManager.getSharedManager().activate(this);
		info.setVisibility(View.GONE);

        mLocation =  MapUtils.latLngToLocation((LatLng) getIntent().getParcelableExtra("LatLng"));

        mSnippet = getIntent().getStringExtra("Snippet");
        mPlace = PlaceManager.getManager().getPlaceByName(name);
        if(StringUtils.stringNotNullOrEmpty(mSnippet)) {
            //now we PWN their web page code for information
            try {
                new HTMLScraper().getInfoContent(infoPage,mSnippet, this);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else{
            TextView noData = HTMLScraper.getParagraphText(this);
            noData.setText(getString(R.string.no_description_available));
            infoPage.addView(noData);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.info_display, menu);
		menu.findItem(R.id.navigate).setOnMenuItemClickListener(this);
        menu.findItem(R.id.webpage).setOnMenuItemClickListener(this);

        //color the icon based on if the item is a favorite
        if(mPlace!=null) {
            MenuItem favorite = menu.add(0, R.id.favorite, 0, getString(R.string.favorite));
            favorite.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            favorite.setOnMenuItemClickListener(this);
            favorite.setIcon(getResources().getDrawable(R.drawable.ic_action_pin));
            new IconPainter(mPlace.isFavorite() ? getResources().getColor(R.color.curious_blue) : Color.WHITE).paint(favorite);
        }
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
        switch (item.getItemId()) {
            case R.id.navigate:
                CurrentLocationManager.getSharedManager().navigate(MapUtils.latLngToLocation(mPosition));

                String message = null;
            /*if(MAP.enableNavigation(SCREEN.getFollowItem(), MapUtils.latLngToLocation(mPosition)))
                message =  "Now navigating to "
                    + ((TextView)findViewById(R.id.title)).getText();
            else
                message = "Current location not found";
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show(); */
                break;
            case R.id.webpage:
                if(StringUtils.stringNotNullOrEmpty(mSnippet)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSnippet));
                    startActivity(intent);
                } else{
                    Toast.makeText(this, getString(R.string.no_snippet_available),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.favorite:
                if(mPlace!=null){
                    mPlace.setFavorite(!mPlace.isFavorite());
                    mPlace.save();
                    supportInvalidateOptionsMenu();
                }
                break;
        }
		return true;
	}

    @Override
    public void onLocationChanged(Location location) {
        distance.setText(PlaceController.calculateDistanceString(location,mLocation));
    }

    @Override
    public void onDownloadComplete() {

    }
}
