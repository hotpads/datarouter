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

import java.util.concurrent.ExecutorService;

public class ParallelScannerContext{

	public final ExecutorService executor;
	public final int numThreads;
	public final boolean allowUnorderedResults;
	public final boolean enabled;

	public ParallelScannerContext(ExecutorService executor, int numThreads, boolean allowUnorderedResults,
			boolean enabled){
		this.executor = executor;
		this.numThreads = numThreads;
		this.allowUnorderedResults = allowUnorderedResults;
		this.enabled = enabled;
	}

	public ParallelScannerContext(ExecutorService executor, int numThreads, boolean allowUnorderedResults){
		this(executor, numThreads, allowUnorderedResults, true);
	}

}
