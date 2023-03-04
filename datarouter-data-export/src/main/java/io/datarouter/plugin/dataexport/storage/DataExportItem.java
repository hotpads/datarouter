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
package io.datarouter.plugin.dataexport.storage;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.InstantFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.util.tuple.Range;

public class DataExportItem extends BaseDatabean<DataExportItemKey,DataExportItem>{

	private String nodeName;
	private String startAfterKey;
	private String endBeforeKey;
	private Long maxRows;
	private Instant dateCreated;

	public static class FieldKeys{
		public static final StringFieldKey nodeName = new StringFieldKey("nodeName");
		public static final StringFieldKey startAfterKey = new StringFieldKey("startAfterKey");
		public static final StringFieldKey endBeforeKey = new StringFieldKey("endBeforeKey");
		public static final LongFieldKey maxRows = new LongFieldKey("maxRows");
		public static final InstantFieldKey dateCreated = new InstantFieldKey("dateCreated");
	}

	public static class DataExportItemFielder extends BaseDatabeanFielder<DataExportItemKey, DataExportItem>{

		public DataExportItemFielder(){
			super(DataExportItemKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(DataExportItem databean){
			return List.of(
					new StringField(FieldKeys.nodeName, databean.nodeName),
					new StringField(FieldKeys.startAfterKey, databean.startAfterKey),
					new StringField(FieldKeys.endBeforeKey, databean.endBeforeKey),
					new LongField(FieldKeys.maxRows, databean.maxRows),
					new InstantField(FieldKeys.dateCreated, databean.dateCreated));
		}

	}

	@Override
	public Supplier<DataExportItemKey> getKeySupplier(){
		return DataExportItemKey::new;
	}

	public DataExportItem(){
		super(new DataExportItemKey(null, null));
	}

	public DataExportItem(
			Long exportId,
			Integer rowId,
			String nodeName,
			String startAfterKey,
			String endBeforeKey,
			Long maxRows){
		super(new DataExportItemKey(exportId, rowId));
		this.nodeName = nodeName;
		this.startAfterKey = startAfterKey;
		this.endBeforeKey = endBeforeKey;
		this.maxRows = maxRows;
		this.dateCreated = Instant.now();
	}

	public String getNodeName(){
		return nodeName;
	}

	public String getStartAfterKey(){
		return startAfterKey;
	}

	public String getEndBeforeKey(){
		return endBeforeKey;
	}

	public Long getMaxRows(){
		return maxRows;
	}

	public Range<String> getRange(){
		return new Range<>(startAfterKey, true, endBeforeKey, false);
	}

}
