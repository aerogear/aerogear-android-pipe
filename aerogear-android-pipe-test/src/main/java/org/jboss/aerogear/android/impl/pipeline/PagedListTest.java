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
package org.jboss.aerogear.android.impl.pipeline;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.impl.pipeline.paging.WrappingPagedList;
import org.jboss.aerogear.android.pipeline.Pipe;

import android.test.AndroidTestCase;

public class PagedListTest extends AndroidTestCase {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testNext() {
        Pipe pipe = mock(Pipe.class);
        ReadFilter next = new ReadFilter();
        List delegate = new ArrayList();
        ReadFilter previous = new ReadFilter();

        next.setLinkUri(URI.create("./next"));
        previous.setLinkUri(URI.create("./previous"));

        WrappingPagedList list = new WrappingPagedList(pipe, delegate, next, previous);
        list.next(mock(Callback.class));
        list.previous(mock(Callback.class));

        verify(pipe).read(eq(next), any(Callback.class));
        verify(pipe).read(eq(previous), any(Callback.class));

    }

}
