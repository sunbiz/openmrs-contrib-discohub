package org.openmrs.contrib.discohub;

/**
 * Copyright 2015, Saptarshi Purkayastha
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author sunbiz
 */
public class HttpUtils {

    public static Map<String, Object> getData(String url, Header[] headers) throws IOException {
        final Map<String, Object> responseMap = new LinkedHashMap<>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeaders(headers);

        // Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    responseMap.put("headers", response.getAllHeaders());
                    //System.out.println("Ratelimit attempts left: " + response.getHeaders("X-RateLimit-Remaining")[0]);
                    //System.out.println("Etag = " + response.getHeaders("ETag")[0]);
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else if (status == 304) {
                    //System.out.println("GOT 304!!!");
                    return null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
        String responseBody = httpclient.execute(httpget, responseHandler);
        responseMap.put("content", responseBody);
        return responseMap;
    }
}
