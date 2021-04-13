package org.prebid.mobile.test.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WhiteBox {

    private WhiteBox() {}

    public static <T> T getInternalState(Object target, String field) {
        Class<?> c = target.getClass();
        try {
            Field f = org.prebid.mobile.test.utils.WhiteBoxHelpers.getFieldFromHierarchy(c, field);
            f.setAccessible(true);
            return (T) f.get(target);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to get internal state on a private field. Please report to mockito mailing list.", e);
        }
    }

    public static void setInternalState(Object target, String field, Object value) {
        Class<?> c = target.getClass();
        try {
            Field f = org.prebid.mobile.test.utils.WhiteBoxHelpers.getFieldFromHierarchy(c, field);
            f.setAccessible(true);
            f.set(target, value);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field. Please report to mockito mailing list.", e);
        }
    }

    public static Field field(Class<?> declaringClass, String fieldName) {
        return org.prebid.mobile.test.utils.WhiteBoxHelpers.getFieldFromHierarchy(declaringClass, fieldName);
    }

    public static Method method(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
        return org.prebid.mobile.test.utils.WhiteBoxHelpers.getMethod(declaringClass, methodName, parameterTypes);
    }
}
