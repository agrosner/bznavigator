package edu.fordham.cis.wisdm.zoo.main;

import cis.fordham.edu.wisdm.utils.FormChecker;
import cis.fordham.edu.wisdm.utils.Operations;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.Preference;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class RegisterActivity extends SherlockActivity implements OnClickListener {

	private Connections mConnection = null;
	
	private Button register;
	private EditText emailField;
	private EditText passField;
	private EditText confPassField;
	private ImageButton[] imButtons = new ImageButton[3];
	
	private static String ID;
	
	Facebook facebook = new Facebook("503456236331740");
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        ID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        
        ActionBar action = this.getSupportActionBar();
        action.setTitle("Create Account");
        
        emailField = (EditText) findViewById(R.id.email);
        passField = (EditText) findViewById(R.id.password);
        confPassField = (EditText) findViewById(R.id.password_confirm);
        
        if(Entry.mConnection!=null){
        	mConnection = Entry.mConnection;
        	emailField.setText(mConnection.getmEmail());
        	passField.setText(mConnection.getmPassword());
        }
        
        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(this);
        
        int id2[] = {R.id.facebook, R.id.twitter, R.id.linkedin};
        Operations.findImageButtonViewsByIds(this, imButtons, id2);
        Operations.setOnClickListeners(this, imButtons);
        
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_register, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.register:
			attemptRegister();
			break;
		case R.id.facebook:
			facebook();
			break;
		}
	}
	
	/**
	 * Attempts to register on the server
	 */
	private void attemptRegister(){
		String email = emailField.getText().toString();
		String password = passField.getText().toString();
		String confPassword = confPassField.getText().toString();
		
		if(!password.equals(confPassword))
			Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
		else if(!FormChecker.checkEmail(emailField)){
			FormChecker.showFormError(this);
		} else if(email.length() ==0 || password.length() == 0){
			Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
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
			if(!isConnected)	Toast.makeText(mContext, "Authorization Failed:\n" + Connections.mServerMessage, Toast.LENGTH_SHORT).show();
			else				finish();
		}
		
	}
	
	private void facebook(){
		String token = Preference.getString("access_token", null);
		long expires = Preference.getLong("access_expires", 0);
		
		if(token!=null)		facebook.setAccessToken(token);
		if(expires != 0)	facebook.setAccessExpires(expires);

		if(!facebook.isSessionValid()){
		
			facebook.authorize(this, new String[]{"email"}, new DialogListener(){

				@Override
				public void onComplete(Bundle values) {
					Preference.putString("access_token", facebook.getAccessToken());
					Preference.putLong("access_expires", facebook.getAccessExpires());
					
					
				}
				
				@Override
				public void onFacebookError(FacebookError e) {
				// 	TODO Auto-generated method stub
					
				}
				
				@Override
				public void onError(DialogError e) {
				// 	TODO Auto-generated method stub
					
				}
				
				@Override
				public void onCancel() {
				// 	TODO Auto-generated method stub
					
				}
				
			});
		}
	}
	
	 @Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 facebook.authorizeCallback(requestCode, resultCode, data);
	 }
}
