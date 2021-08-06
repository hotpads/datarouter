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
package io.datarouter.util.runonce;

public abstract class RunOnceRunnable
extends CheckedRunOnceRunnable<RuntimeException>
implements Runnable{

	public static RunOnceRunnable of(Runnable runnable){
		return new FunctionalRunOnceRunnable(runnable);
	}

	private static class FunctionalRunOnceRunnable extends RunOnceRunnable{

		private final Runnable runnable;

		public FunctionalRunOnceRunnable(Runnable runnable){
			this.runnable = runnable;
		}

		@Override
		protected void load(){
			runnable.run();
		}

	}

}
