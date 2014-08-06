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

import org.jboss.aerogear.android.pipeline.MarshallingConfig;
import org.jboss.aerogear.android.pipeline.RequestBuilder;

import com.google.gson.Gson;

public class GsonRequestBuilder<T> implements RequestBuilder<T> {

    public static final String CONTENT_TYPE = "application/json";

    private Gson gson;
    private MarshallingConfig marshallingConfig = new MarshallingConfig();

    public GsonRequestBuilder() {
        this.gson = new Gson();
    }

    public GsonRequestBuilder(Gson gson) {
        this.gson = gson;
    }

    @Override
    public byte[] getBody(T data) {
        return gson.toJson(data).getBytes(marshallingConfig.getEncoding());
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    /**
     * The marshalling config sets options for reading and processing data
     * 
     * @return the current config
     */
    @Override
    public MarshallingConfig getMarshallingConfig() {
        return marshallingConfig;
    }

    public void setMarshallingConfig(MarshallingConfig marshallingConfig) {
        this.marshallingConfig = marshallingConfig;
    }

}