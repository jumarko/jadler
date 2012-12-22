package net.jadler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class JadlerRuleTest {

    private  static final int PORT = 31456;

    @Rule
    public JadlerRule jadlerRule = new JadlerRule();

    @Rule
    public JadlerRule jadlerRuleForExplicitPort = new JadlerRule(PORT);

    @Before
    public void testMockerReadyInSetUpMethod() {
        checkMockerListeningOnPort();
        checkOngoingConfiguration();
    }

    @Test
    public void mockerReadyInTestMethod() {
        checkMockerListeningOnPort();
        checkOngoingConfiguration();
    }



    private void checkOngoingConfiguration() {
        assertNotNull("Jadler should be initialized with some random port", jadlerRule.getOngoingConfiguration());
        assertNotNull("Jadler should be initialized with some random port",
                jadlerRuleForExplicitPort.getOngoingConfiguration());
    }


    private void checkMockerListeningOnPort() {
        assertTrue("Jadler should be initialized with some random port", jadlerRule.getMockerPort() > 0);
        assertTrue("Jadler should be initialized with some random port",
                jadlerRuleForExplicitPort.getMockerPort() == PORT);
        checkJadlerListeningOnPort(jadlerRule);
        checkJadlerListeningOnPort(jadlerRuleForExplicitPort);
    }

    private void checkJadlerListeningOnPort(JadlerRule jadler) {
        try {
            new ServerSocket(jadler.getMockerPort());
            fail("HttpMocker is not listening on port=" + jadler.getMockerPort());
        } catch (IOException e) {
            // port is not free -> it seems that jadler is listening on proper port
        }
    }

}
