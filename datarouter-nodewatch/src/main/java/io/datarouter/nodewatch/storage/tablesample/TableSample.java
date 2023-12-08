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
package io.datarouter.nodewatch.storage.tablesample;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.client.mysql.ddl.domain.MysqlRowFormat;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.BooleanField;
import io.datarouter.model.field.imp.comparable.BooleanFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.model.util.PercentFieldCodec;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.types.MilliTime;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.number.NumberFormatter;

/*
 * A record storing approximately every 1/Nth PK of a table.
 *
 * Edge cases:
 *   - There is no record for the beginning of the table
 *   - There is a record for the last row of the table, which is also marked isLastSpan=true
 *
 * Convenient queries:

# Records for a single table
select numRows, stringKey
	from TableSample
	where tableName='' limit 50;

# Total rows by client
select clientName, sum(numRows) as r, count(clientName) as c
	from TableSample
	group by clientName
	order by r desc;

# Total rows by client/table/subEntityPrefix
select clientName, tableName, subEntityPrefix, sum(numRows) as r, count(clientName) as c
	from TableSample
	#where tableName = ''
	group by clientName, tableName, subEntityPrefix
	order by r desc;

# Non-stable table records:
select date(dateUpdated) as d, count(*) as cnt
	from TableSample
	where (numStableCounts is null or numStableCounts = 0)
	and dateScheduled is null
	group by d
	order by d asc;

# Old records that refuse to be rescheduled:
select *
	from TableSample
	where (numStableCounts is null or numStableCounts = 0)
	and dateScheduled is null
	and dateUpdated < '2017-09-01'
	limit 1
	\G;

# Stable records:
select date(dateUpdated) as d, count(*) as cnt
	from TableSample
	where (numStableCounts is not null and numStableCounts > 0)
	and dateScheduled is null
	group by d
	order by d asc;

 */
public class TableSample extends BaseDatabean<TableSampleKey,TableSample>{

	public static final Duration MAX_TIME_IN_QUEUE = Duration.ofDays(1);

	private Long numRows;
	private MilliTime created;
	private MilliTime updated;
	private String stringKey;
	private Long countTimeMs;
	private Boolean interrupted;
	private Boolean isLastSpan;
	private Integer numStableCounts;
	private Long samplerId;//null if not scheduled
	private MilliTime scheduled; //null if not scheduled

	private static class FieldKeys{
		private static final LongFieldKey numRows = new LongFieldKey("numRows");
		public static final LongEncodedFieldKey<MilliTime> created = new LongEncodedFieldKey<>("created",
				new MilliTimeFieldCodec());
		public static final LongEncodedFieldKey<MilliTime> updated = new LongEncodedFieldKey<>("updated",
				new MilliTimeFieldCodec());
		private static final LongFieldKey samplerId = new LongFieldKey("samplerId");
		public static final LongEncodedFieldKey<MilliTime> scheduled = new LongEncodedFieldKey<>("scheduled",
				new MilliTimeFieldCodec());
		private static final StringFieldKey stringKey = new StringFieldKey("stringKey")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		private static final LongFieldKey countTimeMs = new LongFieldKey("countTimeMs");
		private static final BooleanFieldKey interrupted = new BooleanFieldKey("interrupted");
		private static final BooleanFieldKey isLastSpan = new BooleanFieldKey("isLastSpan");
		private static final IntegerFieldKey numStableCounts = new IntegerFieldKey("numStableCounts");
	}

	public static class TableSampleFielder extends BaseDatabeanFielder<TableSampleKey,TableSample>{

		public TableSampleFielder(){
			super(TableSampleKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TableSample databean){
			return List.of(
					new LongField(FieldKeys.numRows, databean.numRows),
					new LongEncodedField<>(FieldKeys.created, databean.created),
					new LongEncodedField<>(FieldKeys.updated, databean.updated),
					new StringField(FieldKeys.stringKey, databean.stringKey),
					new LongField(FieldKeys.countTimeMs, databean.countTimeMs),
					new BooleanField(FieldKeys.interrupted, databean.interrupted),
					new BooleanField(FieldKeys.isLastSpan, databean.isLastSpan),
					new IntegerField(FieldKeys.numStableCounts, databean.numStableCounts),
					new LongField(FieldKeys.samplerId, databean.samplerId),
					new LongEncodedField<>(FieldKeys.scheduled, databean.scheduled));
		}

		@Override
		public void configure(){
			addOption(MysqlRowFormat.COMPACT);
		}
	}

	/*--------------- construct ---------------*/

	public TableSample(){
		super(new TableSampleKey(null, null));
	}

	public TableSample(
			ClientTableEntityPrefixNameWrapper nodeNames,
			List<Field<?>> rowKey,
			Long numRows,
			MilliTime dateCreated,
			Long countTimeMs,
			boolean interrupted,
			boolean isLast){
		super(new TableSampleKey(nodeNames, rowKey));
		this.numRows = numRows;
		this.created = dateCreated;
		this.updated = MilliTime.now();
		this.stringKey = PercentFieldCodec.encodeFields(rowKey);
		this.countTimeMs = countTimeMs;
		this.interrupted = interrupted;
		this.isLastSpan = isLast;
		this.numStableCounts = 0;
	}

	/*--------------- Databean ---------------*/

	@Override
	public Supplier<TableSampleKey> getKeySupplier(){
		return TableSampleKey::new;
	}

	@Override
	public String toString(){
		return super.toString() + "[" + stringKey + ", " + NumberFormatter.addCommas(numRows) + "]";
	}

	/*--------------- static ---------------*/

	public static long getTotalRows(Collection<TableSample> samples){
		return samples.stream()
				.mapToLong(TableSample::getNumRows)
				.sum();
	}

	/*--------------- methods ---------------*/

	public void addNumRowsAndCountTimeMsFromOther(TableSample other){
		if(other != null){
			numRows += other.numRows;
			countTimeMs += other.countTimeMs;
		}
	}

	public Optional<Duration> getTimeInQueue(){
		return Optional.ofNullable(scheduled)
				.map(MilliTime::age);
	}

	public boolean hasExceededMaxTimeInQueue(){
		return getTimeInQueue()
				.map(ago -> ComparableTool.gt(ago, MAX_TIME_IN_QUEUE))
				.orElse(false);
	}

	public boolean isInterrupted(){
		return interrupted;
	}

	public boolean isScheduledForRecount(){
		return scheduled != null;
	}

	public void clearScheduleFields(){
		scheduled = null;
		samplerId = null;
	}

	public void setScheduleFields(Long samplerId, MilliTime dateScheduled){
		this.samplerId = samplerId;
		this.scheduled = dateScheduled;
	}

	public void incrementStableCounts(){
		++numStableCounts;
	}

	/*-------------- get/set ----------------*/

	public Long getNumRows(){
		return numRows;
	}

	public MilliTime getDateCreated(){
		return created;
	}

	public MilliTime getDateUpdated(){
		return updated;
	}

	public String getStringKey(){
		return stringKey;
	}

	public Long getCountTimeMs(){
		return countTimeMs;
	}

	public void setInterrupted(Boolean interrupted){
		this.interrupted = interrupted;
	}

	public Boolean isLastSpan(){
		return isLastSpan;
	}

	public void setLastSpan(Boolean isLastSpan){
		this.isLastSpan = isLastSpan;
	}

	public int getNumStableCounts(){
		return numStableCounts;
	}

	public Long getSamplerId(){
		return samplerId;
	}

	public void setNumRows(Long numRows){
		this.numRows = numRows;
	}

	public void setCountTimeMs(Long countTimeMs){
		this.countTimeMs = countTimeMs;
	}

}
