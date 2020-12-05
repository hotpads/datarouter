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
package io.datarouter.client.hbase.node.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;

import io.datarouter.model.entity.Entity;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.entity.EntityPartitioner;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;
import io.datarouter.util.bytes.ByteTool;

public class HBaseEntityQueryBuilder<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	protected final EntityPartitioner<EK> partitioner;

	public HBaseEntityQueryBuilder(EntityFieldInfo<EK,E> entityFieldInfo){
		this.partitioner = entityFieldInfo.getEntityPartitioner();
	}

	public byte[] getRowBytes(EK ek){
		if(ek == null){
			throw new IllegalArgumentException("no nulls");
		}
		return FieldTool.getConcatenatedValueBytesUnterminated(ek.getFields());
	}

	public byte[] getRowBytesWithPartition(EK ek){
		byte[] partitionPrefix = partitioner.getPrefix(ek);
		return ByteTool.concatenate(partitionPrefix, getRowBytes(ek));
	}

	public List<Scan> getScanForEachPartition(EK startKey, boolean startKeyInclusive, boolean keysOnly){
		byte[] ekBytesInclusive = new byte[0];
		if(startKey != null){
			byte[] ekBytes = getRowBytes(startKey);
			ekBytesInclusive = startKeyInclusive ? ekBytes : ByteTool.unsignedIncrement(ekBytes);
		}
		List<Scan> scans = new ArrayList<>();
		for(int partition = 0; partition < partitioner.getNumPartitions(); ++partition){
			byte[] scanStartBytes = ByteTool.concatenate(partitioner.getPrefix(partition), ekBytesInclusive);
			FilterList filterList = new FilterList();
			filterList.addFilter(new PrefixFilter(partitioner.getPrefix(partition)));
			if(keysOnly){
				filterList.addFilter(new FirstKeyOnlyFilter());//FirstKeyOnlyFilter avoids duplicate keys
			}
			Scan scan = new Scan()
					.withStartRow(scanStartBytes)
					.setFilter(filterList);
			scans.add(scan);
		}
		return scans;
	}

}
