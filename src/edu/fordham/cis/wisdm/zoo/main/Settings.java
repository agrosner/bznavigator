package edu.fordham.cis.wisdm.zoo.main;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import android.os.Bundle;

public class Settings extends SherlockPreferenceActivity{

	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		addPreferencesFromResource(R.layout.preferences);
	}
	
}
