/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */

package net.jadler.portallocator;


import org.apache.commons.lang.math.RandomUtils;

import java.io.IOException;
import java.net.ServerSocket;

public class PortAllocator {
    private static final int RANDOM_PORTS_MAX_RANGE = 32000;
    private final int basePort;
    private final int maxTries;

    public static final int DEFAULT_BASE_PORT = 9999;
    public static final int DEFAULT_MAX_TRIES = 10;

    /**
     * Creates new default port allocator which uses random base port and starts to try allocating ports.
     */
    public PortAllocator() {
        this(generateRandomPort(), DEFAULT_MAX_TRIES);
    }

    private static int generateRandomPort() {
        return DEFAULT_BASE_PORT + RandomUtils.nextInt(RANDOM_PORTS_MAX_RANGE);
    }

    public PortAllocator(int basePort) {
        this(basePort, DEFAULT_MAX_TRIES);
    }

    public PortAllocator(int basePort, int maxPortNumberAllocationTries) {
        this.basePort = basePort;
        this.maxTries = maxPortNumberAllocationTries;
    }

    public int allocatePort() throws PortUnavailableException {
        ServerSocket server;
        int portNumberTries = 0;
        while (portNumberTries < maxTries) {
            try {
                server = new ServerSocket(basePort + portNumberTries);
                int localPort = server.getLocalPort();
                server.close();
                // port has been successfuly allocated
                return localPort;
            } catch (IOException e) {
                // probably port is already allocated - ignore and try another port
                portNumberTries++;
            }
        }

        throw new PortUnavailableException("No port starting from basePort=" + basePort + " is not available!");
    }
}