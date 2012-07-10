package edu.fordham.cis.wisdm.zoo.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Isaac
 * @version 1.0
 */
public class SurveyActivity extends SherlockActivity implements OnClickListener, OnSeekBarChangeListener {
	
	//loading spinner and survey submission wait?
	ProgressDialog pd;
	Thread thread;
	
	//reads from survey data file
	Scanner in;
	
	//layout to/from which to write/read
	LinearLayout mainlayout;
	ScrollView scroll;
	
	//list of survey fields to be displayed
	ArrayList<String> surveyfields;
	
	//to keep track of which survey field is displayed currently/next
	int qindex;
	
	//continue to next survey field
	Button ct;	
	
	//start survey
	Button start;
	
	private Button skip;
	
	//survey field name/question
	TextView title;
	
	//text view to display seek bar progress (if survey field is of type seek bar)
	TextView progressdisplay;
	
	//response data from each survey field (to be sent to server for analysis)
	String cumulative;
	
	//list of responses, corresponds with indexes of 'surveyfields' questions
	ArrayList<String> responses;
	
	//separators for survey fields in a text display
	static final String FIELD_START = "+++++";
	static final String FIELD_END = "-----";	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey);
        
        scroll = (ScrollView)this.findViewById(R.id.scroll);
        
        mainlayout = (LinearLayout)this.findViewById(R.id.mainlayout);
                
        surveyfields = new ArrayList<String>();
        qindex = 0;
        
        cumulative = "";
        responses = new ArrayList<String>();
        
        start = (Button)this.findViewById(R.id.start);
        start.setOnClickListener(this);
        
        skip = (Button) this.findViewById(R.id.skip);
        skip.setOnClickListener(this);
        
        ct = new Button(this);
        ct.setText("Continue");
        ct.setHeight(LayoutParams.WRAP_CONTENT);
        ct.setWidth(LayoutParams.FILL_PARENT);
        ct.setOnClickListener(this);
        
        try {
			in = new Scanner(this.getAssets().open("survey.txt"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
        
        
        //parse survey fields from data file to list of survey fields (for easy access/mod)
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
    			mainlayout.removeView(title);
    			title = new TextView(this);
    			title.setTextSize(20);
    			title.setText("" + next.substring(6));
    			mainlayout.addView(title);
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
    	mainlayout.addView(rg);
    	mainlayout.addView(ct);
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
		if(v.equals(ct)){
			
			//string of current response
			String response = "";
			
			responses.add("");
			
			//add entered data from widgets to cumulative data string
			for(int i = 0; i < mainlayout.getChildCount(); i++){
				if(mainlayout.getChildAt(i).getClass().equals(TextView.class)){
					if(!(mainlayout.getChildAt(i).equals(progressdisplay)))
						response += ((TextView)mainlayout.getChildAt(i)).getText() + "=";
				}
				else if(mainlayout.getChildAt(i).getClass().equals(EditText.class)){
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
				mainlayout.removeAllViews();
				TextView complete = new TextView(this);
				complete.setTextSize(20);
				cumulative = "";
				for(String s: responses)
					cumulative += s;
				complete.setText("Survey Complete!\nData String:\n" + cumulative);
				mainlayout.addView(complete);
				startActivity(new Intent(this, Map.class));
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar sb, int arg1, boolean arg2) {
		progressdisplay.setText("" + sb.getProgress());
	}

	@Override
	public void onStartTrackingTouch(SeekBar sb) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar sb) {
		
	}
	
	@Override
	public void onBackPressed(){
		if(qindex > 0){
			qindex--;
			setQuestion(surveyfields.get(qindex));
		}
		else{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setCancelable(false);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
}