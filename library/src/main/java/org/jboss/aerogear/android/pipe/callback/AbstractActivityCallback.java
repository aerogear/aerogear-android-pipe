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

import org.jboss.aerogear.android.pipe.loader.AbstractPipeLoader;

import android.app.Activity;
import java.util.Arrays;

/**
 * 
 * {@link org.jboss.aerogear.android.pipe.LoaderPipe} will consume
 * callbacks of this type will supply it with a {@link Activity} instance before
 * onSuccess or onFailure are called. This should not be done by the user.
 * 
 * These calls are not guaranteed to be thread safe. Instances of the callback
 * should not be shared among Activities and Fragments.
 * 
 * After onSuccess or onFailure have been called, the activity will be set to null.
 * 
 * @param <T> the type of data which will be passed to onSuccess
 */
public abstract class AbstractActivityCallback<T> extends AbstractCallback<T> {

    private transient Activity activity;

    /**
     * This accepts an arbitrary list of Object and uses {@link Arrays#hashCode(Object[]) } to
     * generate a hashcode. This code is used to provided the loader manager
     * with a unique value to determine uniqueness of calls to read, etc.
     * 
     * @param params A collection of objects which will be used to generate a
     *            hashcode
     */
    public AbstractActivityCallback(Object... params) {
        super(params);
    }

    /**
     * This method should be called in the onSuccess or onFailure methods of
     * subclasses.
     * 
     * @return the activity instance
     */
    protected Activity getActivity() {
        return activity;
    }

    /**
     * This method is called by {@link AbstractPipeLoader} during the onLoadComplete method before onSuccess or onFailure are called.
     * 
     * @param activity the activity to be execute against.
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

}
