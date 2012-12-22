package net.jadler;

import net.jadler.httpmocker.HttpMocker;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;


public class JadlerRuleTest {

    private  static final int PORT = 31456;

    /** DO NOTHING implementation for simple testing */
    private static final JadlerRule.JadlerConfiguration configurationCallback =
            new JadlerRule.JadlerConfigurationAdapter();

    @Rule
    public JadlerRule jadlerRuleForExplicitPort = new JadlerRule(PORT);

    @Rule
    public JadlerRule jadlerRule = new JadlerRule();


    @Test
    public void bothMockersStartedInTestMethod() {
        jadlerRule.startMocker(configurationCallback);
        jadlerRuleForExplicitPort.startMocker(configurationCallback);

        checkMockerListeningOnPort(jadlerRule);
        checkMockerListeningOnPort(jadlerRuleForExplicitPort);
    }

    @Test
    public void randomPortMockerStartedInTestMethod() {
        jadlerRule.startMocker(configurationCallback);
        checkMockerListeningOnPort(jadlerRule);
    }


    @Test
    public void explicitPortMockerStartedInTestMethod() {
        jadlerRuleForExplicitPort.startMocker(configurationCallback);
        checkMockerListeningOnPort(jadlerRuleForExplicitPort);
    }


    @Test
    public void customMockerConfigurationApplied() throws IOException {
        jadlerRule.startMocker(new JadlerRule.JadlerConfigurationAdapter() {

            @Override
            public void configure(Jadler.OngoingConfiguration ongoingConfiguration) {
                ongoingConfiguration.respondsWithDefaultStatus(HttpStatus.SC_CREATED);
            }

            @Override
            public void configureMocker(HttpMocker mocker) {
                mocker.onRequest()
                        .havingURIEqualTo("/api/v1/projects")
                        .respond()
                        .withBody("My Projects");
            }
        });

        checkMockerListeningOnPort(jadlerRule);

        final GetMethod httpMethod = new GetMethod(
                String.format("http://localhost:%s/api/v1/projects", jadlerRule.getMockerPort()));
        int status = new HttpClient().executeMethod(httpMethod);
        assertThat(status, is(HttpStatus.SC_CREATED));
        assertThat(httpMethod.getResponseBodyAsString(), is("My Projects"));
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
