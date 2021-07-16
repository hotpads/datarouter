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
package io.datarouter.client.hbase.node.subentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.util.HBaseEntityKeyTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.util.tuple.Pair;

public class HBaseSubEntityResultParser<
		EK extends EntityKey<EK>,
		PK extends EntityPrimaryKey<EK,PK>,
		D extends Databean<PK,D>>{

	private final Supplier<EK> entityKeySupplier;
	private final Supplier<PK> primaryKeySupplier;
	private final Supplier<D> databeanSupplier;
	private final int numPrefixBytes;
	private final byte[] entityColumnPrefixBytes;
	private final Map<String, Field<?>> nonKeyFieldsByColumnNames;
	private final java.lang.reflect.Field keyJavaField;
	private final List<Field<?>> ekFields;
	private final List<Field<?>> ekPkKeyFields;
	private final List<Field<?>> postEkPkFields;

	protected HBaseSubEntityResultParser(
			Supplier<PK> primaryKeySupplier,
			Supplier<EK> entityKeySupplier,
			List<Field<?>> ekFields,
			List<Field<?>> ekPkKeyFields,
			List<Field<?>> postEkPkFields,
			Map<String, Field<?>> nonKeyFieldsByColumnNames,
			int numPrefixBytes,
			byte[] entityColumnPrefixBytes,
			java.lang.reflect.Field keyJavaField, Supplier<D> databeanSupplier){
		this.primaryKeySupplier = primaryKeySupplier;
		this.databeanSupplier = databeanSupplier;
		this.entityKeySupplier = entityKeySupplier;
		this.ekPkKeyFields = ekPkKeyFields;
		this.numPrefixBytes = numPrefixBytes;
		this.entityColumnPrefixBytes = entityColumnPrefixBytes;
		this.ekFields = ekFields;
		this.postEkPkFields = postEkPkFields;
		this.keyJavaField = keyJavaField;
		this.nonKeyFieldsByColumnNames = nonKeyFieldsByColumnNames;
	}

	/*-------------------- parse EK PK and column name ----------------------*/

	public EK getEkFromRowBytes(byte[] rowBytes){
		return HBaseEntityKeyTool.getEkFromRowBytes(rowBytes, entityKeySupplier, numPrefixBytes, ekFields);
	}

	public Pair<PK,String> parsePrimaryKeyAndFieldName(Cell cell){
		PK pk = primaryKeySupplier.get();
		//EK
		//be sure to get the entity key fields from DatabeanFieldInfo in case the PK overrode the EK field names
		parseEkFieldsFromBytesToPk(cell, pk);
		//post-EK
		int fieldNameOffset = parsePostEkFieldsFromBytesToPk(cell, pk);
		//fieldName
		String fieldName = StringByteTool.fromUtf8BytesOffset(CellUtil.cloneQualifier(cell), fieldNameOffset);
		return new Pair<>(pk, fieldName);
	}

	/*-------------------- parse multiple hbase rows ----------------------*/

	public List<PK> getPrimaryKeysWithMatchingQualifierPrefixMulti(Result[] rows){
		List<PK> results = new ArrayList<>();
		for(Result row : rows){
			if(row.isEmpty()){
				continue;
			}
			List<PK> pksFromSingleGet = getPrimaryKeysWithMatchingQualifierPrefix(row);
			results.addAll(pksFromSingleGet);
		}
		return results;
	}

	public List<D> getDatabeansWithMatchingQualifierPrefixMulti(Result[] rows){
		List<D> results = new ArrayList<>();
		for(Result row : rows){
			if(row.isEmpty()){
				continue;
			}
			List<D> databeansFromSingleGet = getDatabeansWithMatchingQualifierPrefix(row, null);
			results.addAll(databeansFromSingleGet);
		}
		return results;
	}

	/*---------------------------- parse single hbase row -------------------*/

	public List<PK> getPrimaryKeysWithMatchingQualifierPrefix(Result row){
		return getPrimaryKeysWithMatchingQualifierPrefix(row, null);
	}

	public List<PK> getPrimaryKeysWithMatchingQualifierPrefix(Result row, Integer limit){
		if(row == null){
			return List.of();
		}
		return getPrimaryKeysWithMatchingQualifierPrefix(row.listCells(), limit);
	}

	public List<PK> getPrimaryKeysWithMatchingQualifierPrefix(List<Cell> cells, Integer limit){
		if(cells == null || cells.isEmpty()){
			return List.of();
		}
		ArrayList<PK> pks = new ArrayList<>();
		PK previousPk = null;
		for(Cell cell : cells){
			if(!matchesNodePrefix(cell)){
				continue;
			}
			Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(cell);
			PK pk = pkAndFieldName.getLeft();
			if(Objects.equals(previousPk, pk)){
				continue;
			}
			pks.add(pk);
			if(limit != null && pks.size() >= limit){
				break;
			}
			previousPk = pk;
		}
		return pks;
	}

	public List<D> getDatabeansWithMatchingQualifierPrefix(Result row, Integer limit){
		if(row == null){
			return List.of();
		}
		return getDatabeansForKvsWithMatchingQualifierPrefix(row.listCells(), limit);
	}

	public List<D> getDatabeansForKvsWithMatchingQualifierPrefix(List<Cell> cells, Integer limit){
		if(cells == null || cells.isEmpty()){
			return List.of();
		}
		List<D> databeans = new ArrayList<>();
		D databean = null;
		for(Cell cell : cells){
			Pair<PK,String> pkAndFieldName = parsePrimaryKeyAndFieldName(cell);
			if(databean == null || ObjectTool.notEquals(databean.getKey(), pkAndFieldName.getLeft())){
				//we're about to start a new databean
				if(limit != null && databeans.size() == limit){
					break;
				}
				databean = databeanSupplier.get();
				ReflectionTool.set(keyJavaField, databean, pkAndFieldName.getLeft());
				databeans.add(databean);
			}
			setDatabeanField(databean, pkAndFieldName.getRight(), CellUtil.cloneValue(cell));
		}
		return databeans;
	}

	public void setDatabeanField(D databean, String fieldName, byte[] bytesValue){
		if(HBaseClientManager.DUMMY_COL_NAME.equals(fieldName)){
			return;
		}
		Field<?> field = nonKeyFieldsByColumnNames.get(fieldName);
		if(field == null){//field doesn't exist in the databean anymore.  skip it
			return;
		}
		//set the databean field value for this hbase cell
		Object value = field.fromBytesButDoNotSet(bytesValue, 0);
		field.setUsingReflection(databean, value);
	}

	/*---------------------------- private ----------------------------------*/

	private boolean matchesNodePrefix(Cell cell){
		  return cell.getQualifierLength() >= entityColumnPrefixBytes.length && Bytes.equals(cell.getQualifierArray(),
				  cell.getQualifierOffset(), entityColumnPrefixBytes.length, entityColumnPrefixBytes, 0,
				  entityColumnPrefixBytes.length);
	}

	//parse the hbase row bytes after the partition offset
	private int parseEkFieldsFromBytesToPk(Cell cell, PK targetPk){
		byte[] fromBytes = CellUtil.cloneRow(cell);
		return parseFieldsFromBytesToPk(ekPkKeyFields, fromBytes, numPrefixBytes, targetPk);
	}

	//parse the hbase qualifier bytes
	private int parsePostEkFieldsFromBytesToPk(Cell cell, PK targetPk){
		int offset = entityColumnPrefixBytes.length;
		byte[] fromBytes = CellUtil.cloneQualifier(cell);
		try{
			return parseFieldsFromBytesToPk(postEkPkFields, fromBytes, offset, targetPk);
		}catch(RuntimeException e){
			throw new RuntimeException("failed to parse post ek field cellBytes=" + Bytes.toStringBinary(CellUtil
					.cloneRow(cell)), e);
		}
	}

	private int parseFieldsFromBytesToPk(List<Field<?>> fields, byte[] fromBytes, int offset, PK targetPk){
		int byteOffset = offset;
		for(Field<?> field : fields){
			Object value;
			try{
				value = field.fromBytesWithSeparatorButDoNotSet(fromBytes, byteOffset);
			}catch(RuntimeException e){
				throw new RuntimeException("failed to parse fromBytes=" + Bytes.toStringBinary(fromBytes)
						+ " byteOffset=" + byteOffset + " fields=" + fields + " field=" + field, e);
			}
			field.setUsingReflection(targetPk, value);
			byteOffset += field.numBytesWithSeparator(fromBytes, byteOffset);
		}
		return byteOffset;
	}

}
