package net.jadler;

import net.jadler.httpmocker.HttpMocker;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;


public class JadlerRuleTest {

    private static final int PORT = 31456;
    private static final String COMMON_RESOURCE_RESPONSE = "Common resource response";
    private static final String COMMON_RESOURCE_URI = "/api/common";
    private static final String PROJECTS_RESOURCE_URI = "/api/v1/projects";
    private static final String PROJECTS_RESOURCE_RESPONSE = "My Projects";


    @Rule
    public JadlerRule jadlerRuleForExplicitPort = new JadlerRule.Builder().withMockerPort(PORT).createJadlerRule();

    @Rule
    public JadlerRule jadlerRule = new JadlerRule.Builder().withCommonConfiguration(new JadlerRule.JadlerConfigurationAdapter() {
        @Override
        public void configure(Jadler.OngoingConfiguration ongoingConfiguration) {
            ongoingConfiguration.respondsWithDefaultStatus(HttpStatus.SC_CREATED);
        }
    })
            .createJadlerRule();


    @Rule
    public JadlerRule commonJadlerRule = new JadlerRule.Builder().withCommonConfiguration(new JadlerRule.JadlerConfigurationAdapter() {
        @Override
        public void configureMocker(HttpMocker mocker) {
            mocker.onRequest().havingURIEqualTo(COMMON_RESOURCE_URI)
                    .respond()
                    .withBody(COMMON_RESOURCE_RESPONSE);
        }
    }).createJadlerRule();


    @Test
    public void bothMockersStartedInTestMethod() {
        checkMockerListeningOnPort(jadlerRule);
        checkMockerListeningOnPort(jadlerRuleForExplicitPort);
    }

    @Test
    public void randomPortMockerStartedInTestMethod() {
        checkMockerListeningOnPort(jadlerRule);
    }


    @Test
    public void explicitPortMockerStartedInTestMethod() {
        checkMockerListeningOnPort(jadlerRuleForExplicitPort);
    }


    /**
     * Tests if common configuration is applied before arbitrary test.
     */
    @Test
    public void customConfigurationApplied() throws IOException {
        checkMockerListeningOnPort(commonJadlerRule);
        checkResponse(String.format("http://localhost:%s%s", commonJadlerRule.getMockerPort(), COMMON_RESOURCE_URI),
                HttpStatus.SC_OK, COMMON_RESOURCE_RESPONSE);
    }


    /**
     * Tests if configuration special for some test is applied properly.
     */
    @Test
    public void customMockerConfigurationApplied() throws IOException {
        jadlerRule.restartMocker(new JadlerRule.JadlerConfigurationAdapter() {
            @Override
            public void configureMocker(HttpMocker mocker) {
                mocker.onRequest()
                        .havingURIEqualTo(PROJECTS_RESOURCE_URI)
                        .respond()
                        .withBody(PROJECTS_RESOURCE_RESPONSE);
            }
        });

        checkMockerListeningOnPort(jadlerRule);

        checkResponse(String.format("http://localhost:%s%s", jadlerRule.getMockerPort(), PROJECTS_RESOURCE_URI),
                HttpStatus.SC_CREATED, PROJECTS_RESOURCE_RESPONSE);
    }

    private void checkResponse(String uri, int expectedStatus, String expectedBody) throws IOException {
        final GetMethod httpMethod = new GetMethod(uri);
        int status = new HttpClient().executeMethod(httpMethod);
        assertThat(status, is(expectedStatus));
        assertThat(httpMethod.getResponseBodyAsString(), is(expectedBody));
    }


    private void checkMockerListeningOnPort(JadlerRule jadler) {
        try {
            new ServerSocket(jadler.getMockerPort());
            fail("HttpMocker is not listening on port=" + jadler.getMockerPort());
        } catch (IOException e) {
            // port is not free -> it seems that jadler is listening on proper port
        }
    }

}
