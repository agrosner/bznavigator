package edu.fordham.cis.wisdm.zoo.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
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

/*
 * TODO
 * 
 * save responses to survey questions
 * so that they are still displayed
 * when back button is pressed
 * 
 * add text entry option for radio
 * buttons in survey maker such that
 * user can enter data in "other" choice
 * 
 * prevent activity reset when screen
 * configuration is changed
 */

/**
 * @author Isaac
 * @version 1.0
 */
public class SurveyActivity extends SherlockActivity implements OnClickListener, OnSeekBarChangeListener {
	
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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey);
       // this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
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
    	String type = "";
    	String next = "";
    	while(in.hasNextLine()){
    		next = in.nextLine();
    		if(next.length() >= 6 && next.substring(0, 6).equals("TITLE:")){
    			//mainlayout.removeView(title);
    			title.setTextSize(20);
    			title.setText("" + next.substring(6));
    			//mainlayout.addView(title);
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
    				RadioButton Trb = new RadioButton(this);
    				Trb.setText(next.substring(7));
    				rg.addView(Trb);
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
    }

	@Override
	public void onClick(View v) {
		
		//start survey
		if(v.equals(start)){
			setQuestion(surveyfields.get(qindex));
		} else if(v.equals(skip)){
			startActivity(new Intent(this, SplashScreenActivity.class));
		}
		
		//submit entered data and display next survey field
		if(v.equals(cont)){
			
			//string of current response
			String response = "";
			
			responses.add("");
			
			//add entered data from widgets to cumulative data string
			response += title.getText() + "=";
			for(int i = 0; i < mainlayout.getChildCount(); i++){
				if(mainlayout.getChildAt(i).getClass().equals(EditText.class)){
					response += ((EditText)mainlayout.getChildAt(i)).getText() + ",";
				}
				else if(mainlayout.getChildAt(i).getClass().equals(RadioGroup.class)){
					RadioGroup Trg = (RadioGroup)mainlayout.getChildAt(i);
					for(int k = 0; k < Trg.getChildCount(); k++)
						if(((RadioButton)Trg.getChildAt(k)).isChecked())
							response += ((RadioButton)Trg.getChildAt(k)).getText() + ",";
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
			
			
			if(qindex < surveyfields.size() - 1){
				qindex++;
				setQuestion(surveyfields.get(qindex));
			}
			else{
				qindex++;
				mainlayout.removeAllViews();
				cont.setVisibility(View.GONE);
				completion.setVisibility(View.GONE);
				TextView complete = new TextView(this);
				complete.setTextSize(20);
				cumulative = "";
				for(String s: responses)
					cumulative += s;
				complete.setText("Survey Complete!\nData String:\n" + cumulative);
				mainlayout.addView(complete);
				startActivity(new Intent(this, SplashScreenActivity.class));
			}
			
			//increment progress
			completion.setProgress(qindex);
		}
		
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
		
		//go back to previous survey question
		if(qindex > 0){
			qindex--;
			setQuestion(surveyfields.get(qindex));
			completion.setProgress(qindex);
		}
		else{
			HoloAlertDialogBuilder message = new HoloAlertDialogBuilder(this);
			message.setTitle("Quit Survey?");
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		
	}
}