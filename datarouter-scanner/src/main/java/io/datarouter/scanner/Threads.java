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

/**
 * Encapsulates an ExecutorService and thread allocation for limiting the number of threads used by an operation.
 */
public record Threads(
		ExecutorService exec,
		int count,
		boolean useCallerForSingleThread){

	public Threads{
		if(count < 1){
			String message = String.format("count=%s must be greater than 0", count);
			throw new IllegalArgumentException(message);
		}
	}

	public Threads(ExecutorService exec, int count){
		this(exec, count, true);
	}

	public static Threads none(){
		return new Threads(null, 1, true);
	}

	public static Threads useExecForSingleThread(ExecutorService exec, int count){
		return new Threads(exec, count, false);
	}

	boolean useExec(){
		if(exec == null){
			return false;
		}
		if(count > 1){
			return true;
		}
		return !useCallerForSingleThread;
	}

}
