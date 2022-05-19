package org.prebid.mobile.reflection;

import org.apache.commons.lang3.reflect.FieldUtils;

public class Reflection {

    public static void setVariableTo(Object object, String fieldName, Object objectToPut) {
        try {
            ReflectionUtils.setVariableValueInObject(object, fieldName, objectToPut);
        } catch (IllegalAccessException e) {
            throw new NullPointerException("Can't set reflection field: " + fieldName + " " + getClassName(object));
        }
    }

    public static Object getField(Object object, String fieldName) {
        try {
            return FieldUtils.readField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new NullPointerException("Can't get reflection field: " + fieldName + " " + getClassName(object));
        }
    }

    private static String getClassName(Object object) {
        return "(" + object.getClass() + ")";
    }

}
