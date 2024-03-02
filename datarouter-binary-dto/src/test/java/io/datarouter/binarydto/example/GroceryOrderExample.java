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

import org.testng.Assert;

import io.datarouter.bytes.HexBlockTool;

public class GroceryOrderExample{

	public static void main(String... args){
		List<GroceryOrderItem> items = List.of(
				new GroceryOrderItem(1L, "egg-dozen-3", 1),
				new GroceryOrderItem(2L, "pringles-sco", 6),
				new GroceryOrderItem(3L, "banana-2", 3));
		var order = new GroceryOrder(
				55L,
				"Arthur",
				GroceryOrderDeliveryType.DELIVERY,
				items);

		// Encode one item
		byte[] item0Bytes = items.getFirst().encodeIndexed();
		HexBlockTool.print(item0Bytes);
		@SuppressWarnings("unused")
		GroceryOrderItem item0Decoded = GroceryOrderItem.decode(item0Bytes);

		// Encode the order
		byte[] orderBytes = order.encodeIndexed();
		HexBlockTool.print(orderBytes);
		@SuppressWarnings("unused")
		GroceryOrder orderDecoded = GroceryOrder.decode(orderBytes);

		// validate decoding
		String orderHex = """
				000880000000000000370106417274687572020844454c4956455259035c03011d00088000000000
				000001010b6567672d646f7a656e2d33020480000001011e00088000000000000002010c7072696e
				676c65732d73636f020480000006011a00088000000000000003010862616e616e612d3202048000
				0003""";
		GroceryOrder orderFromHex = GroceryOrder.decode(HexBlockTool.fromHexBlock(orderHex));
		Assert.assertEquals(orderFromHex, order);
	}

}
