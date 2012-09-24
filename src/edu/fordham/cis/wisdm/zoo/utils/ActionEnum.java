package edu.fordham.cis.wisdm.zoo.utils;

/**
 * ActionBar tab label enums for SplashScreen Activity
 * @author agrosner
 *
 */
public enum ActionEnum {

	FOLLOW,
	NEAREST,
	ABOUT,
	SETTINGS;
	
	@Override
	public String toString(){
		switch(this){
		case FOLLOW:
			return "Follow Me";
		case NEAREST:
			return "Nearest";
		case ABOUT:
			return "About";
		case SETTINGS:
			return "Settings";
		default:
				return "error";
		}
	}
	
}
