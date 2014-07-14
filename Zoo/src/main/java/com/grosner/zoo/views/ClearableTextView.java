package com.grosner.zoo.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.grosner.zoo.R;
import com.grosner.zoo.utils.Operations;

/**
 * Created by: andrewgrosner
 * Date: 7/12/14.
 * Contributors: {}
 * Description:
 */
public class ClearableTextView extends RelativeLayout implements TextWatcher, View.OnClickListener{

    public InstantAutoComplete eText;
    public ImageView btn;
    private Drawable mIcon, mSearchIcon;

    public ClearableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setIcons();
        _createUI(context);
    }

    public ClearableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setIcons();
        _createUI(context);
    }

    public ClearableTextView(Context context, Drawable icon) {
        super(context);
        this.mIcon = icon;
        _createUI(context);
    }

    public ClearableTextView(Context context) {
        super(context);
        setIcons();
        _createUI(context);
    }

    public void setIcons(int xIcon, int searchIcon){
        mIcon = getResources().getDrawable(xIcon);
        mSearchIcon = getResources().getDrawable(searchIcon);
    }

    protected void setIcons(){
        setIcons(R.drawable.exit, R.drawable.search);
    }

    protected void _createUI(Context mContext) {
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        eText = new InstantAutoComplete(mContext);
        eText.setId(getId() + 100000);
        eText.addTextChangedListener(this);

        params.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(eText, params);

        params = new LayoutParams((int)Operations.dp(36), (int)Operations.dp(36));

        btn = new ImageView(mContext);
        btn.setId(getId() + 101000);
        btn.setOnClickListener(this);
        params.addRule(RelativeLayout.ALIGN_RIGHT, getId() + 100000);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(btn, params);
        btn.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View arg0) {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        eText.requestFocus();
        imm.showSoftInput(eText, InputMethodManager.SHOW_IMPLICIT);
        eText.setText("");
    }

    @Override
    public void afterTextChanged(Editable arg0) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(eText.getText().length() > 0 && btn.getVisibility() == View.GONE){
            btn.setVisibility(View.VISIBLE);
            eText.setCompoundDrawablesWithIntrinsicBounds(mSearchIcon, null, mIcon, null);
            eText.setCompoundDrawablePadding((int) Operations.dp(8));
        }else if(eText.getText().length() == 0 && btn.getVisibility()==View.VISIBLE){
            btn.setVisibility(View.GONE);
            eText.setCompoundDrawablesWithIntrinsicBounds(mSearchIcon, null, null, null);
            eText.setCompoundDrawablePadding((int) Operations.dp(8));
        }
    }
}
