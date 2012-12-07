package edu.fordham.cis.wisdm.zoo.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import cis.fordham.edu.wisdm.utils.Operations;

import com.actionbarsherlock.app.SherlockActivity;

import edu.fordham.cis.wisdm.zoo.main.constants.UserConstants;
import edu.fordham.cis.wisdm.zoo.utils.Preference;
import edu.fordham.cis.wisdm.zoo.utils.connections.Connections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Isaac
 * @version 1.0
 */
public class SurveyActivity extends SherlockActivity implements OnClickListener, OnSeekBarChangeListener, OnTouchListener, UserConstants {
	
	
	private Connections mConnection;
	
	//reads from survey data file
	private Scanner in;
	
	//layout to/from which to write/read
	private LinearLayout mainlayout;
	private ScrollView scroll;
	private RelativeLayout whole;
	
	//list of survey fields to be displayed
	private ArrayList<String> surveyfields;
	
	//to keep track of which survey field is displayed currently/next
	private int qindex;
	
	private Button[] buttons = new Button[3];
	
	//other options
	private RadioButton other;
	private EditText otherentry;
	
	//survey completion progress bar
	private SeekBar completion;
	
	//survey field name/question
	private TextView title;
	
	//text view to display seek bar progress (if survey field is of type seek bar)
	private TextView progressdisplay;
	
	//response data from each survey field (to be sent to server for analysis)
	private String cumulative;

	
	//list of responses, corresponds with indexes of 'surveyfields' questions
	private ArrayList<String> responses;
	
	private ArrayList<String> types;
	
	private ArrayList<Integer> qids; 
	
	//separators for survey fields in a text display
	static final String FIELD_START = "+++++";
	static final String FIELD_END = "-----";	
	
	private static String email = null;
	private static Intent splash;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        email = this.getIntent().getExtras().getString("email");
        splash = new Intent(this, SplashScreenActivity.class);
        splash.putExtra("email", email);
        
        String p = getIntent().getExtras().getString("password");
        splash.putExtra("password", p);
        splash.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        mConnection = new Connections(email, p, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
        
        scroll = (ScrollView)this.findViewById(R.id.scroll);
        
        mainlayout = (LinearLayout)this.findViewById(R.id.mainlayout);
        
        whole = (RelativeLayout)this.findViewById(R.id.whole);
                
        surveyfields = new ArrayList<String>();
        qindex = 0;
        
        cumulative = "";
        responses = new ArrayList<String>();
        types = new ArrayList<String>();
        qids = new ArrayList<Integer>();
        
        title = (TextView)this.findViewById(R.id.title);
        
        int id[] = {R.id.start, R.id.skip, R.id.cont};
        Operations.findButtonViewsByIds(this, buttons, id);
        Operations.setOnClickListeners(this, buttons);
                
        other = new RadioButton(this);
        otherentry = new EditText(this);
        otherentry.setOnTouchListener(this);
        otherentry.setVisibility(View.GONE);
                
        completion = (SeekBar)this.findViewById(R.id.completion);
        completion.setOnSeekBarChangeListener(this);
        
        try {
			in = new Scanner(this.getAssets().open("survey.txt"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
        
        
        //parse survey fields from data file to arraylist of survey fields (for easy access/mod)
        String filter = "";
        String next = "";
        while(in.hasNextLine()){
        	next = in.nextLine();
        	if(next.equals(FIELD_START))
        		continue;
        	if(next.equals(FIELD_END)){
        		surveyfields.add(filter);
        		filter = "";
        		continue;
        	}
        	
        	filter += next + "\n";        	
        }
        
        completion.setMax(surveyfields.size());
        completion.setProgress(qindex);
        
    }
    
    //set widgets/layout for specified survey field
    public void setQuestion(String fielddata){
    	mainlayout.removeAllViewsInLayout();
    	in = new Scanner(fielddata);
    	RadioGroup rg = new RadioGroup(this);
    	other.setEnabled(false);
    	other.setSelected(false);
    	String type = "";
    	String next = "";
    	Integer qid;
    	String valType = "";
    	while(in.hasNextLine()){
    		next = in.nextLine();
    		if(next.length() >= 6 && next.substring(0, 6).equals("TITLE:")){
    			String titl = next.substring(6);
    			title.setTextSize(20);
    			title.setText("" + titl);
    		}
    		else if(next.substring(0, 4).equals("QID:")){
    			qid = Integer.valueOf(next.substring(4));
    			if(qids.size()<=qindex) qids.add(0);
    			qids.set(qindex, qid);
    		} else if(next.substring(0, 4).equals("VAL:")){
    			valType = next.substring(4);
    			if(types.size()<=qindex) types.add("");
    			types.set(qindex,valType);
    		}
    		else if(next.length() >= 5 && next.substring(0, 5).equals("TYPE:")){
    			type = next.substring(5);
    			if(type.equals("Text Entry")){
	    			EditText Tet = new EditText(this);
	    			if(in.hasNextLine())
	    				Tet.setInputType(InputType.TYPE_CLASS_NUMBER);
	    			Tet.setImeOptions(EditorInfo.IME_ACTION_DONE);
					mainlayout.addView(Tet);
					break;
    			}
    			if(type.equals("Seek Bar")){
    				SeekBar Tsb = new SeekBar(this);
    				if(in.hasNextLine()){
    					next = in.nextLine();
    					if(next.length() >= 7 && next.substring(0, 7).equals("CHOICE:"))
    						Tsb.setMax(Integer.parseInt(next.substring(7)));
    				}
    				else
    					Tsb.setMax(50);
    				Tsb.setProgress((int)(Tsb.getMax() / 2));
    				Tsb.setOnSeekBarChangeListener(this);
    				progressdisplay = new TextView(this);
    				progressdisplay.setTextSize(30);
    				progressdisplay.setText("" + Tsb.getProgress());
    				mainlayout.addView(progressdisplay);
    				mainlayout.addView(Tsb);
    				break;
    			}
    		}
    		else if(next.length() >= 7 && next.substring(0, 7).equals("CHOICE:")){
    			if(type.equals("Radio Buttons")){
    				if(next.substring(7).equals("OTHER")){    					
    					other = new RadioButton(this);
    					other.setOnClickListener(this);
    					other.setText("Other");
    					other.setEnabled(true);
    					rg.addView(other);
    				}
    				else{
    					RadioButton Trb = new RadioButton(this);
    					Trb.setText(next.substring(7));
    					Trb.setOnClickListener(this);
    					rg.addView(Trb);
    				}
    			}
    			else if(type.equals("Check Boxes")){
    				CheckBox Tcb = new CheckBox(this);
    				Tcb.setText(next.substring(7));
    				mainlayout.addView(Tcb);
    			}
    		}
    	}
    	buttons[2].setVisibility(View.VISIBLE);
    	completion.setVisibility(View.VISIBLE);
    	mainlayout.addView(rg);
    	if(other.isEnabled())
    		mainlayout.addView(otherentry);
    	if(responses.size() > qindex)
    		reloadResponses();
    }
    
    
	@Override
	public void onClick(View v) {
		
		//keyboard manipulation
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		
		//display keyboard when 'other' is selected
		if(v.equals(other)){
			otherentry.setVisibility(View.VISIBLE);
			otherentry.requestFocus();
			imm.showSoftInput(otherentry, InputMethodManager.SHOW_IMPLICIT);
		}
		else{
			otherentry.setVisibility(View.INVISIBLE);
			imm.hideSoftInputFromWindow(otherentry.getWindowToken(), 0);
		}
				
		//start survey
		if(v.equals(buttons[0])){
			setQuestion(surveyfields.get(qindex));
			buttons[1].setVisibility(View.GONE);
		} else if(v.equals(buttons[1])){
			startActivity(splash);
		}
		
		//submit entered data and display next survey field
		if(v.equals(buttons[2])){
			imm.hideSoftInputFromWindow(otherentry.getWindowToken(), 0);
			
			this.uploadCurrentResponses();
						
			if(qindex < surveyfields.size() - 1){
				qindex++;
				setQuestion(surveyfields.get(qindex));
			}
			else{
				qindex++;
				mainlayout.removeAllViews();
				buttons[2].setVisibility(View.GONE);
				completion.setVisibility(View.GONE);
				other.setSelected(false);
				other.setEnabled(false);
				TextView complete = new TextView(this);
				complete.setTextSize(20);
				cumulative = "";
				for(String s: responses)
					cumulative += s + ",";
				complete.setText("Survey Complete!\nData String:\n" + responses.toString()
						+ "\n" + qids.toString() + "\n" + types.toString());
				mainlayout.addView(complete);
				new SendSurveyDataTask(mConnection, this).execute();
			}
			
			//increment progress
			completion.setProgress(qindex);
			
			
		}
		
	}
	
	public void uploadCurrentResponses(){
		//add entered data from widgets to cumulative data string
		if(responses.size() <= qindex)
			responses.add("");
		
		String response = "";
		for(int i = 0; i < mainlayout.getChildCount(); i++){
			if(mainlayout.getChildAt(i).getClass().equals(EditText.class)){
				if(mainlayout.getChildAt(i).equals(otherentry)){
					if(other.isChecked()){
						response = otherentry.getText().toString();
						otherentry.setText("");
					}
				}
				else
					response += ((EditText)mainlayout.getChildAt(i)).getText();
			}
			else if(mainlayout.getChildAt(i).getClass().equals(RadioGroup.class)){
				RadioGroup Trg = (RadioGroup)mainlayout.getChildAt(i);
				for(int k = 0; k < Trg.getChildCount(); k++)
					if(((RadioButton)Trg.getChildAt(k)).isChecked()){
						if(!((RadioButton)Trg.getChildAt(k)).equals(other)){
							response += ((RadioButton)Trg.getChildAt(k)).getText();
						}
					}
			}
			else if(mainlayout.getChildAt(i).getClass().equals(CheckBox.class)){
				if(((CheckBox)mainlayout.getChildAt(i)).isChecked())
					response += ((CheckBox)mainlayout.getChildAt(i)).getText() + ",";
			}
			else if(mainlayout.getChildAt(i).getClass().equals(SeekBar.class)){
				response += ((SeekBar)mainlayout.getChildAt(i)).getProgress();
			}
		}
		responses.set(qindex, response);
	}

	@Override
	public void onProgressChanged(SeekBar sb, int arg1, boolean arg2) {
		if(sb.equals(completion))
			//reset progress
			completion.setProgress(qindex);
		else
			progressdisplay.setText("" + sb.getProgress());
	}

	@Override
	public void onStartTrackingTouch(SeekBar sb) {
		
		//display progress (textually)
		if(sb.equals(completion)){
			double pct = (double)completion.getProgress() / (double)completion.getMax();
			pct *= 100;
			Toast.makeText(this, "Survey is " + (int)(pct) + "% complete!",
					Toast.LENGTH_SHORT).show();
		}
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar sb) {
		if(sb.equals(completion));
			//reset progress
			completion.setProgress(qindex);
	}
	
	@Override
	public void onBackPressed(){
		
		this.uploadCurrentResponses();
		
		//go back to previous survey question
		if(qindex > 0){
			qindex--;
			setQuestion(surveyfields.get(qindex));
			completion.setProgress(qindex);
			
			reloadResponses();
		}
		else{
			final Activity act = this;
			
			AlertDialog.Builder message = new AlertDialog.Builder(this);
			message.setTitle("No previous questions! Logout and Quit Survey?");
			message.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Preference.putBoolean(REMEMBER_ME_LOC, false);
					 
					 Intent upIntent = new Intent(act, Entry.class);
					 if (NavUtils.shouldUpRecreateTask(act, upIntent)) {
						 // This activity is not part of the application's task, so create a new task
						 // with a synthesized back stack.
						 TaskStackBuilder.from(act)
	                        	.addNextIntent(upIntent)
	                        	.startActivities();
						 finish();
					 } else {
						 // 	This activity is part of the application's task, so simply
						 // navigate up to the hierarchical parent activity.
						 NavUtils.navigateUpTo(act, upIntent);
					 }
				}
			});
			message.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			
			message.create().show();
		}
	}
	
	//TODO clean up this function? also, 'other' checked by default if no response
	//resets/displays answers to current survey field from 'responses' list
	public void reloadResponses(){
		
		//set to true if a radio button is checked; prevents 'other' from adopting
		//the checked radio's text/status
		boolean radioSet = false;
		
		//string of response(s) to current survey field
		String Tresponse = responses.get(qindex);
		
		//parsed list of response(s) to current survey field
		ArrayList<String> Tresponses = new ArrayList<String>(
				Arrays.asList(Tresponse.split(",")));
		
		//traverse widgets on screen, check list of responses to see condition
		//of widget (text, empty, checked/unchecked), set widget accordingly
		for(int i = 0; i < mainlayout.getChildCount(); i++){
			if(mainlayout.getChildAt(i).getClass().equals(CheckBox.class)){
				for(String s: Tresponses){
					if(s.equals(((CheckBox)mainlayout.getChildAt(i)).getText()))
						((CheckBox)mainlayout.getChildAt(i)).setChecked(true);
				}
			}
			else if(mainlayout.getChildAt(i).getClass().equals(RadioGroup.class)){
				RadioGroup Trg = (RadioGroup)mainlayout.getChildAt(i);
				for(int k = 0; k < Trg.getChildCount(); k++)
					for(String s: Tresponses)
						if(s.equals(((RadioButton)Trg.getChildAt(k)).getText())){
							((RadioButton)Trg.getChildAt(k)).setChecked(true);
							radioSet = true;
						}
			}
			else if(mainlayout.getChildAt(i).getClass().equals(EditText.class)){
				for(String s: Tresponses){
					if(other.isEnabled() && !radioSet){
						other.setChecked(true);
						otherentry.setVisibility(View.VISIBLE);
						otherentry.setText(s);
					}
				}
			}
			if(mainlayout.getChildAt(i).getClass().equals(SeekBar.class)){
				for(String s: Tresponses){
					((SeekBar)mainlayout.getChildAt(i)).setProgress(Integer.parseInt(s));
					progressdisplay.setText(s);
				}
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		//select 'other' button if edit text is touched
		if(v.equals(otherentry)){
			other.setChecked(true);
		}
		return false;
	}
	
	
	private class SendSurveyDataTask extends AsyncTask<Void,Void,Void>{
		
		private Connections mConnection;
		
		private ProgressDialog dia;
		
		private Context mContext;
		
		public SendSurveyDataTask(Connections con, Context cont){
			mConnection = con;
			mContext = cont;
		}

		@Override
		protected void onPreExecute(){
			dia = ProgressDialog.show(mContext, "Connecting", "Sending Survey Data");
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				Connections.sendSurvey(mConnection, responses, qids, types);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Connections.disconnect();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void arg){
			dia.dismiss();
			startActivity(splash);
		}
	}
}