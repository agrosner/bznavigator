package com.grosner.zoo.utils;

/**
 * Created by: andrewgrosner
 * Date: 7/12/14.
 * Contributors: {}
 * Description:
 */
public class StringUtils {


    public static boolean stringNotNullOrEmpty(String...strings){
        boolean success = true;
        for(String string: strings){
            if(string==null || string.equals("")){
                success = false;
                break;
            }
        }
        return success;
    }
}
