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
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.KeyOnlyFilter;
import org.apache.hadoop.hbase.filter.PageFilter;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Bytes;
import io.datarouter.util.tuple.Range;

public class HBaseScanBuilder{

	// Docs say this returns the first KV (cell) which is strange.
	// Should therefore add the KEY_ONLY_FILTER to be safe.
	private static final FirstKeyOnlyFilter FIRST_CELL_ONLY_FILTER = new FirstKeyOnlyFilter();
	private static final KeyOnlyFilter KEY_ONLY_FILTER = new KeyOnlyFilter();

	private Range<Bytes> range = Range.everything();
	private Integer limit;
	private FirstKeyOnlyFilter firstCellOnlyFilter;
	private KeyOnlyFilter keyOnlyFilter;
	private boolean startIsFullKey;

	public HBaseScanBuilder withRange(Range<Bytes> range){
		this.range = range;
		return this;
	}

	public HBaseScanBuilder withLimit(Integer limit){
		this.limit = limit;
		return this;
	}

	public HBaseScanBuilder withFirstKeyOnly(boolean firstKeyOnly){
		if(firstKeyOnly){
			this.firstCellOnlyFilter = FIRST_CELL_ONLY_FILTER;
			this.keyOnlyFilter = KEY_ONLY_FILTER;
		}
		return this;
	}

	public HBaseScanBuilder withStartIsFullKey(boolean startIsFullKey){
		this.startIsFullKey = startIsFullKey;
		return this;
	}

	public Scan build(){
		Scan scan = getScanForRange();
		//note that bigtable ignores setMaxResultsPerColumnFamily, setBatch, and setCaching
		if(limit != null){
			scan.setLimit(limit);
		}
		makeFilter().ifPresent(scan::setFilter);
		return scan;
	}

	private Scan getScanForRange(){
		byte[] start = getStart();
		byte[] endExclusive = getEndExclusive();
		Scan scan = new Scan();
		if(startIsFullKey || range.getStart() == null || range.getStartInclusive()){
			scan.withStartRow(start, range.getStartInclusive());
		}else{
			scan.withStartRow(ByteTool.unsignedIncrement(start), true);
		}
		if(endExclusive.length > 0){
			scan.withStopRow(endExclusive, false);
		}
		return scan;
	}

	private Optional<Filter> makeFilter(){
		FilterList filterList = new FilterList();
		if(firstCellOnlyFilter != null){
			filterList.addFilter(firstCellOnlyFilter);
		}
		if(keyOnlyFilter != null){
			filterList.addFilter(keyOnlyFilter);
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

}
