/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.httpmocker;

import net.jadler.Jadler;
import net.jadler.stubbing.RequestStubbing;


/**
 * An interface for http mocker. this component is the hearth of the jadler library.
 * It manages the underlying mock http server (and its lifecycle) and provides the client
 * a way to define stubs (by calling {@link #onRequest()}).
 * 
 * You shouldn't create HttpMocker instances directly, please see {@link Jadler} for usage
 * examples.
 */
public interface HttpMocker {
    
    /**
     * Start new stubbing (definition of a <i>WHEN</i>-<i>THEN</i> rule).
     * @return stubbing object to continue the stubbing
     */
    RequestStubbing onRequest();
    
    
    /**
     * Starts the mock http server assigned to this mocker.
     * @throws JadlerException if an error occurred while starting the mock http server.
     * @throws IllegalStateException if the mock server has been started already.
     */
    void start();
    
    
    /**
     * Stops the underlying mock http server.
     * @throws JadlerException if an error occurred while stopping the mock http server.
     * @throws IllegalStateException if the mock server hasn't been started yet.
     */
    void stop();
    
    
    /**
     * @return true, if the mock server has already been started, otherwise false
     */
    boolean isStarted();
}
