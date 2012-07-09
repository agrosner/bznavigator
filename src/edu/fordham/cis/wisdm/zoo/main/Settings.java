package edu.fordham.cis.wisdm.zoo.main;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends SherlockPreferenceActivity{

	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		addPreferencesFromResource(R.layout.preferences);
		
	}
	
}
