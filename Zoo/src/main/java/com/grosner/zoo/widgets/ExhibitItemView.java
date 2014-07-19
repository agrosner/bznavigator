package com.grosner.zoo.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.PlaceController;
import com.grosner.zoo.R;
import com.grosner.zoo.application.ZooApplication;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.utils.DeviceInfo;

/**
 * Created by: andrewgrosner
 * Date: 7/12/14.
 * Contributors: {}
 * Description:
 */
public class ExhibitItemView extends RelativeLayout {

    @SResource private ImageView image;

    @SResource private TextView distanceText;

    @SResource private TextView title;

    private PlaceObject mPlace;

    public ExhibitItemView(Context context) {
        super(context);
        setupUI();
    }

    public ExhibitItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupUI();
    }

    public ExhibitItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupUI();
    }

    protected void setupUI(){
        SmartInflater.inflate(this, R.layout.view_exhibit_item);

        int pad = DeviceInfo.dp(20);
        setPadding(pad, pad, pad,pad);
    }

    public void setPlace(PlaceObject place){
        mPlace = place;

        String title;
        String drawablePath;
        String distance;
        if(place==null){
            title = ZooApplication.getResourceString(R.string.view_all_on_map);
            drawablePath = "0";
            distance = "";
        } else{
            title = place.getName();
            drawablePath = place.getDrawable();
            distance = PlaceController.calculateDistanceString(CurrentLocationManager.getSharedManager().getLastKnownLocation(),
                    place.getLocation());
        }

        this.title.setText(title);

        if(!drawablePath.equals("0")){
            int drawableId = ZooApplication.getResourceId(drawablePath, "drawable");
            image.setImageDrawable(ZooApplication.getContext().getResources().getDrawable(drawableId));
        } else{
            image.setImageDrawable(null);
        }
        distanceText.setText(distance);
    }

    public void setDropDownView(){
        title.setTextColor(Color.BLACK);
        distanceText.setTextColor(Color.BLACK);
    }

    public PlaceObject getPlace() {
        return mPlace;
    }
}
