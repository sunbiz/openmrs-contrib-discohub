package org.openmrs.contrib.discohub;

/**
 * Copyright 2015, Namrata Nehete
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
import static org.openmrs.contrib.discohub.Integrator.GITHUB_JSON_ACCEPT_HEADER;

/**
 * @author namratanehete
 * @author sunbiz
 */
public class DiscourseUtils {

    public static final String DISCOURSE_URL = "https://talk.openmrs.org";
    public static final String GITHUB_USER_FIELD_ID = "2";

    public static List<String> getUsersWithBadges(int badgeId) throws IOException {
        int offset = 0;
        List<String> usernameList = new ArrayList<>();
        Header[] headers = {GITHUB_JSON_ACCEPT_HEADER};
        Map<String, Object> data = HttpUtils.getData(DISCOURSE_URL + "/user_badges.json?offset=" + offset + "&badge_id=" + badgeId, headers);
        List<String> jsonResponseList = getUsernameListFromJson((String) data.get("content"), "users", "username");
        usernameList.addAll(jsonResponseList);
        while (jsonResponseList.size() > 95) {
            offset = offset + 96;
            data = HttpUtils.getData(DISCOURSE_URL + "/user_badges.json?offset=" + offset + "&badge_id=" + badgeId, headers);
            jsonResponseList = getUsernameListFromJson((String) data.get("content"), "users", "username");
            usernameList.addAll(jsonResponseList);
        }
        return usernameList;
    }

    public static String getGithubIdOfUser(String username) throws IOException {
        String githubId = null;
        if (!username.equals("system")) {
            Header[] headers = {GITHUB_JSON_ACCEPT_HEADER};
            Map<String, Object> data = HttpUtils.getData(DISCOURSE_URL + "/users/" + username + ".json", headers);
            githubId = getGithubIdFromUserJson((String) data.get("content"), "user_fields", "2");
        }
        return githubId;
    }

    public static int getCommitCountByGithubIdAndRepo(String githubId, String repo) throws IOException {
        List<String> githubCommits = GithubUtils.getGithubCommits(githubId, repo);
        return githubCommits.size();
    }

    public static List<String> getUsernameListFromJson(String responseBody, String parentJsonKey, String childJsonKey) {
        List<String> list = new ArrayList<>();
        if (null != responseBody) {
            JSONObject jsonObj = new JSONObject(responseBody);
            if (!jsonObj.isNull(parentJsonKey)) {
                JSONArray usersArray = (JSONArray) jsonObj.get(parentJsonKey);
                for (int i = 0; i < usersArray.length(); i++) {
                    JSONObject repoJson = (JSONObject) usersArray.get(i);
                    if (!repoJson.isNull(childJsonKey)) {
                        String repoUrl = (String) repoJson.get(childJsonKey);
                        list.add(repoUrl);
                    }
                }
            }
        }
        return list;
    }

    public static String getGithubIdFromUserJson(String responseBody, String parentJsonKey, String childJsonKey) {
        String githubId = "";
        if (null != responseBody) {
            JSONObject jsonObj = new JSONObject(responseBody);
            if (!jsonObj.isNull("user")) {
                JSONObject userObj = (JSONObject) jsonObj.get("user");
                if (!userObj.isNull(parentJsonKey)) {
                    JSONObject userFieldsObj = (JSONObject) userObj.get(parentJsonKey);
                    if (!userFieldsObj.isNull(childJsonKey)) {
                        githubId = (String) userFieldsObj.get(childJsonKey);
                    }
                }
            }
        }
        return githubId;
    }
}
