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
package org.jboss.aerogear.android.impl.pipeline.paging;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;
import org.jboss.aerogear.android.pipeline.paging.PageParameterExtractor;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * This class assumes the response body paging properties are URI's and parses them
 * accordingly.
 */
public class URIBodyPageParser implements PageParameterExtractor<PageConfig> {
    private final URI baseUri;

    private static final String TAG = URIPageHeaderParser.class.getSimpleName();

    public URIBodyPageParser(URI uri) {
        this.baseUri = uri;
    }

    public URIBodyPageParser(URL url) {
        try {
            this.baseUri = url.toURI();
        } catch (URISyntaxException ex) {
            Log.e(TAG, url + " could not become URI", ex);
            throw new RuntimeException(url + " could not become URI", ex);
        }
    }

    public URIBodyPageParser() {
        this.baseUri = null;
    }

    @Override
    public ReadFilter getNextFilter(HeaderAndBody result, PageConfig config) {
        ReadFilter filter = new ReadFilter();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(new String(result.getBody()));
        URI nextUri = URI.create(getFromJSON(element, config.getNextIdentifier()));
        filter.setLinkUri(baseUri.resolve(nextUri));
        return filter;
    }

    @Override
    public ReadFilter getPreviousFilter(HeaderAndBody result, PageConfig config) {
        ReadFilter filter = new ReadFilter();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(new String(result.getBody()));
        URI nextUri = URI.create(getFromJSON(element, config.getPreviousIdentifier()));
        filter.setLinkUri(baseUri.resolve(nextUri));
        return filter;
    }

    private String getFromJSON(JsonElement element, String nextIdentifier) {
        String[] identifiers = nextIdentifier.split("\\.");
        for (String identifier : identifiers) {
            element = element.getAsJsonObject().get(identifier);
        }

        if (element.isJsonNull()) {
            return null;
        } else {
            return element.getAsString();
        }
    }

}
