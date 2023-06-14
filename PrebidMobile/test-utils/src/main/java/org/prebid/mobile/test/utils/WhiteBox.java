/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field. Please report to mockito mailing list.", e);
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

    public static Field field(Class<?> declaringClass, String fieldName) {
        return org.prebid.mobile.test.utils.WhiteBoxHelpers.getFieldFromHierarchy(declaringClass, fieldName);
    }

    public static Method method(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
        return org.prebid.mobile.test.utils.WhiteBoxHelpers.getMethod(declaringClass, methodName, parameterTypes);
    }
}
