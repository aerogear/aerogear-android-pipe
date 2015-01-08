/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.pipe.util;

import java.lang.reflect.Array;

/**
 * Utilities for working with classes, reflection, etc
 */
public class ClassUtils {
    /**
     * This will return a class of the type T[] from a given class. When we read
     * from the AG pipe, Java needs a reference to a generic array type.
     * 
     * @param klass a class to turn into an array class
     * @param <T> the type of the class.
     * 
     * @return an array of klass with a length of 1
     */
    public static <T> Class<T[]> asArrayClass(Class<T> klass) {
        return (Class<T[]>) Array.newInstance(klass, 1).getClass();
    }
}
