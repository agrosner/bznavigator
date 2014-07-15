package com.grosner.zoo.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Minimum Polygon class for Android.
 */
public class Polygon {

    // Polygon coodinates.
    private int[] polyY, polyX;

    // Number of sides in the polygon.
    private int polySides;

    /**
     * Default constructor.
     * @param px Polygon y coods.
     * @param py Polygon x coods.
     * @param ps Polygon sides count.
     */
    public Polygon( int[] px, int[] py, int ps ) {

        polyX = px;
        polyY = py;
        polySides = ps;
    }
    
    /**
     * Checks if the Polygon contains a point.
     * @param g
     * @return
     */
    public boolean contains(LatLng g){
    	return contains((int)(g.longitude*1E6), (int)(g.latitude*1E6));
    }

    public boolean contains(Location location){
        return contains((int)(location.getLongitude()*1E6), (int)(location.getLatitude()*1E6));
    }

    /**
     * Checks if the Polygon contains a point.
     * @see "http://alienryderflex.com/polygon/"
     * @param x Point horizontal pos.
     * @param y Point vertical pos.
     * @return Point is in Poly flag.
     */
    public boolean contains( int x, int y ) {

        boolean oddTransitions = false;
        for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {
            if( ( polyY[ i ] < y && polyY[ j ] >= y ) || ( polyY[ j ] < y && polyY[ i ] >= y ) ) {
                if( polyX[ i ] + ( y - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < x ) {
                    oddTransitions = !oddTransitions;          
                }
            }
        }
        return oddTransitions;
    }   
}