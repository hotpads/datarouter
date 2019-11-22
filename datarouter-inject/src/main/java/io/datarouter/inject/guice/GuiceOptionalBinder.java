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

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.OptionalBinder;

public interface GuiceOptionalBinder{

	Binder getGuiceBinder();

	default <T> OptionalBinder<T> optionalBinder(Class<T> type){
		return OptionalBinder.newOptionalBinder(getGuiceBinder(), type);
	}

	default <T> OptionalBinder<T> optionalBinder(TypeLiteral<T> type){
		return OptionalBinder.newOptionalBinder(getGuiceBinder(), type);
	}

	default <T> OptionalBinder<T> optionalBinder(Key<T> type){
		return OptionalBinder.newOptionalBinder(getGuiceBinder(), type);
	}

	/*---------- default ------------*/

	default <T> void bindDefault(Class<T> type, Class<? extends T> defaultClass){
		optionalBinder(type).setDefault().to(defaultClass);
	}

	default <T> void bindDefaultInstance(Class<T> type, T defaultInstance){
		optionalBinder(type).setDefault().toInstance(defaultInstance);
	}

	default <T> void bindDefaultInstance(TypeLiteral<T> type, T defaultInstance){
		optionalBinder(type).setDefault().toInstance(defaultInstance);
	}

	default <T> void bindDefaultInstance(Key<T> type, T defaultInstance){
		optionalBinder(type).setDefault().toInstance(defaultInstance);
	}

	/*----------- actual ------------*/

	default <T> void bindActual(Class<T> type, Class<? extends T> actualClass){
		optionalBinder(type).setBinding().to(actualClass);
	}

	default <T> void bindActualInstance(Class<T> type, T actualInstance){
		optionalBinder(type).setBinding().toInstance(actualInstance);
	}

	default <T> void bindActualInstance(TypeLiteral<T> type, T actualInstance){
		optionalBinder(type).setBinding().toInstance(actualInstance);
	}

	default <T> void bindActualInstance(Key<T> type, T actualInstance){
		optionalBinder(type).setBinding().toInstance(actualInstance);
	}

}
