package com.aerospike.examples;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.async.AsyncClient;
import com.aerospike.client.listener.RecordListener;
import com.aerospike.client.listener.WriteListener;
import com.aerospike.client.policy.Policy;

public class AsyncPutGet extends AsyncExample {
	
	public AsyncPutGet(Console console) {
		super(console);
	}

	/**
	 * Asynchronously write and read a bin using alternate methods.
	 */
	@Override
	public void runExample(AsyncClient client, Parameters params) throws Exception {
		Key key = new Key(params.namespace, params.set, "putgetkey");
		Bin bin = new Bin(params.getBinName("putgetbin"), "value");

		runPutGet1(client, params, key, bin);
		waitTillComplete();
		runPutGet2(client, params, key, bin);
		waitTillComplete();
	}
	
	// Inline asynchronous put/get calls.
	private void runPutGet1(final AsyncClient client, final Parameters params, final Key key, final Bin bin) throws AerospikeException {
		
		console.info("Put: namespace=%s set=%s key=%s value=%s", key.namespace, key.setName, key.userKey, bin.value);
		params.writePolicy.timeout = 50;
		
		client.put(params.writePolicy, new WriteListener() {
			public void onSuccess(final Key key) {
				try {
					// Write succeeded.  Now call read.
					console.info("Get: namespace=%s set=%s key=%s", key.namespace, key.setName, key.userKey);

					client.get(params.policy, new RecordListener() {
						public void onSuccess(final Key key, final Record record) {
							validateBin(key, bin, record);
							notifyCompleted();
						}
						
						public void onFailure(AerospikeException e) {
							console.error("Failed to get: namespace=%s set=%s key=%s exception=%s", key.namespace, key.setName, key.userKey, e.getMessage());
							notifyCompleted();
						}
					}, key);
				}
				catch (Exception e) {				
					console.error("Failed to get: namespace=%s set=%s key=%s exception=%s", key.namespace, key.setName, key.userKey, e.getMessage());
				}
			}
			
			public void onFailure(AerospikeException e) {
				console.error("Failed to put: namespace=%s set=%s key=%s exception=%s", key.namespace, key.setName, key.userKey, e.getMessage());
				notifyCompleted();
			}
		}, key, bin);		
	}	

	// Separate combined class asynchronous put/get calls.
	private void runPutGet2(AsyncClient client, Parameters params, Key key, Bin bin) throws Exception {
		console.info("Put: namespace=%s set=%s key=%s value=%s", key.namespace, key.setName, key.userKey, bin.value);
		client.put(params.writePolicy, new CombinedListener(client, params.policy, key, bin), key, bin);
	}
	
	private class CombinedListener implements WriteListener, RecordListener {
		private final AsyncClient client;
		private final Policy policy;
		private final Key key;
		private final Bin bin;
		
		public CombinedListener(AsyncClient client, Policy policy, Key key, Bin bin) {
			this.client = client;
			this.policy = policy;
			this.key = key;
			this.bin = bin;
		}
		
		// Write success callback.
		public void onSuccess(Key key) {
			try {
				// Write succeeded.  Now call read.
				console.info("Get: namespace=%s set=%s key=%s", key.namespace, key.setName, key.userKey);
				client.get(policy, this, key);
			}
			catch (Exception e) {				
				console.error("Failed to get: namespace=%s set=%s key=%s exception=%s", key.namespace, key.setName, key.userKey, e.getMessage());
			}
		}
		
		// Read success callback.
		public void onSuccess(Key key, Record record) {
			// Verify received bin value is what was written.
			validateBin(key, bin, record);
			notifyCompleted();
		}

		// Error callback.
		public void onFailure(AerospikeException e) {
			console.error("Command failed: namespace=%s set=%s key=%s exception=%s", key.namespace, key.setName, key.userKey, e.getMessage());
			notifyCompleted();
		}
	}

	private void validateBin(Key key, Bin bin, Record record) {
		Object received = (record == null)? null : record.getValue(bin.name);
		String expected = bin.value.toString();
		
		if (received != null && received.equals(expected)) {
			console.info("Bin matched: namespace=%s set=%s key=%s bin=%s value=%s", 
				key.namespace, key.setName, key.userKey, bin.name, received);
		}
		else {
			console.error("Put/Get mismatch: Expected %s. Received %s.", expected, received);
		}
	}
	
	private synchronized void waitTillComplete() {
		try {
			super.wait();
		}
		catch (InterruptedException ie) {
		}
	}

	private synchronized void notifyCompleted() {
		super.notify();
	}
}
