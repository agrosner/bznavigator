package com.grosner.zoo.singletons;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * Created by: andrewgrosner
 * Date: 6/28/14.
 * Contributors: {}
 * Description:
 */
public class MethodComparator implements Comparator<Method> {

    @Override
    public int compare(Method lhs, Method rhs) {
        if(lhs.getName().startsWith(MethodNames.SETUP) && !rhs.getName().startsWith(MethodNames.SETUP)){
            return -1;
        } else if(!lhs.getName().startsWith(MethodNames.SETUP) && rhs.getName().startsWith(MethodNames.SETUP)){
            return 1;
            //lhs is the superclass, we want to put those first as well
        } else if(lhs.getDeclaringClass().equals(rhs.getDeclaringClass().getSuperclass())){
            return -1;
        } else if(rhs.getDeclaringClass().equals(lhs.getDeclaringClass().getSuperclass())){
            return 1;
        }
        return 0;
    }
}