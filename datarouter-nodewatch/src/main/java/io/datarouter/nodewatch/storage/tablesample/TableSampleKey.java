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
package io.datarouter.nodewatch.storage.tablesample;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldTool;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.array.ByteArrayFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.util.HashMethods;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.bytes.StringByteTool;

public class TableSampleKey extends BaseRegularPrimaryKey<TableSampleKey>{

	private String clientName;
	private String tableName;
	private String subEntityPrefix;
	private byte[] rowKeyBytes;

	public static class FieldKeys{
		public static final StringFieldKey clientName = new StringFieldKey("clientName")
				.withSize(CommonFieldSizes.LENGTH_50);
		public static final StringFieldKey tableName = new StringFieldKey("tableName")
				.withSize(CommonFieldSizes.LENGTH_50);
		public static final StringFieldKey subEntityPrefix = new StringFieldKey("subEntityPrefix")
				.withSize(CommonFieldSizes.LENGTH_50);
		public static final ByteArrayFieldKey rowKeyBytes = new ByteArrayFieldKey("rowKeyBytes");
	}

	@Override
	public List<Field<?>> getFields(){
		return Arrays.asList(
				new StringField(FieldKeys.clientName, clientName),
				new StringField(FieldKeys.tableName, tableName),
				new StringField(FieldKeys.subEntityPrefix, subEntityPrefix),
				new ByteArrayField(FieldKeys.rowKeyBytes, rowKeyBytes));
	}

	/*--------------- construct ---------------*/

	public TableSampleKey(){
	}

	public TableSampleKey(ClientTableEntityPrefixNameWrapper nodeNames, List<Field<?>> rowKeyFields){
		if(nodeNames != null){
			this.clientName = nodeNames.getClientName();
			this.tableName = nodeNames.getTableName();
			//avoid null in PK by using empty string
			this.subEntityPrefix = Optional.ofNullable(nodeNames.getSubEntityPrefix()).orElse("");
		}
		if(rowKeyFields != null){
			this.rowKeyBytes = FieldTool.getConcatenatedValueBytes(rowKeyFields, false, true, false);
		}
	}

	public TableSampleKey(String clientName, String tableName, String subEntityPrefix, byte[] rowKeyBytes){
		this.clientName = clientName;
		this.tableName = tableName;
		this.subEntityPrefix = subEntityPrefix;
		this.rowKeyBytes = rowKeyBytes;
	}

	public static TableSampleKey createSubEntityPrefix(ClientTableEntityPrefixNameWrapper nodeNames){
		return new TableSampleKey(nodeNames, null);
	}

	/*--------------- static -------------------*/

	public static boolean equalsClientAndTable(TableSampleKey paramA, TableSampleKey paramB){
		boolean isClientEqual = Objects.equals(paramA.getClientName(), paramB.getClientName());
		boolean isTableEqual = Objects.equals(paramA.getTableName(), paramB.getTableName());
		return isClientEqual && isTableEqual;
	}

	/*-----------------methods ---------------------*/

	//DJB shifts left 5 bits after each byte, so should be plenty large
	public long positiveLongHashCode(){
		byte[] hashInput = ArrayTool.concatenate(
				StringByteTool.getUtf8Bytes(clientName),
				StringByteTool.getUtf8Bytes(tableName),
				StringByteTool.getUtf8Bytes(subEntityPrefix),
				rowKeyBytes);
		return HashMethods.longDjbHash(hashInput);
	}

	/*--------------- get/set ---------------*/

	public String getClientName(){
		return clientName;
	}

	public void setClientName(String clientName){
		this.clientName = clientName;
	}

	public String getTableName(){
		return tableName;
	}

	public void setTableName(String tableName){
		this.tableName = tableName;
	}

	public String getSubEntityPrefix(){
		return subEntityPrefix;
	}

	public void setSubEntityPrefix(String subEntityPrefix){
		this.subEntityPrefix = subEntityPrefix;
	}

	public byte[] getRowKeyBytes(){
		return this.rowKeyBytes;
	}

	public void setRowKeyBytes(byte[] rowKeyBytes){
		this.rowKeyBytes = rowKeyBytes;
	}

}