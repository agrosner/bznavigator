package com.grosner.zoo.fragments;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import com.grosner.smartinflater.annotation.SMethod;
import com.grosner.smartinflater.annotation.SResource;
import com.grosner.zoo.R;
import com.grosner.zoo.adapters.ExhibitAdapter;
import com.grosner.zoo.database.PlaceObject;
import com.grosner.zoo.location.CurrentLocationManager;
import com.grosner.zoo.markers.PlaceMarker;
import com.grosner.zoo.singletons.ExhibitManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class PlaceFragment extends ZooFragment implements SwipeRefreshLayout.OnRefreshListener {

    public enum PlaceType implements Serializable {
        EXHIBITS, FOOD, SPECIAL, SHOPS, ADMIN, NEARBY, GATES, PARKING,
        RESTROOMS, MISC;

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
    private ListView exhibitList = null;

    @SResource
    private SwipeRefreshLayout swipeRefresh;

    private ExhibitAdapter mAdapter;

    public boolean isEmpty() {
        return mAdapter == null || mAdapter.getCount() == 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //PowerInflater.loadBundle(this, getArguments());

        mType = (PlaceType) getArguments().getSerializable("Type");

        mTitle = mType.toTitleString();
        mLayout = R.layout.fragment_place;
    }


    @SMethod
    private void onCreateSwipeRefresh(SwipeRefreshLayout swipeRefresh) {
        swipeRefresh.setOnRefreshListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    /**
     * Refreshes a list based on the amount of params passed to it
     */
    public void refresh() {
        exhibitList.setAdapter(mAdapter = new ExhibitAdapter(mType));
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @SMethod
    private void onItemClickExhibitList(int position) {
        MenuFragment list = (MenuFragment) getActivity().getSupportFragmentManager().findFragmentByTag("MenuFragment");

        if (position == -1) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bronxzoostore.com")));
        } else {
            Bundle bundle = new Bundle();
            if (position > 0) {
                bundle.putSerializable("PlaceMarker", mAdapter.getItem(position));
            } else if (position == 0) {
                ArrayList<PlaceObject> placeList = null;
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
                bundle.putSerializable("PlaceList", placeList);
            }

            switchFragment(getString(R.string.fragment_map), MapViewFragment.class, bundle);

        }
    }
}

