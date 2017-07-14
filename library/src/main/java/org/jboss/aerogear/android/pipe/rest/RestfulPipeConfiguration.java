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
package org.jboss.aerogear.android.pipe.rest;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jboss.aerogear.android.core.Config;
import org.jboss.aerogear.android.pipe.rest.gson.GsonRequestBuilder;
import org.jboss.aerogear.android.pipe.rest.gson.GsonResponseParser;
import org.jboss.aerogear.android.pipe.*;
import org.jboss.aerogear.android.pipe.module.PipeModule;
import org.jboss.aerogear.android.pipe.paging.PageConfig;

/**
 * Configures a Pipe which interacts with RESTful endpoints.
 */
public class RestfulPipeConfiguration extends PipeConfiguration<RestfulPipeConfiguration> implements Config<RestfulPipeConfiguration> {
    private URL url;
    private String name;
    private Integer timeout = 60000;

    private final List<PipeModule> modules = new ArrayList<PipeModule>();
    private PageConfig pageConfig;
    private RequestBuilder requestBuilder = new GsonRequestBuilder();
    private ResponseParser responseParser = new GsonResponseParser();
    private PipeHandler handler = null;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RestfulPipeConfiguration setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    protected <DATA> Pipe<DATA> buildPipeForClass(Class<DATA> aClass) {
        if (this.url == null) {
            throw new IllegalStateException("url may not be null");
        }

        return new RestAdapter<DATA>(aClass, this);

    }

    @Override
    public RestfulPipeConfiguration withUrl(URL url) {
        this.url = url;
        return this;
    }

    @Override
    public RestfulPipeConfiguration module(PipeModule module) {
        modules.add(module);
        return this;
    }

    @Override
    public List<PipeModule> getModules() {
        return new ArrayList<PipeModule>(this.modules);
    }

    @Override
    public RestfulPipeConfiguration timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public RestfulPipeConfiguration pageConfig(PageConfig pageConfig) {
        this.pageConfig = pageConfig;
        return this;
    }

    @Override
    public RestfulPipeConfiguration requestBuilder(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
        return this;
    }

    @Override
    public RestfulPipeConfiguration responseParser(ResponseParser responseParser) {
        this.responseParser = responseParser;
        return this;
    }

    public RestfulPipeConfiguration pipeHandler(PipeHandler handler) {
        this.handler = handler;
        return this;
    }

    public PipeHandler getPipeHandler() {
        return this.handler;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public ResponseParser getResponseParser() {
        return this.responseParser;
    }

    public PageConfig getPageConfig() {
        return pageConfig;
    }

    public URL getUrl() {
        return url;
    }

}
