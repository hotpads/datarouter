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
package io.datarouter.client.hbase.util;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;

public class HBaseEntityKeyTool{

	public static <EK extends EntityKey<EK>> EK getEkFromRowBytes(
			byte[] rowBytes,
			Supplier<EK> entityKeySupplier,
			int numPrefixBytes,
			List<Field<?>> ekFields){
		EK ek = entityKeySupplier.get();
		int byteOffset = numPrefixBytes;
		for(Field<?> field : ekFields){
			if(byteOffset == rowBytes.length) {// ran out of bytes. leave remaining fields blank
				break;
			}
			Object value = field.fromKeyBytesWithSeparatorButDoNotSet(rowBytes, byteOffset);
			field.setUsingReflection(ek, value);
			byteOffset += field.numKeyBytesWithSeparator(rowBytes, byteOffset);
		}
		return ek;
	}

}
