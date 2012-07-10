package edu.fordham.cis.wisdm.zoo.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

import cis.fordham.edu.wisdm.utils.Operations;

import com.actionbarsherlock.app.SherlockActivity;

public class SplashScreenActivity extends SherlockActivity implements OnClickListener{

	private Button[] mSplashButtons = new Button[4];
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		setContentView(R.layout.splash_screen);
		
		
		int id[] = {R.id.Restrooms, R.id.Exhibits, R.id.Map, R.id.News};
		Operations.findButtonViewsByIds(this, mSplashButtons, id);
		Operations.setOnClickListeners(this, mSplashButtons);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.Exhibits:
			
			break;
		case R.id.Restrooms:
			
			break;
		case R.id.Map:
			startActivity(new Intent(this, Map.class));
			break;
		case R.id.News:
			
			break;
		}
	}
	
	
}
