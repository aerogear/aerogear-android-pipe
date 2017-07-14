/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.pipe.rest.gson;


import android.support.test.runner.AndroidJUnit4;

import org.jboss.aerogear.android.pipe.helper.Data;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GsonResponseParserTest {

    @Test
    public void testHandleResponseWithEmptyResponse() {
        String jsonResponse = "";
        HeaderAndBody httpResponse = new HeaderAndBody(jsonResponse.getBytes(), new HashMap<String, Object>());

        GsonResponseParser<Data> parser = new GsonResponseParser<Data>();
        List<Data> response = parser.handleResponse(httpResponse, Data.class);

        Assert.assertEquals(0, response.size());
    }

}
