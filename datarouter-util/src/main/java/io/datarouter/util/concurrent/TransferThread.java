/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.concurrent;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;

/**
 * Starts a thread to pull items through a blocking buffer of limited size.
 * The caller must call complete() when finished writing to the buffer, which submits a termination message.
 * After the termination is found the compelte() method returns the result.
 */
public class TransferThread<T,R>{

	private final Consumer<Long> inputStallNanosCallback;
	private final Consumer<Long> outputStallNanosCallback;
	private final BlockingDeque<TransferThreadMessage<T>> deque;
	private final ExecutorService exec;
	private final Future<R> resultFuture;

	private TransferThread(
			Consumer<Long> inputNanosCallback,
			Consumer<Long> outputNanosCallback,
			int size,
			Function<Scanner<T>,R> threadFunction){
		this.inputStallNanosCallback = inputNanosCallback;
		this.outputStallNanosCallback = outputNanosCallback;
		deque = new LinkedBlockingDeque<>(size);
		exec = Executors.newSingleThreadExecutor();
		resultFuture = exec.submit(() -> scan().apply(threadFunction));
	}

	/**
	 * Adds an item to the internal buffer.
	 * It will block until there is space available in the buffer.
	 */
	public void submit(T item){
		long beforeTimestampNs = System.nanoTime();
		BlockingDequeTool.put(deque, TransferThreadMessage.makeDataMessage(item));
		long durationNs = System.nanoTime() - beforeTimestampNs;
		// Blame the stall on the this output thread since there's no room in the buffer to submit new items.
		// This output thread is not draining the buffer fast enough.
		outputStallNanosCallback.accept(durationNs);
	}

	/**
	 * Create a scanner that reads items from the buffer until the termination message is found.
	 * It will block waiting for new items to become available.
	 */
	private Scanner<T> scan(){
		return Scanner.generate(this::nextMessage)
				.advanceUntil(TransferThreadMessage::shouldTerminate)
				.map(TransferThreadMessage::item);
	}

	private TransferThreadMessage<T> nextMessage(){
		long beforeTimestampNs = System.nanoTime();
		TransferThreadMessage<T> message = BlockingDequeTool.pollForever(deque);
		long durationNs = System.nanoTime() - beforeTimestampNs;
		// Blame the stall on the submitter thread since there are no messages for the this output thread to process.
		// The input thread is not submitting messages fast enough.
		inputStallNanosCallback.accept(durationNs);
		return message;
	}

	/**
	 * Puts a termination message in the buffer.
	 * The Future will block until the scan() method inside it finds the termination message.
	 */
	public R complete(){
		BlockingDequeTool.put(deque, TransferThreadMessage.makeTerminationMessage());
		R result = FutureTool.get(resultFuture);
		exec.shutdownNow();
		return result;
	}

	public static class TransferThreadBuilder<T,R>{

		private final int bufferSize;
		private final Function<Scanner<T>,R> threadFunction;
		private Consumer<Long> inputStallNanosCallback = $ -> {};
		private Consumer<Long> outputStallNanosCallback = $ -> {};

		public TransferThreadBuilder(int bufferSize, Function<Scanner<T>,R> threadFunction){
			this.bufferSize = bufferSize;
			this.threadFunction = threadFunction;
		}

		public TransferThreadBuilder<T,R> withInputStallNanosCallback(Consumer<Long> inputStallNanosCallback){
			this.inputStallNanosCallback = inputStallNanosCallback;
			return this;
		}

		public TransferThreadBuilder<T,R> withOutputStallNanosCallback(Consumer<Long> outputStallNanosCallback){
			this.outputStallNanosCallback = outputStallNanosCallback;
			return this;
		}

		public TransferThread<T,R> build(){
			return new TransferThread<>(inputStallNanosCallback, outputStallNanosCallback, bufferSize, threadFunction);
		}

	}

	/**
	 * Contains either data or a poison pill indicating there are no more messages
	 */
	private static record TransferThreadMessage<T>(
			T item,
			boolean shouldTerminate){

		static <T> TransferThreadMessage<T> makeDataMessage(T items){
			return new TransferThreadMessage<>(items, false);
		}

		/**
		 * There should be no entries in the termination message
		 */
		static <T> TransferThreadMessage<T> makeTerminationMessage(){
			return new TransferThreadMessage<>(null, true);
		}

	}

}
