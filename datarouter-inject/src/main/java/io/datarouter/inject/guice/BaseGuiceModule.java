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
package io.datarouter.inject.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;

public abstract class BaseGuiceModule extends AbstractModule implements GuiceOptionalBinder{

	@Override
	public Binder getGuiceBinder(){
		return binder();
	}

	public <T> void bindUnsupported(Class<T> type){
		bind(type).toProvider(() -> {
			throw new UnsupportedOperationException();
		});
	}

}
