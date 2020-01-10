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
package io.datarouter.client.hbase.util;

import java.util.Optional;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.filter.PageFilter;

import io.datarouter.util.bytes.ByteRange;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.tuple.Range;

public class HBaseScanBuilder{

	private static final byte[] EMPTY_PREFIX = new byte[]{};
	private static final KeyOnlyFilter KEY_ONLY_FILTER = new KeyOnlyFilter();
	private static final FirstKeyOnlyFilter FIRST_KEY_ONLY_FILTER = new FirstKeyOnlyFilter();

	private byte[] prefix = EMPTY_PREFIX;
	private Range<ByteRange> range = Range.everything();
	private Filter columnPrefixFilter;
	private Integer limit;
	private Filter keyFilter;
	private boolean cacheBlocks = true;

	public HBaseScanBuilder withPrefix(byte[] prefix){
		this.prefix = prefix;
		return this;
	}

	public HBaseScanBuilder withRange(Range<ByteRange> range){
		this.range = range;
		return this;
	}

	public HBaseScanBuilder withColumnPrefix(String columnPrefix){
		this.columnPrefixFilter = new ColumnPrefixFilter(StringByteTool.getUtf8Bytes(columnPrefix));
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
			this.keyFilter = FIRST_KEY_ONLY_FILTER;
		}
		return this;
	}

	public HBaseScanBuilder withCacheBlocks(boolean cacheBlocks){
		this.cacheBlocks = cacheBlocks;
		return this;
	}

	public Scan build(){
		Scan scan = getScanForRange();
		//note that bigtable ignores setMaxResultsPerColumnFamily, setBatch, and setCaching
		scan.setCacheBlocks(cacheBlocks);
		makeFilter().ifPresent(scan::setFilter);
		return scan;
	}

	private Scan getScanForRange(){
		byte[] startWithPrefix = ByteTool.concatenate(prefix, getStartInclusive());
		byte[] endWithoutPrefix = getEndExclusive();
		if(prefix.length == 0){
			if(endWithoutPrefix.length == 0){
				return new Scan(startWithPrefix);
			}
			return new Scan(startWithPrefix, endWithoutPrefix);
		}
		if(endWithoutPrefix.length == 0){
			return new Scan(startWithPrefix, ByteTool.unsignedIncrement(prefix));
		}
		return new Scan(startWithPrefix, ByteTool.concatenate(prefix, endWithoutPrefix));
	}

	private Optional<Filter> makeFilter(){
		FilterList filterList = new FilterList();
		if(columnPrefixFilter != null){
			filterList.addFilter(columnPrefixFilter);
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

	private byte[] getStartInclusive(){
		if(!range.hasStart()){
			return new byte[]{};
		}
		if(range.getStartInclusive()){
			return range.getStart().toArray();
		}
		return range.getStart().copyToArrayNewArrayAndIncrement();
	}

	private byte[] getEndExclusive(){
		if(!range.hasEnd()){
			return new byte[]{};
		}
		if(range.getEndInclusive()){
			return range.getEnd().copyToArrayNewArrayAndIncrement();
		}
		return range.getEnd().toArray();
	}

}
