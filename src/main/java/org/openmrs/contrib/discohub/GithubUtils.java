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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.openmrs.contrib.discohub.Integrator.GITHUB_BASIC_AUTH_HEADER;
import static org.openmrs.contrib.discohub.Integrator.GITHUB_JSON_ACCEPT_HEADER;
import static org.openmrs.contrib.discohub.Integrator.GITHUB_URL;

/**
 * @author sunbiz
 * @author namratanehete
 */
public class GithubUtils {

    /**
     * This method will return the list of repo names under the
     * user/organization
     *
     * @param user Username or org id for which you need all repos
     * @return a List of repos for the given user
     * @throws IOException
     */
    public static List<String> getGithubRepos(String user) throws IOException {
        List<String> repos = getPageData(GITHUB_URL + "/users/" + user + "/repos?per_page=100", "name");
        return repos;
    }

    /**
     * Gets a list of commits by a github id on a given repo name
     *
     * @param githubId Github ID of the user
     * @param repoName Name of the github repo
     * @return
     * @throws IOException
     */
    public static List<String> getGithubCommits(String githubId, String repoName) throws IOException {
        List<String> commits = getPageData(GITHUB_URL + "/repos/openmrs/" + repoName + "/commits?per_page=100&author=" + githubId, "sha");
        return commits;
    }

    /**
     * Returns a list of items from a Json Array Object based on the jsonKey
     *
     * @param responseBody the response content of a call containing Json string
     * @param jsonKey the key in the JSON for which you need a list of items
     * @return
     */
    public static List<String> getJsonResponseAsList(String responseBody, String jsonKey) {
        List<String> list = new ArrayList<>();
        if (null != responseBody) {
            JSONArray jsonArry = new JSONArray(responseBody);
            for (int i = 0; i < jsonArry.length(); i++) {
                JSONObject repoJson = (JSONObject) jsonArry.get(i);
                String repoUrl = (String) repoJson.get(jsonKey);
                list.add(repoUrl);
            }
        }
        return list;
    }

    /**
     * Will fetch a list of items from a Github GET call based on a json key
     *
     * @param url
     * @param jsonKey
     * @return
     * @throws IOException
     */
    private static List<String> getPageData(String url, String jsonKey) throws IOException {
        List<String> list = new ArrayList<>();
        Header[] headers = {GITHUB_JSON_ACCEPT_HEADER, GITHUB_BASIC_AUTH_HEADER};
        Map<String, Object> data = HttpUtils.getData(url, headers);
        list.addAll(getJsonResponseAsList((String) data.get("content"), jsonKey));
        Header[] responseHeaders = (Header[]) data.get("headers");
        if (null != responseHeaders && responseHeaders.length > 0) {
            for (int i = 0; i < responseHeaders.length; i++) {
                if (responseHeaders[i].getName().equals("Link")) {
                    String headerValue = responseHeaders[i].getValue();
                    if (headerValue.contains("rel=\"next\"")) {
                        list.addAll(getPageData(headerValue.substring(1, headerValue.indexOf(">")), jsonKey));
                    }
                    break;
                }
            }
        }
        return list;
    }
}
