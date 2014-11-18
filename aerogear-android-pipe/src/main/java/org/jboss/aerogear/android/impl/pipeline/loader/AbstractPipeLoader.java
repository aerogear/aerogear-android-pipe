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
package org.jboss.aerogear.android.impl.pipeline.loader;

import org.jboss.aerogear.android.Callback;

import android.content.AsyncTaskLoader;
import android.content.Context;
import org.jboss.aerogear.android.http.HeaderAndBody;

/**
 * This class maintains references to the callback to be called when a Loader
 * supporting a Pipe's operation completes. It also contains a reference to any
 * exception which may have been thrown.
 
 */
public abstract class AbstractPipeLoader<T> extends AsyncTaskLoader<HeaderAndBody> {

    private final Callback<T> callback;
    protected Exception exception;

    public AbstractPipeLoader(Context context, Callback<T> callback) {
        super(context);
        this.callback = callback;
    }

    public boolean hasException() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    protected void onReset() {
        super.onReset();
        exception = null;
    }

    public Callback<T> getCallback() {
        return callback;
    }

}
