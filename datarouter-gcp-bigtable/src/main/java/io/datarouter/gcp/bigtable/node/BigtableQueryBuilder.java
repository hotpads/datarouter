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
package io.datarouter.gcp.bigtable.node;

import java.util.List;
import java.util.Objects;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Bytes;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.TerminatedByteArrayTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;

public class BigtableQueryBuilder<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>>{

	public static <
			EK extends EntityKey<EK>,
			PK extends EntityPrimaryKey<EK,PK>>
	boolean isSingleRowRange(Range<PK> range){
		return range.hasStart()
				&& range.equalsStartEnd()
				&& range.getStartInclusive()
				&& range.getEndInclusive()
				&& Scanner.of(range.getStart().getFieldValues()).noneMatch(Objects::isNull);
	}

	public byte[] getPkBytes(PK pk){
		return getConcatenatedValueBytes(pk.getFields());
	}

	public Bytes getPkByteRange(PK pk){
		if(pk == null){
			return null;
		}
		byte[] pkBytes = getPkBytes(pk);
		if(pkBytes == null){
			return null;
		}
		return new Bytes(pkBytes);
	}

	private static byte[] getConcatenatedValueBytes(List<Field<?>> fields){
		int numTokens = FieldTool.countNonNullLeadingFields(fields);
		if(numTokens == 0){
			return TerminatedByteArrayTool.escapeAndTerminate(EmptyArray.BYTE);
		}
		byte[][] tokens = new byte[numTokens][];
		for(int i = 0; i < numTokens; ++i){
			Field<?> field = fields.get(i);
			tokens[i] = field.getTerminatedKeyBytes();
		}
		return ByteTool.concat(tokens);
	}

}
