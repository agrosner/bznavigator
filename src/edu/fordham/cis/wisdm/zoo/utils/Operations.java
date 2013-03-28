package edu.fordham.cis.wisdm.zoo.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * This class is useful for manipulating android view objects programmatically
 * @author Andrew Grosner
 * @version 1.0
 */
public class Operations {
	
	/**
	 * Removes a view object from the screen
	 * @param view
	 */
	public static void removeView(View...views){
		for(View view: views){
			view.setVisibility(View.GONE);
		}
	}
	
	public static void removeViewAnimate(View v, Animation a){
		v.startAnimation(a);
		removeView(v);
	}
	
	/**
	 * Removes a row from a table
	 * @param con
	 * @param id
	 */
	public static void removeTableRow(Activity con, int id){
		TableRow row = (TableRow) con.findViewById(id);
		Operations.removeView(row);
	}
	/**
	 * Shows a view that was previously removed
	 * @param view
	 */
	public static void addView(View...views){
		for(View view: views){
			view.setVisibility(View.VISIBLE);
		}
	}
	
	public static void addViewAnimate(View v, Animation a){
		v.startAnimation(a);
		addView(v);
	}
	
	/**
	 * Adds or removes a view
	 * @param view
	 * @param show
	 */
	public static void addRemoveView(View view, boolean show){
		if (show){
			addView(view);
		} else{
			removeView(view);
		}
	}
	
	/**
	 * Adds or removes multiple views
	 * @param views
	 * @param show
	 */
	public static void addRemoveViews(View[] views, boolean show){
		for(int i = 0; i < views.length; i++){
			addRemoveView(views[i], show);
		}
	}
	/**
	 * Starts an activity specified
	 * @param c
	 * @param context
	 */
	public static void beginActivity(Class<?> c, Context context){
		Intent i = new Intent(context, c);
		context.startActivity(i);
	}
	
	/**
	 * Switches text from a text view
	 * @param view
	 * @param firstString
	 * @param secondString
	 */
	public static void flipText(TextView view, String firstString, String secondString){
		if (view.getText().equals(firstString)){
			view.setText(secondString);
		} else{
			view.setText(firstString);
		}
	}
	
	/**
	 * Switches text from a buttonview
	 * @param buttonView
	 * @param firstString
	 * @param secondString
	 */
	public static void flipText(CompoundButton buttonView, String firstString,
			String secondString) {
		if (buttonView.getText().toString().equals(firstString)){
			buttonView.setText(secondString);
		} else{
			buttonView.setText(firstString);
		}
	}
	
	/**
	 * Flips text and buttons from the menu screens
	 * @param view
	 * @param firstString
	 * @param secondString
	 * @param buttons
	 */
	public static void expandButton(TextView view, String firstString, String secondString, Button[] buttons){
		flipText(view, firstString, secondString);
		flipButtons(buttons);
	}
	
	/**
	 * If the button array is shown, it will hide the buttons, otherwise show the button
	 * @param buttons
	 */
	public static void flipButtons(Button[] buttons){
		boolean shown = false;
		for (int i =0; i < buttons.length; i ++){
			if (buttons[i].isShown()){
				shown = true;
			}
		}
		if (shown == false)
			addView(buttons);
		else
			removeView(buttons);
	}
	/**
	 * View array of objects that sets all onClicks at once
	 * @param context
	 * @param views
	 */
	public static void setOnClickListeners(View.OnClickListener context, View...views){
		for (int i =0; i < views.length; i++){
			views[i].setOnClickListener(context);
		}
	}
	
	/**
	 * Without needed to explicitly declare each variable and set onClick listeners, 
	 * this function provides an easy way to set a bunch of views with onClick listeners
	 * @param act
	 * @param onclick
	 * @param ids
	 */
	public static void setViewOnClickListeners(View parent, View.OnClickListener onclick, int...ids){
		for(int id: ids){
			parent.findViewById(id).setOnClickListener(onclick);
		}
	}
	
	/**
	 * Without needed to explicitly declare each variable and set onClick listeners, 
	 * this function provides an easy way to set a bunch of views with onClick listeners
	 * @param act
	 * @param onclick
	 * @param ids
	 */
	public static void setViewOnClickListeners(Activity act, View.OnClickListener onclick, int...ids){
		for(int id: ids){
			act.findViewById(id).setOnClickListener(onclick);
		}
	}
	
	/**
	 * Sets onCheckedChangeListeners for multiple buttons
	 * @param listener
	 * @param checks
	 */
	public static void setOnCheckedChangeListeners(OnCheckedChangeListener listener, CompoundButton[] checks){
		for(int i =0; i < checks.length; i++){
			checks[i].setOnCheckedChangeListener(listener);
		}
	}
	
	/**
	 * Good for more than 2 buttons, puts view objects into memory for manipulation 
	 * @param context
	 * @param button
	 * @param ids
	 */
	public static void findButtonViewsByIds(Activity context, Button[] button, int...ids){
		for (int i =0; i < button.length; i++){
			button[i] = (Button) context.findViewById(ids[i]);
		}
	}
	
	/**
	 * Finds and sets a view text
	 * @param parent
	 * @param text
	 * @param id
	 */
	public static void setViewText(View parent, String text, int id){
		((TextView) parent.findViewById(id)).setText(text);
	}
	
	/**
	 * Good for more than 2 buttons, puts view objects into memory for manipulation
	 * @param layout
	 * @param button
	 * @param ids
	 */
	public static void findButtonViewsByIds(View layout, Button[] button, int...ids){
		for (int i =0; i < button.length; i++){
			button[i] = (Button) layout.findViewById(ids[i]);
		}
	}
	/**
	 * Good for more than 2 textViews, puts view objects into memory for manipulation
	 * @param context
	 * @param text
	 * @param ids
	 */
	public static void findTextViewsByIds(Activity context, TextView[] text, int...ids){
		for (int i =0; i < text.length; i++){
			text[i] = (TextView) context.findViewById(ids[i]);
		}
	}
	
	/**
	 * Gets multiple views in one line of code
	 * @param layout
	 * @param text
	 * @param ids
	 */
	public static void findTextViewsByIds(View layout, TextView[] text, int...ids){
		for(int i =0; i < text.length; i++){
			text[i] = (TextView) layout.findViewById(ids[i]);
		}
	}
	/**
	 * Loads checkBox views into memory, allowing changes to each view object
	 * @param context
	 * @param check
	 * @param ids
	 */
	public static void findCheckBoxViewsByIds(Activity context, CheckBox[] check, int...ids){
		for (int i = 0; i < check.length; i++){
			check[i] = (CheckBox) context.findViewById(ids[i]);
		}
	}
	
	/**
	 * Good for more than 2 textViews, puts view objects into memory for manipulation
	 * @param context
	 * @param text
	 * @param ids
	 */
	public static void findEditTextViewsByIds(Activity context, EditText[] text, int...ids){
		for (int i =0; i < text.length; i++){
			text[i] = (EditText) context.findViewById(ids[i]);
		}
	}
	
	/**
	 * Parses all larger than two decimals down to only two decimal places
	 * @param d
	 * @return new d
	 */
	public static double roundTwoDecimals(double d){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
	
	/**
	 * Rounds a string number into specified number of decimal places
	 * @param s
	 * @param number
	 * @return
	 */
	public static String roundDecimals(String s, int number){
		BigDecimal bd = new BigDecimal(s);
		bd = bd.setScale(number, BigDecimal.ROUND_UP);
		
		return bd.toString();
	}
	
	/**
	 * Swaps showing view to non-showing view
	 * @param isShowing
	 * @param isNotShowing
	 */
	public static void swapViews(View isShowing, View isNotShowing){
		addView(isNotShowing);
		removeView(isShowing);
	}

	public static void swapViewsAnimate(View showing, View notShowing, Animation a1, Animation a2){
		removeViewAnimate(showing, a1);
		addViewAnimate(notShowing, a2);
	}
	
	/**
	 * Finds multiple imagebuttons in succession from memory
	 * @param layout
	 * @param buttons
	 * @param ids
	 */
	public static void findImageButtonViewsByIds(View layout,ImageButton[] buttons, int...ids) {
		for (int i =0; i < buttons.length; i++){
			buttons[i] = (ImageButton) layout.findViewById(ids[i]);
		}
	}
	/**
	 * Finds multiple imagebuttons in succession from memory
	 * @param layout
	 * @param buttons
	 * @param ids
	 */
	public static void findImageButtonViewsByIds(Activity con,ImageButton[] buttons, int...ids) {
		for (int i =0; i < buttons.length; i++){
			buttons[i] = (ImageButton) con.findViewById(ids[i]);
		}
	}
	
	public static void setViewTexts(TextView[] views, String...strings){
		for(int i =0; i < views.length; i++){
			views[i].setText(strings[i]);
		}
	}
}
