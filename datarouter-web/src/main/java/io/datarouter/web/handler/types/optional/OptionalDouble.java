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
package io.datarouter.web.handler.types.optional;

import io.datarouter.util.number.NumberTool;

/**
 * @deprecated use the JDK {@code Optional<Double>}
 */
@Deprecated
public class OptionalDouble extends OptionalParameter<Double>{

	public OptionalDouble(){
	}

	public OptionalDouble(Double optDouble){
		super(optDouble);
	}

	@Override
	public Class<Double> getInternalType(){
		return Double.class;
	}

	@Override
	public OptionalParameter<Double> fromString(String stringValue){
		return new OptionalDouble(NumberTool.getDoubleNullSafe(stringValue, null));
	}

}