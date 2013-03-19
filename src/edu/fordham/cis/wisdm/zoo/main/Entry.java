package edu.fordham.cis.wisdm.zoo.main;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.Operations;
import edu.fordham.cis.wisdm.zoo.main.constants.UserConstants;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import edu.fordham.cis.wisdm.zoo.utils.Connections;
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
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the first screen the user sees. Handles the login screen.
 * @author Andrew
 *
 */
public class Entry extends SherlockActivity implements OnClickListener, UserConstants{
	
	/**
	 * Form fields
	 */
	private EditText[] fields = new EditText[2];
	
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
	
	private boolean start = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
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
    
    @Override
    public void onResume(){
    	super.onResume();
    	if(start)	chooseLogin();
    }
    
    /**
     * A popup dialog will ask a new user whether he/she wants to register or login as a guest
     */
    private void chooseLogin(){
    	new HoloAlertDialogBuilder(this).setTitle("Choose your login")
    		.setMessage("Register for an account and receive multiple benefits!" +
    			"\nIncludes (future) visit path viewing, personal settings, social features, and many more")
    		.setPositiveButton("Member Login", new DialogInterface.OnClickListener(){
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				Preference.putBoolean("member", true);
    				isMember = true;
    				enhance();
    			}
    		})
    		.setNegativeButton("Login as Guest", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				Preference.putBoolean("member", false);
    				isMember = false;
    				login("","", false);
    			}
    		}).setCancelable(false).show();
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
    	
    	//set up buttons
    	Operations.setViewOnClickListeners(this, this, R.id.SignUp, R.id.LoginButton);
    	
    	//set up text fields
    	Operations.findEditTextViewsByIds(this, fields, R.id.Email, R.id.Password);
    	rememberMe = (CheckBox) findViewById(R.id.RememberMe);
    	
    	ActionBar mAction = this.getSupportActionBar();
		mAction.setDisplayHomeAsUpEnabled(true);
        mAction.setTitle("Choose Login");
    	largeScreen = false;
    	
    	DisplayMetrics display = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(display);
    	int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                
    	if(screensize >= Configuration.SCREENLAYOUT_SIZE_LARGE)	largeScreen = true;
    	
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
    	if(email.equals("") && isMember){
    		MessageBuilder.showToast("Email cannot be blank", this);
    	} else if(isMember && !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
			new AlertDialog.Builder(this).setTitle("Error").setMessage("Ensure a valid email is entered correctly").setPositiveButton("OK", null).show();
		} else if(password.length() < PASS_LENGTH && isMember){
			new AlertDialog.Builder(this).setTitle("Error").setMessage("Ensure password is more than " + PASS_LENGTH + " characters").show();
		} else{
			Preference.putBoolean(REMEMBER_ME_LOC, remember);
			mConnection = new Connections(email, password, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
			
    		if(isMember){
    			Preference.putString(EMAIL_LOC, email);
    			Preference.putString(PASS_LOC, password);
    		} else{
    			Preference.putString(EMAIL_LOC, "");
    			Preference.putString(PASS_LOC, "");
    		}
    		new ConnectToServerTask(mConnection, this).execute();
    	}
    }
    
    private void signUp(){
    	Intent signup = new Intent(this, RegisterActivity.class);
    	mConnection = new Connections(
    			fields[0].getText().toString(), 
    			fields[1].getText().toString(), 
    			Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
    	startActivityForResult(signup, 0);
    }
    
    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	Bundle ex = data.getExtras();
    	if(ex.containsKey("reg") && resultCode == RESULT_OK){
    		Operations.setViewTexts(fields, ex.getStringArray("reg"));
    	}
    	
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
			if(!isConnected && isMember){
				enhance();
				Toast.makeText(mContext, "Connection failed:\n" + Connections.mServerMessage, Toast.LENGTH_SHORT).show();
				Operations.setViewTexts(fields, mConnection.getmEmail(), mConnection.getmPassword());
				rememberMe.setChecked(true);
				Preference.putBoolean("member", false);
				Preference.putBoolean(REMEMBER_ME_LOC, false);
	    		
			}else{
				if(!gotVisit && isMember){
					Toast.makeText(mContext, "Could not register visit for some reason", Toast.LENGTH_SHORT).show();
				}else{
					startActivity(new Intent(mContext, SurveyActivity.class)
					.putExtra("user", mConnection));
				}
			}
			dia.dismiss();
			start = true;
		}
		
	}
}