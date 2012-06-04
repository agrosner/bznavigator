package edu.fordham.cis.wisdm.zoo.main;

import edu.fordham.cis.wisdm.utils.FormChecker;
import edu.fordham.cis.wisdm.utils.MessageBuilder;
import edu.fordham.cis.wisdm.utils.view.Operations;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Entry extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	private Button[] buttons = new Button[2];
	private EditText[]	fields = new EditText[2];
	
	private CheckBox rememberMe;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //initialize shared preference object
        
        Preference.initPrefForContext(this);
        
        setUpView();
    }
    
    
    /*
     * Loads up widgets into memory
     */
    private void setUpView(){
    	//set up buttons
    	int id[] = {R.id.SignUp, R.id.LoginButton};
    	Operations.findButtonViewsByIds(this, buttons, id);
    	Operations.setOnClickListeners(this, buttons);
    	
    	int ids[] = {R.id.Email, R.id.Password};
    	Operations.findEditTextViewsByIds(this, fields, ids);
    	
    	rememberMe = (CheckBox) findViewById(R.id.RememberMe);
    	boolean fill = Preference.getBoolean("edu.fordham.cis.wisdm.zoo.rememberme", false);
    	if(fill){
    		String email = Preference.getString("edu.fordham.cis.wisdm.zoo.email", "");
    		fields[0].setText(email);
    		rememberMe.setChecked(true);
    	}
    	
    }
    
    private void login(){
    	
    	String email = fields[0].getText().toString();
    	String password = fields[1].getText().toString();
    	
    	if(!FormChecker.checkEmail(fields[0])){
    		MessageBuilder.showMessage(this, "Error", "Ensure email is entered correctly", "");
    	} else{
    	
    		if(rememberMe.isChecked()){
    			Preference.putString("edu.fordham.cis.wisdm.zoo.email", email);
    		} else{
    			Preference.putString("edu.fordham.cis.wisdm.zoo.email", "");
    		}
    		Preference.putBoolean("edu.fordham.cis.wisdm.zoo.rememberme", rememberMe.isChecked());
    		
    		Intent login = new Intent(this, Main.class);
    		login.putExtra("email", email);
    		startActivity(login);
    	}
		
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.LoginButton:
			login();
			break;
		case R.id.SignUp:
			
			break;
			
		}
	}
}