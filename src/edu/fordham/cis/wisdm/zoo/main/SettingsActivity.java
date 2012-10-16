package edu.fordham.cis.wisdm.zoo.main;

import java.io.FileNotFoundException;

import cis.fordham.edu.wisdm.messages.MessageBuilder;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends SherlockPreferenceActivity implements OnPreferenceClickListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.prefs);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       
       Preference pref = this.findPreference("delete");
       pref.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
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
	public boolean onPreferenceClick(Preference pref) {
		if(pref.getKey().equals("delete")){
			String fName = "gps";
			if(!deleteFile(fName + "1.txt"))
				MessageBuilder.showToast("File not deleted", this);
			if(!deleteFile(fName + "2.txt"))
				MessageBuilder.showToast("Files not deleted", this);
			return true;
		}
		
		return false;
	}

}
