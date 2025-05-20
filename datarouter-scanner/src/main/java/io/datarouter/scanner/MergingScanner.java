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
package io.datarouter.scanner;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.atomic.AtomicLong;

public class MergingScanner<T> extends BaseScanner<T>{

	private final Threads threads;
	private final Scanner<ScannerWithId<T>> inputScannersWithIds;
	private final CompletionService<CompletionServiceResult<T>> completionService;
	private boolean opened;
	private int numActive;
	private boolean closed;

	public MergingScanner(Threads threads, Scanner<Scanner<T>> inputScanners){
		this.threads = threads;
		var scannerIdTracker = new AtomicLong();
		this.inputScannersWithIds = inputScanners
				.map(scanner -> new ScannerWithId<>(scanner, scannerIdTracker.getAndIncrement()));
		this.completionService = new ExecutorCompletionService<>(threads.exec());
		this.opened = false;
		this.numActive = 0;
		this.closed = false;
	}

	private record ScannerWithId<T>(
			Scanner<T> scanner,
			long id){
	}

	private record CompletionServiceResult<T>(
			ScannerWithId<T> scannerWithId,
			ScannerNextItem<T> optItem){
	}

	@Override
	public boolean advance(){
		if(closed){
			return false;
		}
		if(!opened){
			submitInitialTasks();
			opened = true;
		}
		Optional<ScannerNextItem<T>> nextResult = nextPresentResult();
		current = nextResult.map(ScannerNextItem::value).orElse(null);
		if(nextResult.isPresent()){
			return true;
		}
		closed = true;
		return false;
	}

	// Avoid accidentally closing the scanner of inputs
	private void submitInitialTasks(){
		inputScannersWithIds
				.take(threads.count())
				.forEach(this::submit);
	}

	private void submit(ScannerWithId<T> scannerWithId){
		Callable<CompletionServiceResult<T>> callable = () -> new CompletionServiceResult<>(
				scannerWithId,
				scannerWithId.scanner().next());
		completionService.submit(callable);
		++numActive;
	}

	// Empty if all input scanners were exhausted.
	private Optional<ScannerNextItem<T>> nextPresentResult(){
		return Scanner.generate(this::nextCompletionServiceResult)
				.advanceWhile(_ -> numActive > 0)
				.include(ScannerNextItem::isPresent)
				.findFirst();
	}

	// Empty if one of the input scanners was exhausted.
	private ScannerNextItem<T> nextCompletionServiceResult(){
		CompletionServiceResult<T> result = takeNextAvailable();
		if(result.optItem().isPresent()){
			submit(result.scannerWithId());// Resubmit the scanner to start finding the next item.
		}else{
			inputScannersWithIds.next().ifPresent(this::submit);
		}
		return result.optItem();
	}

	private CompletionServiceResult<T> takeNextAvailable(){
		try{
			--numActive;
			return completionService.take().get();
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}catch(ExecutionException e){
			throw new RuntimeException(e);
		}
	}

}
