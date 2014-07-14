package com.grosner.zoo.location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by: andrewgrosner
 * Date: 7/13/14.
 * Contributors: {}
 * Description: Called when the user chooses to navigate to a location on the map.
 */
public interface OnNavigateListener {

    /**
     * Called when the location changes and the user is tracking his/her location.
     * @param location - users current location
     * @param bearing - the bearing that the listener should use to animate the map
     */
    public void onNavigated(LatLng location, float bearing);
}
