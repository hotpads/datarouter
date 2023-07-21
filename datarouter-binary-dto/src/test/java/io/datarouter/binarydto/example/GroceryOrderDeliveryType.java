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
package io.datarouter.binarydto.example;

import java.util.Map;

import io.datarouter.binarydto.fieldcodec.BinaryDtoConvertingFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.IntBinaryDtoFieldCodec;
import io.datarouter.scanner.Scanner;

public enum GroceryOrderDeliveryType{
	DELIVERY(0),
	PICKUP(1);

	private int intValue;

	private GroceryOrderDeliveryType(int intValue){
		this.intValue = intValue;
	}

	public static final Map<Integer,GroceryOrderDeliveryType> BY_INT = Scanner.of(values())
			.toMap(value -> value.intValue);

	public static class GroceryOrderDeliveryTypeBinaryDtoIntCodec
	extends BinaryDtoConvertingFieldCodec<GroceryOrderDeliveryType,Integer>{
		public GroceryOrderDeliveryTypeBinaryDtoIntCodec(){
			super(value -> value.intValue, BY_INT::get, new IntBinaryDtoFieldCodec(), true);
		}
	}
}
