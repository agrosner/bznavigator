package edu.fordham.cis.wisdm.zoo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This class deals with preference handling
 * @author Andrew Grosner
 * @version 1.0
 */
public class Preference {

	private static SharedPreferences sprefs;
	
	/**
	 * Initializes preferences
	 * @param con
	 */
	public static void initPrefForContext(Context con){
		sprefs = PreferenceManager.getDefaultSharedPreferences(con);
	}
	
	public static void putString(String loc, String value){
		sprefs.edit().putString(loc, value).commit();
	}
	
	public static String getString(String loc, String defalt){
		return sprefs.getString(loc, defalt);
	}
	
	public static void putBoolean(String loc, boolean val){
		sprefs.edit().putBoolean(loc, val).commit();
	}
	
	public static boolean getBoolean(String loc, boolean defVal){
		return sprefs.getBoolean(loc, defVal);
	}
}
