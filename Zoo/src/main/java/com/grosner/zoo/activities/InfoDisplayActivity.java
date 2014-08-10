package com.grosner.zoo.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.grosner.painter.IconPainter;
import com.grosner.painter.actionbar.ActionBarAlphaSlider;
import com.grosner.smartinflater.annotation.SMethod;
import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.PlaceController;
import com.grosner.zoo.R;
import com.grosner.zoo.adapters.FeatureAdapter;
import com.grosner.zoo.database.PlaceManager;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.html.DownloadListener;
import com.grosner.zoo.html.InfoDisplayScraper;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.utils.DeviceUtils;
import com.grosner.zoo.utils.MapUtils;
import com.grosner.zoo.utils.StringUtils;
import com.grosner.zoo.utils.ViewUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;

import it.sephiroth.android.library.widget.HListView;

public class InfoDisplayActivity extends FragmentActivity implements MenuItem.OnMenuItemClickListener, LocationSource.OnLocationChangedListener, DownloadListener {

	private LatLng mPosition;

    @SResource private TextView title, distance;

    @SResource private TextView schedule, description;

    @SResource private ImageView placeImage;

    @SResource private HListView hListView;

    @SResource private ScrollView scrollView;

    private Location mLocation;

    private String mSnippet;

    private PlaceObject mPlace;

    private IconPainter mPainter;

    private ActionBarAlphaSlider mAlphaSlider;

    private boolean isLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if(!DeviceUtils.isTablet()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		setContentView(SmartInflater.inflate(this, R.layout.activity_info_display));

        getActionBar().setTitle(getString(R.string.bronx_zoo));

        String name = getIntent().getStringExtra("Title");
		title.setText(name);
        CurrentLocationManager.getSharedManager().activate(this);

        mLocation =  MapUtils.latLngToLocation((LatLng) getIntent().getParcelableExtra("LatLng"));

        mSnippet = getIntent().getStringExtra("Snippet");
        mPlace = PlaceManager.getManager().getPlaceByName(name);
        if(StringUtils.stringNotNullOrEmpty(mSnippet)) {
            //now we PWN their web page code for information
            try {
                new InfoDisplayScraper().getInfoContent(null,mSnippet, this);
                isLoading = true;
                supportInvalidateOptionsMenu();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        mPainter = new IconPainter(Color.WHITE);

        mAlphaSlider = new ActionBarAlphaSlider(false, getActionBar(), getResources().getColor(R.color.actionbar_color));
        mAlphaSlider.onSlide(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.info_display, menu);
		menu.findItem(R.id.navigate).setOnMenuItemClickListener(this);
        mPainter.paint(menu.findItem(R.id.webpage).setOnMenuItemClickListener(this));

        //color the icon based on if the item is a favorite
        if(mPlace!=null) {
            MenuItem favorite = menu.add(0, R.id.favorite, 0, getString(R.string.favorite));
            favorite.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            favorite.setOnMenuItemClickListener(this);
            favorite.setIcon(getResources().getDrawable(R.drawable.ic_action_pin));
            mPainter.paintColor(true, mPlace.isFavorite() ? getResources().getColor(R.color.curious_blue) : Color.WHITE, favorite);
        }

        if(isLoading) {
            ProgressBar progressBar = new ProgressBar(this);
            menu.add(0, R.id.progressBar, 0, "").setActionView(progressBar).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
		return true;
	}

    @SMethod
    private void onCreatePlaceImage(ImageView placeImage){
        int width = getResources().getDisplayMetrics().widthPixels;

        placeImage.setLayoutParams(new RelativeLayout.LayoutParams(width, width));
    }

    @SMethod
    private void onCreateScrollView(final ScrollView scrollView){
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();
                float value = Math.max(0, Math.min(500, scrollY));
                mAlphaSlider.onSlide(value/500f);
                Log.d(getClass().getName(), "Scroll: " + scrollY);
            }
        });
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
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
    public void onDownloadComplete(Object htmlObject) {
        if(StringUtils.stringNotNullOrEmpty(htmlObject.getImageUrl())) {
            Picasso.with(this).load(htmlObject.getImageUrl()).into(placeImage, new Callback() {
                @Override
                public void onSuccess() {
                    scrollView.scrollTo(0,0);
                }

                @Override
                public void onError() {
                }
            });
        }
        if(StringUtils.stringNotNullOrEmpty(htmlObject.getSchedule())) {
            schedule.setText(htmlObject.getSchedule());
        } else{
            ViewUtils.setViewsGone(schedule);
            ViewUtils.setViewsGone(this, R.id.scheduleTitle);
        }

        if(StringUtils.stringNotNullOrEmpty(htmlObject.getDescription())) {
            description.setText(htmlObject.getDescription());
        } else{
            ViewUtils.setViewsGone(description);
            ViewUtils.setViewsGone(this, R.id.descriptionTitle);
        }

        if(!htmlObject.features().isEmpty()) {
            hListView.setAdapter(new FeatureAdapter(htmlObject.features()));
        } else{
            ViewUtils.setViewsGone(hListView);
        }

        isLoading = false;
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onDownloadFailed() {
        ViewUtils.setViewsGone(hListView, schedule);
        ViewUtils.setViewsGone(this, R.id.descriptionTitle, R.id.scheduleTitle);
        isLoading = false;
        description.setText(getString(R.string.no_description_available));
        supportInvalidateOptionsMenu();
        mAlphaSlider.onSlide(1.0f);
    }
}
