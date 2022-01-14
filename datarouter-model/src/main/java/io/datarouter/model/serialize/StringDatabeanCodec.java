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
package io.datarouter.model.serialize;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;

public interface StringDatabeanCodec{

	static final Charset CHARSET = StandardCharsets.UTF_8;

	<PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	String toString(D databean, F fielder);

	default <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	byte[] toBytes(D databean, F fielder){
		return toString(databean, fielder).getBytes(CHARSET);
	}

	<PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	D fromString(String string, F fielder, Supplier<D> databeanSupplier);

	String getCollectionSeparator();
	String getCollectionPrefix();
	String getCollectionSuffix();

	default byte[] getCollectionSeparatorBytes(){
		return getCollectionSeparator().getBytes(CHARSET);
	}

	default byte[] getCollectionPrefixBytes(){
		return getCollectionPrefix().getBytes(CHARSET);
	}

	default byte[] getCollectionSuffixBytes(){
		return getCollectionSuffix().getBytes(CHARSET);
	}

	<PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<D> fromStringMulti(String string, F fielder, Supplier<D> databeanSupplier);


	default List<List<byte[]>> makeGroups(
			List<byte[]> encodedDatabeans,
			int maxBoundedBytesPerMessage){
		List<List<byte[]>> groups = new ArrayList<>();
		List<byte[]> group = new ArrayList<>();
		int groupLengthWithoutSeparators = 0;
		for(byte[] encodedDatabean : encodedDatabeans){
			int totalGroupLength = groupLengthWithoutSeparators + encodedDatabean.length
					+ getCollectionSeparatorBytes().length * group.size();
			if(totalGroupLength > maxBoundedBytesPerMessage){
				groups.add(group);
				group = new ArrayList<>();
				groupLengthWithoutSeparators = 0;
			}
			group.add(encodedDatabean);
			groupLengthWithoutSeparators += encodedDatabean.length;
		}
		if(!group.isEmpty()){
			groups.add(group);
		}
		return groups;
	}

	default String concatGroup(List<byte[]> group){
		var databeanGroup = new ByteArrayOutputStream();
		databeanGroup.write(getCollectionPrefixBytes(), 0, getCollectionPrefixBytes().length);
		for(int i = 0; i < group.size(); i++){
			databeanGroup.write(group.get(i), 0, group.get(i).length);
			if(i < group.size() - 1){
				databeanGroup.write(getCollectionSeparatorBytes(), 0, getCollectionSeparatorBytes().length);
			}
		}
		databeanGroup.write(getCollectionSuffixBytes(), 0, getCollectionSuffixBytes().length);
		return StringCodec.UTF_8.decode(databeanGroup.toByteArray());
	}

}
