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

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;

public final class GroceryOrderItem extends BinaryDto<GroceryOrderItem>{
	@BinaryDtoField(index = 0)
	public final Long id;
	@BinaryDtoField(index = 1)
	public final String productCode;
	@BinaryDtoField(index = 2)
	public final Integer quantity;

	public GroceryOrderItem(Long id, String productCode, Integer quantity){
		this.id = id;
		this.productCode = productCode;
		this.quantity = quantity;
	}

	public static GroceryOrderItem decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(GroceryOrderItem.class).decode(bytes);
	}
}
