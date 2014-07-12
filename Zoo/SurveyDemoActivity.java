package edu.fordham.cis.netlab.zoo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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

/**
 * @author Isaac
 * @version 1.0
 */
public class SurveyDemoActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {
	
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
	
	//survey field name/question
	TextView title;
	
	//SLIDER ADDITION
	//text view to display seek bar progress (if survey field is of type seek bar)
	TextView progressdisplay;
	
	//response data from each survey field (to be sent to server for analysis)
	String cumulative;
	
	//separators for survey fields in a text display
	static final String FIELD_START = "+++++";
	static final String FIELD_END = "-----";	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        scroll = (ScrollView)this.findViewById(R.id.scroll);
        
        mainlayout = (LinearLayout)this.findViewById(R.id.mainlayout);
                
        surveyfields = new ArrayList<String>();
        qindex = 0;
        
        cumulative = "";
        
        start = (Button)this.findViewById(R.id.start);
        start.setOnClickListener(this);
        
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
    			//SLIDER ADDITION
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
    			//SLIDER ADDITION
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
		}
		
		//submit entered data and display next survey field
		if(v.equals(ct)){
			
			//add entered data from widgets to cumulative data string
			for(int i = 0; i < mainlayout.getChildCount(); i++){
				if(mainlayout.getChildAt(i).getClass().equals(TextView.class)){
					//SLIDER ADDITION
					if(!(mainlayout.getChildAt(i).equals(progressdisplay)))
						cumulative += ((TextView)mainlayout.getChildAt(i)).getText() + "=";
				}
				else if(mainlayout.getChildAt(i).getClass().equals(EditText.class)){
					cumulative += ((EditText)mainlayout.getChildAt(i)).getText() + ",";
				}
				else if(mainlayout.getChildAt(i).getClass().equals(RadioGroup.class)){
					RadioGroup Trg = (RadioGroup)mainlayout.getChildAt(i);
					for(int k = 0; k < Trg.getChildCount(); k++)
						if(((RadioButton)Trg.getChildAt(k)).isChecked())
							cumulative += ((RadioButton)Trg.getChildAt(k)).getText() + ",";
				}
				else if(mainlayout.getChildAt(i).getClass().equals(CheckBox.class)){
					if(((CheckBox)mainlayout.getChildAt(i)).isChecked())
						cumulative += ((CheckBox)mainlayout.getChildAt(i)).getText() + ",";
				}
				//SLIDER ADDITION
				else if(mainlayout.getChildAt(i).getClass().equals(SeekBar.class)){
					cumulative += ((SeekBar)mainlayout.getChildAt(i)).getProgress() + ",";
				}
			}
			cumulative += ";";
			
			if(qindex < surveyfields.size() - 1){
				qindex++;
				setQuestion(surveyfields.get(qindex));
			}
			else{
				mainlayout.removeAllViews();
				TextView complete = new TextView(this);
				complete.setTextSize(20);
				complete.setText("Survey Complete!\nData String:\n" + cumulative);
				mainlayout.addView(complete);
				pd = ProgressDialog.show(this, "Submitting survey...", 
						"Thank you!");
			}
		}
	}

	//SLIDER ADDITION
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
}