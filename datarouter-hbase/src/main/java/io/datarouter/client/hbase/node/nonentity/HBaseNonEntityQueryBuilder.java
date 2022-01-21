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
package io.datarouter.client.hbase.node.nonentity;

import java.util.Objects;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Bytes;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;

public class HBaseNonEntityQueryBuilder<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>>{

	private final EntityPartitioner<EK> partitioner;

	public HBaseNonEntityQueryBuilder(EntityPartitioner<EK> partitioner){
		this.partitioner = partitioner;
	}

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

	private byte[] getEkBytes(EK ek){
		return FieldTool.getConcatenatedValueBytesUnterminated(ek.getFields());
	}

	private byte[] getPkBytes(PK pk){
		return FieldTool.getConcatenatedValueBytesUnterminated(pk.getFields());
	}

	public byte[] getPkBytesWithPartition(PK pk){
		byte[] prefix = partitioner.getPrefix(pk.getEntityKey());
		return ByteTool.concat(prefix, getPkBytes(pk));
	}

	public Bytes getEkByteRange(EK ek){
		return ek == null ? null : new Bytes(getEkBytes(ek));
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

}
