/* SPDX-License-Identifier: MIT
 * Copyright (c) 2020, Tobias Gruetzmacher
 */
package org.jenkins.plugins.lockableresources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import hudson.model.FreeStyleProject;
import hudson.model.Queue;
import java.io.IOException;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkins.plugins.lockableresources.actions.LockableResourcesRootAction;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public final class TestHelpers {

    private static final Logger LOGGER = Logger.getLogger(TestHelpers.class.getName());

    private static final int SLEEP_TIME = 100;
    private static final int MAX_WAIT = 5000;

    @Mock
    private StaplerRequest2 req;

    @Mock
    private StaplerResponse2 rsp;

    private final AutoCloseable mocks;

    // Utility class
    public TestHelpers() {
        this.mocks = MockitoAnnotations.openMocks(this);
    }

    public static void waitForQueue(Jenkins jenkins, FreeStyleProject job) throws InterruptedException {
        waitForQueue(jenkins, job, Queue.Item.class);
    }

    /** Schedule a build and make sure it has been added to Jenkins' queue. */
    public static void waitForQueue(Jenkins jenkins, FreeStyleProject job, Class<?> itemType)
            throws InterruptedException {
        LOGGER.info("Waiting for job to be queued...");
        int waitTime = 0;
        while (!itemType.isInstance(jenkins.getQueue().getItem(job)) && waitTime < MAX_WAIT) {
            Thread.sleep(SLEEP_TIME);
            waitTime += SLEEP_TIME;
            if (waitTime % 1000 == 0) {
                LOGGER.info(" " + waitTime / 1000 + "s");
            }
        }
    }

    /**
     * Get a resource from the JSON API and validate some basic properties. This allows to verify that
     * the API returns sane values while running other tests.
     */
    public static JSONObject getResourceFromApi(JenkinsRule rule, String resourceName, boolean isLocked)
            throws IOException {
        JSONObject data = getApiData(rule);
        JSONArray resources = data.getJSONArray("resources");
        assertThat(resources, is(not(nullValue())));
        JSONObject res = (JSONObject) resources.stream()
                .filter(e -> resourceName.equals(((JSONObject) e).getString("name")))
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find '" + resourceName + "' in API."));
        assertThat(res, hasEntry("locked", isLocked));
        return res;
    }

    public static JSONObject getApiData(JenkinsRule rule) throws IOException {
        return rule.getJSON("plugin/lockable-resources/api/json").getJSONObject();
    }

    /** Simulate the click on the button in the LRM page
     *  note: Currently does not click on the button. Just simulate the doAction (stapler request)
     *  on the given resource.
     *  We shall provide some better solution like selenium tests. But for now it is fine.
     */
    public void clickButton(String action, String resourceName) throws Exception {
        LOGGER.info(action + " on " + resourceName);
        LockableResourcesRootAction doAction = new LockableResourcesRootAction();
        when(req.getMethod()).thenReturn("POST");
        when(req.getParameter("resource")).thenReturn(resourceName);

        switch (action) {
            case "reserve": {
                doAction.doReserve(req, rsp);
                break;
            }
            case "unreserve": {
                doAction.doUnreserve(req, rsp);
                break;
            }
            case "unlock": {
                LOGGER.info("doUnlock");
                doAction.doUnlock(req, rsp);
                break;
            }
        }
    }
}
