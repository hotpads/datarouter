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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.FieldSetTool;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.util.bytes.ByteRange;
import io.datarouter.util.bytes.ByteTool;
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
		return range.equalsStartEnd()
				&& range.getStartInclusive()
				&& range.getEndInclusive()
				&& FieldSetTool.areAllFieldsNonNull(range.getStart());
	}

	private byte[] getEkBytes(EK ek){
		return FieldTool.getConcatenatedValueBytes(ek.getFields(), true, true, false);
	}

	private byte[] getPkBytes(PK pk){
		return FieldTool.getConcatenatedValueBytes(pk.getFields(), true, true, false);
	}

	public byte[] getPkBytesWithPartition(PK pk){
		byte[] prefix = partitioner.getPrefix(pk.getEntityKey());
		return ByteTool.concatenate(prefix, getPkBytes(pk));
	}

	public ByteRange getEkByteRange(EK ek){
		return ek == null ? null : new ByteRange(getEkBytes(ek));
	}

	public ByteRange getPkByteRange(PK pk){
		return pk == null ? null : new ByteRange(getPkBytes(pk));
	}

}
