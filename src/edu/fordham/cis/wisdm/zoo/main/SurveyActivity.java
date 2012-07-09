package edu.fordham.cis.wisdm.zoo.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.actionbarsherlock.app.SherlockActivity;


import cis.fordham.edu.wisdm.messages.MessageBuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.view.KeyEvent;
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
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is used to generate the survey data that will be sent to the server
 * @author Andrew Grosner
 * @author Isaac
 * @version 1.0
 */
public class SurveyActivity extends SherlockActivity implements OnClickListener{
	//loading spinner and survey submission wait?
	private ProgressDialog pd;
	private Thread thread;
		
	//reads from survey data file
	private Scanner in;
		
	//layout to/from which to write/read
	private LinearLayout mainlayout;
	private ScrollView scroll;
		
	//list of survey fields to be displayed
	private ArrayList<String> surveyfields;
		
	//to keep track of which survey field is displayed currently/next
	private int qindex;
		
	//continue to next survey field
	private Button ct;	
		
	//start survey
	private Button start;
		
	//survey field name/question
	private TextView title;
		
	//list of responses to questions (to be parsed to string and added to 'cumulative')
	//indexes correspond to indexes of 'surveyfields' questions
	private ArrayList<String> responses;
	
	//response data from each survey field (to be sent to server for analysis)
	private String cumulative;
		
	//separators for survey fields in a text display
	private static final String FIELD_START = "+++++";
	private static final String FIELD_END = "-----";
		
	
	
	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		setContentView(R.layout.survey);
		scroll = (ScrollView)this.findViewById(R.id.scroll);
	        
	    mainlayout = (LinearLayout)this.findViewById(R.id.mainlayout);
	                
	    surveyfields = new ArrayList<String>();
	    qindex = 0;
	        
	    responses = new ArrayList<String>();
	    cumulative = "";
	        
	    start = (Button)this.findViewById(R.id.start);
        start.setOnClickListener(this);
	        
        ct = new Button(this);
        ct.setText("Continue");
        ct.setTextColor(getResources().getColor(R.color.forestgreen));
        ct.setBackgroundDrawable(getResources().getDrawable(R.drawable.appwidget_bg_clickable));
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
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setTitle("Sign Out");
	}
	
	 /**
	  * set widgets/layout for specified survey field
	  * @param fielddata
	  */
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
    			title.setTextColor(getResources().getColor(R.color.androidblue));
    			title.setTextSize(20);
    			title.setText("" + next.substring(6));
    			mainlayout.addView(title);
    		}
    		else if(next.length() >= 5 && next.substring(0, 5).equals("TYPE:")){
    			type = next.substring(5);
    			if(type.equals("Text Entry")){
	    			EditText Tet = new EditText(this);
	    			Tet.setTextColor(getResources().getColor(R.color.forestgreen));
	    			if(in.hasNextLine())
	    				Tet.setInputType(InputType.TYPE_CLASS_NUMBER);
	    			Tet.setImeOptions(EditorInfo.IME_ACTION_DONE);
					mainlayout.addView(Tet);
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
    				Tcb.setBackgroundDrawable(getResources().getDrawable(R.drawable.checkbox));
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
			if(!surveyfields.isEmpty()){
				setQuestion(surveyfields.get(qindex));
			} else{
				MessageBuilder.showToast("Survey File is empty!!", this);
				startActivity(new Intent(this, Main.class));
			}
		}
		
		//submit entered data and display next survey field
		if(v.equals(ct)){
			
			responses.add("");
			String response = "";
			
			//add entered data from widgets to cumulative data string
			for(int i = 0; i < mainlayout.getChildCount(); i++){
				if(mainlayout.getChildAt(i).getClass().equals(TextView.class)){
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
				//pd = ProgressDialog.show(this, "Submitting survey...", 
					//	"Thank you!");
				startActivity(new Intent(this, Main.class));
			}
		}
	}
	
	@Override
	public void onBackPressed(){
		if(qindex > 0){
			qindex--;
			setQuestion(surveyfields.get(qindex));
		}
		else{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"No previous questions! Would you like to go back to the log in screen?");
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
			builder.show();
			//Toast.makeText(this, "No previous questions!", Toast.LENGTH_SHORT).show();
		}
	}
	
}
