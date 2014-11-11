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

import org.jboss.aerogear.android.impl.pipeline.paging.DefaultParameterProvider;

public class PageConfig {

    public static enum MetadataLocations implements MetadataLocation {

        WEB_LINKING("webLinking"),
        HEADERS("headers"),
        BODY("body");
        private final String value;

        private MetadataLocations(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private MetadataLocation metadataLocation = MetadataLocations.WEB_LINKING;
    private String nextIdentifier = "next";
    private String previousIdentifier = "previous";
    private String offsetValue = "0";
    private Integer limitValue = 10;
    private ParameterProvider parameterProvider = new DefaultParameterProvider();
    private PageParameterExtractor pageParameterExtractor;

    /**
     * MetadataLocation indicates whether paging information is received from
     * the response header, the response body (body) or via RFC 5988
     * (webLinking), which is the default
     * 
     * @return the current location
     */
    public MetadataLocation getMetadataLocation() {
        return metadataLocation;
    }

    /**
     * MetadataLocation indicates whether paging information is received from
     * the response header, the response body (body) or via RFC 5988
     * (webLinking), which is the default
     * 
     * @param metadataLocation a different metaDataLocation
     */
    public void setMetadataLocation(MetadataLocation metadataLocation) {
        this.metadataLocation = metadataLocation;
    }

    /**
     * NextIdentifier names the element containing data for the next page
     * (default: next)
     * 
     * @return the current identifier
     */
    public String getNextIdentifier() {
        return nextIdentifier;
    }

    /**
     * NextIdentifier names the element containing data for the next page
     * (default: next)
     * 
     * @param nextIdentifier names the element containing data for the next page
     *            (default: next)
     * 
     */
    public void setNextIdentifier(String nextIdentifier) {
        this.nextIdentifier = nextIdentifier;
    }

    /**
     * PreviousIdentifier names the element containing data for the previous
     * page (default: previous)
     * 
     * @return the current identifier
     */
    public String getPreviousIdentifier() {
        return previousIdentifier;
    }

    /**
     * PreviousIdentifier names the element containing data for the previous
     * page (default: previous)
     * 
     * @param previousIdentifier the element containing data for the previous
     *            page (default: previous)
     */
    public void setPreviousIdentifier(String previousIdentifier) {
        this.previousIdentifier = previousIdentifier;
    }

    /**
     * OffsetValue is the offset of the first element that should be included in
     * the returned collection (default: 0)
     * 
     * @return the current offset
     */
    public String getOffsetValue() {
        return offsetValue;
    }

    /**
     * OffsetValue is the offset of the first element that should be included in
     * the returned collection (default: 0)
     * 
     * @param offsetValue the first element that should be included in
     *            the returned collection (default: 0)
     */
    public void setOffsetValue(String offsetValue) {
        this.offsetValue = offsetValue;
    }

    /**
     * LimitValue is the maximum number of results the server should return
     * (default: 10)
     * 
     * @return the current limit
     */
    public Integer getLimitValue() {
        return limitValue;
    }

    /**
     * LimitValue is the maximum number of results the server should return
     * (default: 10)
     * 
     * @param limitValue a new Limit value
     */
    public void setLimitValue(Integer limitValue) {
        this.limitValue = limitValue;
    }

    /**
     * The {@link ParameterProvider} for paging. Defaults to {@link DefaultParameterProvider}
     * 
     * @return the current provider
     */
    public ParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    /**
     * The {@link ParameterProvider} for paging. Defaults to {@link DefaultParameterProvider}
     * 
     * @param parameterProvider a new provider
     */
    public void setParameterProvider(ParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }

    /**
     * PageParameterExtractor is the a {@link PageParameterExtractor} which
     * parses the response and provides data to a Pipe for the "next" and
     * "previous" pages.
     * 
     * The default value depends on the current setting of MetadataLocation.
     * 
     * @return the current PageParameterExtractor
     */
    public PageParameterExtractor getPageParameterExtractor() {
        return pageParameterExtractor;
    }

    /**
     * PageParameterExtractor is the a {@link PageParameterExtractor} which
     * parses the response and provides data to a Pipe for the "next" and
     * "previous" pages.
     * 
     * The default value depends on the current setting of MetadataLocation.
     * 
     * @param pageParameterExtractor an new Extractor
     * 
     */
    public void setPageParameterExtractor(PageParameterExtractor pageParameterExtractor) {
        this.pageParameterExtractor = pageParameterExtractor;
    }
}
