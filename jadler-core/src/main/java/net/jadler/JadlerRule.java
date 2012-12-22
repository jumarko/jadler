package net.jadler;

import net.jadler.httpmocker.HttpMocker;
import net.jadler.portallocator.PortAllocator;
import org.apache.commons.lang.Validate;
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
    private HttpMocker httpMocker;


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


    //---------------------------- PUBLIC STUFF ------------------------------------------------------------------------
    public static interface JadlerConfiguration {
        /**
         * Client should perform all specific configuration of {@link HttpMocker} in this method.
         * @param ongoingConfiguration
         */
        void configure(OngoingConfiguration ongoingConfiguration);

        /**
         * Client should perform all specific configuration of {@link HttpMocker} in this method.
         * @param mocker
         */
        void configureMocker(HttpMocker mocker);
    }

    /**
     * Default empty implementation of {@link JadlerConfiguration}.
     * Client can extend this class instead of direct implementation of {@link JadlerConfiguration}
     * when it does not want to implement all methods. E.g. in many cases it is sufficient to implement
     * only {@link JadlerConfiguration#configureMocker(net.jadler.httpmocker.HttpMocker)} method.
     */
    public static class JadlerConfigurationAdapter implements JadlerConfiguration {
        @Override
        public void configure(OngoingConfiguration ongoingConfiguration) { }

        @Override
        public void configureMocker(HttpMocker mocker) { }
    }

    /**
     * @return port on which the jadler mocker server is listening.
     */
    public int getMockerPort() {
        return mockerPort;
    }


    //---------------------------- LIFECYCLE JUNIT RULE  METHODS -------------------------------------------------------

    /**
     * Starts jadler mocking server on random port.
     * @throws Throwable
     */
    @Override
    protected void before() throws Throwable {
        ongoingConfiguration = createConfiguration();
    }

    public void startMocker(JadlerConfiguration configurationCallback) {
        if (configurationCallback != null) {
            // perform client specific configuration
            configurationCallback.configure(ongoingConfiguration);
        }

        final HttpMocker mocker = ongoingConfiguration.build();

        if (configurationCallback != null) {
            // perform client specific configuration
            configurationCallback.configureMocker(mocker);
        }

        try {
            mocker.start();
            httpMocker = mocker;
            logger.debug("HTTP mocker has been started on port=", getMockerPort());
        } catch (Exception e) {
            logger.error("Cannot start mocker on port=" + getMockerPort(), e);
            Assert.fail("Cannot start mocker on port=" + getMockerPort());
        }
    }

    /**
     * Stops running jadler mocking server.
     */
    @Override
    protected void after() {
        stopMocker();
    }


    //-------------------------------------- PRIVATE METHODS -----------------------------------------------------------

    private OngoingConfiguration createConfiguration() {
        return new OngoingConfiguration().usesStandardServerListeningOn(mockerPort);
    }

    private void stopMocker() {
        try {
            if (httpMocker != null && httpMocker.isStarted()) {
                httpMocker.stop();
                logger.debug("HTTP mocker on port={} has been stopped.", getMockerPort());
            }
        } catch (Exception e) {
            logger.warn("Cannot stop HTTP mocker on port=" + getMockerPort(), e);
        }
    }


}
