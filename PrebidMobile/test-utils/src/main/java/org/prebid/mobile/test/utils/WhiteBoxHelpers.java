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
import java.util.LinkedHashSet;
import java.util.Set;

class WhiteBoxHelpers {

    private WhiteBoxHelpers() {}

    public static Method getMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
        Class<?> thisType = type;
        if (parameterTypes == null) {
            parameterTypes = new Class<?>[0];
        }
        while (thisType != null) {
            Method[] methodsToTraverse = null;
            if (thisType.isInterface()) {
                // Interfaces only contain public (and abstract) methods, no
                // need to traverse the hierarchy.
                methodsToTraverse = getAllPublicMethods(thisType);
            }
            else {
                methodsToTraverse = thisType.getDeclaredMethods();
            }
            for (Method method : methodsToTraverse) {
                if (methodName.equals(method.getName())
                    && checkIfParameterTypesAreSame(method.isVarArgs(), parameterTypes, method.getParameterTypes())) {
                    method.setAccessible(true);
                    return method;
                }
            }
            thisType = thisType.getSuperclass();
        }

        throw new RuntimeException("Method \"" + methodName + "\" not found in " + type.getName());
    }

    private static Method[] getAllPublicMethods(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("You must specify a class in order to get the methods.");
        }
        Set<Method> methods = new LinkedHashSet<Method>();

        for (Method method : clazz.getMethods()) {
            method.setAccessible(true);
            methods.add(method);
        }
        return methods.toArray(new Method[0]);
    }

    public static boolean checkIfParameterTypesAreSame(boolean isVarArgs, Class<?>[] expectedParameterTypes,
                                                       Class<?>[] actualParameterTypes) {
        return new ParameterTypesMatcher(isVarArgs, expectedParameterTypes, actualParameterTypes).match();
    }

    public static Field getFieldFromHierarchy(Class<?> clazz, String field) {
        Field f = getField(clazz, field);
        while (f == null && clazz != Object.class) {
            clazz = clazz.getSuperclass();
            f = getField(clazz, field);
        }
        if (f == null) {
            throw new RuntimeException(
                "You want me to get this field: '" + field +
                "' on this class: '" + clazz.getSimpleName() +
                "' but this field is not declared withing hierarchy of this class!");
        }
        f.setAccessible(true);
        return f;
    }

    private static Field getField(Class<?> clazz, String field) {
        try {
            return clazz.getDeclaredField(field);
        }
        catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static class ParameterTypesMatcher {

        private final boolean isVarArgs;
        private final Class<?>[] expectedParameterTypes;
        private final Class<?>[] actualParameterTypes;

        public ParameterTypesMatcher(
                boolean isVarArgs,
                Class<?>[] expectedParameterTypes,
                Class<?>... actualParameterTypes
        ) {
            this.isVarArgs = isVarArgs;
            this.expectedParameterTypes = expectedParameterTypes;
            this.actualParameterTypes = actualParameterTypes;
        }

        private boolean isRemainParamsVarArgs(
                int index,
                Class<?> actualParameterType
        ) {
            return isVarArgs && index == expectedParameterTypes.length - 1 && actualParameterType.getComponentType()
                                                                                                 .isAssignableFrom(
                                                                                                         expectedParameterTypes[index]);
        }

        private boolean isParameterTypesNotMatch(Class<?> actualParameterType, Class<?> expectedParameterType) {
            if (actualParameterType == null) {
                return false;
            }
            if (expectedParameterType == null) {
                return false;
            }
            return !actualParameterType.isAssignableFrom(expectedParameterType);
        }

        public boolean match() {
            assertParametersTypesNotNull();
            if (isParametersLengthMatch()) {
                return false;
            }
            else {
                return isParametersMatch();
            }
        }

        private boolean isParametersLengthMatch() {return expectedParameterTypes.length != actualParameterTypes.length;}

        private void assertParametersTypesNotNull() {
            if (expectedParameterTypes == null || actualParameterTypes == null) {
                throw new IllegalArgumentException("parameter types cannot be null");
            }
        }

        private Boolean isParametersMatch() {
            for (int index = 0; index < expectedParameterTypes.length; index++) {
                final Class<?> actualParameterType = getType(actualParameterTypes[index]);
                if (isRemainParamsVarArgs(index, actualParameterType)) {
                    return true;
                } else {
                    final Class<?> expectedParameterType = expectedParameterTypes[index];
                    if (isParameterTypesNotMatch(actualParameterType, expectedParameterType)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private static Class<?> getType(Object object) {
            Class<?> type = null;
            if (isClass(object)) {
                type = (Class<?>) object;
            }
            else if (object != null) {
                type = object.getClass();
            }
            return type;
        }

        private static boolean isClass(Object argument) {
            return argument instanceof Class<?>;
        }
    }
}
