/*
 * Aerospike Client - Java Library
 *
 * Copyright 2012 by Aerospike, Inc. All rights reserved.
 *
 * Availability of this source code to partners and customers includes
 * redistribution rights covered by individual contract. Please check your
 * contract for exact rights and responsibilities.
 */
package com.aerospike.client.policy;

/**
 * Container object for client policy Command.
 */
public class ClientPolicy {
	/**
	 * Initial host connection timeout in milliseconds.  The timeout when opening a connection 
	 * to the server host for the first time.
	 */
	public int timeout = 1000;

	/**
	 * Estimate of incoming threads concurrently using synchronous methods in the client instance.
	 * This field is used to size the synchronous connection pool for each server node.
	 */
	public int maxThreads = 300;
	
	/**
	 * Maximum socket idle in seconds.  Socket connection pools will discard sockets
	 * that have been idle longer than the maximum.
	 */
	public int maxSocketIdle = 14;

	/**
	 * Throw exception if host connection fails during addHost().
	 */
	public boolean failIfNotConnected;	
}
