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
package io.datarouter.nodewatch.storage.alertthreshold;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class TableSizeAlertThreshold extends BaseDatabean<TableSizeAlertThresholdKey,TableSizeAlertThreshold>{

	private Long maxRows;

	public static class FieldKeys{
		public static final LongFieldKey maxRows = new LongFieldKey("maxRows");
	}

	public static class TableSizeAlertThresholdFielder
	extends BaseDatabeanFielder<TableSizeAlertThresholdKey,TableSizeAlertThreshold>{

		public TableSizeAlertThresholdFielder(){
			super(TableSizeAlertThresholdKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TableSizeAlertThreshold databean){
			return List.of(new LongField(FieldKeys.maxRows, databean.maxRows));
		}

	}

	public TableSizeAlertThreshold(){
		super(new TableSizeAlertThresholdKey());
	}

	public TableSizeAlertThreshold(String clientName, String tableName, Long maxRows){
		this(new TableSizeAlertThresholdKey(clientName, tableName), maxRows);
	}

	public TableSizeAlertThreshold(TableSizeAlertThresholdKey key, Long maxRows){
		super(key);
		this.maxRows = maxRows;
	}

	@Override
	public Supplier<TableSizeAlertThresholdKey> getKeySupplier(){
		return TableSizeAlertThresholdKey::new;
	}

	public void setMaxRows(Long maxRows){
		this.maxRows = maxRows;
	}

	public Long getMaxRows(){
		return maxRows;
	}

}
