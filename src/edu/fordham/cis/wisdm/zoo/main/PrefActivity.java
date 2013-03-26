package edu.fordham.cis.wisdm.zoo.main;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class PrefActivity extends SherlockPreferenceActivity implements OnPreferenceClickListener{

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_prefs);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       
       Preference pref = this.findPreference("delete");
       pref.setOnPreferenceClickListener(this);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public boolean onPreferenceClick(Preference pref) {
		if(pref.getKey().equals("delete")){
			String fName = "gps";
			if(!deleteFile(fName + "1.txt"))
				Toast.makeText(this, "File not deleted", Toast.LENGTH_SHORT).show();
			else	Toast.makeText(this, "Files deleted", Toast.LENGTH_SHORT).show();
			if(!deleteFile(fName + "2.txt"))
				Toast.makeText(this, "Files not deleted", Toast.LENGTH_SHORT).show();
			return true;
		}
		
		return false;
	}

}
