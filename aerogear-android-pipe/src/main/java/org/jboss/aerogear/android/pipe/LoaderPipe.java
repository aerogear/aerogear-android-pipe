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
package org.jboss.aerogear.android.pipe;

import java.util.List;
import java.util.Map;

/**
 * Sometimes a Pipe will actually be wrapped in a Loader. Classes which do so
 * implement this interface and have certain methods like reset exposed.
 */
public interface LoaderPipe<T> extends Pipe<T> {

    /**
     * Bundle key for callbacks handed to Pipe methods.
     */
    public static final String CALLBACK = "org.jboss.aerogear.android.impl.pipeline.LoaderPipe.CALLBACK";

    /**
     * Bundle key for the Pipe method which was called
     */
    public static final String METHOD = "org.jboss.aerogear.android.impl.pipeline.LoaderPipe.METHOD";

    /**
     * Bundle key for the ReadFilter param
     */
    public static final String FILTER = "org.jboss.aerogear.android.impl.pipeline.LoaderPipe.FILTER";

    /**
     * Bundle key for the item to be saved
     */
    public static final String ITEM = "org.jboss.aerogear.android.impl.pipeline.LoaderPipe.ITEM";

    /**
     * Bundle key for the id of the item to save
     */
    public static final String SAVE_ID = "org.jboss.aerogear.android.impl.pipeline.LoaderPipe.SAVE_ID";

    /**
     * Bundle key for the id of the item to remove
     */
    public static final String REMOVE_ID = "org.jboss.aerogear.android.impl.pipeline.LoaderPipe.REMOVIE_ID";

    /**
     * Calls reset on all loaders associated with this pipe.
     */
    public void reset();

    /**
     * Passes in a multimap of ids for the named pipe. LoaderPipe should manage this collection.
     * 
     * @param idsForNamedPipes A map of all LoaderIds for pipes hased by their name
     */
    public void setLoaderIds(Map<String, List<Integer>> idsForNamedPipes);

}
