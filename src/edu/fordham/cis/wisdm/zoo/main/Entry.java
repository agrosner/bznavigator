package edu.fordham.cis.wisdm.zoo.main;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.SherlockActivity;

import cis.fordham.edu.wisdm.messages.MessageBuilder;
import cis.fordham.edu.wisdm.utils.FormChecker;
import cis.fordham.edu.wisdm.utils.Operations;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class Entry extends SherlockActivity implements OnClickListener{
    /** Called when the activity is first created. */
	
	//location where the remember me preference is stored
	private static String REMEMBER_ME_LOC = "edu.fordham.cis.wisdm.zoo.askagain";
	
	//the button collection
	private Button[] buttons = new Button[2];
	
	//text fields
	private EditText[]	fields = new EditText[2];
	
	//the rememeber me checkbox widget
	private CheckBox rememberMe;
	
	//private CheckBox rememberMe;
	private AlertDialog loginDialog;
	
	public static boolean largeScreen;
	
	private static boolean isMember = false;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_extended);
        
        //initialize shared preference object
        Preference.initPrefForContext(this);
        
        chooseLogin();
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	
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
				setUpView(true);
			}
    		
    	});
    	
    	builder.setNegativeButton("Login as Guest", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Preference.putBoolean("member", false);
				setUpView(false);
			}
		}).setCancelable(false).show();
    }
    
    /*
     * Loads up widgets into memory
     */
    private void setUpView(boolean isMember){
    	largeScreen = false;
    	
    	DisplayMetrics display = new DisplayMetrics();
    	this.getWindowManager().getDefaultDisplay().getMetrics(display);
    	 int screensize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                
    	if(screensize >= Configuration.SCREENLAYOUT_SIZE_LARGE){
    		largeScreen = true;
    	}
    	
    	
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
			new HoloAlertDialogBuilder(this).setTitle("Error").setMessage("Ensure email is entered correctly").show();
    	} else{
    		
    		if(rememberMe.isChecked() && isMember){
    			Preference.putString("edu.fordham.cis.wisdm.zoo.email", email);
				Preference.putBoolean(REMEMBER_ME_LOC, true);
    		} else{
    			Preference.putString("edu.fordham.cis.wisdm.zoo.email", "");
				Preference.putBoolean(REMEMBER_ME_LOC, false);
    		}
    		
    		Intent login = new Intent(this, SurveyActivity.class);
    		login.putExtra("email", email);
    		startActivity(login);
    		//startActivity(new Intent(this, OfflineMapActivity.class));
    	}
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.LoginButton:
			login();
			break;
		}
	}
}

			
