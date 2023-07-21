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

import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.binarydto.example.GroceryOrderDeliveryType.GroceryOrderDeliveryTypeBinaryDtoIntCodec;

public final class GroceryOrder extends BinaryDto<GroceryOrder>{
	@BinaryDtoField(index = 0)
	public final Long id;
	@BinaryDtoField(index = 1)
	public final String customerName;
	@BinaryDtoField(index = 2, codec = GroceryOrderDeliveryTypeBinaryDtoIntCodec.class)
	public final GroceryOrderDeliveryType deliveryType;
	@BinaryDtoField(index = 3)
	public final List<GroceryOrderItem> items;

	public GroceryOrder(
			Long id,
			String customerName,
			GroceryOrderDeliveryType deliveryType,
			List<GroceryOrderItem> items){
		this.id = id;
		this.customerName = customerName;
		this.deliveryType = deliveryType;
		this.items = items;
	}

	public static GroceryOrder decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(GroceryOrder.class).decode(bytes);
	}
}
