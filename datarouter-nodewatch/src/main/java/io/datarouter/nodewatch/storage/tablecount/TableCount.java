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
package io.datarouter.nodewatch.storage.tablecount;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.types.MilliTime;
import io.datarouter.util.number.NumberFormatter;

public class TableCount extends BaseDatabean<TableCountKey,TableCount>{

	public static final Comparator<TableCount> COMPARE_CREATED_MS = Comparator.comparing(
			Databean::getKey,
			TableCountKey.COMPARE_CREATED_MS);
	public static final Comparator<TableCount> COMPARE_COUNT_TIME_MS = Comparator.comparing(
			TableCount::getCountTimeMs);

	private Long numRows;
	private MilliTime updated;
	private Long countTimeMs;
	private Long numSpans;
	private Long numSlowSpans;

	public static class FieldKeys{
		public static final LongFieldKey numRows = new LongFieldKey("numRows");
		public static final LongEncodedFieldKey<MilliTime> updated = new LongEncodedFieldKey<>("updated",
				new MilliTimeFieldCodec());
		public static final LongFieldKey countTimeMs = new LongFieldKey("countTimeMs");
		public static final LongFieldKey numSpans = new LongFieldKey("numSpans");
		public static final LongFieldKey numSlowSpans = new LongFieldKey("numSlowSpans");
	}

	public static class TableCountFielder extends BaseDatabeanFielder<TableCountKey,TableCount>{

		public TableCountFielder(){
			super(TableCountKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TableCount databean){
			return List.of(
					new LongField(FieldKeys.numRows, databean.numRows),
					new LongEncodedField<>(FieldKeys.updated, databean.updated),
					new LongField(FieldKeys.countTimeMs, databean.countTimeMs),
					new LongField(FieldKeys.numSpans, databean.numSpans),
					new LongField(FieldKeys.numSlowSpans, databean.numSlowSpans));
		}

	}

	@Override
	public Supplier<TableCountKey> getKeySupplier(){
		return TableCountKey::new;
	}

	public TableCount(){
		super(new TableCountKey(null, null, null));
	}

	public TableCount(
			String clientName,
			String tableName,
			MilliTime createdMs,
			Long numRows,
			Long countTimeMs,
			Long numSpans,
			Long numSlowSpans){
		super(new TableCountKey(clientName, tableName, createdMs));
		this.numRows = numRows;
		this.updated = createdMs;
		this.countTimeMs = countTimeMs;
		this.numSpans = numSpans;
		this.numSlowSpans = numSlowSpans;
	}

	public static class TableCountLatestEntryComparator implements Comparator<TableCount>{

		@Override
		public int compare(TableCount tableCountA, TableCount tableCountB){
			return tableCountB.getKey().getCreatedMs().compareTo(tableCountA.getKey().getCreatedMs());
		}

	}

	public static class TableCountRowsComparator implements Comparator<TableCount>{

		@Override
		public int compare(TableCount tableCountA, TableCount tableCountB){
			int diff = tableCountB.getNumRows().compareTo(tableCountA.getNumRows());
			if(diff != 0){
				return diff;
			}
			return tableCountA.getKey().getTableName().compareTo(tableCountB.getKey().getTableName());
		}

	}

	public Long getCountTimeMs(){
		return countTimeMs;
	}

	public String getCountTimeSecondsFormatted(){
		return NumberFormatter.addCommas(countTimeMs / 1000);
	}

	public String getNumRowsFormatted(){
		return NumberFormatter.addCommas(numRows);
	}

	public Long getNumRows(){
		return numRows;
	}

	public Instant getDateUpdated(){
		return updated.toInstant();
	}

	public Long getNumSpans(){
		return numSpans;
	}

	public Long getNumSlowSpans(){
		return numSlowSpans;
	}

}
