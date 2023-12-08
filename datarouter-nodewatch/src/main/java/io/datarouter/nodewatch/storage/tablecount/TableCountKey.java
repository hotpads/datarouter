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

import java.util.Comparator;
import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.types.MilliTime;

public class TableCountKey extends BaseRegularPrimaryKey<TableCountKey>{

	public static final Comparator<TableCountKey> COMPARE_CREATED_MS = Comparator.comparing(
			TableCountKey::getCreatedMs);

	private String clientName;
	private String tableName;
	private MilliTime createdMs;

	public static class FieldKeys{
		public static final StringFieldKey clientName = new StringFieldKey("clientName");
		public static final StringFieldKey tableName = new StringFieldKey("tableName");
		public static final LongEncodedFieldKey<MilliTime> createdMs = new LongEncodedFieldKey<>("createdMs",
				new MilliTimeFieldCodec());
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.clientName, clientName),
				new StringField(FieldKeys.tableName, tableName),
				new LongEncodedField<>(FieldKeys.createdMs, createdMs));
	}

	public TableCountKey(){
	}

	public TableCountKey(String clientName, String tableName, MilliTime createdMs){
		this.clientName = clientName;
		this.tableName = tableName;
		this.createdMs = createdMs;
	}

	public static TableCountKey prefix(String clientName, String tableName){
		return new TableCountKey(clientName, tableName, null);
	}

	public String getClientName(){
		return clientName;
	}

	public String getTableName(){
		return tableName;
	}

	public MilliTime getCreatedMs(){
		return createdMs;
	}

	public void setClientName(String clientName){
		this.clientName = clientName;
	}

	public void setTableName(String tableName){
		this.tableName = tableName;
	}

}
