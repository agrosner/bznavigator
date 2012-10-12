package edu.fordham.cis.wisdm.zoo.main;

import com.actionbarsherlock.app.SherlockActivity;

import cis.fordham.edu.wisdm.utils.FormChecker;
import cis.fordham.edu.wisdm.utils.Operations;
import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Entry extends SherlockActivity implements OnClickListener{
    /** Called when the activity is first created. */
	
	//location where the remember me preference is stored
	private static final String REMEMBER_ME_LOC = "edu.fordham.cis.wisdm.zoo.askagain";
	
	//the button collection
	private Button[] buttons = new Button[2];
	
	//text fields
	private EditText[]	fields = new EditText[2];
	
	//the rememeber me checkbox widget
	private CheckBox rememberMe;
	
	public static boolean largeScreen;
	
	private static boolean isMember = false;
	
	public static Connections mConnection = null;
	
	private boolean isConnected = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_extended);
       
        //initialize shared preference object
        Preference.initPrefForContext(this);
        
        chooseLogin();
    }
    
    /**
     * A popup dialog will ask a new user whether he/she wants to register or login as a guest
     */
    private void chooseLogin(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Choose your login");
    	builder.setMessage("Register for an account and receive multiple benefits!" +
    			"\nIncludes (future) visit path viewing, personal settings, social features, and many more");
    	builder.setPositiveButton("Member Login", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Preference.putBoolean("member", true);
				isMember = true;
				setUpView();
			}
    		
    	});
    	
    	builder.setNegativeButton("Login as Guest", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Preference.putBoolean("member", false);
				isMember = false;
				setUpView();
			}
		}).setCancelable(false).show();
    }
    
    /*
     * Loads up widgets into memory
     */
    private void setUpView(){
    	largeScreen = false;
    	
    	DisplayMetrics display = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(display);
    	 int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                
    	if(screensize >= Configuration.SCREENLAYOUT_SIZE_LARGE)	largeScreen = true;
    	
    	//set up buttons
    	int id[] = {R.id.SignUp, R.id.LoginButton};
    	Operations.findButtonViewsByIds(this, buttons, id);
    	Operations.setOnClickListeners(this, buttons);
    	
    	//set up text fields
    	int ids[] = {R.id.Email, R.id.Password};
    	Operations.findEditTextViewsByIds(this, fields, ids);
    	rememberMe = (CheckBox) findViewById(R.id.RememberMe);
    	
    	if(!isMember){
    		Operations.removeView(fields[1]);
    		Operations.removeView(rememberMe);
    		rememberMe.setChecked(false);
    	}
    	
    	boolean fill = Preference.getBoolean(REMEMBER_ME_LOC, false);
    	if(fill && isMember){
    		String email = Preference.getString("edu.fordham.cis.wisdm.zoo.email", "");
    		fields[0].setText(email);
    		String password = Preference.getString("edu.fordham.cis.wisdm.zoo.password", "");
    		fields[1].setText(password);
    		rememberMe.setChecked(true);
    	}
    	
    	this.getSupportActionBar().hide();
    	
    	//play around with widgets and resize them
    	if(largeScreen){
    		int newWidth = display.widthPixels/2;
    		
    		RelativeLayout loginScreen =  (RelativeLayout) findViewById(R.id.login);
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(newWidth, LayoutParams.WRAP_CONTENT);
    		params.addRule(RelativeLayout.CENTER_IN_PARENT);
    		loginScreen.setLayoutParams(params);
    		
    	}
    	
    }
    
    /**
     * Begins the login process
     */
    private void login(){
    	
    	String email = fields[0].getText().toString();
		String password = fields[1].getText().toString();
		
		if(!FormChecker.checkEmail(fields[0])){
			new AlertDialog.Builder(this).setTitle("Error").setMessage("Ensure email is entered correctly").show();
    	} else{
    		
    		if(rememberMe.isChecked() && isMember){
    			Preference.putString("edu.fordham.cis.wisdm.zoo.email", email);
    			Preference.putString("edu.cis.fordham.wisdm.zoo.password", password);
				Preference.putBoolean(REMEMBER_ME_LOC, true);
    		} else{
    			Preference.putString("edu.fordham.cis.wisdm.zoo.email", "");
    			Preference.putString("edu.cis.fordham.wisdm.zoo.password", "");
				Preference.putBoolean(REMEMBER_ME_LOC, false);
    		}
    		
    		if(isMember){
    			mConnection = new Connections(email, password, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
    			new ConnectToServerTask(mConnection, this).execute();
    		} else	if(!isMember){
    			Intent login = new Intent(this, SurveyActivity.class);
    			login.putExtra("email", email);
    			login.putExtra("password", password);
    			//Intent login = new Intent(this, OSMTestActivity.class);
    			startActivity(login);
    			//startActivity(new Intent(this, OfflineMapActivity.class));
    		}
    	}
    }
    
    private void signUp(){
    	Intent signup = new Intent(this, RegisterActivity.class);
    	startActivity(signup);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.LoginButton:
			login();
			break;
		case R.id.SignUp:
			signUp();
			break;
		}
	}
	
	private class ConnectToServerTask extends AsyncTask<Void, Void, Void>{
		private Connections mConnection;
		
		private ProgressDialog dia;
		
		private Context mContext;
		
		
		public ConnectToServerTask(Connections con, Context cont){
			mConnection = con;
			mContext = cont;
		}

		@Override
		protected void onPreExecute(){
			dia = ProgressDialog.show(mContext, "Connecting", "Authorizing with server");
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			isConnected = Connections.prepare(mConnection);
			Connections.disconnect();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void aarg){
			if(!isConnected)
				Toast.makeText(mContext, "Connection failed:\n" + Connections.mServerMessage, Toast.LENGTH_SHORT).show();
			else{
				Intent login = new Intent(mContext, SurveyActivity.class);
    			login.putExtra("email", mConnection.getmEmail());
    			login.putExtra("password", mConnection.getmPassword());
    			//Intent login = new Intent(this, OSMTestActivity.class);
    			startActivity(login);
    			//startActivity(new Intent(this, OfflineMapActivity.class));
			}
			dia.dismiss();
		}
		
	}
}

			
