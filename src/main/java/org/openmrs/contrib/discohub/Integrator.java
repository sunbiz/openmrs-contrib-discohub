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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 *
 * @author sunbiz
 * @author namratanehete
 */
public class Integrator {

    private static final Log log = LogFactory.getLog(Integrator.class);

    public static final Header GITHUB_JSON_ACCEPT_HEADER = new BasicHeader("Accept", "application/vnd.github.v3+json");
    public static Header GITHUB_BASIC_AUTH_HEADER = new BasicHeader("Authorization", "Basic "
            + Base64.encodeBase64URLSafeString((getUsername() + ":" + getPassword()).getBytes()));
    public static final String GITHUB_URL = "https://api.github.com";
    public static final Header ETAG_HEADER = new BasicHeader("If-Not-Modified", "SOME-TAG");
    public static final String GITHUB_REPO_USER = "openmrs";
    public static final int GITHUB_BADGE_ID = 132;

    public Integrator() throws IOException {
        List<String> usernameList = DiscourseUtils.getUsersWithBadges(GITHUB_BADGE_ID);
        DatabaseUtils.createConnection();
        DatabaseUtils.createDb();
        for (String username : usernameList) {
            String githubId = DiscourseUtils.getGithubIdOfUser(username);
            System.out.println("Username = " + username + " GithubId = " + githubId);
            List<String> githubRepos = GithubUtils.getGithubRepos(GITHUB_REPO_USER);
            for (String repo : githubRepos) {
                int commitCount = DiscourseUtils.getCommitCountByGithubIdAndRepo(githubId, repo);
                System.out.println("COMMIT count = " + commitCount);
                if (commitCount > 0) {
                    log.info("COUNT commits by " + githubId + " = " + commitCount + " on repo = " + repo);
                    System.out.println("COUNT commits by " + githubId + " = " + commitCount + " on repo = " + repo);
                    DatabaseUtils.setUserCommitCount(username, githubId, commitCount, repo);
                }
            }
        }
        DatabaseUtils.closeConnection();
    }

    public static void main(String[] args) throws IOException {
        Integrator runner = new Integrator();
    }

    public static Properties loadPropertiesFromFile() {
        File propsFile = new File("discohub.properties");
        Properties props = new Properties();
        if (!propsFile.exists()) {
            System.out.println("FILE LOC = " + propsFile.getAbsolutePath());
            System.exit(1);
        } else {
            try {
                FileInputStream fileStream = new FileInputStream(propsFile);
                props.load(fileStream);
            } catch (IOException ex) {
                log.error(ex);
            }
        }
        return props;
    }

    private static String getUsername() {
        return loadPropertiesFromFile().getProperty("github.username");
    }

    private static String getPassword() {
        return loadPropertiesFromFile().getProperty("github.password");
    }

}
