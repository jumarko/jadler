package net.jadler;

import net.jadler.portallocator.PortAllocator;
import org.junit.Assert;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.jadler.Jadler.*;

/**
 * JUnit rule which simplyfies setup of Jadler.
 * If this rule is declared in JUnit test then {@link net.jadler.httpmocker.HttpMocker} is automatically
 * start in Before phase and stop in After phase.
 * Port on which the mocker is listening can be random or explicitly configured.
 */
public class JadlerRule extends ExternalResource {

    private static final Logger logger = LoggerFactory.getLogger(JadlerRule.class);

    private final PortAllocator portAllocator = new PortAllocator();

    private final int mockerPort;
    private Jadler.OngoingConfiguration ongoingConfiguration;


    /**
     * Create rule that starts HttpMocker on random port.
     * This is the most common (and useful) case.
     */
    public JadlerRule() {
        this(-1);
    }

    /**
     * Create rule that starts HttpMocker on passed {@code mockerPort}.
     * Please, consider using {@link #JadlerRule()} for starting mocker on random port instead!
     * @param mockerPort
     */
    public JadlerRule(int mockerPort) {
        if (mockerPort <= 0) {
            this.mockerPort = portAllocator.allocatePort();
        } else {
            this.mockerPort = mockerPort;
        }
    }


    //---------------------------- PUBLIC METHODS ----------------------------------------------------------------------

    /**
     * @return port on which the jadler mocker server is listening.
     */
    public int getMockerPort() {
        return mockerPort;
    }


    /**
     * Client can use this method to retrieve ongoing configuration of mocker server and for test specific configuration.
     * @return ongoing configuration of Jadler initialized in {@link #before()} method.
     */
    public OngoingConfiguration getOngoingConfiguration() {
        return ongoingConfiguration;
    }



    //---------------------------- LIFECYCLE JUNIT RULE  METHODS -------------------------------------------------------

    /**
     * Starts jadler mocking server on random port.
     * @throws Throwable
     */
    @Override
    protected void before() throws Throwable {
        startMocker();
    }

    /**
     * Stops running jadler mocking server.
     */
    @Override
    protected void after() {
        stopMocker();
    }


    //-------------------------------------- PRIVATE METHODS -----------------------------------------------------------
    private void startMocker() {
        try {
            ongoingConfiguration = initJadlerThat().usesStandardServerListeningOn(mockerPort);
            startStubServer();
            logger.debug("HTTP mocker has been started on port=", getMockerPort());
        } catch (Exception e) {
            logger.error("Cannot start mocker on port=" + getMockerPort(), e);
            Assert.fail("Cannot start mocker on port=" + getMockerPort());
        }
    }


    private void stopMocker() {
        try {
            stopStubServer();
            logger.debug("HTTP mocker on port={} has been stopped.", getMockerPort());
        } catch (Exception e) {
            logger.warn("Cannot stop HTTP mocker on port=" + getMockerPort(), e);
        }
    }


}
