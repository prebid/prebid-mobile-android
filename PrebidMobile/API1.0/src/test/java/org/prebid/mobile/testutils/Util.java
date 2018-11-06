package org.prebid.mobile.testutils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Util {
    public static Object getPrivateField(Object object, String fieldName) {
        try {
            Field f = object.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object callPrivateMethodOnObject(Object object, String method, Object... params) {
        int len = params.length;
        Class<?>[] classes = new Class[len];
        for (int i = 0; i < len; i++) {
            classes[i] = params[i].getClass();
        }
        try {
            Method m = object.getClass().getDeclaredMethod(method, classes);
            m.setAccessible(true);
            return m.invoke(object, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
