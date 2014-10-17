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
package org.jboss.aerogear.android.pipeline.support;

import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.impl.pipeline.loader.support.AbstractSupportPipeLoader;
import org.jboss.aerogear.android.pipeline.AbstractCallback;
import org.jboss.aerogear.android.pipeline.LoaderPipe;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import com.google.common.base.Objects;

/**
 * 
 * {@link LoaderPipe} and {@link AuthenticationModule} instances which consume
 * callbacks of this type will supply it with a {@link Activity} instance before
 * onSuccess or onFailure are called.  This should not be done by the user.
 * 
 * These calls are not guaranteed to be thread safe.  Instances of the callback
 * should not be shared among Activities and Fragments.
 * 
 * After onSuccess or onFailure have been called, the activity will be set to null.
 * 
 */
public abstract class AbstractFragmentActivityCallback<T> extends AbstractCallback<T> {

    private transient FragmentActivity activity;

    /**
     * This accepts an arbitrary list of Object and uses {@link Objects} to 
     * generate a hashcode.  This code is used to provided the loader manager
     * with a unique value to determine uniqueness of calls to read, etc.
     * 
     * @param params A collection of objects which will be used to generate a 
     * hashcode
     */
    public AbstractFragmentActivityCallback(Object... params) {
        super(params);
    }

    /**
     * This method should be called in the onSuccess or onFailure methods of 
     * subclasses.
     * 
     * @return the activity instance 
     */
    protected FragmentActivity getFragmentActivity() {
        return activity;
    }

    /**
     * This method is called by {@link AbstractSupportPipeLoader} or AbstractSupportAuthenticationLoader
     * during the onLoadComplete method before onSuccess or onFailure are called.
     * 
     * @param activity the activity which will handle this callback
     */
    public void setFragmentActivity(FragmentActivity activity) {
        this.activity = activity;
    }

}
