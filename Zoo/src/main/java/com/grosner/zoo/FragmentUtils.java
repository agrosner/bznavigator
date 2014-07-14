package com.grosner.zoo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created By: andrewgrosner
 * Date: 8/30/13
 * Contributors:
 * Description:
 */
public class FragmentUtils {


    public static void goToFragment(FragmentActivity activity, String fragmentName, Class fragmentClass, Bundle extras) {
        goToFragment(activity, fragmentName, fragmentClass, extras, false);
    }


    public static void goToFragment(FragmentActivity activity, String fragmentName, Class fragmentClass, Bundle extras,
                             boolean backstack,
                             boolean popBackStack) {
        //getSliderModule().closeMenu();
        Fragment testFrag = activity.getSupportFragmentManager().findFragmentByTag(fragmentName);
        if (testFrag == null || !testFrag.isVisible()) {
            FragmentManager manager = activity.getSupportFragmentManager();
            if(popBackStack)
                manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            try {
                Fragment fragment = (Fragment) Class.forName(fragmentClass.getName()).newInstance();
                if (extras != null) {
                    fragment.setArguments(extras);
                }
                replaceFragment(activity, fragment, backstack, R.id.ContentView, fragmentName);
            } catch (Throwable e) {
            }
        }

    }

    public static Fragment getFragment(FragmentActivity activity, Class fragmentClass, String tag, Bundle extras){
        Fragment testFrag = activity.getSupportFragmentManager().findFragmentByTag(tag);
        if (testFrag == null || !testFrag.isVisible()) {
            FragmentManager manager = activity.getSupportFragmentManager();
            manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            try {
                testFrag = (Fragment) Class.forName(fragmentClass.getName()).newInstance();
                if (extras != null) {
                    testFrag.setArguments(extras);
                }
            }catch (Throwable t){
            }
        }
        return testFrag;
    }

    public static Fragment getFragment(Class clazz){
        try {
            return (Fragment) Class.forName(clazz.getName()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void goToFragment(FragmentActivity activity, String fragmentName, Class fragmentClass, Bundle extras, boolean backstack){
        goToFragment(activity, fragmentName, fragmentClass, extras, backstack, true);
    }

    public static void replaceFragment(Context context, Fragment frag, boolean backStack, int layout,
                                String tag) {
        if(context==null)
            return;
        FragmentTransaction transaction = ((FragmentActivity)context).getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(layout, frag, tag);
        if (backStack)
            transaction.addToBackStack(null);
        transaction.setCustomAnimations(android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
        transaction.commit();
    }
}
