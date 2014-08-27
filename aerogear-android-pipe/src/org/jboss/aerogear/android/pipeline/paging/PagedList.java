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
package org.jboss.aerogear.android.pipeline.paging;

import java.util.List;

import org.jboss.aerogear.android.Callback;

/**
 * This class wraps a result and provides methods for retrieving the next and previous result sets.
 * 
 * @param <T> the data type of the list
 */
public interface PagedList<T> extends List<T> {

    /**
     * Retrieve the next result set.  This method MUST NOT pass data to the callback which can not be used.
     * 
     * @param callback a Callback that will handle the next page of data
     */
    public void next(Callback<List<T>> callback);

    /**
     * Retrieve the previous result set.  This method MUST NOT pass data to the callback which can not be used.
     * 
     * @param callback a Callback that will handle the previous page of data
     */
    public void previous(Callback<List<T>> callback);
}
