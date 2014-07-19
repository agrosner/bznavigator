package com.grosner.zoo.utils;

import com.grosner.zoo.R;
import com.grosner.zoo.application.ZooApplication;

/**
 * Created by: andrewgrosner
 * Date: 7/16/14.
 * Contributors: {}
 * Description:
 */
public class DeviceUtils {

    enum DeviceSize {
        HANDSET,
        SMALL_TABLET,
        TABLET;

        public static DeviceSize getFromString(String type){
            return DeviceSize.valueOf(type.toUpperCase());
        }
    }

    private static DeviceSize sDeviceSize;

    /**
     * Will initialize the device size globally using string resources as primary
     * determinate when using {@link com.fuzz.android.common.GlobalContext}.
     * ome devices report incorrect screen width dp so we are relying on the resource qualifiers to get it done.
     */
    static void checkDeviceSize(){
        if(sDeviceSize==null) {
            sDeviceSize = DeviceSize.getFromString(ZooApplication.getResourceString(R.string.core_device_size));
        }
    }

    public static boolean isTablet(){
        checkDeviceSize();
        return (!sDeviceSize.equals(DeviceSize.HANDSET));
    }
}
