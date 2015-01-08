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
package org.jboss.aerogear.android.pipe.paging;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;

import android.util.Log;

/**
 * This class assumes the header responses in a response are URI's and parses them
 * accordingly.
 */
public class URIPageHeaderParser implements PageParameterExtractor {
    private final URI baseUri;

    private static final String TAG = URIPageHeaderParser.class.getSimpleName();

    public URIPageHeaderParser(URI uri) {
        this.baseUri = uri;
    }

    public URIPageHeaderParser(URL url) {
        try {
            this.baseUri = url.toURI();
        } catch (URISyntaxException ex) {
            Log.e(TAG, url + " could not become URI", ex);
            throw new RuntimeException(url + " could not become URI", ex);
        }
    }

    public URIPageHeaderParser() {
        this.baseUri = null;
    }

    @Override
    public ReadFilter getNextFilter(HeaderAndBody result, PageConfig config) {
        ReadFilter filter = new ReadFilter();
        URI nextUri = URI.create(result.getHeader(config.getNextIdentifier()).toString());
        filter.setLinkUri(baseUri.resolve(nextUri));
        return filter;
    }

    @Override
    public ReadFilter getPreviousFilter(HeaderAndBody result, PageConfig config) {
        ReadFilter filter = new ReadFilter();
        URI nextUri = URI.create(result.getHeader(config.getPreviousIdentifier()).toString());
        filter.setLinkUri(baseUri.resolve(nextUri));
        return filter;
    }

}
