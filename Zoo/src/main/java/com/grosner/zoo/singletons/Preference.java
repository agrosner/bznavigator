package com.grosner.zoo.singletons;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.grosner.zoo.application.ZooApplication;

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
	public static SharedPreferences getSharedInstance(){
        if(sprefs==null){
		    sprefs = PreferenceManager.getDefaultSharedPreferences(ZooApplication.getContext());
        }
        return sprefs;
	}
	
	public static void putString(String loc, String value){
		try{
			getSharedInstance().edit().putString(loc, value).commit();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String getString(String loc, String defalt){
		try{
			return getSharedInstance().getString(loc, defalt);
		} catch(NullPointerException n){
			return defalt;
		}
	}
	
	public static void putBoolean(String loc, boolean val){
        getSharedInstance().edit().putBoolean(loc, val).commit();
	}
	
	public static boolean getBoolean(String loc, boolean defVal){
		try{
			return getSharedInstance().getBoolean(loc, defVal);
		} catch (NullPointerException n){
			return defVal;
		}
	}
	
	public static String getEmail(){
		return getString("edu.fordham.cis.wisdm.zoo.email", "");
	}
	
	public static long getLong(String loc, long val){
		try{
			return getSharedInstance().getLong(loc, val);
		} catch(NullPointerException n){
			return val;
		}
	}
	
	public static void putLong(String loc, long val){
        getSharedInstance().edit().putLong(loc, val).commit();
	}
	
	public static Float getFloat(String loc, Float defVal){
		try{
			return getSharedInstance().getFloat(loc, defVal);
		} catch(NullPointerException n){
			return defVal;
		}
	}
	
	public static void putFloat(String loc, float val){
        getSharedInstance().edit().putFloat(loc, val).commit();
	}

}
