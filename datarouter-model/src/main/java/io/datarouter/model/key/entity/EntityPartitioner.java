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
package io.datarouter.model.key.entity;

import java.util.List;

import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.key.primary.EntityPrimaryKeyTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.tuple.Range;

public interface EntityPartitioner<EK extends EntityKey<EK>>{

	int getNumPartitions();
	List<Integer> getAllPartitions();
	boolean isLastPartition(int partition);
	int getNumPrefixBytes();
	List<byte[]> getAllPrefixes();
	byte[][] getAllPrefixesArray();
	byte[] getPrefix(int partition);
	byte[] getPrefix(EK ek);
	byte[] getNextPrefix(int partition);
	int getPartition(EK ek);
	int parsePartitionFromBytes(byte[] bytes);

	default Scanner<Integer> scanAllPartitions(){
		return Scanner.of(getAllPartitions());
	}

	default Scanner<byte[]> scanAllPrefixes(){
		return Scanner.of(getAllPrefixes());
	}

	default <PK extends EntityPrimaryKey<EK,PK>>
	Scanner<Integer> scanPartitions(Range<PK> range){
		if(EntityPrimaryKeyTool.isSingleEntity(range)){
			return Scanner.of(getPartition(range.getStart().getEntityKey()));
		}
		return scanAllPartitions();
	}

	default <PK extends EntityPrimaryKey<EK,PK>>
	Scanner<byte[]> scanPrefixes(Range<PK> range){
		return scanPartitions(range)
				.map(this::getPrefix);
	}

}
