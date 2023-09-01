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

import java.util.Optional;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.filter.PageFilter;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Bytes;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.util.tuple.Range;

public class HBaseScanBuilder{

	private static final byte[] EMPTY_PREFIX = EmptyArray.BYTE;
	private static final KeyOnlyFilter KEY_ONLY_FILTER = new KeyOnlyFilter();
	private static final FirstKeyOnlyFilter FIRST_KEY_ONLY_FILTER = new FirstKeyOnlyFilter();

	private byte[] prefix = EMPTY_PREFIX;
	private byte[] nextPrefix = EMPTY_PREFIX;
	private boolean hasNextPrefix = false;
	private Range<Bytes> range = Range.everything();
	private Filter columnPrefixFilter;
	private Integer limit;
	private FirstKeyOnlyFilter firstKeyFilter;
	private KeyOnlyFilter keyFilter;
	private boolean cacheBlocks = true;
	private boolean startIsFullKey;

	public HBaseScanBuilder withPrefix(byte[] prefix){
		this.prefix = prefix;
		this.nextPrefix = getNextPrefix();
		this.hasNextPrefix = nextPrefix != null && anyNonZero(nextPrefix);
		return this;
	}

	public HBaseScanBuilder withRange(Range<Bytes> range){
		this.range = range;
		return this;
	}

	public HBaseScanBuilder withColumnPrefix(String columnPrefix){
		this.columnPrefixFilter = new ColumnPrefixFilter(StringCodec.UTF_8.encode(columnPrefix));
		return this;
	}

	public HBaseScanBuilder withLimit(Integer limit){
		this.limit = limit;
		return this;
	}

	public HBaseScanBuilder withKeyOnly(boolean keyOnly){
		if(keyOnly){
			this.keyFilter = KEY_ONLY_FILTER;
		}
		return this;
	}

	public HBaseScanBuilder withFirstKeyOnly(boolean firstKeyOnly){
		if(firstKeyOnly){
			this.firstKeyFilter = FIRST_KEY_ONLY_FILTER;
			this.keyFilter = KEY_ONLY_FILTER;
		}
		return this;
	}

	public HBaseScanBuilder withCacheBlocks(boolean cacheBlocks){
		this.cacheBlocks = cacheBlocks;
		return this;
	}

	public HBaseScanBuilder withStartIsFullKey(boolean startIsFullKey){
		this.startIsFullKey = startIsFullKey;
		return this;
	}

	public Scan build(){
		Scan scan = getScanForRange();
		//note that bigtable ignores setMaxResultsPerColumnFamily, setBatch, and setCaching
		scan.setCacheBlocks(cacheBlocks);
		if(limit != null){
			scan.setLimit(limit);
		}
		makeFilter().ifPresent(scan::setFilter);
		return scan;
	}

	private Scan getScanForRange(){
		byte[] startWithPrefix = ByteTool.concat(prefix, getStart());
		byte[] endExclusiveWithoutPrefix = getEndExclusive();
		Scan scan = new Scan();
		if(startIsFullKey || range.getStart() == null || range.getStartInclusive()){
			scan.withStartRow(startWithPrefix, range.getStartInclusive());
		}else{
			scan.withStartRow(ByteTool.unsignedIncrement(startWithPrefix), true);
		}
		if(endExclusiveWithoutPrefix.length == 0){
			if(hasNextPrefix){
				scan.withStopRow(nextPrefix, false);
			}
		}else{
			scan.withStopRow(ByteTool.concat(prefix, endExclusiveWithoutPrefix), false);
		}
		return scan;
	}

	private Optional<Filter> makeFilter(){
		FilterList filterList = new FilterList();
		if(columnPrefixFilter != null){
			filterList.addFilter(columnPrefixFilter);
		}
		if(firstKeyFilter != null){
			filterList.addFilter(firstKeyFilter);
		}
		if(keyFilter != null){
			filterList.addFilter(keyFilter);
		}
		if(limit != null){
			filterList.addFilter(new PageFilter(limit));
		}
		if(filterList.getFilters().isEmpty()){
			return Optional.empty();
		}
		if(filterList.getFilters().size() == 1){
			return Optional.of(filterList.getFilters().get(0));
		}
		return Optional.of(filterList);
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

	private byte[] getNextPrefix(){
		return ByteTool.unsignedIncrementOverflowToNull(prefix);
	}

	private boolean anyNonZero(byte[] bytes){
		for(int i = 0; i < bytes.length; ++i){
			if(bytes[i] != 0){
				return true;
			}
		}
		return false;
	}

}
