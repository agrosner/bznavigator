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
	 * Initializes preferences (only needs to be called once in program)
	 * @param con
	 */
	public static void initPrefForContext(Context con){
		sprefs = PreferenceManager.getDefaultSharedPreferences(con);
	}
	
	public static void putString(String loc, String value){
		sprefs.edit().putString(loc, value).commit();
	}
	
	public static String getString(String loc, String defalt){
		try{
			return sprefs.getString(loc, defalt);
		} catch(NullPointerException n){
			return defalt;
		}
	}
	
	public static void putBoolean(String loc, boolean val){
		sprefs.edit().putBoolean(loc, val).commit();
	}
	
	public static boolean getBoolean(String loc, boolean defVal){
		try{
			return sprefs.getBoolean(loc, defVal);
		} catch (NullPointerException n){
			return defVal;
		}
	}
	
	public static String getEmail(){
		return getString("edu.fordham.cis.wisdm.zoo.email", "");
	}
	
	public static long getLong(String loc, long val){
		try{
			return sprefs.getLong(loc, val);
		} catch(NullPointerException n){
			return val;
		}
	}
	
	public static void putLong(String loc, long val){
		sprefs.edit().putLong(loc, val).commit();
	}
	
	public static float getFloat(String loc, float defVal){
		return sprefs.getFloat(loc, defVal);
	}
	
	public static void putFloat(String loc, float val){
		sprefs.edit().putFloat(loc, val).commit();
	}
}
