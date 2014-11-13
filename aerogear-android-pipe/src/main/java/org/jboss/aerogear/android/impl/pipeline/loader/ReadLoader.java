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

import java.util.List;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;

import android.content.Context;
import org.jboss.aerogear.android.http.HeaderAndBody;

/**
 * This class performs a read operation on behalf of a Pipe using the Loader
 * infrastructure.
 * 
 * As a Loader it will retain a reference it its result until reset() is called.
 * 
 */
public class ReadLoader<T> extends AbstractPipeLoader<List<T>> {

    private final PipeHandler<T> runner;
    private HeaderAndBody result;
    private final ReadFilter filter;
    private final Pipe<T> requestingPipe;

    public ReadLoader(Context context, Callback<List<T>> callback, PipeHandler<T> runner, ReadFilter filter, Pipe<T> pipe) {
        super(context, callback);
        this.filter = filter;
        this.runner = runner;
        this.requestingPipe = pipe;
    }

    @Override
    public HeaderAndBody loadInBackground() {
        try {
            return (result = runner.onRawReadWithFilter(filter, requestingPipe));
        } catch (Exception e) {
            super.exception = e;
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        if (result != null) {
            deliverResult(result);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        result = null;
    }
}
