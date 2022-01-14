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
package io.datarouter.bytes;

import java.util.function.Function;

public class FunctionalCodec<A,B>
implements Codec<A,B>{

	private final Function<A,B> encodeFunction;
	private final Function<B,A> decodeFunction;

	public FunctionalCodec(Function<A,B> encodeFunction, Function<B,A> decodeFunction){
		this.encodeFunction = encodeFunction;
		this.decodeFunction = decodeFunction;
	}

	@Override
	public B encode(A value){
		return encodeFunction.apply(value);
	}

	@Override
	public A decode(B encodedValue){
		return decodeFunction.apply(encodedValue);
	}

}
