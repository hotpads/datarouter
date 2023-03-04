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
package io.datarouter.client.memory.node.databean;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.tuple.Range;

public class MemoryDatabeanKeyCodec<PK extends PrimaryKey<PK>>
implements Codec<PK,byte[]>{

	private final Supplier<PK> pkSupplier;
	private final List<Field<?>> fields;

	public MemoryDatabeanKeyCodec(Supplier<PK> pkSupplier, List<Field<?>> fields){
		this.pkSupplier = pkSupplier;
		this.fields = fields;
	}

	@Override
	public byte[] encode(PK value){
		return FieldTool.getConcatenatedValueBytes(value.getFields());
	}

	@Override
	public PK decode(byte[] bytes){
		return FieldSetTool.fromConcatenatedValueBytes(pkSupplier, fields, bytes);
	}

	public Range<byte[]> encodeRange(Range<PK> pkRange){
		boolean startInclusive = pkRange.getStartInclusive();
		byte[] start = null;
		if(pkRange.hasStart()){
			start = encode(pkRange.getStart());
			var startFields = pkRange.getStart().getFields();
			if(FieldTool.countNonNullLeadingFields(startFields) != startFields.size() && !startInclusive){
				start = ByteTool.unsignedIncrement(start);
			}
		}

		// When querying a prefix the endKey may be equal to but shorter than the startKey, which means it compares
		// before the startKey. To fix that we increment the end key and force endInclusive=false
		byte[] end = null;
		if(pkRange.hasEnd()){
			if(pkRange.getEndInclusive()){
				end = ByteTool.unsignedIncrement(encode(pkRange.getEnd()));
			}else{
				end = encode(pkRange.getEnd());
			}
		}
		boolean endInclusive = false;

		return new Range<>(Arrays::compareUnsigned, start, startInclusive, end, endInclusive);
	}

}
