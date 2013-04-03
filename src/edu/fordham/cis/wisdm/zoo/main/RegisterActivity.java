package edu.fordham.cis.wisdm.zoo.main;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.Operations;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Allows for a user to register information with the server
 * @author Andrew Grosner
 *
 */
public class RegisterActivity extends SherlockActivity implements OnClickListener {

	private Connections mConnection = null;
	
	private EditText[] fields;
	
	private static String ID;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        ID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        getSupportActionBar().setTitle("Create Account");
        
        fields = Operations.findEditTextViewsByIds(this, R.id.email, R.id.password, R.id.password_confirm);
        
        if(savedInstanceState!=null){
        	mConnection = (Connections) getIntent().getExtras().get("user");
        	Operations.setViewTexts(fields, mConnection.getmEmail(),mConnection.getmPassword());
        }
        
        Operations.setOnClickListeners(this, this, R.id.register);
    }
    
    /**
	 * When a user clicks on the top left of the screen to go back
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home)	onBackPressed();
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.register:
			attemptRegister();
			break;
		}
	}
	
	/**
	 * Attempts to register on the server
	 */
	private void attemptRegister(){
		String email = fields[0].getText().toString();
		String password = fields[1].getText().toString();
		String confPassword = fields[2].getText().toString();
		
		if(!password.equals(confPassword))
			Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
		else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
			new AlertDialog.Builder(this).setTitle("Error").setMessage("Ensure a valid email is entered correctly").setPositiveButton("OK", null).show();
		} else if(email.length() ==0 || password.length() == 0){
			Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
		} else if(password.length() < LoginActivity.PASS_LENGTH){
			Toast.makeText(this, "Password length must be at least 8 characters", Toast.LENGTH_SHORT).show();
		} else{ 
			mConnection = new Connections(email, password, ID);
			new AuthorizeUserTask(mConnection, this).execute();
		}
	}
	
	/**
	 * Authorizes user with the server in background thread
	 * @author agrosner
	 *
	 */
	private class AuthorizeUserTask extends AsyncTask<Void, Void, Void>{
		private Connections mConnection;
		
		private ProgressDialog dia;
		
		private Context mContext;
		
		private boolean isConnected = false;
		
		
		public AuthorizeUserTask(Connections con, Context cont){
			mConnection = con;
			mContext = cont;
		}

		@Override
		protected void onPreExecute(){
			dia = ProgressDialog.show(mContext, "Connecting", "Wait a moment as we register you");
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			isConnected = Connections.createUser(mConnection);
			Connections.disconnect();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void aarg){
			dia.dismiss();
			if(!isConnected)	Toast.makeText(mContext, "Authorization Failed:" + Connections.mServerMessage, Toast.LENGTH_SHORT).show();
			else {
				String[] reg = {mConnection.getmEmail(), mConnection.getmPassword()};
				setResult(RESULT_OK, getIntent().putExtra("reg", reg));
				finish();
			}
		}
		
	}
}
