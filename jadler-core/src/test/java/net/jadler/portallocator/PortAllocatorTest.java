package net.jadler.portallocator;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.Assert.assertTrue;

public class PortAllocatorTest {

    private static final int BASE_PORT = 1025;
    private final PortAllocator portAllocator = new PortAllocator(BASE_PORT, 500);


    @Test
    public void allocateOneNewPort() {
        allocateNewPort(portAllocator);
    }


    @Test
    public void allocateManyNewPorts() {
        for (int i = 0; i < 100; i++) {
            allocateNewPort(portAllocator);
        }
    }


    @Test(expected = PortUnavailableException.class)
    public void allocateTooManyNewPorts() {
        for (int i = 0; i < 11; i++) {
            allocateNewPort(new PortAllocator(BASE_PORT, 10));
        }
    }


    @Test
    public void allocateOneNewPortStartingFromRandomBasePort() {
        allocateNewPort(new PortAllocator());
    }


    @Test
    public void allocateManyNewPortsStartingFromRandomBasePort() {
        for (int i = 0; i < 100; i++) {
            allocateNewPort(new PortAllocator());
        }
    }


    private int allocateNewPort(PortAllocator portAllocator) {
        int allocatedPort = portAllocator.allocatePort();
        assertTrue("New allocated port must not be lower than base port=" + BASE_PORT, allocatedPort >= BASE_PORT);

        // engage new port
        try {
            new ServerSocket(allocatedPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot allocate port", e);
        }

        return allocatedPort;
    }


}
