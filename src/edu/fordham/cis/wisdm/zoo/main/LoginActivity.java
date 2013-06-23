package edu.fordham.cis.wisdm.zoo.main;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import edu.fordham.cis.wisdm.zoo.main.constants.UserConstants;
import edu.fordham.cis.wisdm.zoo.utils.Operations;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import edu.fordham.cis.wisdm.zoo.utils.Connections;
import edu.fordham.cis.wisdm.zoo.utils.ZooDialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * This is the first screen the user sees. Handles the login screen.
 * @author Andrew
 *
 */
public class LoginActivity extends SherlockActivity implements OnClickListener, UserConstants{
	
	/**
	 * Form fields
	 */
	private EditText[] fields;
	
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
        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
       
        //initialize shared preference object
        Preference.initPrefForContext(this);
        
        isMember = Preference.getBoolean(REMEMBER_ME_LOC, false);
        
        email = Preference.getString(EMAIL_LOC, "");
    	password = Preference.getString(PASS_LOC, "");
        if(isMember && email.length()> 1 && password.length() >= PASS_LENGTH){
        	login(email, password, true);
        } else{
        	enhance();
        }
    }
   
    /**
     * Changes the layout objects if member, large screen, and chosen to remember
     */
    private void enhance(){
    	DisplayMetrics display = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(display);
    	int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                
    	if(screensize >= Configuration.SCREENLAYOUT_SIZE_LARGE)	largeScreen = true;
    	
    	if(!largeScreen){
    		this.setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
    	}
    	
    	setContentView(R.layout.login_extended);
    	
    	//set up buttons
    	Operations.setOnClickListeners(this, this, R.id.SignUp, R.id.LoginButton, R.id.GuestLogin);
    	
    	//set up text fields
    	fields = Operations.findEditTextViewsByIds(this,R.id.Email, R.id.Password);
    	rememberMe = (CheckBox) findViewById(R.id.RememberMe);
    	
    	ActionBar mAction = this.getSupportActionBar();
        mAction.setTitle("Choose Login");
    	largeScreen = false;
    	
    	
    	//play around with widgets and resize them
    	if(largeScreen && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
    		int newWidth = display.widthPixels/2;
    		
    		RelativeLayout loginScreen =  (RelativeLayout) findViewById(R.id.login);
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(newWidth, LayoutParams.WRAP_CONTENT);
    		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    		loginScreen.setLayoutParams(params);
    		
    	}
    	
    }
    
    /**
     * Begins the login process
     */
    private void login(String email, String password, boolean remember){
    	if(email.equals("") && isMember){
    		Toast.makeText(this, "Email cannot be blank", Toast.LENGTH_SHORT).show();
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
    	signup.putExtra("user", mConnection);
    	startActivityForResult(signup, 0);
    }
    
    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if(resultCode!=0){
    		Bundle ex = data.getExtras();
	    	if(ex.containsKey("reg") && resultCode == RESULT_OK){
	    		Operations.setViewTexts(fields, ex.getStringArray("reg"));
	    	}
    	}
    	
    }
    
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.LoginButton:
			isMember = true;
			login(fields[0].getText().toString(), fields[1].getText().toString(), rememberMe.isChecked());
			break;
		case R.id.SignUp:
			signUp();
			break;
		case R.id.GuestLogin:
			isMember = false;
			login("","", false);
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
			dia = ProgressDialog.show(new ContextThemeWrapper(mContext, R.style.AlertDialogAppTheme), "Connecting", "Authorizing with server");
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
					final ZooDialog survey = new ZooDialog(mContext);
					survey.setPositiveButton("Take it", new OnClickListener(){

						@Override
						public void onClick(View v) {
							startActivity(new Intent(mContext, SurveyActivity.class)
							.putExtra("user", mConnection));
							survey.dismiss();
						}
						
					});
					survey.setNegativeButton("Skip it", new OnClickListener(){

						@Override
						public void onClick(View v) {
							startActivity(new Intent(mContext, SlidingScreenActivity.class)
							.putExtra("user", mConnection));
							survey.dismiss();
						}
						
					});
					survey.setMessage("If you would like to take a survey to allow us" +
							" to learn how to best meet your needs, please click the button below");
					survey.setTitle("Visitor Survey");
					survey.show();
				}
			}
			dia.dismiss();
		}
		
	}
}