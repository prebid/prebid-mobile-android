package org.prebid.mobile.reflection;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static void setVariableValueInObject(
        Object object,
        String variable,
        Object value
    ) throws IllegalAccessException {
        Field field = getFieldByNameIncludingSuperclasses(variable, object.getClass());
        field.setAccessible(true);
        field.set(object, value);
    }

    public static Field getFieldByNameIncludingSuperclasses(
        String fieldName,
        Class clazz
    ) {
        Field retValue = null;
        try {
            retValue = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superclass = clazz.getSuperclass();
            if (superclass != null) {
                retValue = getFieldByNameIncludingSuperclasses(fieldName, superclass);
            }
        }
        return retValue;
    }

}
