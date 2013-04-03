package edu.fordham.cis.wisdm.zoo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
	 * Shows a view that was previously removed
	 * @param view
	 */
	public static void addView(View...views){
		for(View view: views){
			view.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Shows a view with the specified animation
	 * @param v
	 * @param a
	 */
	public static void addViewAnimate(View v, Animation a){
		v.startAnimation(a);
		addView(v);
	}
	
	/**
	 * Removes a view object from the screen
	 * @param view
	 */
	public static void removeView(View...views){
		for(View view: views){
			view.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Removes a view from the screen with the specified animation
	 * @param v
	 * @param a
	 */
	public static void removeViewAnimate(View v, Animation a){
		v.startAnimation(a);
		removeView(v);
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
	public static void addRemoveViews(boolean show, View...views){
		for(int i = 0; i < views.length; i++){
			addRemoveView(views[i], show);
		}
	}
	/**
	 * Starts an activity specified with empty intent
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
	 * View array of objects that sets all onClicks at once
	 * @param context
	 * @param views
	 */
	public static void setOnClickListeners(View.OnClickListener context, View...views){
		for (View v: views) v.setOnClickListener(context);
	}
	
	/**
	 * Without needed to explicitly declare each variable and set onClick listeners, 
	 * this function provides an easy way to set a bunch of views with onClick listeners
	 * @param act
	 * @param onclick
	 * @param ids
	 */
	public static void setOnClickListeners(View parent, View.OnClickListener onclick, int...ids){
		for(int id: ids) parent.findViewById(id).setOnClickListener(onclick);
		
	}
	
	/**
	 * Without needed to explicitly declare each variable and set onClick listeners, 
	 * this function provides an easy way to set a bunch of views with onClick listeners
	 * @param act
	 * @param onclick
	 * @param ids
	 */
	public static void setOnClickListeners(Activity act, View.OnClickListener onclick, int...ids){
		for(int id: ids) act.findViewById(id).setOnClickListener(onclick);
	}
	
	/**
	 * Sets onCheckedChangeListeners for multiple buttons
	 * @param listener
	 * @param checks
	 */
	public static void setOnCheckedChangeListeners(OnCheckedChangeListener listener, CompoundButton[] checks){
		for(CompoundButton button: checks) button.setOnCheckedChangeListener(listener);
	}
	
	/**
	 * Returns an array of buttons specified by id 
	 * @param context
	 * @param ids
	 * @return array
	 */
	public static Button[] findButtonViewsByIds(Activity context, int...ids){
		Button[] button = new Button[ids.length];
		for (int i =0; i < button.length; i++){
			button[i] = (Button) context.findViewById(ids[i]);
		}
		return button;
	}
	
	
	/**
	 * Returns an array of buttons specified by id 
	 * @param layout
	 * @param ids
	 * @return array;
	 */
	public static Button[] findButtonViewsByIds(View layout, int...ids){
		Button[] button = new Button[ids.length];
		for (int i =0; i < button.length; i++){
			button[i] = (Button) layout.findViewById(ids[i]);
		}
		return button;
	}
	
	/**
	 * Returns an array of textviews specified by id
	 * @param layout
	 * @param ids
	 * @return array;
	 */
	public static TextView[] findTextViewsByIds(Activity layout, int...ids){
		TextView[] text = new TextView[ids.length];
		for(int i =0; i < text.length; i++){
			text[i] = (TextView) layout.findViewById(ids[i]);
		}
		return text;
	}
	
	/**
	 * Returns an array of textviews specified by id
	 * @param layout
	 * @param ids
	 * @return array;
	 */
	public static TextView[] findTextViewsByIds(View layout, int...ids){
		TextView[] text = new TextView[ids.length];
		for(int i =0; i < text.length; i++){
			text[i] = (TextView) layout.findViewById(ids[i]);
		}
		return text;
	}
	
	/**
	 * Returns an array of checkboxes specified by id
	 * @param context
	 * @param ids
	 * @return array
	 */
	public static CheckBox[] findCheckBoxViewsByIds(Activity context, int...ids){
		CheckBox[] check = new CheckBox[ids.length];
		for (int i = 0; i < check.length; i++){
			check[i] = (CheckBox) context.findViewById(ids[i]);
		}
		return check;
	}
	
	/**
	 * Returns an array of edittexts specified by id
	 * @param context
	 * @param ids
	 * @return array
	 */
	public static EditText[] findEditTextViewsByIds(View context, int...ids){
		EditText[] edits = new EditText[ids.length];
		for (int i =0; i < ids.length; i++){
			edits[i] = (EditText) context.findViewById(ids[i]);
		}
		return edits;
	}
	
	
	/**
	 * Returns an array of edittexts specified by id
	 * @param context
	 * @param ids
	 * @return array
	 */
	public static EditText[] findEditTextViewsByIds(Activity context, int...ids){
		EditText[] edits = new EditText[ids.length];
		for (int i =0; i < ids.length; i++){
			edits[i] = (EditText) context.findViewById(ids[i]);
		}
		return edits;
	}
	
	/**
	 * Returns an array of imagebuttons specified by id
	 * @param con
	 * @param ids
	 * @return array
	 */
	public static ImageButton[] findImageButtonViewsByIds(View con, int...ids) {
		ImageButton[] buttons = new ImageButton[ids.length];
		for (int i =0; i < buttons.length; i++){
			buttons[i] = (ImageButton) con.findViewById(ids[i]);
		}
		return buttons;
	}
	
	/**
	 * Returns an array of imagebuttons specified by id
	 * @param con
	 * @param ids
	 * @return array
	 */
	public static ImageButton[] findImageButtonViewsByIds(Activity con, int...ids) {
		ImageButton[] buttons = new ImageButton[ids.length];
		for (int i =0; i < buttons.length; i++){
			buttons[i] = (ImageButton) con.findViewById(ids[i]);
		}
		return buttons;
	}
	
	/**
	 * Finds and sets a view text, returning the object if needed
	 * @param parent
	 * @param id
	 * @return array
	 */
	public static TextView setViewText(View parent, String text, int id){
		TextView v = ((TextView) parent.findViewById(id));
		v.setText(text);
		return v;
	}
	
	/**
	 * Sets multiple views with the specified strings
	 * @param views
	 * @param strings
	 */
	public static void setViewTexts(TextView[] views, String...strings){
		for(int i =0; i < views.length; i++){
			views[i].setText(strings[i]);
		}
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
	
	
}
