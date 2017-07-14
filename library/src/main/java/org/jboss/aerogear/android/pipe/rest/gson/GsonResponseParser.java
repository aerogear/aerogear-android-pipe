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
package org.jboss.aerogear.android.pipe.rest.gson;

import org.jboss.aerogear.android.pipe.MarshallingConfig;
import org.jboss.aerogear.android.pipe.ResponseParser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.util.ClassUtils;

public class GsonResponseParser<T> implements ResponseParser<T> {

    private Gson gson;
    private MarshallingConfig marshallingConfig = new MarshallingConfig();

    public GsonResponseParser() {
        this.gson = new Gson();
    }

    public GsonResponseParser(Gson gson) {
        this.gson = gson;
    }

    @Override
    public List<T> handleResponse(HeaderAndBody httpResponse, Class<T> responseType) {
        byte[] responseBody = httpResponse.getBody();
        List<T> result;
        String responseAsString = new String(responseBody, marshallingConfig.getEncoding());
        JsonParser parser = new JsonParser();
        JsonElement httpJsonResult = parser.parse(responseAsString);
        httpJsonResult = getResultElement(httpJsonResult, marshallingConfig.getDataRoot());

        if(responseAsString.trim().isEmpty()) {
            return new ArrayList<T>();
        }

        if (httpJsonResult.isJsonArray()) {
            T[] resultArray = gson.fromJson(httpJsonResult.toString(), ClassUtils.asArrayClass(responseType));
            result = Arrays.asList(resultArray);

        } else {
            T resultObject = gson.fromJson(httpJsonResult.toString(), responseType);
            List<T> resultList = new ArrayList<T>(1);
            resultList.add(resultObject);
            result = resultList;

        }

        return result;
    }

    private JsonElement getResultElement(JsonElement element, String dataRoot) {
        String[] identifiers = dataRoot.split("\\.");
        for (String identifier : identifiers) {
            if (identifier.equals("")) {
                return element;
            }
            JsonElement newElement = element.getAsJsonObject().get(identifier);
            if (newElement == null) {
                return element;
            } else {
                element = newElement;
            }
        }
        return element;
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
