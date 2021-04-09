/**
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
package io.datarouter.util.runonce;

public abstract class CheckedRunOnceRunnable<E extends Exception> implements CheckedRunnable<E>{

	private volatile boolean hasRun = false;

	protected CheckedRunOnceRunnable(){
	}

	protected abstract void load() throws E;

	@Override
	public void run() throws E{
		if(!hasRun){
			synchronized (this){
				load();
				hasRun = true;
			}
		}
	}

	/*----------------------- CheckedRunOnceRunnableFunctional -------------------------*/

	public static <E extends Exception> CheckedRunOnceRunnable<E> ofChecked(CheckedRunnable<E> runnable){
		return new CheckedRunOnceRunnableFunctional<>(runnable);
	}

	private static class CheckedRunOnceRunnableFunctional<E extends Exception> extends CheckedRunOnceRunnable<E>{

		private final CheckedRunnable<E> runnable;

		public CheckedRunOnceRunnableFunctional(CheckedRunnable<E> runnable){
			this.runnable = runnable;
		}

		@Override
		protected void load() throws E{
			runnable.run();
		}

	}

}
