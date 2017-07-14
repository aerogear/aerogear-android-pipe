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
package org.jboss.aerogear.android.pipe.loader;

import android.content.Context;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.pipe.Pipe;
import org.jboss.aerogear.android.pipe.PipeHandler;

import java.util.List;

/**
 * This class performs a read by id operation on behalf of a Pipe using the Loader infrastructure.
 * 
 * As a Loader it will retain a reference it its result until reset() is called.
 */
public class IdReadLoader<T> extends ReadLoader<T> {

    public IdReadLoader(Context context, Callback<List<T>> callback,
                        PipeHandler<T> runner, ReadFilter filter, Pipe<T> pipe) {
        super(context, callback, runner, filter, pipe);
    }

}
