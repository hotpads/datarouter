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

import com.google.cloud.bigtable.data.v2.models.Filters.Filter;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.cloud.bigtable.data.v2.models.Range.ByteStringRange;
import com.google.protobuf.ByteString;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Bytes;
import io.datarouter.util.tuple.Range;

public class BigtableScanBuilder{

	private String tableId;
	private Range<Bytes> range = Range.everything();
	private Integer limit;
	private boolean startIsFullKey;
	private Filter filter = BigtableReaderNode.LATEST_VERSION_FILTER;

	public BigtableScanBuilder(String tableId){
		this.tableId = tableId;
	}

	public BigtableScanBuilder withRange(Range<Bytes> range){
		this.range = range;
		return this;
	}

	public BigtableScanBuilder withLimit(Integer limit){
		this.limit = limit;
		return this;
	}

	public BigtableScanBuilder withFirstKeyOnly(boolean firstKeyOnly){
		if(firstKeyOnly){
			filter = BigtableReaderNode.KEY_ONLY_FILTER;
		}
		return this;
	}

	public BigtableScanBuilder withStartIsFullKey(boolean startIsFullKey){
		this.startIsFullKey = startIsFullKey;
		return this;
	}

	public Query build(){
		Query scan = getScanForRange();
		if(limit != null){
			scan.limit(limit);
		}
		scan.filter(filter);
		return scan;
	}

	private Query getScanForRange(){
		byte[] start = getStart();
		byte[] endExclusive = getEndExclusive();
		ByteStringRange rangeOne = ByteStringRange.unbounded();
		if(startIsFullKey || range.getStart() == null || range.getStartInclusive()){
			if(range.getStartInclusive()){
				rangeOne.startClosed(ByteString.copyFrom(start));
			}else{
				rangeOne.startOpen(ByteString.copyFrom(start));
			}
		}else{
			rangeOne.startClosed(ByteString.copyFrom(ByteTool.unsignedIncrement(start)));
		}
		if(endExclusive.length > 0){
			rangeOne.endOpen(ByteString.copyFrom(endExclusive));
		}
		return Query.create(tableId)
				.range(rangeOne);
	}

	private byte[] getStart(){
		if(!range.hasStart()){
			return new byte[]{};
		}
		return range.getStart().toArray();
	}

	private byte[] getEndExclusive(){
		if(!range.hasEnd()){
			return new byte[]{};
		}
		if(range.getEndInclusive()){
			return ByteTool.unsignedIncrement(range.getEnd().toArray());
		}
		return range.getEnd().toArray();
	}

}
