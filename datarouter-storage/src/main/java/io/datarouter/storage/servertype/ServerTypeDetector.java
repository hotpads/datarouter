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
package io.datarouter.storage.servertype;

public interface ServerTypeDetector{

	boolean mightBeProduction();
	boolean mightBeDevelopment();

	default void assertNotProductionServer(){
		if(mightBeProduction()){
			throw new IllegalStateException("isProduction=true");
		}
	}

	static class NoOpServerTypeDetector implements ServerTypeDetector{

		@Override
		public boolean mightBeProduction(){
			return false;
		}

		@Override
		public boolean mightBeDevelopment(){
			return false;
		}

	}

	static class OnlyDevelopmentServerTypeDetector implements ServerTypeDetector{

		@Override
		public boolean mightBeProduction(){
			return false;
		}

		@Override
		public boolean mightBeDevelopment(){
			return true;
		}

	}

}
