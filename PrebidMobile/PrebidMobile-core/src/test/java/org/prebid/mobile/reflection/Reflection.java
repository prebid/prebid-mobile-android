package org.prebid.mobile.reflection;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;

public class Reflection {

    public static void setVariableTo(
        Object object,
        String fieldName,
        Object objectToPut
    ) {
        try {
            ReflectionUtils.setVariableValueInObject(object, fieldName, objectToPut);
        } catch (IllegalAccessException e) {
            throw new NullPointerException("Can't set field using reflection: " + fieldName + " " + getClassName(object));
        }
    }

    public static void setStaticVariableTo(
        Class classType,
        String fieldName,
        Object objectToPut
    ) {
        try {
            Field field = classType.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, objectToPut);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new NullPointerException("Can't set static field using reflection: " + fieldName + " " + classType);
        }
    }

    public static <T> T getStaticFieldOf(
        Class classType,
        String fieldName
    ) {
        try {
            Field field = ReflectionUtils.getFieldByNameIncludingSuperclasses(fieldName, classType);
            field.setAccessible(true);
            // noinspection unchecked
            return (T) field.get(null);
        } catch (IllegalAccessException e) {
            throw new NullPointerException("Can't get static field using reflection: " + fieldName + " " + classType);
        }
    }

    public static <T> T getFieldOf(
        Object object,
        String fieldName
    ) {
        try {
            // noinspection unchecked
            return (T) FieldUtils.readField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new NullPointerException("Can't get field using reflection: " + fieldName + " " + getClassName(object));
        }
    }

    private static String getClassName(Object object) {
        return "(" + object.getClass() + ")";
    }

}
