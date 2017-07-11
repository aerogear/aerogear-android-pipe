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
package org.jboss.aerogear.android.pipe.callback;

import org.jboss.aerogear.android.core.Callback;

import java.util.Arrays;

/**
 * This class provides a hashcode method for a callback based on constructor
 * parameters. This callback is meant to be used in conjuction with
 * Activities/Fragments and LoaderPipes.
 * 
 * Using this class instead of an anonymous Callback will allow your application
 * to persist results from loaders through configuration changes.
 * 
 * @param <T> the type of data to be passed on onSuccess
 */
public abstract class AbstractCallback<T> implements Callback<T> {

    private static final long serialVersionUID = 2336090029150712942L;
    final int hashcode;

    /**
     * This accepts an arbitrary list of Object and uses {@link Arrays#hashCode(Object[]) } to
     * generate a hashcode. This code is used to provided the loader manager
     * with a unique value to determine uniqueness of calls to read, etc.
     * 
     * @param params A collection of objects which will be used to generate a
     *            hashcode
     * 
     */
    public AbstractCallback(Object... params) {
        hashcode = Arrays.hashCode(params);
    }

    @Override
    public int hashCode() {
        return hashcode;
    }
}
