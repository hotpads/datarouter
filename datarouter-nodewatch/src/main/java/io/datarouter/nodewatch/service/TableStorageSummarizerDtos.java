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
package io.datarouter.nodewatch.service;

import java.util.List;
import java.util.Map;

import io.datarouter.bytes.ByteLength;
import io.datarouter.scanner.Scanner;

public class TableStorageSummarizerDtos{

	public record TableSummary(
			List<ColumnSummary> columnSummaries,
			long numRowsIncluded){

		public static final TableSummary EMPTY = new TableSummary(List.of(), 0);

		public static TableSummary combine(TableSummary first, TableSummary second){
			if(first.columnSummaries.isEmpty()){
				return second;
			}
			if(second.columnSummaries.isEmpty()){
				return first;
			}
			long totalRows = first.numRowsIncluded + second.numRowsIncluded;
			Map<String,ColumnSummary> firstByName = Scanner.of(first.columnSummaries)
					.toMap(ColumnSummary::name);
			return Scanner.of(second.columnSummaries)
					.map(col -> new ColumnSummary(
							col.name(),
							ColumnSize.combine(totalRows, col.size(), firstByName.get(col.name()).size())))
					.listTo(cols -> new TableSummary(cols, totalRows));
		}
	}

	public record ColumnSummary(
			String name,
			ColumnSize size){
	}

	public record ColumnSize(
			ByteLength nameBytes,
			ByteLength valueBytes,
			long numRowsIncluded){

		public static final ColumnSize EMPTY = new ColumnSize(ByteLength.MIN, ByteLength.MIN, 0);

		public ByteLength avgNameBytes(){
			double avg = (double)nameBytes.toBytes() / (double) numRowsIncluded;
			return ByteLength.ofBytes((long)avg);
		}

		public ByteLength avgValueBytes(){
			double avg = (double)valueBytes.toBytes() / (double) numRowsIncluded;
			return ByteLength.ofBytes((long)avg);
		}

		public static ColumnSize combine(long numRowsIncluded, ColumnSize first, ColumnSize second){
			return new ColumnSize(
					ByteLength.sum(first.nameBytes, second.nameBytes),
					ByteLength.sum(first.valueBytes, second.valueBytes),
					numRowsIncluded);
		}
	}

}
