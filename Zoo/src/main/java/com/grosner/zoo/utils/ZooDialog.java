package com.grosner.zoo.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.grosner.zoo.R;

/**
 * This class builds a custom dialog since we cannot do so completely by style
 * @author Andrew Grosner
 *
 */
public class ZooDialog extends Dialog {

	private Button mPositive;
	
	private Button mNeutral;
	
	private Button mNegative;
	
	private View mFirstDivider;
	
	private View mSecondDivider;
	
	private TextView mTitle;
	
	private TextView mMessage;
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			dismiss();
		}
	};
	
	public ZooDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_app);
		
		mPositive = (Button) findViewById(R.id.positiveButton);
		mNeutral = (Button)findViewById(R.id.neutralButton);
		mNegative = (Button)findViewById(R.id.negativeButton);
		
		mFirstDivider = findViewById(R.id.firstDivider);
		mSecondDivider = findViewById(R.id.secondDivider);
		
		mTitle = (TextView) findViewById(R.id.title);
		mMessage = (TextView) findViewById(R.id.message);
	}
	
	public void setPositiveButton(String text, View.OnClickListener onclick){
		mPositive.setText(text);
		
		setOnClickListener(mPositive, onclick);
		
		mPositive.setVisibility(View.VISIBLE);
		
		if(mNegative.isShown()){
			mSecondDivider.setVisibility(View.VISIBLE);
		}
		
		if(mNeutral.isShown()){
			mSecondDivider.setVisibility(View.VISIBLE);
		}
	
	}
	
	public void setNegativeButton(String text, View.OnClickListener onclick){
		mNegative.setText(text);
		setOnClickListener(mNegative, onclick);
		mNegative.setVisibility(View.VISIBLE);
		
		if(mNeutral.isShown()){
			mSecondDivider.setVisibility(View.VISIBLE);
		}
		
		if(mPositive.isShown()){
			mFirstDivider.setVisibility(View.VISIBLE);
		}
		
	}
	
	public void setNeutralButton(String text, View.OnClickListener onclick){
		mNeutral.setText(text);
		setOnClickListener(mNeutral, onclick);
		mNeutral.setVisibility(View.VISIBLE);
		
		if(mPositive.isShown()){
			mFirstDivider.setVisibility(View.VISIBLE);
		}
		
		if(mNegative.isShown()){
			mSecondDivider.setVisibility(View.VISIBLE);
		}
	}
	
	public void setTitle(String title){
		mTitle.setText(title);
	}

	public void setMessage(String text){
		mMessage.setText(text);
		mMessage.setVisibility(View.VISIBLE);
	}
	
	private void setOnClickListener(Button btn, View.OnClickListener onclick){
		if(onclick!=null)
			btn.setOnClickListener(onclick);
		else
			btn.setOnClickListener(mOnClickListener);
	}
}
