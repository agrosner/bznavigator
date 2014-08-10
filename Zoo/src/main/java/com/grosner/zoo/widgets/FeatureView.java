package com.grosner.zoo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.grosner.smartinflater.annotation.SResource;
import com.grosner.smartinflater.view.SmartInflater;
import com.grosner.zoo.R;
import com.grosner.zoo.database.content.FeatureObject;
import com.squareup.picasso.Picasso;

/**
 * Created by: andrewgrosner
 * Date: 7/22/14.
 * Contributors: {}
 * Description:
 */
public class FeatureView extends FrameLayout {

    @SResource private TextView featureText;
    @SResource private ImageView featureImage;

    public FeatureView(Context context) {
        super(context);
        setupUI();
    }

    public FeatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupUI();
    }

    public FeatureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupUI();
    }

    protected void setupUI(){
        SmartInflater.inflate(this, R.layout.view_feature);
    }

    public void setFeature(FeatureObject feature){
        featureText.setText(feature.getLabel());
        Picasso.with(getContext()).load(feature.getImageUrl()).into(featureImage);
    }
}
