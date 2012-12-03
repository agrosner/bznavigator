package edu.fordham.cis.wisdm.zoo.main;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import cis.fordham.edu.wisdm.utils.FormChecker;
import cis.fordham.edu.wisdm.utils.Operations;
import edu.fordham.cis.wisdm.zoo.main.constants.UserConstants;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import edu.fordham.cis.wisdm.zoo.utils.connections.Connections;
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
import android.widget.TextView;
import android.widget.Toast;

public class Entry extends SherlockActivity implements OnClickListener, UserConstants{
	
	/**
	 * Button collection
	 */
	private static Button[] buttons = new Button[2];
	
	/**
	 * Form fields
	 */
	private static EditText[] fields = new EditText[2];
	
	/**
	 * "Remember Me" checkbox widget
	 */
	private CheckBox rememberMe;
	
	/**
	 * Whether device is a large screen or not
	 */
	public static boolean largeScreen;
	
	/**
	 * Whether user is a member and has saved login information
	 */
	private static boolean isMember = false;
	
	/**
	 * Connection to the server handle
	 */
	public static Connections mConnection = null;
	
	/**
	 * If active connection or not
	 */
	private boolean isConnected = false;
	
	private String email;
	
	private String password;
	
	public static final int PASS_LENGTH = 5;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.login_extended);
       
        //initialize shared preference object
        Preference.initPrefForContext(this);
        
        isMember = Preference.getBoolean(REMEMBER_ME_LOC, false);
        
        email = Preference.getString(EMAIL_LOC, "");
    	password = Preference.getString(PASS_LOC, "");
        if(isMember && email.length()> 1 && password.length() >= PASS_LENGTH){
        	login(email, password, true);
        } else{
        	chooseLogin();
        }
    }
    
    /**
     * A popup dialog will ask a new user whether he/she wants to register or login as a guest
     */
    private void chooseLogin(){
    	HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(this);
    	builder.setTitle("Choose your login");
    	builder.setMessage("Register for an account and receive multiple benefits!" +
    			"\nIncludes (future) visit path viewing, personal settings, social features, and many more");
    	builder.setPositiveButton("Member Login", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Preference.putBoolean("member", true);
				isMember = true;
				enhance();
			}
    		
    	});
    	
    	builder.setNegativeButton("Login as Guest", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Preference.putBoolean("member", false);
				isMember = false;
				enhance();
			}
		}).setCancelable(false).show();
    }
    
    /*
     * Loads up widgets into memory
     */
    private void setUpView(){
    	//set up buttons
    	int id[] = {R.id.SignUp, R.id.LoginButton};
    	Operations.findButtonViewsByIds(this, buttons, id);
    	Operations.setOnClickListeners(this, buttons);
    	
    	//set up text fields
    	int ids[] = {R.id.Email, R.id.Password};
    	Operations.findEditTextViewsByIds(this, fields, ids);
    	rememberMe = (CheckBox) findViewById(R.id.RememberMe);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == android.R.id.home)	{
			Intent intent = getIntent();
	    	finish();
	    	startActivity(intent);
		}
		return false;
	}
    
    /**
     * Changes the layout objects if member, large screen, and chosen to remember
     */
    private void enhance(){
    	setContentView(R.layout.login_extended);
    	setUpView();
    	
    	ActionBar mAction = this.getSupportActionBar();
		mAction.setDisplayHomeAsUpEnabled(true);
        mAction.setTitle("Choose Login");
    	largeScreen = false;
    	
    	DisplayMetrics display = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(display);
    	 int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                
    	if(screensize >= Configuration.SCREENLAYOUT_SIZE_LARGE)	largeScreen = true;
    	
    	if(!isMember){
    		Operations.removeView(fields[1]);
    		Operations.removeView(rememberMe);
    		rememberMe.setChecked(false);
    	}
    	
    	
    	//play around with widgets and resize them
    	if(largeScreen){
    		int newWidth = display.widthPixels/2;
    		
    		RelativeLayout loginScreen =  (RelativeLayout) findViewById(R.id.login);
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(newWidth, LayoutParams.WRAP_CONTENT);
    		params.addRule(RelativeLayout.CENTER_IN_PARENT);
    		loginScreen.setLayoutParams(params);
    		
    		TextView title = (TextView) loginScreen.findViewById(R.id.Welcome);
    		title.setTextSize(40);
    		
    	}
    	
    }
    
    /**
     * Begins the login process
     */
    private void login(String email, String password, boolean remember){
		if(!FormChecker.checkEmail(email)){
			new AlertDialog.Builder(this).setTitle("Error").setMessage("Ensure email is entered correctly").show();
		} else if(password.length() < PASS_LENGTH && isMember){
			new AlertDialog.Builder(this).setTitle("Error").setMessage("Ensure password is more than 8 characters").show();
		} else{
			Preference.putBoolean(REMEMBER_ME_LOC, remember);
    		if(isMember){
    			Preference.putString(EMAIL_LOC, email);
    			Preference.putString(PASS_LOC, password);
    			mConnection = new Connections(email, password, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
    			new ConnectToServerTask(mConnection, this).execute();
    		} else{
    			Preference.putString(EMAIL_LOC, "");
    			Preference.putString(PASS_LOC, "");
    			startActivity(new Intent(this, SurveyActivity.class)
				.putExtra("email", email)
				.putExtra("password", password));
    		}
    	}
    }
    
    private void signUp(){
    	Intent signup = new Intent(this, RegisterActivity.class);
    	startActivity(signup);
    }
    
    public static void setFields(String email, String password){
   		fields[0].setText(email);
   		fields[1].setText(password);
    }
    
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.LoginButton:
			login(fields[0].getText().toString(), fields[1].getText().toString(), rememberMe.isChecked());
			break;
		case R.id.SignUp:
			signUp();
			break;
		}
	}
	
	/**
	 * Connects user to server for authentication
	 * @author Andrew Grosner
	 *
	 */
	private class ConnectToServerTask extends AsyncTask<Void, Void, Void>{
		private Connections mConnection;
		
		private ProgressDialog dia;
		
		private Context mContext;
		
		private boolean gotVisit = false;
		
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
			gotVisit = Connections.visit(mConnection);
			Connections.disconnect();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void aarg){
			if(!isConnected){
				enhance();
				Toast.makeText(mContext, "Connection failed:\n" + Connections.mServerMessage, Toast.LENGTH_SHORT).show();
				setFields(mConnection.getmEmail(), mConnection.getmPassword());
				rememberMe.setChecked(true);
			}else{
				if(!gotVisit){
					Toast.makeText(mContext, "Could not register visit for some reason", Toast.LENGTH_SHORT).show();
				}else{
					startActivity(new Intent(mContext, SurveyActivity.class)
					.putExtra("email", mConnection.getmEmail())
					.putExtra("password", mConnection.getmPassword()));
				}
			}
			dia.dismiss();
		}
		
	}
}

			
