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
package io.datarouter.nodewatch.storage.latesttablecount;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.util.number.NumberFormatter;

public class LatestTableCount extends BaseDatabean<LatestTableCountKey,LatestTableCount>{

	public static final Comparator<LatestTableCount> COMPARE_CLIENT
			= Comparator.comparing(Databean::getKey, LatestTableCountKey.COMPARE_CLIENT);
	public static final Comparator<LatestTableCount> COMPARE_TABLE
			= Comparator.comparing(Databean::getKey, LatestTableCountKey.COMPARE_TABLE);
	public static final Comparator<LatestTableCount> COMPARE_ROWS
			= Comparator.comparing(LatestTableCount::getNumRows);
	public static final Comparator<LatestTableCount> COMPARE_DATE_UPDATED
			= Comparator.comparing(LatestTableCount::getDateUpdated);
	public static final Comparator<LatestTableCount> COMPARE_COUNT_TIME
			= Comparator.comparing(LatestTableCount::getCountTimeMs);
	public static final Comparator<LatestTableCount> COMPARE_SPANS
			= Comparator.comparing(LatestTableCount::getNumSpans);
	public static final Comparator<LatestTableCount> COMPARE_SLOW_SPANS
			= Comparator.comparing(LatestTableCount::getNumSlowSpans);

	private Long numRows;
	private Date dateUpdated = new Date();
	private Long countTimeMs;
	private Long numSpans;
	private Long numSlowSpans;

	public static class FieldKeys{
		public static final LongFieldKey numRows = new LongFieldKey("numRows");
		public static final LongFieldKey countTimeMs = new LongFieldKey("countTimeMs");
		@SuppressWarnings("deprecation")
		public static final DateFieldKey dateUpdated = new DateFieldKey("dateUpdated");
		public static final LongFieldKey numSpans = new LongFieldKey("numSpans");
		public static final LongFieldKey numSlowSpans = new LongFieldKey("numSlowSpans");
	}

	public static class LatestTableCountFielder
	extends BaseDatabeanFielder<LatestTableCountKey,LatestTableCount>{

		public LatestTableCountFielder(){
			super(LatestTableCountKey::new);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(LatestTableCount databean){
			return List.of(
					new LongField(FieldKeys.numRows, databean.numRows),
					new DateField(FieldKeys.dateUpdated, databean.dateUpdated),
					new LongField(FieldKeys.countTimeMs, databean.countTimeMs),
					new LongField(FieldKeys.numSpans, databean.numSpans),
					new LongField(FieldKeys.numSlowSpans, databean.numSlowSpans));
		}
	}

	@Override
	public Supplier<LatestTableCountKey> getKeySupplier(){
		return LatestTableCountKey::new;
	}

	public LatestTableCount(){
		super(new LatestTableCountKey(null, null));
	}

	public LatestTableCount(TableCount tableCount){
		this(tableCount.getKey().getClientName(),
				tableCount.getKey().getTableName(),
				tableCount.getNumRows(),
				tableCount.getCountTimeMs(),
				tableCount.getNumSpans(),
				tableCount.getNumSlowSpans());
	}

	public LatestTableCount(
			String clientName,
			String tableName,
			Long numRows,
			Long countTimeMs,
			Long numSpans,
			Long numSlowSpans){
		super(new LatestTableCountKey(clientName, tableName));
		this.numRows = numRows;
		this.dateUpdated = new Date();
		this.countTimeMs = countTimeMs;
		this.numSpans = numSpans;
		this.numSlowSpans = numSlowSpans;
	}

	public String getNumRowsFormatted(){
		return NumberFormatter.addCommas(numRows);
	}

	public Long getNumRows(){
		return numRows;
	}

	public Long getCountTimeMs(){
		return countTimeMs;
	}

	public Instant getDateUpdated(){
		return dateUpdated.toInstant();
	}

	public Long getNumSpans(){
		return numSpans;
	}

	public Long getNumSlowSpans(){
		return numSlowSpans;
	}

	public Double getPercentSlowSpans(){
		return calcSlowSpansPercent(numSlowSpans, numSpans);
	}

	public static Double calcSlowSpansPercent(Long numSlowSpans, Long numSpans){
		if(numSlowSpans == null || numSpans == null || numSpans == 0){
			return 0d;
		}
		return 100 * (double)numSlowSpans / (double)numSpans;
	}

}
