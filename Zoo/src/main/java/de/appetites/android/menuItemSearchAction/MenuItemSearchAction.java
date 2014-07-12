package de.appetites.android.menuItemSearchAction;
/*
 * Copyright (C) 2012 Benjamin Mock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItemCompat;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.grosner.zoo.R;


/**
 * <p>This class creates a MenuItem with an expandable and collapsable ActionView for search purposes</p> 
 *  
 * <p>It is based on the great ActionBar Sherlock of Jake Wharton. The problem with the original SearchView is, 
 * that the style for the dark ActionBar is wrong; i.e. black text on a dark gray background.</p>
 *
 * @author Benjamin Mock <mail@benjaminmock.de>
 */
public class MenuItemSearchAction extends EditText {

    private MenuItem searchItem;
    private Context context;
    private SearchPerformListener searchPerformListener;

    public MenuItemSearchAction(Context context, Menu menu, SearchPerformListener searchPerformListener, Drawable drawable, TextWatcher text, LinearLayout list) {
        super(context);
        this.searchPerformListener = searchPerformListener;
        this.context = context;
        
        createMenuItem(menu, drawable, list);
        
        // show search button on keyboard; this needs the setting of TYPE_TEXT... to be shown
        setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        setMaxLines(1);
        
        this.addTextChangedListener(text);
    }

    /**
     * Listen for back button clicks, in order to close the keyboard and collapse the ActionView
     */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
        	searchItem.collapseActionView();
        } else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
        	searchPerformListener.performSearch(MenuItemSearchAction.this.getText().toString());

			searchItem.collapseActionView();
			
        }
        return super.dispatchKeyEvent(event);
    }
    
    /**
     * Returns the the created MenuItem for customizations etc.
     * @return the MenuItem, which holds search ActionView
     */
    public MenuItem getMenuItem(){
    	return searchItem;
    }
    
    private void createMenuItem(Menu menu, Drawable drawable, final LinearLayout list){
    	ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		setLayoutParams(layoutParams);
		
		setHint(R.string.menu_item_search_action_hint);

		searchItem = menu.add(R.string.menu_item_search_action_menu_text).setIcon(drawable);
		searchItem.setActionView(this).setTitle("Search");
		searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// open keyboard
				list.setVisibility(View.VISIBLE);
				MenuItemSearchAction.this.setText("");
				MenuItemSearchAction.this.requestFocus();

				MenuItemSearchAction.this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                },200);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// close keyboard
				list.setVisibility(View.GONE);
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(MenuItemSearchAction.this.getWindowToken(), 0);

				return true;
			}
		});

		setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					// perform Search
					searchPerformListener.performSearch(MenuItemSearchAction.this.getText().toString());

					searchItem.collapseActionView();
					return true;
				}
				return false;
			}
		});
	}
}
