package edu.fordham.cis.wisdm.zoo.main;

import com.actionbarsherlock.app.SherlockFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewsActivity extends SherlockFragment{

	@Override
	public void onCreate(Bundle instance){
		super.onCreate(instance);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance	){
		return inflater.inflate(R.layout.news, container);
	}
}
