package edu.fordham.cis.wisdm.zoo.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
public class SurveyActivity extends SherlockActivity implements OnClickListener, OnSeekBarChangeListener, OnTouchListener {
	
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
	
	//continue to next survey field
	private Button cont;	
	
	//start survey
	private Button start;
	private Button skip;
	
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
        splash.putExtra("password", getIntent().getExtras().getString("password"));
        splash.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        scroll = (ScrollView)this.findViewById(R.id.scroll);
        
        mainlayout = (LinearLayout)this.findViewById(R.id.mainlayout);
        
        whole = (RelativeLayout)this.findViewById(R.id.whole);
                
        surveyfields = new ArrayList<String>();
        qindex = 0;
        
        cumulative = "";
        responses = new ArrayList<String>();
        
        title = (TextView)this.findViewById(R.id.title);
        
        start = (Button)this.findViewById(R.id.start);
        start.setOnClickListener(this);
        
        skip = (Button) this.findViewById(R.id.skip);
        skip.setOnClickListener(this);
        
        cont = (Button)this.findViewById(R.id.cont);
        cont.setOnClickListener(this);
                
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
    	while(in.hasNextLine()){
    		next = in.nextLine();
    		if(next.length() >= 6 && next.substring(0, 6).equals("TITLE:")){
    			title.setTextSize(20);
    			title.setText("" + next.substring(6));
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
    	cont.setVisibility(View.VISIBLE);
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
		if(v.equals(start)){
			setQuestion(surveyfields.get(qindex));
		} else if(v.equals(skip)){
			startActivity(splash);
		}
		
		//submit entered data and display next survey field
		if(v.equals(cont)){
			imm.hideSoftInputFromWindow(otherentry.getWindowToken(), 0);
			
			this.uploadCurrentResponses();
						
			if(qindex < surveyfields.size() - 1){
				qindex++;
				setQuestion(surveyfields.get(qindex));
			}
			else{
				qindex++;
				mainlayout.removeAllViews();
				cont.setVisibility(View.GONE);
				completion.setVisibility(View.GONE);
				other.setSelected(false);
				other.setEnabled(false);
				TextView complete = new TextView(this);
				complete.setTextSize(20);
				cumulative = "";
				for(String s: responses)
					cumulative += s;
				complete.setText("Survey Complete!\nData String:\n" + cumulative);
				mainlayout.addView(complete);
				startActivity(splash);
			}
			
			//increment progress
			completion.setProgress(qindex);
			
			
		}
		
	}
	
	public void uploadCurrentResponses(){
		//add entered data from widgets to cumulative data string
		if(responses.size() <= qindex)
			responses.add("");
		
		//string of current response
		String response = "";
		response += title.getText() + "=";
		for(int i = 0; i < mainlayout.getChildCount(); i++){
			if(mainlayout.getChildAt(i).getClass().equals(EditText.class)){
				if(mainlayout.getChildAt(i).equals(otherentry)){
					if(other.isChecked()){
						response += otherentry.getText() + ",";
						otherentry.setText("");
					}
				}
				else
					response += ((EditText)mainlayout.getChildAt(i)).getText() + ",";
			}
			else if(mainlayout.getChildAt(i).getClass().equals(RadioGroup.class)){
				RadioGroup Trg = (RadioGroup)mainlayout.getChildAt(i);
				for(int k = 0; k < Trg.getChildCount(); k++)
					if(((RadioButton)Trg.getChildAt(k)).isChecked()){
						if(!((RadioButton)Trg.getChildAt(k)).equals(other)){
							response += ((RadioButton)Trg.getChildAt(k)).getText() + ",";
						}
					}
			}
			else if(mainlayout.getChildAt(i).getClass().equals(CheckBox.class)){
				if(((CheckBox)mainlayout.getChildAt(i)).isChecked())
					response += ((CheckBox)mainlayout.getChildAt(i)).getText() + ",";
			}
			else if(mainlayout.getChildAt(i).getClass().equals(SeekBar.class)){
				response += ((SeekBar)mainlayout.getChildAt(i)).getProgress() + ",";
			}
		}
		response += ";";
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
			AlertDialog.Builder message = new AlertDialog.Builder(this);
			message.setTitle("No previous questions! Quit Survey?");
			message.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
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
		String Tresponse = responses.get(qindex).substring(
				responses.get(qindex).indexOf('=') + 1, responses.get(qindex).indexOf(';'));
		
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
}