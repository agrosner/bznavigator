package edu.fordham.cis.wisdm.zoo.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class ExhibitFragment extends SherlockFragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance){
		
		return inflater.inflate(R.layout.exhibit, container, false);
		
	}
	
}
