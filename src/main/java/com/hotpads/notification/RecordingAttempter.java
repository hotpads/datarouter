package com.hotpads.notification;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.exception.ExceptionRecordKey;

public class RecordingAttempter {

	private static final long RECORD_TIMEOUT_MS = 200;

	private SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> exceptionRecordNode;
	private ExecutorService executor;

	public RecordingAttempter(SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> exceptionRecordNode) {
		this.exceptionRecordNode = exceptionRecordNode;
		this.executor = Executors.newSingleThreadExecutor();
	}
	
	public boolean rec(ExceptionRecord exceptionRecord) {
		Future<Boolean> future = executor.submit(new RecordingAttempt(exceptionRecord));
		try {
			return future.get(RECORD_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private class RecordingAttempt implements Callable<Boolean> {
		
		private ExceptionRecord exceptionRecord;

		public RecordingAttempt(ExceptionRecord exceptionRecord) {
			this.exceptionRecord = exceptionRecord;
		}

		@Override
		public Boolean call() throws Exception {
			try {
				exceptionRecordNode.put(exceptionRecord, null);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
}
