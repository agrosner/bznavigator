package com.grosner.zoo.fragments;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;

import com.grosner.smartinflater.annotation.SMethod;
import com.grosner.smartinflater.annotation.SResource;
import com.grosner.zoo.R;
import com.grosner.zoo.adapters.ExhibitAdapter;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.markers.PlaceMarker;
import com.grosner.zoo.singletons.ExhibitManager;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class PlaceFragment extends ZooFragment {

    public enum PlaceType implements Serializable {
        EXHIBITS, FOOD, SPECIAL, SHOPS, ADMIN, NEARBY, GATES, PARKING,
        RESTROOMS, MISC, SEARCH;

        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        /**
         * Returns capitalized name
         *
         * @return
         */
        public String toTitleString() {
            String s = toString();
            String first = s.substring(0, 1);
            return s.replaceFirst(first, first.toUpperCase());
        }

        public static PlaceType getFromFileName(String fName) {
            String[] files = ExhibitManager.DATA_FILES;
            if (fName.equals(files[0])) {
                return EXHIBITS;
            } else if (fName.equals(files[1])) {
                return FOOD;
            } else if (fName.equals(files[2])) {
                return SHOPS;
            } else if (fName.equals(files[3])) {
                return GATES;
            } else if (fName.equals(files[4])) {
                return PARKING;
            } else if (fName.equals(files[5])) {
                return ADMIN;
            } else if (fName.equals(files[6])) {
                return SPECIAL;
            } else if (fName.equals(files[7])) {
                return RESTROOMS;
            } else return MISC;
        }
    }

    ;

    /**
     * The mType of this fragment
     */
    private PlaceType mType = PlaceType.EXHIBITS;

    /**
     * The view that holds all of the child placeitem views
     */
    @SResource
    private ListView listView = null;

    private ExhibitAdapter mAdapter;

    private SwingLeftInAnimationAdapter mAnimationAdapter;

    public boolean isEmpty() {
        return mAdapter == null || mAdapter.getCount() == 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //PowerInflater.loadBundle(this, getArguments());

        mType = (PlaceType) getArguments().getSerializable("Type");

        mTitle = mType.toTitleString();
        mLayout = R.layout.fragment_listview;
    }

    @SMethod
    private void onCreateListView(ListView listView){
        mAnimationAdapter = new SwingLeftInAnimationAdapter(mAdapter = new ExhibitAdapter(mType));
        mAnimationAdapter.setAbsListView(listView);
        listView.setAdapter(mAnimationAdapter);
    }

    public void refresh(){
        if(mAdapter!=null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @SMethod
    private void onItemClickListView(int position) {
        if (position == -1) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bronxzoostore.com")));
        } else {
            MapViewFragment map = (MapViewFragment) getActivity().getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_map));
            if(map!=null) {

                if (position > 0) {
                    map.addPlace(new PlaceMarker().place(mAdapter.getItem(position)));
                } else if (position == 0) {
                    ArrayList<PlaceObject> placeList;
                    if (mType != PlaceType.NEARBY) {
                        placeList = (ArrayList<PlaceObject>) mAdapter.getObjects();
                    } else {
                        List<PlaceObject> objects = mAdapter.getObjects();
                        Collections.sort(objects, new Comparator<PlaceObject>() {

                            @Override
                            public int compare(PlaceObject lhs, PlaceObject rhs) {
                                Location loc = CurrentLocationManager.getSharedManager().getLastKnownLocation();

                                return Float.valueOf(lhs.getLocation().distanceTo(loc))
                                        .compareTo(rhs.getLocation().distanceTo(loc));
                            }

                        });
                        List<PlaceObject> reducePoints = new LinkedList<>();
                        while (reducePoints.size() != 10) {
                            reducePoints.add(objects.get(reducePoints.size()));
                        }
                        placeList = (ArrayList<PlaceObject>) reducePoints;
                    }
                    ArrayList<PlaceMarker> placeMarkers = new ArrayList<>();
                    for(PlaceObject place: placeList){
                        placeMarkers.add(new PlaceMarker().place(place));
                    }
                    map.addPlaceList(placeMarkers);
                }
            }
            getZooActivity().closeDrawers();
            getZooActivity().onBackPressed();
        }
    }
}

