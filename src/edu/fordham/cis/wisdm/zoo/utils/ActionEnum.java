package edu.fordham.cis.wisdm.zoo.utils;

import java.util.Locale;

/**
 * ActionBar tab label enums for SplashScreen Activity
 * @author agrosner
 *
 */
public enum ActionEnum {

	FOLLOW,
	PARK,
	ABOUT,
	SETTINGS;
	
	@Override
	public String toString(){
		return name().toLowerCase(Locale.ENGLISH);
	}
	
}
