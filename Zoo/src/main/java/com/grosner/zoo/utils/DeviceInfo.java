package com.grosner.zoo.utils;

import com.grosner.zoo.application.ZooApplication;

public class DeviceInfo {

    /**
	 * Convert pixel size of text into density-independent pixels
	 * @param con
	 * @param size
	 * @return
	 */
	public static int dp(float size){
        return (int) dpFloat(size);
    }

    public static float dpFloat(float size){
        return (ZooApplication.getContext().getResources().getDisplayMetrics().density*size);
    }
}
