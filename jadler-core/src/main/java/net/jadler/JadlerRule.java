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
    private final JadlerConfiguration commonConfiguration;


    /**
     * Builder for JadlerRule-s. If no attributes are explicitly set then create new JadlerRule
     * which allocates random port for {@link HttpMocker} and uses no client specific configuration of HttpMocker
     * for tests.
     */
    public static class Builder {
        private int mockerPort = -1;
        private JadlerRule.JadlerConfiguration commonConfiguration;

        /**
         * Specifies explicit port for HttpMocker.
         * In most cases, please, simply do not call this method and you'll get random free port.
         * @param mockerPort
         * @return
         */
        public Builder withMockerPort(int mockerPort) {
            this.mockerPort = mockerPort;
            return this;
        }

        /**
         * Specifies common configuration shared by all (or the most of) tests. Use this method if you some part
         * of your configuration for HttpMocker should be shared by all tests.
         * <p>
         *     Test specific configuration can still be performed when calling
         *     {@link #startMocker(net.jadler.JadlerRule.JadlerConfiguration)}.
         * </p>
         * @param commonConfiguration configuration shared by all tests.
         * @return
         */
        public Builder withCommonConfiguration(JadlerRule.JadlerConfiguration commonConfiguration) {
            this.commonConfiguration = commonConfiguration;
            return this;
        }

        public JadlerRule createJadlerRule() {
            return new JadlerRule(mockerPort, commonConfiguration);
        }
    }
    /**
     * Create rule that starts HttpMocker on passed {@code mockerPort} and configure it with (otpional)
     * {@code commonConfiguration}
     * Please, consider using {@link #JadlerRule()} for starting mocker on random port instead!
     * @param mockerPort
     */
    private JadlerRule(int mockerPort, JadlerConfiguration commonConfiguration) {
        if (mockerPort <= 0) {
            this.mockerPort = portAllocator.allocatePort();
        } else {
            this.mockerPort = mockerPort;
        }
        this.commonConfiguration = commonConfiguration;
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

        if (commonConfiguration != null) {
            // perform client specific COMMON configuration
            commonConfiguration.configure(ongoingConfiguration);
        }

        if (configurationCallback != null) {
            // perform client specific configuration
            configurationCallback.configure(ongoingConfiguration);
        }

        final HttpMocker mocker = ongoingConfiguration.build();

        if (commonConfiguration != null) {
            // perform client specific COMMON configuration
            commonConfiguration.configureMocker(httpMocker);
        }

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
